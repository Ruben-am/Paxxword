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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

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
        }
    )
}

@Composable
fun AuthContent(
    isRegister: Boolean,
    state: AuthState,
    onAuthAction: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }

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
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (password.isNotBlank()) onAuthAction(password)
                    }
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state is AuthState.Error
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state is AuthState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { onAuthAction(password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = password.isNotBlank()
                ) {
                    Text(if (isRegister) "Registrar" else "Iniciar sesion")
                }
            }

            if (state is AuthState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (state as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "1. Login - Normal")
@Composable
fun AuthScreenLoginPreview() {
    MaterialTheme {
        AuthContent(
            isRegister = false,
            state = AuthState.Idle,
            onAuthAction = {}
        )
    }
}

@Preview(showBackground = true, name = "2. Registro - Normal")
@Composable
fun AuthScreenRegisterPreview() {
    MaterialTheme {
        AuthContent(
            isRegister = true,
            state = AuthState.Idle,
            onAuthAction = {}
        )
    }
}

@Preview(showBackground = true, name = "3. Cargando")
@Composable
fun AuthScreenLoadingPreview() {
    MaterialTheme {
        AuthContent(
            isRegister = false,
            state = AuthState.Loading,
            onAuthAction = {}
        )
    }
}

@Preview(showBackground = true, name = "4. Error Contraseña")
@Composable
fun AuthScreenErrorPreview() {
    MaterialTheme {
        AuthContent(
            isRegister = false,
            state = AuthState.Error("Contraseña incorrecta, inténtalo de nuevo"),
            onAuthAction = {}
        )
    }
}