package com.rubenalba.paxxword.ui.settings

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.domain.model.AppLanguage
import com.rubenalba.paxxword.domain.model.AppTheme
import com.rubenalba.paxxword.ui.theme.JetBrainsMonoFontFamily
import kotlinx.coroutines.launch

enum class ChangePasswordStep { NONE, VERIFY_CURRENT, ENTER_NEW }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.settingsState.collectAsState()
    val backupState by viewModel.backupState.collectAsState()
    val changePassState by viewModel.changePasswordState.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showPasswordDialog by remember { mutableStateOf<BackupOperation?>(null) }
    var tempUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var changePassStep by remember { mutableStateOf(ChangePasswordStep.NONE) }

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

    LaunchedEffect(changePassState) {
        when(val s = changePassState) {
            is ChangePasswordState.Success -> {
                Toast.makeText(context, "Contraseña actualizada con éxito", Toast.LENGTH_SHORT).show()
                viewModel.resetChangePasswordState()
            }
            is ChangePasswordState.Error -> {
                Toast.makeText(context, context.resources.getString(s.msgId), Toast.LENGTH_LONG).show()
                viewModel.resetChangePasswordState()
            }
            else -> {}
        }
    }

    if (changePassState is ChangePasswordState.Loading) {
        Dialog(
            onDismissRequest = { /* Bloqueado */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Re-encriptando bóveda...", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Por favor, no cierres la app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Flujo 1: Verificar contraseña actual
    if (changePassStep == ChangePasswordStep.VERIFY_CURRENT) {
        PasswordConfirmDialog(
            title = "Verificar identidad",
            message = "Introduce tu contraseña maestra actual para continuar.",
            onConfirm = { pass ->
                scope.launch {
                    val isValid = viewModel.verifyCurrentPasswordAuth(pass)
                    if (isValid) {
                        changePassStep = ChangePasswordStep.ENTER_NEW
                    } else {
                        Toast.makeText(context, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { changePassStep = ChangePasswordStep.NONE }
        )
    }

    // Flujo 2: Introducir nueva contraseña
    if (changePassStep == ChangePasswordStep.ENTER_NEW) {
        NewMasterPasswordDialog(
            onConfirm = { newPass ->
                viewModel.changeMasterPassword(newPass)
                changePassStep = ChangePasswordStep.NONE
            },
            onDismiss = { changePassStep = ChangePasswordStep.NONE },
            onValidatePolicy = viewModel::validatePasswordPolicy
        )
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
                title = { Text(stringResource(R.string.settings_title)) }
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

            // SECCIÓN DE SEGURIDAD
            Text(text = "Seguridad", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { changePassStep = ChangePasswordStep.VERIFY_CURRENT },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cambiar Contraseña Maestra")
            }

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
    var isPasswordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(message, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 16.sp
                    ),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.settings_label_pass_simple)) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Mostrar/Ocultar contraseña"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(password) }, enabled = password.isNotEmpty()) {
                Text(stringResource(R.string.btn_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
fun NewMasterPasswordDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    onValidatePolicy: (String) -> Int?
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmVisible by remember { mutableStateOf(false) }

    val policyErrorId = if (password.isNotEmpty()) onValidatePolicy(password) else null
    val matchErrorId = if (confirmPassword.isNotEmpty() && password != confirmPassword) R.string.auth_error_password_mismatch else null

    val isEnabled = password.isNotEmpty() && confirmPassword.isNotEmpty() && policyErrorId == null && matchErrorId == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Contraseña Maestra", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 16.sp
                    ),
                    label = { Text("Nueva contraseña") },
                    singleLine = true,
                    isError = policyErrorId != null,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Mostrar/Ocultar contraseña"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (policyErrorId != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(policyErrorId),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 16.sp
                    ),
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    isError = matchErrorId != null,
                    visualTransformation = if (isConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isConfirmVisible = !isConfirmVisible }) {
                            Icon(
                                imageVector = if (isConfirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Mostrar/Ocultar contraseña"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (matchErrorId != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(matchErrorId),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(password) }, enabled = isEnabled) {
                Text("Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}