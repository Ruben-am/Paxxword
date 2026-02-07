package com.rubenalba.paxxword.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val backupManager: BackupManager,
    private val userDao: UserDao
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

            val isValid = verifyMasterPassword(password)

            if (isValid) {
                try {
                    backupManager.exportBackup(uri, password)
                    _backupState.value = BackupState.Success("Exportación completada y cifrada con tu Contraseña Maestra.")
                } catch (e: Exception) {
                    backupManager.deleteBackupFile(uri)
                    _backupState.value = BackupState.Error("Error al escribir el archivo.")
                }
            } else {
                backupManager.deleteBackupFile(uri)
                _backupState.value = BackupState.Error("Contraseña incorrecta. Debes usar tu Contraseña Maestra actual.")
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
                _backupState.value = BackupState.Error("Contraseña del archivo incorrecta o archivo dañado.")
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
            decryptedPhrase == "PAXXWORD_VERIFIED_USER"
        } catch (e: Exception) {
            false
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