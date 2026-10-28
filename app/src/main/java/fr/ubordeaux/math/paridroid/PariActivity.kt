package fr.ubordeaux.math.paridroid

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.SparseBooleanArray
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.MultiAutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import java.io.FileNotFoundException
import java.util.Arrays

class PariActivity : AppCompatActivity() {

    companion object {
        const val MSG_TAG = "PariDroid"
    }

    private lateinit var inputText: MultiAutoCompleteTextView
    private lateinit var resultList: ListView
    private lateinit var outputArrayAdapter: ArrayAdapter<String>
    private val outputArrayList = ArrayList<String>(255)
    private val inputArrayList = ArrayList<String>(255)
    private var cursor = 0

    private lateinit var openFileLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        // Set up open document launcher
        openFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    Log.d(MSG_TAG, "open_file $uri")
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
                                resultList.clearChoices()
                                outputArrayList.add(0, output)
                                outputArrayAdapter.notifyDataSetChanged()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        val version = try {
            val pi = packageManager.getPackageInfo(packageName, 0)
            pi.versionName ?: ""
        } catch (e: Exception) {
            ""
        }
        title = "${getString(R.string.app_name)} $version"

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val parisizeStr = prefs.getString("parisize", getString(R.string.parisize_default))
        val parisize = parisizeStr?.toLongOrNull() ?: 100000000L
        PariNative.paridroidInit(parisize)

        val map = prefs.all
        for ((key, value) in map) {
            if (key != "parisize" && value != null) {
                PariUtil.setDefault(key, value.toString())
            }
        }

        val enterButton = findViewById<ImageButton>(R.id.enterButton)
        enterButton.setOnClickListener {
            val inStr = inputText.text.toString()
            evaluate(inStr)
            inputText.hint = null
            clear()
        }

        val clearButton = findViewById<ImageButton>(R.id.clearButton)
        clearButton.setOnClickListener {
            clear()
        }

        val nextButton = findViewById<ImageButton>(R.id.nextButton)
        nextButton.setOnClickListener {
            cursor++
            if (cursor >= inputArrayList.size) cursor = 0
            if (inputArrayList.isNotEmpty()) {
                val hist = getFromHistory(cursor)
                inputText.setText(hist)
                inputText.setSelection(inputText.text.length)
            }
        }

        val prevButton = findViewById<ImageButton>(R.id.prevButton)
        prevButton.setOnClickListener {
            cursor--
            if (cursor < 0) cursor = if (inputArrayList.isNotEmpty()) inputArrayList.size - 1 else 0
            if (inputArrayList.isNotEmpty()) {
                val hist = getFromHistory(cursor)
                inputText.setText(hist)
                inputText.setSelection(inputText.text.length)
            }
        }

        inputText = findViewById(R.id.inputText)
        inputText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                enterButton.performClick()
                true
            } else {
                false
            }
        }
        inputText.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                enterButton.performClick()
                true
            } else {
                false
            }
        }

        val functions = PariNative.getCommands() ?: arrayOf()
        Arrays.sort(functions)
        val adapter = ArrayAdapter(this, R.layout.suggestion_list_item, functions)
        inputText.setAdapter(adapter)

        val tokenizer = object : MultiAutoCompleteTextView.Tokenizer {
            override fun findTokenStart(text: CharSequence, cursor: Int): Int {
                var i = cursor
                while (i > 0 && Character.isLetterOrDigit(text[i - 1])) {
                    i--
                }
                return i
            }

            override fun findTokenEnd(text: CharSequence, cursor: Int): Int {
                val len = text.length
                var i = cursor
                while (i < len - 1 && Character.isLetterOrDigit(text[i + 1])) {
                    i++
                }
                return i
            }

            override fun terminateToken(text: CharSequence): CharSequence {
                return "$text("
            }
        }
        inputText.setTokenizer(tokenizer)
        inputText.threshold = 4

        resultList = findViewById(R.id.resultList)
        outputArrayAdapter = ArrayAdapter(this, R.layout.result_list_item, outputArrayList)
        resultList.adapter = outputArrayAdapter

        val gd = GestureDetector(this, ResultListGestureListener(resultList))
        resultList.setOnTouchListener { _, event ->
            gd.onTouchEvent(event)
        }
    }

    private fun evaluate(cmd: String) {
        if (cmd.replace(Regex("^[:whitespace:]*$"), "").isNotEmpty()) {
            inputArrayList.add(cmd)
            cursor = inputArrayList.size
            if (cmd.startsWith("??")) {
                val showUrl = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(
                        getString(R.string.doc_url) +
                                cmd.replace(Regex("^\\?*"), "").replace(Regex("^ *"), "")
                    )
                }
                startActivity(showUrl)
            } else {
                val result = PariNative.paridroidEval(cmd) ?: ""
                val output = result.replace("\n\n", "\n").replace(Regex("\n$"), "")
                if (output.isNotEmpty()) {
                    outputArrayList.add(0, "? $cmd\n$output")
                } else {
                    outputArrayList.add(0, "? $cmd")
                }
                resultList.clearChoices()
                outputArrayAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun clear() {
        inputText.setText("")
    }

    fun getFromHistory(i: Int): String {
        val n = inputArrayList.size
        return if (n == 0 || i < 0 || i >= n) "" else inputArrayList[i]
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                val settings = Intent(this, PariSettingsActivity::class.java)
                startActivity(settings)
                true
            }
            R.id.docs -> {
                val showUrl = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.doc_url)))
                startActivity(showUrl)
                true
            }
            R.id.app_license -> {
                val msg = "This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.\n\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n\nSee the GNU General Public License for more details.\nhttp://www.gnu.org/licenses/gpl.txt"
                val alert = AlertDialog.Builder(this)
                alert.setMessage(msg)
                alert.setPositiveButton("OK", null)
                alert.show()
                true
            }
            R.id.read -> {
                val openFile = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                openFileLauncher.launch(openFile)
                true
            }
            R.id.shareAll -> {
                val shareAll = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, TextUtils.join("\n", outputArrayList))
                }
                startActivity(shareAll)
                true
            }
            R.id.shareSelected -> {
                var content = ""
                val sba: SparseBooleanArray = resultList.checkedItemPositions
                for (i in 0 until resultList.count) {
                    if (sba.get(i)) {
                        content += outputArrayList[i] + "\n"
                    }
                }
                if (content.isEmpty()) {
                    Toast.makeText(this, "Nothing selected; tap to select results", Toast.LENGTH_LONG).show()
                } else {
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, content)
                    }
                    startActivity(share)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
