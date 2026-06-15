package com.rubenalba.paxxword.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubenalba.paxxword.domain.model.AccountModel
import com.rubenalba.paxxword.domain.repository.PasswordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val repository: PasswordRepository
) : ViewModel() {

    val trashedAccounts: StateFlow<List<AccountModel>> = repository.getTrashedAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restoreAccount(id: Long) {
        viewModelScope.launch {
            repository.restoreAccount(id)
        }
    }

    fun permanentlyDeleteAccount(id: Long) {
        viewModelScope.launch {
            repository.permanentlyDeleteAccount(id)
        }
    }
}