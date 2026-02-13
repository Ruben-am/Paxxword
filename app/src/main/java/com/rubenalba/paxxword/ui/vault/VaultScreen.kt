package com.rubenalba.paxxword.ui.vault

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.ui.vault.components.AccountDetailSheet
import com.rubenalba.paxxword.ui.vault.components.AccountItem
import com.rubenalba.paxxword.ui.vault.components.AddFolderDialog
import com.rubenalba.paxxword.ui.vault.components.FolderFilterBar
import com.rubenalba.paxxword.ui.vault.components.VaultSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: VaultViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedAccount by viewModel.selectedAccount.collectAsState()
    val isSheetOpen by viewModel.isSheetOpen.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val folders by viewModel.folders.collectAsState()
    val selectedFolderId by viewModel.selectedFolderId.collectAsState()
    var showAddFolderDialog by remember { mutableStateOf(false) }

    AccountDetailSheet(
        account = selectedAccount,
        allFolders = folders,
        isOpen = isSheetOpen,
        onDismiss = viewModel::onDismissSheet,
        onSave = viewModel::saveAccount,
        onDelete = viewModel::deleteAccount
    )

    if (showAddFolderDialog) {
        AddFolderDialog(
            onDismiss = { showAddFolderDialog = false },
            onConfirm = { name ->
                viewModel.onCreateFolder(name)
                showAddFolderDialog = false
            }
        )
    }

    // body
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.vault_title)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onAddClick) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.content_desc_add_account)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            VaultSearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange
            )

            FolderFilterBar(
                folders = folders,
                selectedFolderId = selectedFolderId,
                onFolderSelected = viewModel::onFolderSelect,
                onAddFolderClick = { showAddFolderDialog = true },
                onDeleteFolder = viewModel::onDeleteFolder
            )

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
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
}