package fr.ubordeaux.math.paridroid

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.util.Arrays

data class ResultItem(
    val id: Long = System.currentTimeMillis() + (0..100000).random(),
    val text: String,
    val isChecked: Boolean = false
)

data class CustomScript(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val code: String
)

class PariViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("paridroid_scripts", Context.MODE_PRIVATE)

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _outputList = MutableStateFlow<List<ResultItem>>(emptyList())
    val outputList: StateFlow<List<ResultItem>> = _outputList.asStateFlow()

    private val _customScripts = MutableStateFlow<List<CustomScript>>(loadScripts())
    val customScripts: StateFlow<List<CustomScript>> = _customScripts.asStateFlow()

    private val inputArrayList = ArrayList<String>(255)
    private var cursor = 0

    val availableCommands: Array<String> by lazy {
        val cmds = PariNative.getCommands() ?: arrayOf()
        Arrays.sort(cmds)
        cmds
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun clearInput() {
        _inputText.value = ""
    }

    fun evaluate(cmd: String) {
        val trimmed = cmd.trim()
        if (trimmed.isNotEmpty()) {
            inputArrayList.add(cmd)
            cursor = inputArrayList.size
            if (cmd.startsWith("??")) {
                // Handled in UI if needed
            } else {
                val result = PariNative.paridroidEval(cmd) ?: ""
                val output = result.replace("\n\n", "\n").replace(Regex("\n$"), "")
                val formatted = if (output.isNotEmpty()) "? $cmd\n$output" else "? $cmd"
                _outputList.update { current ->
                    listOf(ResultItem(text = formatted)) + current
                }
            }
            clearInput()
        }
    }

    fun navigateHistory(direction: Int) {
        if (inputArrayList.isEmpty()) return
        cursor += direction
        if (cursor >= inputArrayList.size) cursor = 0
        if (cursor < 0) cursor = inputArrayList.size - 1
        if (cursor in inputArrayList.indices) {
            _inputText.value = inputArrayList[cursor]
        }
    }

    fun toggleSelection(index: Int) {
        _outputList.update { list ->
            list.mapIndexed { idx, item ->
                if (idx == index) item.copy(isChecked = !item.isChecked) else item
            }
        }
    }

    fun removeItem(index: Int) {
        _outputList.update { list ->
            list.filterIndexed { idx, _ -> idx != index }
        }
    }

    fun removeSelectedItems() {
        _outputList.update { list ->
            list.filter { !it.isChecked }
        }
    }

    fun clearAllResults() {
        _outputList.value = emptyList()
    }

    fun addOutput(text: String) {
        _outputList.update { current ->
            listOf(ResultItem(text = text)) + current
        }
    }

    fun loadResultFromFile(uri: Uri, contentResolver: ContentResolver) {
        try {
            val pfd = contentResolver.openFileDescriptor(uri, "r")
            val fd = pfd?.fileDescriptor
            if (fd != null) {
                val descriptorField = fd.javaClass.getDeclaredField("descriptor")
                descriptorField.isAccessible = true
                val intFd = descriptorField.getInt(fd)
                val output = PariNative.read(intFd)
                pfd.close()
                if (!output.isNullOrEmpty()) {
                    addOutput(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadScripts(): List<CustomScript> {
        val jsonStr = prefs.getString("scripts_json", null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<CustomScript>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    CustomScript(
                        id = obj.optString("id", i.toString()),
                        name = obj.getString("name"),
                        code = obj.getString("code")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveScript(name: String, code: String) {
        val current = _customScripts.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.name == name }
        val newScript = CustomScript(name = name, code = code)
        if (existingIndex >= 0) {
            current[existingIndex] = newScript
        } else {
            current.add(newScript)
        }
        _customScripts.value = current
        persistScripts(current)
    }

    fun deleteScript(id: String) {
        val current = _customScripts.value.filter { it.id != id }
        _customScripts.value = current
        persistScripts(current)
    }

    private fun persistScripts(scripts: List<CustomScript>) {
        try {
            val jsonArray = JSONArray()
            for (script in scripts) {
                val obj = JSONObject().apply {
                    put("id", script.id)
                    put("name", script.name)
                    put("code", script.code)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString("scripts_json", jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
