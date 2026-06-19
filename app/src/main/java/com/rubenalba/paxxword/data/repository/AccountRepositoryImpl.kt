package com.rubenalba.paxxword.data.repository

import com.rubenalba.paxxword.data.local.dao.AccountDao
import com.rubenalba.paxxword.data.manager.SessionManager
import com.rubenalba.paxxword.data.mapper.AccountMapper
import com.rubenalba.paxxword.domain.model.AccountModel
import com.rubenalba.paxxword.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val sessionManager: SessionManager
) : AccountRepository {

    private fun getKey(): SecretKeySpec {
        return sessionManager.getKey()
            ?: throw IllegalStateException("¡ERROR CRÍTICO! Intento de acceso a datos sin sesión activa.")
    }

    override fun getAccounts(folderId: Long?): Flow<List<AccountModel>> {
        val sourceFlow = if (folderId == null) accountDao.getAllAccounts() else accountDao.getAccountsForFolder(folderId)
        return sourceFlow.map { encryptedList ->
            val key = getKey()
            encryptedList.map { entity -> AccountMapper.toDomain(entity, key) }
        }
    }

    override suspend fun getAccountById(id: Long): AccountModel? {
        val entity = accountDao.getAccountById(id) ?: return null
        return AccountMapper.toDomain(entity, getKey())
    }

    override suspend fun saveAccount(account: AccountModel) {
        val entity = AccountMapper.toEntity(account, getKey())
        if (account.id == 0L) {
            accountDao.insert(entity)
        } else {
            accountDao.update(entity)
        }
    }

    override suspend fun deleteAccount(id: Long) {
        val entity = accountDao.getAccountById(id)
        if (entity != null) {
            accountDao.update(entity.copy(isDeleted = true, lastModified = System.currentTimeMillis()))
        }
    }

    override fun getTrashedAccounts(): Flow<List<AccountModel>> {
        return accountDao.getTrashedAccounts().map { encryptedList ->
            val key = getKey()
            encryptedList.map { entity -> AccountMapper.toDomain(entity, key) }
        }
    }

    override suspend fun restoreAccount(id: Long) {
        val entity = accountDao.getAccountById(id)
        if (entity != null) {
            accountDao.update(entity.copy(isDeleted = false, lastModified = System.currentTimeMillis()))
        }
    }

    override suspend fun permanentlyDeleteAccount(id: Long) {
        val entity = accountDao.getAccountById(id)
        if (entity != null) accountDao.delete(entity)
    }
}