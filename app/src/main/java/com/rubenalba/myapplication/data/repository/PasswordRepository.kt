package com.rubenalba.myapplication.data.repository

import com.rubenalba.myapplication.data.model.AccountModel
import kotlinx.coroutines.flow.Flow

interface PasswordRepository {
    fun getAccounts(folderId: Long?): Flow<List<AccountModel>>

    suspend fun getAccountById(id: Long): AccountModel?

    suspend fun saveAccount(account: AccountModel)

    suspend fun deleteAccount(id: Long)
}