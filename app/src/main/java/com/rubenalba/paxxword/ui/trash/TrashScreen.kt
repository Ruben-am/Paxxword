package com.rubenalba.paxxword.ui.trash

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.domain.model.AccountModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onBack: () -> Unit,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val trashedAccounts by viewModel.trashedAccounts.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.trash_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                    }
                }
            )
        }
    ) { padding ->
        if (trashedAccounts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.trash_empty), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(trashedAccounts, key = { it.id }) { account ->
                    TrashedAccountItem(
                        account = account,
                        onRestore = { viewModel.restoreAccount(account.id) },
                        onDeleteForever = { viewModel.permanentlyDeleteAccount(account.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TrashedAccountItem(
    account: AccountModel,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.trash_dialog_delete_title)) },
            text = { Text(stringResource(R.string.trash_dialog_delete_msg, account.serviceName)) },
            confirmButton = {
                Button(onClick = { onDeleteForever(); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text(stringResource(R.string.trash_btn_delete))
                }
            },
            dismissButton = {
                Text(stringResource(R.string.trash_btn_cancel))
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(account.serviceName, style = MaterialTheme.typography.titleMedium)
                Text(account.username.ifEmpty { account.email }, style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick = onRestore) {
                    Icon(Icons.Default.Restore, contentDescription = stringResource(R.string.trash_desc_restore), tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.DeleteForever, contentDescription = stringResource(R.string.trash_desc_delete_forever), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}