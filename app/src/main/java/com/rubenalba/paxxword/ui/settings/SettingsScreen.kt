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
import com.rubenalba.paxxword.util.Constants
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Upload

enum class ChangePasswordStep { NONE, VERIFY_CURRENT, ENTER_NEW }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.settingsState.collectAsStateWithLifecycle()
    val backupState by viewModel.backupState.collectAsStateWithLifecycle()
    val changePassState by viewModel.changePasswordState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showPasswordDialog by remember { mutableStateOf<BackupOperation?>(null) }
    var tempUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var changePassStep by remember { mutableStateOf(ChangePasswordStep.NONE) }

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(Constants.MIME_TYPE_JSON)) { uri ->
            if (uri != null) {
                tempUri = uri
                showPasswordDialog = BackupOperation.EXPORT
            }
        }

    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                tempUri = uri
                showPasswordDialog = BackupOperation.IMPORT
            }
        }

    LaunchedEffect(backupState) {
        when (val s = backupState) {
            is BackupState.Success -> {
                Toast.makeText(context, s.msgId, Toast.LENGTH_SHORT).show()
                viewModel.resetBackupState()
            }

            is BackupState.Error -> {
                Toast.makeText(context, s.msgId, Toast.LENGTH_LONG).show()
                viewModel.resetBackupState()
            }

            else -> {}
        }
    }

    LaunchedEffect(changePassState) {
        when (val s = changePassState) {
            is ChangePasswordState.Success -> {
                Toast.makeText(context, R.string.settings_msg_pass_success, Toast.LENGTH_SHORT)
                    .show()
                viewModel.resetChangePasswordState()
            }

            is ChangePasswordState.Error -> {
                Toast.makeText(context, s.msgId, Toast.LENGTH_LONG).show()
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
            Card(modifier = Modifier.padding(16.dp), shape = MaterialTheme.shapes.large) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.settings_dialog_reencrypt_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.settings_dialog_reencrypt_msg),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (changePassStep == ChangePasswordStep.VERIFY_CURRENT) {
        PasswordConfirmDialog(
            title = stringResource(R.string.settings_dialog_verify_title),
            message = stringResource(R.string.settings_dialog_verify_msg),
            onConfirm = { pass ->
                scope.launch {
                    val isValid = viewModel.verifyCurrentPasswordAuth(pass)
                    if (isValid) {
                        changePassStep = ChangePasswordStep.ENTER_NEW
                    } else {
                        Toast.makeText(
                            context,
                            R.string.settings_msg_pass_incorrect,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            onDismiss = { changePassStep = ChangePasswordStep.NONE }
        )
    }

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
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_theme_label)) },
                    leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                    supportingContent = { ThemeSelector(currentTheme = state.theme, onThemeSelected = viewModel::updateTheme) }
                )

                HorizontalDivider()

                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_language_label)) },
                    leadingContent = { Icon(Icons.Default.Language, contentDescription = null) },
                    supportingContent = {
                        LanguageSelector(
                            currentLanguage = state.language,
                            onLanguageSelected = { lang -> viewModel.updateLanguage(lang) }
                        )
                    }
                )

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.system_colors)) },
                        supportingContent = { Text(stringResource(R.string.system_colors_material_you)) },
                        leadingContent = { Icon(Icons.Default.FormatPaint, contentDescription = null) },
                        trailingContent = {
                            Switch(
                                checked = state.useDynamicColor,
                                onCheckedChange = viewModel::updateDynamicColor
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_section_security),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
            )
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_btn_change_pass)) },
                    leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier.clickable { changePassStep = ChangePasswordStep.VERIFY_CURRENT }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_section_data),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
            )
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_btn_export)) },
                    leadingContent = { Icon(Icons.Default.Upload, contentDescription = null) },
                    modifier = Modifier.clickable { exportLauncher.launch(Constants.DEFAULT_BACKUP_FILE_NAME) }
                )

                HorizontalDivider()

                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_btn_import)) },
                    supportingContent = { Text(stringResource(R.string.settings_import_note)) },
                    leadingContent = { Icon(Icons.Default.Download, contentDescription = null) },
                    modifier = Modifier.clickable { importLauncher.launch(arrayOf(Constants.MIME_TYPE_ANY)) }
                )
            }
        }
    }
}

@Composable
fun ThemeSelector(currentTheme: AppTheme, onThemeSelected: (AppTheme) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppTheme.values().forEach { theme ->
            val labelRes = when (theme) {
                AppTheme.SYSTEM -> R.string.theme_system
                AppTheme.LIGHT -> R.string.theme_light
                AppTheme.DARK -> R.string.theme_dark
            }
            FilterChip(
                selected = currentTheme == theme,
                onClick = { onThemeSelected(theme) },
                label = { Text(stringResource(labelRes)) }
            )
        }
    }
}

@Composable
fun LanguageSelector(currentLanguage: AppLanguage, onLanguageSelected: (AppLanguage) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppLanguage.values().forEach { lang ->
            val labelRes = when (lang) {
                AppLanguage.SYSTEM -> R.string.language_system
                AppLanguage.ENGLISH -> R.string.language_english
                AppLanguage.SPANISH -> R.string.language_spanish
            }
            FilterChip(
                selected = currentLanguage == lang,
                onClick = { onLanguageSelected(lang) },
                label = { Text(stringResource(labelRes)) }
            )
        }
    }
}

enum class BackupOperation { EXPORT, IMPORT }

@Composable
fun PasswordConfirmDialog(
    title: String,
    message: String,
    onConfirm: (CharArray) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf(CharArray(0)) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(message, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = String(password),
                    onValueChange = { password = it.toCharArray() },
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
                                contentDescription = stringResource(R.string.content_desc_visibility)
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
    onConfirm: (CharArray) -> Unit,
    onDismiss: () -> Unit,
    onValidatePolicy: (CharArray) -> Int?
) {
    var password by remember { mutableStateOf(CharArray(0)) }
    var confirmPassword by remember { mutableStateOf(CharArray(0)) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmVisible by remember { mutableStateOf(false) }

    val policyErrorId = if (password.isNotEmpty()) onValidatePolicy(password) else null
    val matchErrorId =
        if (confirmPassword.isNotEmpty() && !password.contentEquals(confirmPassword)) R.string.auth_error_password_mismatch else null

    val isEnabled =
        password.isNotEmpty() && confirmPassword.isNotEmpty() && policyErrorId == null && matchErrorId == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.settings_dialog_new_pass_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = String(password),
                    onValueChange = { password = it.toCharArray() },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 16.sp
                    ),
                    label = { Text(stringResource(R.string.settings_label_new_pass)) },
                    singleLine = true,
                    isError = policyErrorId != null,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = stringResource(R.string.content_desc_visibility)
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
                    value = String(confirmPassword), // MODIFICADO
                    onValueChange = { confirmPassword = it.toCharArray() },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 16.sp
                    ),
                    label = { Text(stringResource(R.string.settings_label_confirm_pass)) },
                    singleLine = true,
                    isError = matchErrorId != null,
                    visualTransformation = if (isConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isConfirmVisible = !isConfirmVisible }) {
                            Icon(
                                imageVector = if (isConfirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = stringResource(R.string.content_desc_visibility)
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
                Text(stringResource(R.string.settings_btn_update))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}