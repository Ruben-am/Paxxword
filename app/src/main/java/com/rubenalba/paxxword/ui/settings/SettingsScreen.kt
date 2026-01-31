package com.rubenalba.paxxword.ui.settings

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.domain.model.AppLanguage
import com.rubenalba.paxxword.domain.model.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.settingsState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(text = stringResource(R.string.settings_theme_label), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ThemeSelector(currentTheme = state.theme, onThemeSelected = viewModel::updateTheme)

            Divider(modifier = Modifier.padding(vertical = 24.dp))

            Text(text = stringResource(R.string.settings_language_label), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LanguageSelector(
                currentLanguage = state.language,
                onLanguageSelected = { lang ->
                    viewModel.updateLanguage(lang)
                    if (context is Activity) {
                        context.recreate()
                    }
                }
            )
        }
    }
}

@Composable
fun ThemeSelector(currentTheme: AppTheme, onThemeSelected: (AppTheme) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppTheme.values().forEach { theme ->
            FilterChip(
                selected = currentTheme == theme,
                onClick = { onThemeSelected(theme) },
                label = { Text(theme.name) }
            )
        }
    }
}

@Composable
fun LanguageSelector(currentLanguage: AppLanguage, onLanguageSelected: (AppLanguage) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppLanguage.values().forEach { lang ->
            FilterChip(
                selected = currentLanguage == lang,
                onClick = { onLanguageSelected(lang) },
                label = { Text(lang.name) }
            )
        }
    }
}