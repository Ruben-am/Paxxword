package com.rubenalba.paxxword.ui.vault

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.ui.vault.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: VaultViewModel = hiltViewModel(),
    onNavigateToTrash: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedAccount by viewModel.selectedAccount.collectAsState()
    val isSheetOpen by viewModel.isSheetOpen.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val folders by viewModel.folders.collectAsState()
    val selectedFolderId by viewModel.selectedFolderId.collectAsState()
    var showAddFolderDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val toastFormatMsg = stringResource(R.string.toast_copied)

    AccountDetailSheet(
        account = selectedAccount,
        allFolders = folders,
        isOpen = isSheetOpen,
        onDismiss = viewModel::onDismissSheet,
        onSave = viewModel::saveAccount,
        onDelete = viewModel::deleteAccount,
        onCopy = { label, text, isSensitive ->
            viewModel.copyToClipboard(label, text, isSensitive)
            val finalMsg = String.format(toastFormatMsg, label)
            Toast.makeText(context, finalMsg, Toast.LENGTH_SHORT).show()
        }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.vault_title)) },
                actions = {
                    IconButton(onClick = onNavigateToTrash) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.content_desc_trash))
                    }
                }
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
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (val state = uiState) {
                    is VaultUiState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is VaultUiState.Error -> {
                        val errorText = state.exceptionMessage ?: stringResource(id = state.fallbackMessageResId)
                        Text(text = errorText, color = MaterialTheme.colorScheme.error)
                    }

                    is VaultUiState.Success -> {
                        if (state.accounts.isEmpty()) {
                            Text(
                                text = stringResource(R.string.vault_empty),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 88.dp)
                            ) {
                                items(state.accounts, key = { it.id }) { account ->

                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = { dismissValue ->
                                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                                viewModel.deleteAccount(account.id)
                                                true
                                            } else {
                                                false
                                            }
                                        }
                                    )

                                    SwipeToDismissBox(
                                        state = dismissState,
                                        enableDismissFromStartToEnd = false,
                                        backgroundContent = {
                                            val color =
                                                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                                    MaterialTheme.colorScheme.errorContainer
                                                } else {
                                                    Color.Transparent
                                                }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                                    .background(color, MaterialTheme.shapes.medium),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.vault_desc_move_trash),
                                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.padding(end = 16.dp)
                                                )
                                            }
                                        },
                                        content = {
                                            val folderName = if (selectedFolderId == null) {
                                                folders.find { it.id == account.folderId }?.name
                                            } else null

                                            AccountItem(
                                                account = account,
                                                folderName = folderName,
                                                onClick = { viewModel.onAccountClick(account) }
                                            )
                                        }
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