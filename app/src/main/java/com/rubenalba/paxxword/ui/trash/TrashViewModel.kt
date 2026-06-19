package com.rubenalba.paxxword.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubenalba.paxxword.domain.model.AccountModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.rubenalba.paxxword.domain.repository.AccountRepository

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    val trashedAccounts: StateFlow<List<AccountModel>> = accountRepository.getTrashedAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restoreAccount(id: Long) {
        viewModelScope.launch {
            accountRepository.restoreAccount(id)
        }
    }

    fun permanentlyDeleteAccount(id: Long) {
        viewModelScope.launch {
            accountRepository.permanentlyDeleteAccount(id)
        }
    }
}