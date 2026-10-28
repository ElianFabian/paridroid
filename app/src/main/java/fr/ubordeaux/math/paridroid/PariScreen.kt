package fr.ubordeaux.math.paridroid

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PariScreen(
    viewModel: PariViewModel,
    versionName: String,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val outputList by viewModel.outputList.collectAsStateWithLifecycle()
    val customScripts by viewModel.customScripts.collectAsStateWithLifecycle()
    val filteredCommands by viewModel.filteredCommands.collectAsStateWithLifecycle()

    var textFieldValue by remember { mutableStateOf(TextFieldValue(inputText)) }

    // Keep local TextFieldValue in sync if ViewModel updates inputText externally
    LaunchedEffect(inputText) {
        if (textFieldValue.text != inputText) {
            textFieldValue = TextFieldValue(
                text = inputText,
                selection = TextRange(inputText.length)
            )
        }
    }

    var menuExpanded by remember { mutableStateOf(false) }
    var licenseDialogVisible by remember { mutableStateOf(false) }
    var scriptsDialogVisible by remember { mutableStateOf(false) }
    var newScriptName by remember { mutableStateOf("") }
    var newScriptCode by remember { mutableStateOf("") }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedCount = outputList.count { it.isChecked }

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.loadResultFromFile(uri, context)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${stringResource(R.string.app_name)} $versionName",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
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
                            leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
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
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
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
                            leadingIcon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                val selectedText = outputList.filter { it.isChecked }.joinToString("\n") { it.text }
                                if (selectedText.isEmpty()) {
                                    Toast.makeText(context, "Nothing selected; tap to select results", Toast.LENGTH_LONG).show()
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
                            leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                scriptsDialogVisible = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings)) },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onOpenSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.documentation)) },
                            leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.doc_url)))
                                context.startActivity(intent)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.license)) },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            var autocompleteExpanded by remember { mutableStateOf(false) }
            var textFieldHeight by remember { mutableIntStateOf(0) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        viewModel.updateInput(newValue.text, newValue.selection.end)
                        autocompleteExpanded = newValue.text.isNotEmpty()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            textFieldHeight = coordinates.size.height
                        },
                    placeholder = { Text(stringResource(R.string.commandHint)) },
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp
                    ),
//                    trailingIcon = {
//                        IconButton(onClick = { autocompleteExpanded = !autocompleteExpanded }) {
//                            Icon(
//                                imageVector = if (autocompleteExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
//                                contentDescription = "Toggle autocomplete"
//                            )
//                        }
//                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            autocompleteExpanded = false
                        }
                    ),
                    singleLine = false,
                    maxLines = 5
                )

                if (autocompleteExpanded && filteredCommands.isNotEmpty()) {
                    Popup(
                        alignment = Alignment.TopStart,
                        offset = IntOffset(0, textFieldHeight + 4),
                        properties = PopupProperties(focusable = false),
                        onDismissRequest = { autocompleteExpanded = false }
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            tonalElevation = 6.dp
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                items(filteredCommands, key = { it }) { cmd ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = cmd,
                                                fontFamily = FontFamily.Monospace,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        },
                                        onClick = {
                                            val currentText = textFieldValue.text
                                            val cursorPos = textFieldValue.selection.end

                                            var start = cursorPos
                                            while (start > 0 && (currentText[start - 1].isLetterOrDigit() || currentText[start - 1] == '_')) {
                                                start--
                                            }

                                            var end = cursorPos
                                            while (end < currentText.length && (currentText[end].isLetterOrDigit() || currentText[end] == '_')) {
                                                end++
                                            }

                                            val newText = currentText.substring(0, start) + cmd + currentText.substring(end)
                                            val newCursorPos = start + cmd.length

                                            viewModel.updateInput(newText, newCursorPos)
                                            textFieldValue = TextFieldValue(
                                                text = newText,
                                                selection = TextRange(newCursorPos)
                                            )
                                            autocompleteExpanded = false
                                        },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Control Buttons Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = { viewModel.clearInput() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(painter = painterResource(R.drawable.baseline_clear_black_24), contentDescription = stringResource(R.string.clear))
                    }
                    FilledTonalIconButton(
                        onClick = { viewModel.navigateHistory(-1) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(painter = painterResource(R.drawable.baseline_arrow_back_black_24), contentDescription = stringResource(R.string.prev))
                    }
                    FilledTonalIconButton(
                        onClick = { viewModel.navigateHistory(1) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(painter = painterResource(R.drawable.baseline_arrow_forward_black_24), contentDescription = stringResource(R.string.next))
                    }
                    Button(
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
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(painter = painterResource(R.drawable.baseline_check_black_24), contentDescription = stringResource(R.string.enter))
                    }
                }
            }

            // Selection Mode Toolbar
            AnimatedVisibility(
                visible = selectionMode,
                enter = fadeIn(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$selectedCount selected",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { viewModel.selectAll(true) }) {
                                Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                            }
                            IconButton(onClick = { viewModel.selectAll(false) }) {
                                Icon(Icons.Default.Deselect, contentDescription = "Unselect All")
                            }
                            IconButton(onClick = { selectionMode = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Exit Selection Mode")
                            }
                        }
                    }
                }
            }

            // Output Results Stream
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(outputList, key = { _, item -> item.id }) { index, item ->
                    var offsetX by remember { mutableFloatStateOf(0f) }
                    val isSelected = item.isChecked

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (selectionMode) {
                                        viewModel.toggleSelection(index)
                                    }
                                },
                                onLongClick = {
                                    if (!selectionMode) {
                                        selectionMode = true
                                        viewModel.toggleSelection(index)
                                    }
                                }
                            )
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { _, dragAmount ->
                                        offsetX += dragAmount
                                    },
                                    onDragEnd = {
                                        if (abs(offsetX) > 120f) {
                                            viewModel.removeItem(index)
                                        }
                                        offsetX = 0f
                                    }
                                )
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectionMode) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { viewModel.toggleSelection(index) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = item.text,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
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
                Text(
                    text = "This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.\n\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n\nSee the GNU General Public License for more details.\nhttp://www.gnu.org/licenses/gpl.txt",
                    style = MaterialTheme.typography.bodySmall
                )
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
                        .heightIn(max = 420.dp)
                ) {
                    OutlinedTextField(
                        value = newScriptName,
                        onValueChange = { newScriptName = it },
                        label = { Text("Script Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newScriptCode,
                        onValueChange = { newScriptCode = it },
                        label = { Text("PARI/GP Code") },
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
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
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Saved Scripts:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        itemsIndexed(customScripts) { _, script ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = script.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = script.code,
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                            maxLines = 2
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            viewModel.updateInput(script.code, script.code.length)
                                            scriptsDialogVisible = false
                                        }) {
                                            Icon(Icons.Default.Input, contentDescription = "Load")
                                        }
                                        IconButton(onClick = {
                                            viewModel.evaluate(script.code)
                                            scriptsDialogVisible = false
                                        }) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                                        }
                                        IconButton(onClick = { viewModel.deleteScript(script.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
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
