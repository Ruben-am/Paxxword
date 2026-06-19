package com.rubenalba.paxxword.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.domain.usecase.LoginUseCase
import com.rubenalba.paxxword.domain.usecase.RegisterUseCase
import com.rubenalba.paxxword.domain.usecase.RestoreBackupUseCase
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
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    // sing up create master password
    fun register(password: CharArray, defaultEmailLabel: String) {
        if (password.isEmpty()) return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                registerUseCase(password, defaultEmailLabel)
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
            try {
                val success = loginUseCase(password)
                if (success) {
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
        if (password.size < 8) return R.string.auth_error_policy_length
        if (!password.any { it.isDigit() }) return R.string.auth_error_policy_digit
        if (!password.any { !it.isLetterOrDigit() }) return R.string.auth_error_policy_symbol
        return null
    }

    fun restoreFromBackup(uriString: String, password: CharArray, defaultEmailLabel: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val success = restoreBackupUseCase(uriString, password, defaultEmailLabel)
                if (success) {
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error(R.string.auth_error_incorrect)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(R.string.auth_error_generic)
            } finally {
                password.fill('\u0000')
            }
        }
    }
}