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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rubenalba.paxxword.ui.theme.JetBrainsMonoFontFamily

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
        title = { Text("Generador Seguro", style = MaterialTheme.typography.titleLarge) },
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
                                Icon(Icons.Default.Refresh, contentDescription = "Regenerar")
                            }
                            IconButton(onClick = {
                                viewModel.copyToClipboard()
                                Toast.makeText(context, "Contraseña copiada (Se borrará en 45s)", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Longitud: ${state.length.toInt()}", fontWeight = FontWeight.Bold)
                Slider(
                    value = state.length,
                    onValueChange = { viewModel.updateLength(it) },
                    valueRange = 6f..32f,
                    steps = 25
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.useLower, onCheckedChange = { viewModel.toggleLower(it) })
                    Text("Minúsculas (a-z)")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.useUpper, onCheckedChange = { viewModel.toggleUpper(it) })
                    Text("Mayúsculas (A-Z)")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.useDigits, onCheckedChange = { viewModel.toggleDigits(it) })
                    Text("Números (0-9)")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.useSymbols, onCheckedChange = { viewModel.toggleSymbols(it) })
                    Text("Símbolos (!@#)")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onApply(state.generatedPassword) }) {
                Text("Usar Contraseña")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}