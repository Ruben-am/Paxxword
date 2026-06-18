package com.rubenalba.paxxword.ui.generator

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.ui.theme.JetBrainsMonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    viewModel: GeneratorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.generator_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = String(state.generatedPassword),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                trailingIcon = {
                    IconButton(onClick = { viewModel.generatePassword() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.generator_desc_regenerate)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.copyToClipboard()
                    Toast.makeText(context, R.string.toast_password_copied, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.generator_btn_copy))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.generator_label_length, state.length.toInt()),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Slider(
                value = state.length,
                onValueChange = { viewModel.updateLength(it) },
                valueRange = 6f..32f,
                steps = 25
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = state.useLower, onCheckedChange = { viewModel.toggleLower(it) })
                Text(stringResource(R.string.generator_chk_lower), style = MaterialTheme.typography.bodyLarge)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = state.useUpper, onCheckedChange = { viewModel.toggleUpper(it) })
                Text(stringResource(R.string.generator_chk_upper), style = MaterialTheme.typography.bodyLarge)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = state.useDigits, onCheckedChange = { viewModel.toggleDigits(it) })
                Text(stringResource(R.string.generator_chk_digits), style = MaterialTheme.typography.bodyLarge)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = state.useSymbols, onCheckedChange = { viewModel.toggleSymbols(it) })
                Text(stringResource(R.string.generator_chk_symbols), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}