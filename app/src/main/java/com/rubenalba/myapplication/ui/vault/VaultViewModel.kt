package com.rubenalba.myapplication.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubenalba.myapplication.data.model.AccountModel
import com.rubenalba.myapplication.data.repository.PasswordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface VaultUiState {
    data object Loading : VaultUiState
    data class Success(val accounts: List<AccountModel>) : VaultUiState
    data class Error(val message: String) : VaultUiState
}

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val repository: PasswordRepository
) : ViewModel() {

    val uiState: StateFlow<VaultUiState> = repository.getAccounts(folderId = null)
        .map { list ->
            if (list.isEmpty()) {
                VaultUiState.Success(emptyList()) as VaultUiState
            } else {
                VaultUiState.Success(list) as VaultUiState
            }
        }
        .catch { e ->
            emit(VaultUiState.Error(e.message ?: "Error desconocido"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VaultUiState.Loading
        )
}