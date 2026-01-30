package com.rubenalba.paxxword.ui.vault

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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.ui.vault.components.AccountDetailSheet
import com.rubenalba.paxxword.ui.vault.components.AccountItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: VaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedAccount by viewModel.selectedAccount.collectAsState()
    val isSheetOpen by viewModel.isSheetOpen.collectAsState()

    AccountDetailSheet(
        account = selectedAccount,
        isOpen = isSheetOpen,
        onDismiss = viewModel::onDismissSheet,
        onSave = viewModel::saveAccount,
        onDelete = viewModel::deleteAccount
    )

    // body
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.vault_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onAddClick) { // Abre el Sheet vacío
                Icon(Icons.Default.Add, contentDescription = "Añadir Cuenta")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is VaultUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is VaultUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is VaultUiState.Success -> {
                    if (state.accounts.isEmpty()) {
                        Text(
                            text = stringResource(R.string.vault_empty),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.accounts) { account ->
                                AccountItem(
                                    account = account,
                                    onClick = { viewModel.onAccountClick(account) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}