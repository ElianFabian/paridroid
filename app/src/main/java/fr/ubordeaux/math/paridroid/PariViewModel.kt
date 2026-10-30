package fr.ubordeaux.math.paridroid

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.util.Arrays
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

data class ResultItem(
	val id: Long = System.currentTimeMillis() + (0..100000).random(),
	val text: String,
	val isChecked: Boolean = false,
)

data class CustomScript(
	val id: String = System.currentTimeMillis().toString(),
	val name: String,
	val code: String,
)

class PariViewModel(application: Application) : AndroidViewModel(application) {

	private val prefs = application.getSharedPreferences("paridroid_scripts", Context.MODE_PRIVATE)

	private val _inputText = MutableStateFlow("")
	val inputText: StateFlow<String> = _inputText.asStateFlow()

	private val _cursorPosition = MutableStateFlow(0)

	private val _outputList = MutableStateFlow<List<ResultItem>>(emptyList())
	val outputList: StateFlow<List<ResultItem>> = _outputList.asStateFlow()

	private val _customScripts = MutableStateFlow<List<CustomScript>>(loadScripts())
	val customScripts: StateFlow<List<CustomScript>> = _customScripts.asStateFlow()

	private val inputArrayList = ArrayList<String>(255)
	private var cursor = 0

	val availableCommands: Array<String> by lazy {
		val cmds = ParidroidGP.getSupportedCommands().toTypedArray()
		Arrays.sort(cmds)
		cmds
	}

	// Filter commands based on the token precisely at the current cursor position
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	val filteredCommands = combine(_inputText, _cursorPosition) { query, cursorIndex ->
		if (query.isBlank() || cursorIndex <= 0) {
			return@combine emptyList<String>()
		}
		// Extract text strictly up to the cursor position
		val subQuery = query.substring(0, minOf(cursorIndex, query.length))
		val tokens = subQuery.split(Regex("[^a-zA-Z0-9_]+"))
		val currentToken = tokens.lastOrNull { it.isNotBlank() } ?: ""

		if (currentToken.isBlank()) {
			return@combine emptyList<String>()
		}

		val lowerToken = currentToken.lowercase()
		val matched = availableCommands.filter { it.lowercase().startsWith(lowerToken) }
		if (matched.size == 1 && matched[0].lowercase() == lowerToken) {
			return@combine emptyList<String>()
		}
		return@combine matched
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5000),
		initialValue = emptyList(),
	)

	fun updateInput(text: String, cursorPosition: Int) {
		_inputText.value = text
		_cursorPosition.value = cursorPosition
	}

	fun clearInput() {
		_inputText.value = ""
		_cursorPosition.value = 0
	}

	fun evaluate(cmd: String) {
		val trimmed = cmd.trim()
		if (trimmed.isNotEmpty()) {
			inputArrayList.add(cmd)
			cursor = inputArrayList.size
			if (cmd.startsWith("??")) {
				// Handled in UI if needed
			}
			else {
				val result = runCatching { ParidroidGP.eval(cmd) }
				val output = result.getOrElse { it.message ?: "" }.replace("\n\n", "\n").replace(Regex("\n$"), "")
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
		if (cursor < 0) cursor = inputArrayList.size

		if (cursor in inputArrayList.indices) {
			val text = inputArrayList[cursor]
			_inputText.value = text
			_cursorPosition.value = text.length
		}
	}

	fun toggleSelection(index: Int) {
		_outputList.update { list ->
			list.mapIndexed { idx, item ->
				if (idx == index) item.copy(isChecked = !item.isChecked) else item
			}
		}
	}

	fun selectAll(select: Boolean) {
		_outputList.update { list ->
			list.map { it.copy(isChecked = select) }
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

	fun loadResultFromFile(uri: Uri, context: Context) {
		try {
			val fileName = uri.path
			val scriptContent = context.openFileInput(fileName).bufferedReader().use { it.readText() }
			ParidroidGP.eval(scriptContent)
		}
		catch (e: Exception) {
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
		}
		catch (e: Exception) {
			emptyList()
		}
	}

	fun saveScript(name: String, code: String) {
		val current = _customScripts.value.toMutableList()
		val existingIndex = current.indexOfFirst { it.name == name }
		val newScript = CustomScript(name = name, code = code)
		if (existingIndex >= 0) {
			current[existingIndex] = newScript
		}
		else {
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
			prefs.edit { putString("scripts_json", jsonArray.toString()) }
		}
		catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun closeAndReinit() {
		runCatching {
			ParidroidGP.close()
			ParidroidGP.init(100000000L)
		}
	}
}
