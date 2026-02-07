package com.rubenalba.paxxword.domain.repository

import com.rubenalba.paxxword.data.local.entity.Folder
import com.rubenalba.paxxword.domain.model.AccountModel
import kotlinx.coroutines.flow.Flow

interface PasswordRepository {
    fun getAccounts(folderId: Long?): Flow<List<AccountModel>>

    suspend fun getAccountById(id: Long): AccountModel?

    suspend fun saveAccount(account: AccountModel)

    suspend fun deleteAccount(id: Long)
    fun getAllFolders(): Flow<List<Folder>>
    suspend fun insertFolder(folder: Folder): Long
    suspend fun deleteFolder(folder: Folder)
}