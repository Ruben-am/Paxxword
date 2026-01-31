package com.rubenalba.paxxword.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubenalba.paxxword.data.local.entity.Folder
import com.rubenalba.paxxword.domain.model.AccountModel
import com.rubenalba.paxxword.domain.repository.PasswordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
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

    val folders: StateFlow<List<Folder>> = repository.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Carpeta seleccionada (null = "Todas")
    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    val selectedFolderId = _selectedFolderId.asStateFlow()

    // 3. UI State reactivo: Cada vez que cambia selectedFolderId, recarga las cuentas
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<VaultUiState> = _selectedFolderId
        .flatMapLatest { folderId ->
            repository.getAccounts(folderId)
        }
        .map<List<AccountModel>, VaultUiState> { list ->
            VaultUiState.Success(list)
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

    fun onFolderSelect(id: Long?) {
        _selectedFolderId.value = id
    }

    fun onCreateFolder(name: String) {
        viewModelScope.launch {
            repository.insertFolder(Folder(folderName = name))
        }
    }

    fun onDeleteFolder(folder: Folder) {
        viewModelScope.launch {
            if (_selectedFolderId.value == folder.id) {
                _selectedFolderId.value = null
            }
            repository.deleteFolder(folder)
        }
    }

    fun onAddClick() {
        _selectedAccount.value = AccountModel(
            serviceName = "",
            folderId = _selectedFolderId.value
        )
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