package com.rubenalba.myapplication.ui.vault

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.rubenalba.myapplication.domain.model.AccountModel
import com.rubenalba.myapplication.ui.vault.components.AccountItem

@Composable
fun VaultScreen(
    viewModel: VaultViewModel = hiltViewModel(),
    onAddClick: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    VaultContent(
        state = uiState,
        onAddClick = onAddClick,
        onItemClick = onItemClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultContent(
    state: VaultUiState,
    onAddClick: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis contraseñas") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    ) { paddingValues ->

        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            when (state) {
                is VaultUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is VaultUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is VaultUiState.Success -> {
                    if (state.accounts.isEmpty()) {
                        Text(
                            text = "No tienes contraseñas.\n¡Pulsa + para añadir una nueva!",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        LazyColumn {
                            items(state.accounts) { account ->
                                AccountItem(
                                    account = account,
                                    onClick = { onItemClick(account.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "1. Estado con Datos")
@Composable
fun VaultScreenPreviewSuccess() {
    val listaFalsa = listOf(
        AccountModel(id = 1, serviceName = "Netflix", username = "yo@gmail.com", password = "123"),
        AccountModel(id = 2, serviceName = "Amazon", username = "compras", password = "abc"),
        AccountModel(id = 3, serviceName = "Spotify", username = "musica", password = "xyz")
    )

    MaterialTheme {
        VaultContent(
            state = VaultUiState.Success(listaFalsa),
            onAddClick = {},
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true, name = "2. Estado Vacío")
@Composable
fun VaultScreenPreviewEmpty() {
    MaterialTheme {
        VaultContent(
            state = VaultUiState.Success(emptyList()),
            onAddClick = {},
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true, name = "3. Estado Cargando")
@Composable
fun VaultScreenPreviewLoading() {
    MaterialTheme {
        VaultContent(
            state = VaultUiState.Loading,
            onAddClick = {},
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true, name = "4. Estado Error")
@Composable
fun VaultScreenPreviewError() {
    MaterialTheme {
        VaultContent(
            state = VaultUiState.Error("Fallo al desencriptar la base de datos"),
            onAddClick = {},
            onItemClick = {}
        )
    }
}