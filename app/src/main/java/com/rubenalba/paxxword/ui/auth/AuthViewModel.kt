package com.rubenalba.paxxword.ui.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.data.manager.SessionManager
import com.rubenalba.paxxword.data.manager.BackupManager
import com.rubenalba.paxxword.data.local.dao.UserDao
import com.rubenalba.paxxword.data.local.entity.User
import com.rubenalba.paxxword.data.manager.CryptoManager
import com.rubenalba.paxxword.data.manager.KeyDerivationUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    data object Idle : AuthState() // waiting user input
    data object Loading : AuthState() // auth
    data object Success : AuthState() // enter
    data class Error(val messageId: Int) : AuthState() // incorrect password
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    // Secret phrase if we are able to decrypt it we can enter
    private val VERIFICATION_PHRASE = "PAXXWORD_VERIFIED_USER"

    // sing up create master password
    fun register(password: String) {
        if (password.isBlank()) return

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // create random salt
                val salt = KeyDerivationUtil.generateSalt()

                // generate key (Password + Salt -> Key)
                val key = KeyDerivationUtil.deriveKey(password.toCharArray(), salt)

                // encrypt VERIFICATION_PHRASE with key
                val iv = CryptoManager.generateIv()

                val verificationBytes = VERIFICATION_PHRASE.toByteArray(Charsets.UTF_8)
                val encryptedVerification = CryptoManager.encrypt(verificationBytes, key, iv)

                // save in db (salt and VERIFICATION_PHRASE no key or password)
                val user = User(
                    userEmail = "usuario_local", //place holder
                    userSalt = CryptoManager.bytesToBase64(salt),
                    encryptedVerificationValue = encryptedVerification,
                    ivVerificationValue = CryptoManager.bytesToBase64(iv)
                )
                // delete other users no more than one user is permitted
                userDao.deleteAllUsers()
                userDao.insertUser(user)

                // save key in ram and enter
                sessionManager.setKey(key)
                _authState.value = AuthState.Success

            } catch (e: Exception) {
                _authState.value = AuthState.Error(R.string.auth_error_generic)
            }
        }
    }

    // login
    fun login(password: String) {
        if (password.isBlank()) return

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // search user
            val user = userDao.getAppUser()
            if (user == null) {
                _authState.value = AuthState.Error(R.string.auth_error_no_user)
                return@launch
            }

            try {
                val salt = CryptoManager.base64ToBytes(user.userSalt)

                val keyCandidate = KeyDerivationUtil.deriveKey(password.toCharArray(), salt)

                val iv = CryptoManager.base64ToBytes(user.ivVerificationValue)

                val decryptedPhrase = CryptoManager.decrypt(
                    user.encryptedVerificationValue,
                    keyCandidate,
                    iv
                )

                // check it
                if (decryptedPhrase == VERIFICATION_PHRASE) {
                    sessionManager.setKey(keyCandidate)
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error(R.string.auth_error_incorrect)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(R.string.auth_error_incorrect)
            }
        }
    }

    fun validatePasswordPolicy(password: String): Int? {
        if (password.length < 8) {
            return R.string.auth_error_policy_length
        }
        if (!password.any { it.isDigit() }) {
            return R.string.auth_error_policy_digit
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            return R.string.auth_error_policy_symbol
        }
        return null
    }

    fun restoreFromBackup(uri: Uri, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                // generate salt, derivate key (one time)
                val salt = KeyDerivationUtil.generateSalt()
                val key = KeyDerivationUtil.deriveKey(password.toCharArray(), salt)

                // establish temporary session with that key
                sessionManager.setKey(key)

                // import data
                val success = backupManager.importBackup(uri, password)

                if (success) {
                    // create user
                    val iv = CryptoManager.generateIv()
                    val verificationBytes = "PAXXWORD_VERIFIED_USER".toByteArray(Charsets.UTF_8)
                    val encryptedVerification = CryptoManager.encrypt(verificationBytes, key, iv)

                    val user = User(
                        userEmail = "usuario_restaurado",
                        userSalt = CryptoManager.bytesToBase64(salt),
                        encryptedVerificationValue = encryptedVerification,
                        ivVerificationValue = CryptoManager.bytesToBase64(iv)
                    )

                    userDao.deleteAllUsers()
                    userDao.insertUser(user)

                    _authState.value = AuthState.Success
                } else {
                    sessionManager.clearSession()
                    _authState.value = AuthState.Error(R.string.auth_error_incorrect)
                }
            } catch (e: Exception) {
                sessionManager.clearSession()
                userDao.deleteAllUsers()
                _authState.value = AuthState.Error(R.string.auth_error_generic)
            }
        }
    }
}