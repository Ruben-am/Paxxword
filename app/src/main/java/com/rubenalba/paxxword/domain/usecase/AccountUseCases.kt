package com.rubenalba.paxxword.domain.usecase

import com.rubenalba.paxxword.domain.model.AccountModel
import com.rubenalba.paxxword.domain.repository.AccountRepository
import javax.inject.Inject

class GetAccountsUseCase @Inject constructor(private val repo: AccountRepository) {
    operator fun invoke(folderId: Long?) = repo.getAccounts(folderId)
}
class SaveAccountUseCase @Inject constructor(private val repo: AccountRepository) {
    suspend operator fun invoke(account: AccountModel) = repo.saveAccount(account)
}
class DeleteAccountUseCase @Inject constructor(private val repo: AccountRepository) {
    suspend operator fun invoke(id: Long) = repo.deleteAccount(id)
}
class GetTrashedAccountsUseCase @Inject constructor(private val repo: AccountRepository) {
    operator fun invoke() = repo.getTrashedAccounts()
}
class RestoreAccountUseCase @Inject constructor(private val repo: AccountRepository) {
    suspend operator fun invoke(id: Long) = repo.restoreAccount(id)
}
class PermanentlyDeleteAccountUseCase @Inject constructor(private val repo: AccountRepository) {
    suspend operator fun invoke(id: Long) = repo.permanentlyDeleteAccount(id)
}