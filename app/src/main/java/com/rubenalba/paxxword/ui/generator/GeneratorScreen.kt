package com.rubenalba.paxxword.ui.generator

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.ui.theme.JetBrainsMonoFontFamily
import com.rubenalba.paxxword.ui.components.PasswordStrengthBar
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    viewModel: GeneratorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardLabel = stringResource(R.string.clipboard_label_password)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(stringResource(R.string.generator_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = String(state.generatedPassword),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 16.dp),
                    textAlign = TextAlign.Center,
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PasswordStrengthBar(
                password = String(state.generatedPassword),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ElevatedCard(
                    onClick = { viewModel.generatePassword() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.generator_desc_regenerate),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                ElevatedCard(
                    onClick = {
                        viewModel.copyToClipboard(clipboardLabel)
                        Toast.makeText(context, R.string.toast_password_copied, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.generator_btn_copy),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Cabecera del Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.generator_label_length, state.length.toInt()),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalIconButton(
                    onClick = { if (state.length > 6f) viewModel.updateLength(state.length - 1f) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Reducir longitud", modifier = Modifier.size(20.dp))
                }

                Slider(
                    value = state.length,
                    onValueChange = { viewModel.updateLength(it) },
                    valueRange = 6f..32f,
                    steps = 25,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                )

                FilledTonalIconButton(
                    onClick = { if (state.length < 32f) viewModel.updateLength(state.length + 1f) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar longitud", modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElevatedFilterChip(
                        selected = state.useLower,
                        onClick = { viewModel.toggleLower(!state.useLower) },
                        label = {
                            Text(
                                text = stringResource(R.string.generator_chk_lower),
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        leadingIcon = if (state.useLower) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) } } else null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = 48.dp),
                        shape = MaterialTheme.shapes.medium
                    )
                    ElevatedFilterChip(
                        selected = state.useUpper,
                        onClick = { viewModel.toggleUpper(!state.useUpper) },
                        label = {
                            Text(
                                text = stringResource(R.string.generator_chk_upper),
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        leadingIcon = if (state.useUpper) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) } } else null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = 48.dp),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElevatedFilterChip(
                        selected = state.useDigits,
                        onClick = { viewModel.toggleDigits(!state.useDigits) },
                        label = {
                            Text(
                                text = stringResource(R.string.generator_chk_digits),
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        leadingIcon = if (state.useDigits) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) } } else null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = 48.dp),
                        shape = MaterialTheme.shapes.medium
                    )
                    ElevatedFilterChip(
                        selected = state.useSymbols,
                        onClick = { viewModel.toggleSymbols(!state.useSymbols) },
                        label = {
                            Text(
                                text = stringResource(R.string.generator_chk_symbols),
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        leadingIcon = if (state.useSymbols) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) } } else null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = 48.dp),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }
        }
    }
}