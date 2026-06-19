package com.rubenalba.paxxword.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubenalba.paxxword.data.manager.SecureClipboardManager
import com.rubenalba.paxxword.domain.model.AccountModel
import com.rubenalba.paxxword.domain.model.FolderModel
import com.rubenalba.paxxword.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface VaultUiState {
    data object Loading : VaultUiState
    data class Success(val accounts: List<AccountModel>) : VaultUiState
    data class Error(val message: String) : VaultUiState
}

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val getFoldersUseCase: GetFoldersUseCase,
    private val addFolderUseCase: AddFolderUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val saveAccountUseCase: SaveAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val secureClipboardManager: SecureClipboardManager
) : ViewModel() {

    val folders: StateFlow<List<FolderModel>> = getFoldersUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    val selectedFolderId = _selectedFolderId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<VaultUiState> = _selectedFolderId
        .flatMapLatest { folderId -> getAccountsUseCase(folderId) }
        .combine(_searchQuery) { accounts, query ->
            if (query.isBlank()) {
                VaultUiState.Success(accounts)
            } else {
                val filtered = accounts.filter { account ->
                    account.serviceName.contains(query, ignoreCase = true) ||
                            account.username.contains(query, ignoreCase = true) ||
                            account.email.contains(query, ignoreCase = true)
                }
                VaultUiState.Success(filtered)
            } as VaultUiState
        }
        .catch { e -> emit(VaultUiState.Error(e.message ?: "Error desconocido")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VaultUiState.Loading)

    private val _selectedAccount = MutableStateFlow<AccountModel?>(null)
    val selectedAccount = _selectedAccount.asStateFlow()

    private val _isSheetOpen = MutableStateFlow(false)
    val isSheetOpen = _isSheetOpen.asStateFlow()

    fun onSearchQueryChange(newQuery: String) { _searchQuery.value = newQuery }
    fun onFolderSelect(id: Long?) { _selectedFolderId.value = id }

    fun onCreateFolder(name: String) {
        viewModelScope.launch {
            addFolderUseCase(FolderModel(name = name))
        }
    }

    fun onDeleteFolder(folder: FolderModel) {
        viewModelScope.launch {
            if (_selectedFolderId.value == folder.id) { _selectedFolderId.value = null }
            deleteFolderUseCase(folder)
        }
    }

    fun onAddClick() {
        _selectedAccount.value = AccountModel(serviceName = "", folderId = _selectedFolderId.value)
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
            try {
                saveAccountUseCase(account)
                onDismissSheet()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch {
            try {
                deleteAccountUseCase(id)
                onDismissSheet()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun copyToClipboard(label: String, text: String, isSensitive: Boolean) {
        if (isSensitive) { secureClipboardManager.copySensitiveText(label, text) }
        else { secureClipboardManager.copyStandardText(label, text) }
    }
}