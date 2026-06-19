package com.rubenalba.paxxword.ui.auth

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.data.local.dao.UserDao
import com.rubenalba.paxxword.data.local.entity.User
import com.rubenalba.paxxword.data.manager.BackupManager
import com.rubenalba.paxxword.data.manager.CryptoManager
import com.rubenalba.paxxword.data.manager.KeyDerivationUtil
import com.rubenalba.paxxword.data.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val backupManager: BackupManager,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    // sing up create master password
    fun register(password: CharArray) {
        if (password.isEmpty()) return

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // create random salt
                val salt = KeyDerivationUtil.generateSalt()

                // generate key (Password + Salt -> Key)
                val key = KeyDerivationUtil.deriveKey(password, salt)

                // encrypt VERIFICATION_PHRASE with key
                val iv = CryptoManager.generateIv()

                val uniqueToken = java.util.UUID.randomUUID().toString()
                val verificationBytes = uniqueToken.toByteArray(Charsets.UTF_8)
                val encryptedVerification = CryptoManager.encrypt(verificationBytes, key, iv)

                // save in db (salt and VERIFICATION_PHRASE no key or password)
                val user = User(
                    userEmail = context.getString(R.string.placeholder_user_local),
                    userSalt = CryptoManager.bytesToBase64(salt),
                    verificationToken = uniqueToken,
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
            } finally {
                password.fill('\u0000')
            }
        }
    }

    // login
     fun login(password: CharArray) {
        if (password.isEmpty()) return

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // search user
            val user = userDao.getAppUser()
            if (user == null) {
                _authState.value = AuthState.Error(R.string.auth_error_no_user)
                password.fill('\u0000')
                return@launch
            }

            try {
                val salt = CryptoManager.base64ToBytes(user.userSalt)

                val keyCandidate = KeyDerivationUtil.deriveKey(password, salt)

                val iv = CryptoManager.base64ToBytes(user.ivVerificationValue)

                val decryptedPhrase = CryptoManager.decrypt(
                    user.encryptedVerificationValue,
                    keyCandidate,
                    iv
                )

                if (decryptedPhrase == user.verificationToken) {
                    sessionManager.setKey(keyCandidate)
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error(R.string.auth_error_incorrect)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(R.string.auth_error_incorrect)
            } finally {
                password.fill('\u0000')
            }
        }
    }

    fun validatePasswordPolicy(password: CharArray): Int? {
        if (password.size < 8) {
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

    fun restoreFromBackup(uri: Uri, password: CharArray) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                // generate salt, derivate key (one time)
                val salt = KeyDerivationUtil.generateSalt()
                val key = KeyDerivationUtil.deriveKey(password, salt)

                // establish temporary session with that key
                sessionManager.setKey(key)

                // import data
                val success = backupManager.importBackup(uri, String(password))

                if (success) {
                    // create user
                    val uniqueToken = java.util.UUID.randomUUID().toString()
                    val iv = CryptoManager.generateIv()
                    val verificationBytes = uniqueToken.toByteArray(Charsets.UTF_8)
                    val encryptedVerification = CryptoManager.encrypt(verificationBytes, key, iv)

                    val user = User(
                        userEmail = context.getString(R.string.placeholder_user_restored),
                        userSalt = CryptoManager.bytesToBase64(salt),
                        verificationToken = uniqueToken,
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
            } finally {
                password.fill('\u0000')
            }
        }
    }
}