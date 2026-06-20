package com.rubenalba.paxxword.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.data.manager.BackupManager
import com.rubenalba.paxxword.data.repository.UserPreferencesRepository
import com.rubenalba.paxxword.domain.model.AppLanguage
import com.rubenalba.paxxword.domain.model.AppTheme
import com.rubenalba.paxxword.domain.model.SettingsState
import com.rubenalba.paxxword.domain.usecase.ChangeMasterPasswordUseCase
import com.rubenalba.paxxword.domain.usecase.VerifyMasterPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    data class Success(val msgId: Int) : ChangePasswordState()
    data class Error(val msgId: Int) : ChangePasswordState()
}

sealed class BackupState {
    object Idle : BackupState()
    object Loading : BackupState()
    data class Success(val msgId: Int) : BackupState()
    data class Error(val msgId: Int) : BackupState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val backupManager: BackupManager,
    private val changeMasterPasswordUseCase: ChangeMasterPasswordUseCase,
    private val verifyMasterPasswordUseCase: VerifyMasterPasswordUseCase
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

    fun updateDynamicColor(useDynamic: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveDynamicColor(useDynamic)
        }
    }

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState = _backupState.asStateFlow()

    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState = _changePasswordState.asStateFlow()

    suspend fun verifyCurrentPasswordAuth(password: CharArray): Boolean {
        return try {
            verifyMasterPasswordUseCase(password)
        } finally {
            password.fill('\u0000')
        }
    }

    fun changeMasterPassword(newPassword: CharArray) {
        viewModelScope.launch {
            _changePasswordState.value = ChangePasswordState.Loading
            try {
                val success = changeMasterPasswordUseCase(newPassword)
                if (success) {
                    _changePasswordState.value = ChangePasswordState.Success(R.string.backup_msg_import_success)
                } else {
                    _changePasswordState.value = ChangePasswordState.Error(R.string.auth_error_generic)
                }
            } catch (e: Exception) {
                _changePasswordState.value = ChangePasswordState.Error(R.string.auth_error_generic)
            } finally {
                newPassword.fill('\u0000')
            }
        }
    }

    fun resetChangePasswordState() { _changePasswordState.value = ChangePasswordState.Idle }

    fun validatePasswordPolicy(password: CharArray): Int? {
        if (password.size < 8) return R.string.auth_error_policy_length
        if (!password.any { it.isDigit() }) return R.string.auth_error_policy_digit
        if (!password.any { !it.isLetterOrDigit() }) return R.string.auth_error_policy_symbol
        return null
    }

    fun exportVault(uri: Uri, password: CharArray) {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading

            try {
                val isValid = verifyMasterPasswordUseCase(password)

                if (isValid) {
                    try {
                        backupManager.exportBackup(uri, password)
                        _backupState.value = BackupState.Success(R.string.backup_msg_export_success)
                    } catch (e: Exception) {
                        backupManager.deleteBackupFile(uri)
                        _backupState.value = BackupState.Error(R.string.backup_error_write)
                    }
                } else {
                    backupManager.deleteBackupFile(uri)
                    _backupState.value = BackupState.Error(R.string.backup_error_pass_incorrect)
                }
            } finally {
                password.fill('\u0000')
            }
        }
    }

    fun importVault(uri: Uri, password: CharArray) {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading
            try {
                val result = backupManager.importBackup(uri, password)
                if (result) {
                    _backupState.value = BackupState.Success(R.string.backup_msg_import_success)
                } else {
                    _backupState.value = BackupState.Error(R.string.backup_error_import_pass)
                }
            } finally {
                password.fill('\u0000')
            }
        }
    }

    fun resetBackupState() { _backupState.value = BackupState.Idle }
}