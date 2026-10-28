package fr.ubordeaux.math.paridroid

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PariScreen(
    viewModel: PariViewModel,
    versionName: String,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val inputText by viewModel.inputText.collectAsState()
    val outputList by viewModel.outputList.collectAsState()
    val customScripts by viewModel.customScripts.collectAsState()

    var menuExpanded by remember { mutableStateOf(false) }
    var licenseDialogVisible by remember { mutableStateOf(false) }
    var scriptsDialogVisible by remember { mutableStateOf(false) }
    var newScriptName by remember { mutableStateOf("") }
    var newScriptCode by remember { mutableStateOf("") }

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.loadResultFromFile(uri, context.contentResolver)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${stringResource(R.string.app_name)} $versionName") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.read)) },
                            onClick = {
                                menuExpanded = false
                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                    type = "*/*"
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                }
                                openFileLauncher.launch(intent)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.shareAll)) },
                            onClick = {
                                menuExpanded = false
                                val allText = outputList.joinToString("\n") { it.text }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, allText)
                                }
                                context.startActivity(Intent.createChooser(intent, null))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.shareSelected)) },
                            onClick = {
                                menuExpanded = false
                                val selectedText = outputList.filter { it.isChecked }.joinToString("\n") { it.text }
                                if (selectedText.isEmpty()) {
                                    android.widget.Toast.makeText(context, "Nothing selected; tap to select results", android.widget.Toast.LENGTH_LONG).show()
                                } else {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, selectedText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, null))
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Custom Scripts") },
                            onClick = {
                                menuExpanded = false
                                scriptsDialogVisible = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings)) },
                            onClick = {
                                menuExpanded = false
                                onOpenSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.documentation)) },
                            onClick = {
                                menuExpanded = false
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.doc_url)))
                                context.startActivity(intent)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.license)) },
                            onClick = {
                                menuExpanded = false
                                licenseDialogVisible = true
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            // Input Text Field (Multiline)
            OutlinedTextField(
                value = inputText,
                onValueChange = { viewModel.updateInputText(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp, max = 150.dp),
                placeholder = { Text(stringResource(R.string.commandHint), color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Control Buttons Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = { viewModel.clearInput() },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFEEEEEE))
                        .padding(4.dp)
                ) {
                    Icon(painter = painterResource(R.drawable.baseline_clear_black_24), contentDescription = stringResource(R.string.clear))
                }
                IconButton(
                    onClick = { viewModel.navigateHistory(-1) },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFEEEEEE))
                        .padding(4.dp)
                ) {
                    Icon(painter = painterResource(R.drawable.baseline_arrow_back_black_24), contentDescription = stringResource(R.string.prev))
                }
                IconButton(
                    onClick = { viewModel.navigateHistory(1) },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFEEEEEE))
                        .padding(4.dp)
                ) {
                    Icon(painter = painterResource(R.drawable.baseline_arrow_forward_black_24), contentDescription = stringResource(R.string.next))
                }
                IconButton(
                    onClick = {
                        val cmd = inputText
                        if (cmd.startsWith("??")) {
                            val url = context.getString(R.string.doc_url) + cmd.replace(Regex("^\\?*"), "").replace(Regex("^ *"), "")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                            viewModel.clearInput()
                        } else {
                            viewModel.evaluate(cmd)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFEEEEEE))
                        .padding(4.dp)
                ) {
                    Icon(painter = painterResource(R.drawable.baseline_check_black_24), contentDescription = stringResource(R.string.enter))
                }
            }

            // Results List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentPadding = PaddingValues(4.dp)
            ) {
                itemsIndexed(outputList, key = { _, item -> item.id }) { index, item ->
                    var offsetX by remember { mutableStateOf(0f) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable { viewModel.toggleSelection(index) }
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { _, dragAmount ->
                                        offsetX += dragAmount
                                    },
                                    onDragEnd = {
                                        if (abs(offsetX) > 100f) {
                                            viewModel.removeItem(index)
                                        }
                                        offsetX = 0f
                                    }
                                )
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isChecked) Color.DarkGray else Color.Black
                        )
                    ) {
                        Text(
                            text = item.text,
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }

    // License Dialog
    if (licenseDialogVisible) {
        AlertDialog(
            onDismissRequest = { licenseDialogVisible = false },
            title = { Text("License") },
            text = {
                Text("This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.\n\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n\nSee the GNU General Public License for more details.\nhttp://www.gnu.org/licenses/gpl.txt")
            },
            confirmButton = {
                TextButton(onClick = { licenseDialogVisible = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Custom Scripts Dialog / Manager
    if (scriptsDialogVisible) {
        AlertDialog(
            onDismissRequest = { scriptsDialogVisible = false },
            title = { Text("Custom Scripts & Functions") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    OutlinedTextField(
                        value = newScriptName,
                        onValueChange = { newScriptName = it },
                        label = { Text("Script Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newScriptCode,
                        onValueChange = { newScriptCode = it },
                        label = { Text("PARI/GP Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (newScriptName.isNotBlank() && newScriptCode.isNotBlank()) {
                                viewModel.saveScript(newScriptName, newScriptCode)
                                newScriptName = ""
                                newScriptCode = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Save Script")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Saved Scripts:", style = MaterialTheme.typography.titleSmall)
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        itemsIndexed(customScripts) { _, script ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(script.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                    Text(script.code, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                }
                                Row {
                                    Button(onClick = {
                                        viewModel.updateInputText(script.code)
                                        scriptsDialogVisible = false
                                    }) {
                                        Text("Load")
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Button(onClick = {
                                        viewModel.evaluate(script.code)
                                        scriptsDialogVisible = false
                                    }) {
                                        Text("Run")
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(onClick = { viewModel.deleteScript(script.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { scriptsDialogVisible = false }) {
                    Text("Close")
                }
            }
        )
    }
}
