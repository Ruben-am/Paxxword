package com.rubenalba.paxxword.domain.repository

import com.rubenalba.paxxword.domain.model.AccountModel
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAccounts(folderId: Long?): Flow<List<AccountModel>>
    suspend fun getAccountById(id: Long): AccountModel?
    suspend fun saveAccount(account: AccountModel)
    suspend fun deleteAccount(id: Long)
    fun getTrashedAccounts(): Flow<List<AccountModel>>
    suspend fun restoreAccount(id: Long)
    suspend fun permanentlyDeleteAccount(id: Long)
}