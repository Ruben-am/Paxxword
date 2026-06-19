package com.rubenalba.paxxword.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.data.local.dao.UserDao
import com.rubenalba.paxxword.data.manager.BackupManager
import com.rubenalba.paxxword.data.manager.CryptoManager
import com.rubenalba.paxxword.data.manager.KeyDerivationUtil
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
import com.rubenalba.paxxword.domain.repository.AuthRepository

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    data class Success(val msgId: Int) : ChangePasswordState()
    data class Error(val msgId: Int) : ChangePasswordState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val backupManager: BackupManager,
    private val userDao: UserDao,
    private val authRepository: AuthRepository
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

    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState = _changePasswordState.asStateFlow()

    suspend fun verifyCurrentPasswordAuth(password: String): Boolean {
        return verifyMasterPassword(password)
    }

    fun changeMasterPassword(newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = ChangePasswordState.Loading
            try {
                val success = authRepository.changeMasterPassword(newPassword)
                if (success) {
                    _changePasswordState.value = ChangePasswordState.Success(R.string.backup_msg_import_success) // Reusar un string o crear uno nuevo
                } else {
                    _changePasswordState.value = ChangePasswordState.Error(R.string.auth_error_generic)
                }
            } catch (e: Exception) {
                _changePasswordState.value = ChangePasswordState.Error(R.string.auth_error_generic)
            }
        }
    }

    fun resetChangePasswordState() { _changePasswordState.value = ChangePasswordState.Idle }

    fun validatePasswordPolicy(password: String): Int? {
        if (password.length < 8) return R.string.auth_error_policy_length
        if (!password.any { it.isDigit() }) return R.string.auth_error_policy_digit
        if (!password.any { !it.isLetterOrDigit() }) return R.string.auth_error_policy_symbol
        return null
    }

    fun exportVault(uri: Uri, password: String) {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading

            val isValid = verifyMasterPassword(password)

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
        }
    }

    fun importVault(uri: Uri, password: String) {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading
            val result = backupManager.importBackup(uri, password)
            if (result) {
                _backupState.value = BackupState.Success(R.string.backup_msg_import_success)
            } else {
                _backupState.value = BackupState.Error(R.string.backup_error_import_pass)
            }
        }
    }

    private suspend fun verifyMasterPassword(password: String): Boolean {
        val user = userDao.getAppUser() ?: return false
        return try {
            val salt = CryptoManager.base64ToBytes(user.userSalt)
            val keyCandidate = KeyDerivationUtil.deriveKey(password.toCharArray(), salt)
            val iv = CryptoManager.base64ToBytes(user.ivVerificationValue)

            val decryptedPhrase = CryptoManager.decrypt(
                user.encryptedVerificationValue,
                keyCandidate,
                iv
            )
            decryptedPhrase == user.verificationToken
        } catch (e: Exception) {
            false
        }
    }

    fun resetBackupState() { _backupState.value = BackupState.Idle }
}

sealed class BackupState {
    object Idle : BackupState()
    object Loading : BackupState()
    data class Success(val msgId: Int) : BackupState()
    data class Error(val msgId: Int) : BackupState()
}