package com.rubenalba.paxxword.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rubenalba.paxxword.domain.model.AppLanguage
import com.rubenalba.paxxword.domain.model.AppTheme
import com.rubenalba.paxxword.domain.model.SettingsState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.rubenalba.paxxword.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey(Constants.PREF_THEME_KEY)
        val LANGUAGE = stringPreferencesKey(Constants.PREF_LANGUAGE_KEY)
        val DYNAMIC_COLOR = booleanPreferencesKey(Constants.PREF_DYNAMIC_COLOR_KEY)
    }

    val settingsFlow: Flow<SettingsState> = context.dataStore.data.map { preferences ->
        val themeName = preferences[Keys.THEME] ?: AppTheme.SYSTEM.name
        val langName = preferences[Keys.LANGUAGE] ?: AppLanguage.SYSTEM.name
        val dynamicColor = preferences[Keys.DYNAMIC_COLOR] ?: false

        SettingsState(
            theme = AppTheme.valueOf(themeName),
            language = AppLanguage.valueOf(langName),
            useDynamicColor = dynamicColor
        )
    }

    suspend fun saveTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME] = theme.name
        }
    }

    suspend fun saveLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.name
        }
    }

    suspend fun saveDynamicColor(useDynamic: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DYNAMIC_COLOR] = useDynamic
        }
    }
}