package com.rubenalba.paxxword.ui.settings

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.domain.model.AppLanguage
import com.rubenalba.paxxword.domain.model.AppTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.settingsState.collectAsState()
    val backupState by viewModel.backupState.collectAsState()
    val context = LocalContext.current

    var showPasswordDialog by remember { mutableStateOf<BackupOperation?>(null) }
    var tempUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            tempUri = uri
            showPasswordDialog = BackupOperation.EXPORT
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            tempUri = uri
            showPasswordDialog = BackupOperation.IMPORT
        }
    }

    LaunchedEffect(backupState) {
        when(val s = backupState) {
            is BackupState.Success -> {
                Toast.makeText(context, context.resources.getString(s.msgId), Toast.LENGTH_SHORT).show()
                viewModel.resetBackupState()
            }
            is BackupState.Error -> {
                Toast.makeText(context, context.resources.getString(s.msgId), Toast.LENGTH_LONG).show()
                viewModel.resetBackupState()
            }
            else -> {}
        }
    }

    if (showPasswordDialog != null) {
        val dialogTitle = if (showPasswordDialog == BackupOperation.EXPORT)
            stringResource(R.string.settings_dialog_export_title)
        else
            stringResource(R.string.settings_dialog_import_title)

        val dialogMessage = if (showPasswordDialog == BackupOperation.EXPORT)
            stringResource(R.string.settings_dialog_export_msg)
        else
            stringResource(R.string.settings_dialog_import_msg)

        PasswordConfirmDialog(
            title = dialogTitle,
            message = dialogMessage,
            onConfirm = { pass ->
                tempUri?.let { uri ->
                    if (showPasswordDialog == BackupOperation.EXPORT) {
                        viewModel.exportVault(uri, pass)
                    } else {
                        viewModel.importVault(uri, pass)
                    }
                }
                showPasswordDialog = null
            },
            onDismiss = { showPasswordDialog = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(text = stringResource(R.string.settings_theme_label), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ThemeSelector(currentTheme = state.theme, onThemeSelected = viewModel::updateTheme)

            Divider(modifier = Modifier.padding(vertical = 24.dp))

            Text(text = stringResource(R.string.settings_language_label), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LanguageSelector(
                currentLanguage = state.language,
                onLanguageSelected = { lang ->
                    viewModel.updateLanguage(lang)
                    if (context is Activity) {
                        context.recreate()
                    }
                }
            )

            Divider(modifier = Modifier.padding(vertical = 24.dp))

            Text(text = "Gestión de Datos", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { exportLauncher.launch("paxxword_backup.paxx") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_btn_export))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { importLauncher.launch(arrayOf("*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_btn_import))
            }
            Text(
                text = stringResource(R.string.settings_import_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ThemeSelector(currentTheme: AppTheme, onThemeSelected: (AppTheme) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppTheme.values().forEach { theme ->
            FilterChip(
                selected = currentTheme == theme,
                onClick = { onThemeSelected(theme) },
                label = { Text(theme.name) }
            )
        }
    }
}

@Composable
fun LanguageSelector(currentLanguage: AppLanguage, onLanguageSelected: (AppLanguage) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppLanguage.values().forEach { lang ->
            FilterChip(
                selected = currentLanguage == lang,
                onClick = { onLanguageSelected(lang) },
                label = { Text(lang.name) }
            )
        }
    }
}

enum class BackupOperation { EXPORT, IMPORT }

@Composable
fun PasswordConfirmDialog(
    title: String,
    message: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(message)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.settings_label_pass_simple)) }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(password) }, enabled = password.isNotEmpty()) {
                Text(stringResource(R.string.btn_confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) } }
    )
}