package com.rubenalba.paxxword.ui.addedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AddAccountScreen(
    onBackClick: () -> Unit,
    viewModel: AddEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // navigation logic
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onBackClick()
        }
    }

    // We call the pure UI by passing the data and events
    AddAccountContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onSaveClick = viewModel::saveAccount,
        onServiceNameChange = viewModel::onServiceNameChange,
        onEmailChange = viewModel::onEmailChange,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onUrlChange = viewModel::onUrlChange,
        onNotesChange = viewModel::onNotesChange
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountContent(
    uiState: AddEditUiState,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onServiceNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onNotesChange: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Contraseña") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onSaveClick) {
                Icon(Icons.Default.Check, contentDescription = "Guardar")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. service
            OutlinedTextField(
                value = uiState.serviceName,
                onValueChange = onServiceNameChange,
                label = { Text("Nombre del servicio *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.error != null && uiState.serviceName.isBlank(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. email
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. user
            OutlinedTextField(
                value = uiState.username,
                onValueChange = onUsernameChange,
                label = { Text("Usuario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. password
            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text("Contraseña *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.error != null && uiState.password.isBlank(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 5. URL
            OutlinedTextField(
                value = uiState.url,
                onValueChange = onUrlChange,
                label = { Text("Sitio Web (URL)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Notes
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = onNotesChange,
                label = { Text("Notas") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Formulario Vacío")
@Composable
fun AddAccountPreviewEmpty() {
    MaterialTheme {
        AddAccountContent(
            uiState = AddEditUiState(),
            onBackClick = {},
            onSaveClick = {},
            onServiceNameChange = {},
            onEmailChange = {},
            onUsernameChange = {},
            onPasswordChange = {},
            onUrlChange = {},
            onNotesChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Formulario Relleno")
@Composable
fun AddAccountPreviewFilled() {
    MaterialTheme {
        AddAccountContent(
            uiState = AddEditUiState(
                serviceName = "Amazon Prime",
                username = "Usuario",
                email = "correo@test.com",
                password = "Password123",
                url = "https://inventada",
                notes = "Esta es una nota de prueba larga para ver cómo queda el campo de texto."
            ),
            onBackClick = {},
            onSaveClick = {},
            onServiceNameChange = {},
            onEmailChange = {},
            onUsernameChange = {},
            onPasswordChange = {},
            onUrlChange = {},
            onNotesChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Formulario con Error")
@Composable
fun AddAccountPreviewError() {
    MaterialTheme {
        AddAccountContent(
            uiState = AddEditUiState(
                serviceName = "",
                error = "El nombre del servicio es obligatorio"
            ),
            onBackClick = {},
            onSaveClick = {},
            onServiceNameChange = {},
            onEmailChange = {},
            onUsernameChange = {},
            onPasswordChange = {},
            onUrlChange = {},
            onNotesChange = {}
        )
    }
}