package com.rubenalba.myapplication.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubenalba.myapplication.domain.model.AccountModel
import com.rubenalba.myapplication.domain.repository.PasswordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    // ui state list
    val uiState: StateFlow<VaultUiState> = repository.getAccounts(folderId = null)
        .map<List<AccountModel>, VaultUiState> { list ->
            if (list.isEmpty()) VaultUiState.Success(emptyList())
            else VaultUiState.Success(list)
        }
        .catch { e -> emit(VaultUiState.Error(e.message ?: "Error desconocido")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VaultUiState.Loading
        )

    // bottom sheet state
    // null -> close, account model -> open
    // id 0 -> new account, id -> edit
    private val _selectedAccount = MutableStateFlow<AccountModel?>(null)
    val selectedAccount = _selectedAccount.asStateFlow()

    private val _isSheetOpen = MutableStateFlow(false)
    val isSheetOpen = _isSheetOpen.asStateFlow()

    fun onAddClick() {
        _selectedAccount.value = AccountModel(serviceName = "")
        _isSheetOpen.value = true
    }

    fun onAccountClick(account: AccountModel) {
        _selectedAccount.value = account
        _isSheetOpen.value = true
    }

    fun onDismissSheet() {
        _isSheetOpen.value = false
        _selectedAccount.value = null
    }

    fun saveAccount(account: AccountModel) {
        viewModelScope.launch {
            repository.saveAccount(account)
            onDismissSheet()
        }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch {
            repository.deleteAccount(id)
            onDismissSheet()
        }
    }
}