package com.rubenalba.myapplication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubenalba.myapplication.data.SessionManager
import com.rubenalba.myapplication.data.dao.UserDao
import com.rubenalba.myapplication.data.model.User
import com.rubenalba.myapplication.utils.crypto.CryptoManager
import com.rubenalba.myapplication.utils.crypto.KeyDerivationUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    data object Idle : AuthState()        // waiting user input
    data object Loading : AuthState()     // auth
    data object Success : AuthState()     // enter
    data class Error(val message: String) : AuthState() // incorrect password
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
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
                val encryptedVerification = CryptoManager.encrypt(VERIFICATION_PHRASE, key, iv)

                // save in db (salt and VERIFICATION_PHRASE no key or password)
                val user = User(
                    userEmail = "usuario_local", // place holder
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
                _authState.value = AuthState.Error("Error al registrar: ${e.message}")
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
                _authState.value = AuthState.Error("No hay usuario registrado. Reinstala la app.")
                return@launch
            }

            try {
                // recover salt
                val salt = CryptoManager.base64ToBytes(user.userSalt)

                // generate key with the password
                val keyCandidate = KeyDerivationUtil.deriveKey(password.toCharArray(), salt)

                // try to decrypt VERIFICATION_PHRASE
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
                    _authState.value = AuthState.Error("Contraseña incorrecta")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Contraseña incorrecta")
            }
        }
    }
}