package com.rubenalba.paxxword.ui.vault.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.data.local.entity.Folder
import com.rubenalba.paxxword.domain.model.AccountModel
import com.rubenalba.paxxword.ui.components.PasswordStrengthBar
import com.rubenalba.paxxword.ui.generator.PasswordGeneratorDialog
import com.rubenalba.paxxword.ui.theme.JetBrainsMonoFontFamily
import com.rubenalba.paxxword.ui.theme.ManropeFontFamily
import com.rubenalba.paxxword.domain.model.FolderModel
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailSheet(
    account: AccountModel?, // id 0 -> new account, id -> edit
    allFolders: List<FolderModel>,
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSave: (AccountModel) -> Unit,
    onDelete: (Long) -> Unit,
    onCopy: (String, String, Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (isOpen && account != null) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState
        ) {
            AccountDetailContent(
                account = account,
                allFolders = allFolders,
                onSave = onSave,
                onDelete = onDelete,
                onCancel = onDismiss,
                onCopy = onCopy
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailContent(
    account: AccountModel,
    allFolders: List<FolderModel>,
    onSave: (AccountModel) -> Unit,
    onDelete: (Long) -> Unit,
    onCancel: () -> Unit,
    onCopy: (String, String, Boolean) -> Unit
) {
    var isEditing by rememberSaveable { mutableStateOf(account.id == 0L) }

    var serviceName by rememberSaveable { mutableStateOf(account.serviceName) }
    var username by rememberSaveable { mutableStateOf(account.username) }
    var email by rememberSaveable { mutableStateOf(account.email) }
    var password by rememberSaveable { mutableStateOf(account.password) }
    var url by rememberSaveable { mutableStateOf(account.url) }
    var notes by rememberSaveable { mutableStateOf(account.notes) }

    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var showGenerator by rememberSaveable { mutableStateOf(false) }

    if (showGenerator) {
        PasswordGeneratorDialog(
            onDismiss = { showGenerator = false },
            onApply = { generatedPass ->
                password = generatedPass
                showGenerator = false
            }
        )
    }

    // reset form if account change
    LaunchedEffect(account) {
        serviceName = account.serviceName
        username = account.username
        email = account.email
        password = account.password
        url = account.url
        notes = account.notes
        isEditing = (account.id == 0L) // reset mode
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        // header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            val titleText = if (account.id == 0L) {
                stringResource(R.string.add_account_title)
            } else {
                serviceName.ifEmpty { stringResource(R.string.edit_account_default_title) }
            }

            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            // edit button/ cancel edition
            if (account.id != 0L) {
                IconButton(onClick = { isEditing = !isEditing }) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = stringResource(R.string.content_desc_edit)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (account.isDecryptionFailed) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.error_decryption_banner),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // body
        @Composable
        fun SheetTextField(
            value: String,
            onValueChange: (String) -> Unit,
            label: String,
            isSecret: Boolean = false,
            onCopyClick: (() -> Unit)? = null
        ) {
            val isNotesField = label == stringResource(R.string.label_notes)
            val isFieldEnabled = isEditing && !account.isDecryptionFailed

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = if (isSecret) JetBrainsMonoFontFamily else ManropeFontFamily,
                    fontSize = 16.sp
                ),
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFieldEnabled,
                singleLine = !(!isSecret && isNotesField),

                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,

                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,

                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,

                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),

                visualTransformation = if (isSecret && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = if (isSecret) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        if (value.isNotEmpty() && onCopyClick != null) {
                            IconButton(onClick = onCopyClick) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = stringResource(R.string.account_desc_copy, label)
                                )
                            }
                        }

                        if (isSecret && isEditing) {
                            IconButton(onClick = { showGenerator = true }) {
                                Icon(
                                    Icons.Default.Password,
                                    contentDescription = stringResource(R.string.account_desc_generate)
                                )
                            }
                        }

                        if (isSecret) {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = stringResource(R.string.content_desc_toggle_password)
                                )
                            }
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        val serviceLabel = stringResource(R.string.label_service)
        val usernameLabel = stringResource(R.string.label_username)
        val emailLabel = stringResource(R.string.label_email)
        val passwordLabel = stringResource(R.string.label_password)
        val urlLabel = stringResource(R.string.label_url)
        val notesLabel = stringResource(R.string.label_notes)

        SheetTextField(
            serviceName,
            { serviceName = it },
            serviceLabel
        )

        SheetTextField(
            username,
            { username = it },
            usernameLabel,
            onCopyClick = { onCopy(usernameLabel, username, false) }
        )

        SheetTextField(
            email,
            { email = it },
            emailLabel,
            onCopyClick = { onCopy(emailLabel, email, false) }
        )

        SheetTextField(
            password,
            { password = it },
            passwordLabel,
            isSecret = true,
            onCopyClick = { onCopy(passwordLabel, password, true) }
        )

        if (isEditing && password.isNotEmpty()) {
            PasswordStrengthBar(password = password, modifier = Modifier.padding(bottom = 8.dp))
        }

        SheetTextField(
            url,
            { url = it },
            urlLabel,
            onCopyClick = { onCopy(urlLabel, url, false) }
        )

        SheetTextField(
            notes,
            { notes = it },
            notesLabel
        )

        Spacer(modifier = Modifier.height(16.dp))

        var expanded by rememberSaveable { mutableStateOf(false) }
        var selectedFolderId by rememberSaveable { mutableStateOf(account.folderId) }

        val folderLabel = allFolders.find { it.id == selectedFolderId }?.name
            ?: stringResource(R.string.folder_none_label)

        if (isEditing) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                val noFolderLabel = stringResource(R.string.option_no_folder)

                OutlinedTextField(
                    readOnly = true,
                    value = allFolders.find { it.id == selectedFolderId }?.name
                        ?: noFolderLabel,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.label_folder_dropdown)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.option_no_folder)) },
                        onClick = {
                            selectedFolderId = null
                            expanded = false
                        }
                    )

                    allFolders.forEach { folder ->
                        DropdownMenuItem(
                            text = { Text(folder.name) },
                            onClick = {
                                selectedFolderId = folder.id
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // action buttons
        if (isEditing) {
            Button(
                onClick = {
                    val updatedAccount = account.copy(
                        serviceName = serviceName,
                        username = username,
                        email = email,
                        password = password,
                        url = url,
                        notes = notes,
                        folderId = selectedFolderId
                    )
                    onSave(updatedAccount)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = serviceName.isNotBlank() && password.isNotBlank() && !account.isDecryptionFailed
            ) {
                Text(stringResource(R.string.btn_save))
            }
        }

        // Delete button (if account exist)
        if (account.id != 0L) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onDelete(account.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_delete_account))
            }
        }
    }
}