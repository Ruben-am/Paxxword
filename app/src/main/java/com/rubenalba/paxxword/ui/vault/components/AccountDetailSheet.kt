package com.rubenalba.paxxword.ui.vault.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.domain.model.AccountModel
import com.rubenalba.paxxword.ui.theme.JetBrainsMonoFontFamily
import com.rubenalba.paxxword.ui.theme.ManropeFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailSheet(
    account: AccountModel?, // id 0 -> new account, id -> edit
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSave: (AccountModel) -> Unit,
    onDelete: (Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (isOpen && account != null) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState
        ) {
            AccountDetailContent(
                account = account,
                onSave = onSave,
                onDelete = onDelete,
                onCancel = onDismiss
            )
        }
    }
}

@Composable
fun AccountDetailContent(
    account: AccountModel,
    onSave: (AccountModel) -> Unit,
    onDelete: (Long) -> Unit,
    onCancel: () -> Unit
) {
    // state (id 0 -> new (edit mode), else reading)
    var isEditing by remember { mutableStateOf(account.id == 0L) }

    var serviceName by remember { mutableStateOf(account.serviceName) }
    var username by remember { mutableStateOf(account.username) }
    var email by remember { mutableStateOf(account.email) }
    var password by remember { mutableStateOf(account.password) }
    var url by remember { mutableStateOf(account.url) }
    var notes by remember { mutableStateOf(account.notes) }

    var isPasswordVisible by remember { mutableStateOf(false) }

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
            .padding(bottom = 48.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (account.id == 0L) stringResource(R.string.add_account_title) else serviceName.ifEmpty { "Detalles" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // edit button/ cancel edition
            if (account.id != 0L) {
                IconButton(onClick = { isEditing = !isEditing }) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = "Editar"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // body
        @Composable
        fun SheetTextField(
            value: String,
            onValueChange: (String) -> Unit,
            label: String,
            isSecret: Boolean = false
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = if (isSecret) JetBrainsMonoFontFamily else ManropeFontFamily,
                    fontSize = 16.sp
                ),
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditing,
                singleLine = !(!isSecret && label == stringResource(R.string.label_notes)),

                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,

                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,

                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,

                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),

                visualTransformation = if (isSecret && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = if (isSecret) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
                trailingIcon = if (isSecret) {
                    {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (isPasswordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                                ),
                                contentDescription = null
                            )
                        }
                    }
                } else null
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        SheetTextField(serviceName, { serviceName = it }, stringResource(R.string.label_service))
        SheetTextField(username, { username = it }, stringResource(R.string.label_username))
        SheetTextField(email, { email = it }, stringResource(R.string.label_email))
        SheetTextField(password, { password = it }, stringResource(R.string.label_password), isSecret = true)
        SheetTextField(url, { url = it }, stringResource(R.string.label_url))
        SheetTextField(notes, { notes = it }, stringResource(R.string.label_notes))

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
                        notes = notes
                    )
                    onSave(updatedAccount)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = serviceName.isNotBlank() && password.isNotBlank()
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
                Text("Eliminar Cuenta")
            }
        }
    }
}