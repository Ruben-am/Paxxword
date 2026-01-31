package com.rubenalba.paxxword.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubenalba.paxxword.data.repository.UserPreferencesRepository
import com.rubenalba.paxxword.domain.model.AppLanguage
import com.rubenalba.paxxword.domain.model.AppTheme
import com.rubenalba.paxxword.domain.model.SettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val settingsState: StateFlow<SettingsState> = preferencesRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsState()
        )

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesRepository.saveTheme(theme)
        }
    }

    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferencesRepository.saveLanguage(language)
        }
    }
}