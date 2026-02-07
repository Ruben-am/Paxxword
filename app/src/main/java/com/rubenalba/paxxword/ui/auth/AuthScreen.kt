package com.rubenalba.paxxword.ui.auth

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.ui.theme.JetBrainsMonoFontFamily
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.rubenalba.paxxword.ui.theme.PaxxwordTheme
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@Composable
fun AuthScreen(
    isRegister: Boolean,
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.authState.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onAuthSuccess()
        }
    }

    AuthContent(
        isRegister = isRegister,
        state = state,
        onAuthAction = { password ->
            if (isRegister) viewModel.register(password) else viewModel.login(password)
        },
        onValidatePassword = viewModel::validatePasswordPolicy,
        onRestoreBackup = { uri, password ->
            viewModel.restoreFromBackup(uri, password)
        }
    )
}

@Composable
fun AuthContent(
    isRegister: Boolean,
    state: AuthState,
    onAuthAction: (String) -> Unit,
    onValidatePassword: (String) -> Int?,
    onRestoreBackup: (Uri, String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmVisible by remember { mutableStateOf(false) }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreUri by remember { mutableStateOf<Uri?>(null) }

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            restoreUri = uri
            showRestoreDialog = true
        }
    }

    val focusManager = LocalFocusManager.current

    val policyErrorId = if (isRegister && password.isNotEmpty()) {
        onValidatePassword(password)
    } else null

    val matchErrorId = if (isRegister && confirmPassword.isNotEmpty() && password != confirmPassword) {
        R.string.auth_error_password_mismatch
    } else null

    val isButtonEnabled = if (isRegister) {
        password.isNotEmpty() &&
                confirmPassword.isNotEmpty() &&
                policyErrorId == null &&
                matchErrorId == null
    } else {
        password.isNotEmpty()
    }

    if (showRestoreDialog && restoreUri != null) {
        RestorePasswordDialog(
            onConfirm = { pass ->
                onRestoreBackup(restoreUri!!, pass)
                showRestoreDialog = false
            },
            onDismiss = { showRestoreDialog = false }
        )
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            AuthHeader()

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (isRegister) stringResource(R.string.auth_register_title) else stringResource(R.string.auth_login_title),
                style = MaterialTheme.typography.headlineSmall
            )

            if (isRegister) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.auth_register_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 16.sp
                ),
                label = { Text(stringResource(R.string.auth_label_master_password)) },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (isPasswordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                            ),
                            contentDescription = stringResource(R.string.content_desc_visibility)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (isRegister) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    onDone = { if (isButtonEnabled) onAuthAction(password) }
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = policyErrorId != null
            )

            if (policyErrorId != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(policyErrorId),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            if (isRegister) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 16.sp
                    ),
                    label = { Text(stringResource(R.string.auth_label_confirm_password)) },
                    visualTransformation = if (isConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isConfirmVisible = !isConfirmVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (isConfirmVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                                ),
                                contentDescription = stringResource(R.string.content_desc_visibility)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (isButtonEnabled) onAuthAction(password) }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = matchErrorId != null
                )

                if (matchErrorId != null) {
                    Text(
                        text = stringResource(matchErrorId),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state is AuthState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { onAuthAction(password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isButtonEnabled
                ) {
                    Text(if (isRegister) stringResource(R.string.auth_btn_register) else stringResource(R.string.auth_btn_login))
                }
            }

            if (isRegister && state !is AuthState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { restoreLauncher.launch(arrayOf("*/*")) }) {
                    Text("¿Tienes un backup? Restaura tu bóveda .paxx")
                }
            }

            if (state is AuthState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(state.messageId),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AuthHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_paxxword_name),
                contentDescription = stringResource(R.string.content_desc_logo),
                modifier = Modifier.height(40.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_paxxword_shield),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun RestorePasswordDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var isVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restaurar Copia de Seguridad") },
        text = {
            Column {
                Text("Introduce la contraseña maestra original del archivo .paxx para descifrarlo e importarlo.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña del Backup") },
                    singleLine = true,
                    visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isVisible = !isVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (isVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                                ),
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(password) },
                enabled = password.isNotEmpty()
            ) {
                Text("Restaurar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(name = "1. Header Only", showBackground = true)
@Composable
fun HeaderPreview() {
    PaxxwordTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AuthHeader()
        }
    }
}

//@Preview(name = "2. Login Screen (Light)", showBackground = true)
//@Composable
//fun AuthScreenLightPreview() {
//    PaxxwordTheme {
//        AuthContent(
//            isRegister = true,
//            state = AuthState.Idle,
//            onAuthAction = {},
//            onValidatePassword = { null }
//        )
//    }
//}
//
//@Preview(
//    name = "3. Login Screen (Dark)",
//    showBackground = true,
//    uiMode = Configuration.UI_MODE_NIGHT_YES
//)
//@Composable
//fun AuthScreenDarkPreview() {
//    PaxxwordTheme {
//        AuthContent(
//            isRegister = false,
//            state = AuthState.Idle,
//            onAuthAction = {},
//            onValidatePassword = { null }
//        )
//    }
//}