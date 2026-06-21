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
import com.rubenalba.paxxword.domain.usecase.GetTrashedAccountsUseCase
import com.rubenalba.paxxword.domain.usecase.PermanentlyDeleteAccountUseCase
import com.rubenalba.paxxword.domain.usecase.RestoreAccountUseCase

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val getTrashedAccountsUseCase: GetTrashedAccountsUseCase,
    private val restoreAccountUseCase: RestoreAccountUseCase,
    private val permanentlyDeleteAccountUseCase: PermanentlyDeleteAccountUseCase
) : ViewModel() {

    val trashedAccounts: StateFlow<List<AccountModel>> = getTrashedAccountsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restoreAccount(id: Long) {
        viewModelScope.launch { restoreAccountUseCase(id) }
    }

    fun permanentlyDeleteAccount(id: Long) {
        viewModelScope.launch { permanentlyDeleteAccountUseCase(id) }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            trashedAccounts.value.forEach { account ->
                permanentlyDeleteAccountUseCase(account.id)
            }
        }
    }
}