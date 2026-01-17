package com.rubenalba.myapplication.ui.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubenalba.myapplication.data.model.AccountModel
import com.rubenalba.myapplication.data.repository.PasswordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
data class AddEditUiState(
    val serviceName: String = "",
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val url: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repository: PasswordRepository
) : ViewModel() {

    // val were we save the state
    private val _uiState = MutableStateFlow(AddEditUiState())
    // ui read
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    // functions to update fields
    // Every time the user types a letter, update the status
    fun onServiceNameChange(v: String) { _uiState.update { it.copy(serviceName = v) } }
    fun onEmailChange(v: String) { _uiState.update { it.copy(email = v) } }
    fun onUsernameChange(v: String) { _uiState.update { it.copy(username = v) } }
    fun onPasswordChange(v: String) { _uiState.update { it.copy(password = v) } }
    fun onUrlChange(v: String) { _uiState.update { it.copy(url = v) } }
    fun onNotesChange(v: String) { _uiState.update { it.copy(notes = v) } }

    // save
    fun saveAccount() {
        // Simple validation
        if (_uiState.value.serviceName.isBlank()) {
            _uiState.update { it.copy(error = "El nombre del servicio es obligatorio") }
            return
        }
        if (_uiState.value.password.isBlank()) {
            _uiState.update { it.copy(error = "La contraseña es obligatoria") }
            return
        }

        // Launch corrutin (Secondary thread)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // create the clean object
                val account = AccountModel(
                    serviceName = _uiState.value.serviceName,
                    email = _uiState.value.email,
                    username = _uiState.value.username,
                    password = _uiState.value.password,
                    url = _uiState.value.url,
                    notes = _uiState.value.notes
                )

                // repository encrypt and save
                repository.saveAccount(account)

                _uiState.update { it.copy(isSaved = true, isLoading = false) }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar: ${e.message}", isLoading = false) }
            }
        }
    }
}