package com.rubenalba.paxxword.ui.vault.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rubenalba.paxxword.data.local.entity.Folder
import com.rubenalba.paxxword.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderFilterBar(
    folders: List<Folder>,
    selectedFolderId: Long?,
    onFolderSelected: (Long?) -> Unit,
    onAddFolderClick: () -> Unit,
    onDeleteFolder: (Folder) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            FilterChip(
                selected = selectedFolderId == null,
                onClick = { onFolderSelected(null) },
                label = { Text(stringResource(R.string.folder_filter_all)) }
            )
        }

        items(folders) { folder ->
            var showDeleteDialog by remember { mutableStateOf(false) }

            val chipShape = RoundedCornerShape(8.dp)

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(stringResource(R.string.folder_dialog_delete_title)) },
                    text = { Text(stringResource(R.string.folder_dialog_delete_message, folder.folderName)) },
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

            Box(contentAlignment = Alignment.Center) {

                FilterChip(
                    selected = selectedFolderId == folder.id,
                    onClick = { /* managed by upper box */ },
                    label = { Text(folder.folderName) },
                    leadingIcon = { Icon(Icons.Default.Menu, null, Modifier.size(16.dp)) },
                    shape = chipShape,
                    modifier = Modifier.height(32.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFolderId == folder.id
                    )
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(chipShape)
                        .combinedClickable(
                            onClick = { onFolderSelected(folder.id) },
                            onLongClick = { showDeleteDialog = true }
                        )
                )
            }
        }

        item {
            IconButton(onClick = onAddFolderClick) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.folder_dialog_new_title))
            }
        }
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