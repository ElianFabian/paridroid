package fr.ubordeaux.math.paridroid

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PariSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)

    var parisize by remember {
        mutableStateOf(prefs.getString("parisize", context.getString(R.string.parisize_default)) ?: "100000000")
    }
    var parisizemax by remember {
        mutableStateOf(prefs.getString("parisizemax", context.getString(R.string.parisizemax_default)) ?: "0")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = parisize,
                onValueChange = { newValue ->
                    parisize = newValue
                    prefs.edit().putString("parisize", newValue).apply()
                    if (newValue.toLongOrNull() != null) {
                        PariUtil.setDefault("parisize", newValue)
                    }
                },
                label = { Text("parisize") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = parisizemax,
                onValueChange = { newValue ->
                    parisizemax = newValue
                    prefs.edit().putString("parisizemax", newValue).apply()
                    if (newValue.toLongOrNull() != null) {
                        PariUtil.setDefault("parisizemax", newValue)
                    }
                },
                label = { Text("parisizemax") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
