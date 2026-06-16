package com.rubenalba.paxxword.ui.generator

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rubenalba.paxxword.ui.theme.JetBrainsMonoFontFamily
import com.rubenalba.paxxword.R

@Composable
fun PasswordGeneratorDialog(
    onDismiss: () -> Unit,
    onApply: (String) -> Unit,
    viewModel: GeneratorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.generator_secure_title), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.generatedPassword,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { viewModel.generatePassword() }) {
                                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.content_desc_regenerate))
                            }
                            IconButton(onClick = {
                                viewModel.copyToClipboard()
                                Toast.makeText(context, R.string.toast_password_copied_temp, Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = stringResource(R.string.content_desc_copy),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(stringResource(R.string.generator_length, state.length.toInt()), fontWeight = FontWeight.Bold)
                Slider(
                    value = state.length,
                    onValueChange = { viewModel.updateLength(it) },
                    valueRange = 6f..32f,
                    steps = 25
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.useLower, onCheckedChange = { viewModel.toggleLower(it) })
                    Text(stringResource(R.string.generator_chk_lowercase))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.useUpper, onCheckedChange = { viewModel.toggleUpper(it) })
                    Text(stringResource(R.string.generator_chk_uppercase))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.useDigits, onCheckedChange = { viewModel.toggleDigits(it) })
                    Text(stringResource(R.string.generator_chk_numbers))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.useSymbols, onCheckedChange = { viewModel.toggleSymbols(it) })
                    Text(stringResource(R.string.generator_chk_symbols))
                }
            }
        },
        confirmButton = {
            Button(onClick = { onApply(state.generatedPassword) }) {
                Text(stringResource(R.string.generator_btn_use_password))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_close))
            }
        }
    )
}