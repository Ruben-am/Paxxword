package com.rubenalba.paxxword.ui.vault.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.domain.model.FolderModel

@Composable
fun VaultDrawerSheet(
    folders: List<FolderModel>,
    selectedFolderId: Long?,
    onFolderSelected: (Long?) -> Unit,
    onAddFolderClick: () -> Unit,
    onDeleteFolder: (FolderModel) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(320.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.List, contentDescription = null) },
            label = { Text(stringResource(R.string.folder_filter_all)) },
            selected = selectedFolderId == null,
            onClick = { onFolderSelected(null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(folders) { folder ->
                var showDeleteDialog by remember { mutableStateOf(false) }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text(stringResource(R.string.folder_dialog_delete_title)) },
                        text = { Text(stringResource(R.string.folder_dialog_delete_message, folder.name)) },
                        confirmButton = {
                            TextButton(onClick = { onDeleteFolder(folder); showDeleteDialog = false }) {
                                Text(stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text(stringResource(R.string.btn_cancel))
                            }
                        }
                    )
                }

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                    label = { Text(folder.name) },
                    selected = selectedFolderId == folder.id,
                    onClick = { onFolderSelected(folder.id) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    badge = {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Eliminar carpeta",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            label = { Text(stringResource(R.string.folder_dialog_new_title)) },
            selected = false,
            onClick = onAddFolderClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun AddFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.folder_dialog_new_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.folder_label_name)) },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank()
            ) {
                Text(stringResource(R.string.folder_btn_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
        }
    )
}