package com.rubenalba.myapplication.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rubenalba.myapplication.R

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

        onValidatePassword = viewModel::validatePasswordPolicy
    )
}

@Composable
fun AuthContent(
    isRegister: Boolean,
    state: AuthState,
    onAuthAction: (String) -> Unit,
    onValidatePassword: (String) -> String?
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val policyError = if (isRegister && password.isNotEmpty()) {
        onValidatePassword(password)
    } else null

    val matchError = if (isRegister && confirmPassword.isNotEmpty() && password != confirmPassword) {
        stringResource(R.string.error_password_mismatch)
    } else null

    val isButtonEnabled = if (isRegister) {
        password.isNotEmpty() &&
                confirmPassword.isNotEmpty() &&
                policyError == null &&
                matchError == null
    } else {
        password.isNotEmpty()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isRegister) "Crear Contraseña Maestra" else "Bienvenido",
                style = MaterialTheme.typography.headlineMedium
            )

            if (isRegister) {
                Text(
                    text = "Esta contraseña encriptará todos tus datos. No la olvides.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña Maestra") },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (isPasswordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                            ),
                            contentDescription = null
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
                isError = policyError != null
            )

            if (policyError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = policyError,
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
                    label = { Text("Confirmar contraseña") },
                    visualTransformation = if (isConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isConfirmVisible = !isConfirmVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (isConfirmVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                                ),
                                contentDescription = null
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
                    isError = matchError != null
                )

                if (matchError != null) {
                    Text(
                        text = matchError,
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
                    Text(if (isRegister) "Registrar" else "Iniciar sesion")
                }
            }

            if (state is AuthState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

//@Preview(showBackground = true, name = "1. Login - Normal")
//@Composable
//fun AuthScreenLoginPreview() {
//    MaterialTheme {
//        AuthContent(
//            isRegister = false,
//            state = AuthState.Idle,
//            onAuthAction = {}
//        )
//    }
//}
//
//@Preview(showBackground = true, name = "2. Registro - Normal")
//@Composable
//fun AuthScreenRegisterPreview() {
//    MaterialTheme {
//        AuthContent(
//            isRegister = true,
//            state = AuthState.Idle,
//            onAuthAction = {}
//        )
//    }
//}
//
//@Preview(showBackground = true, name = "3. Cargando")
//@Composable
//fun AuthScreenLoadingPreview() {
//    MaterialTheme {
//        AuthContent(
//            isRegister = false,
//            state = AuthState.Loading,
//            onAuthAction = {}
//        )
//    }
//}
//
//@Preview(showBackground = true, name = "4. Error Contraseña")
//@Composable
//fun AuthScreenErrorPreview() {
//    MaterialTheme {
//        AuthContent(
//            isRegister = false,
//            state = AuthState.Error("Contraseña incorrecta, inténtalo de nuevo"),
//            onAuthAction = {}
//        )
//    }
//}