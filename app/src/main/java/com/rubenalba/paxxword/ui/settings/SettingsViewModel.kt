package com.rubenalba.paxxword.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubenalba.paxxword.data.manager.BackupManager
import com.rubenalba.paxxword.data.repository.UserPreferencesRepository
import com.rubenalba.paxxword.domain.model.AppLanguage
import com.rubenalba.paxxword.domain.model.AppTheme
import com.rubenalba.paxxword.domain.model.SettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val backupManager: BackupManager
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

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState = _backupState.asStateFlow()

    fun exportVault(uri: Uri, password: String) {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading
            try {
                backupManager.exportBackup(uri, password)
                _backupState.value = BackupState.Success("Exportación completada")
            } catch (e: Exception) {
                _backupState.value = BackupState.Error("Error al exportar")
            }
        }
    }

    fun importVault(uri: Uri, password: String) {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading
            val result = backupManager.importBackup(uri, password)
            if (result) {
                _backupState.value = BackupState.Success("Importación exitosa")
            } else {
                _backupState.value = BackupState.Error("Contraseña incorrecta o archivo inválido")
            }
        }
    }

    fun resetBackupState() { _backupState.value = BackupState.Idle }
}

sealed class BackupState {
    object Idle : BackupState()
    object Loading : BackupState()
    data class Success(val msg: String) : BackupState()
    data class Error(val msg: String) : BackupState()
}