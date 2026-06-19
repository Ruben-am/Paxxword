package com.rubenalba.paxxword.data.repository

import androidx.room.withTransaction
import com.rubenalba.paxxword.data.local.AppDatabase
import com.rubenalba.paxxword.data.manager.CryptoManager
import com.rubenalba.paxxword.data.manager.KeyDerivationUtil
import com.rubenalba.paxxword.data.manager.SessionManager
import com.rubenalba.paxxword.data.mapper.AccountMapper
import com.rubenalba.paxxword.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val sessionManager: SessionManager
) : AuthRepository {

    private val accountDao = db.accountDao()
    private val userDao = db.userDao()

    override suspend fun changeMasterPassword(newPassword: String): Boolean {
        val currentKey = sessionManager.getKey() ?: return false
        val currentUser = userDao.getAppUser() ?: return false

        return try {
            val currentEncryptedAccounts = accountDao.getAllAccounts().first()
            val decryptedAccounts = currentEncryptedAccounts.map { AccountMapper.toDomain(it, currentKey) }

            val newSalt = KeyDerivationUtil.generateSalt()
            val newKey = KeyDerivationUtil.deriveKey(newPassword.toCharArray(), newSalt)
            val newIvVerification = CryptoManager.generateIv()
            val newVerificationBytes = currentUser.verificationToken.toByteArray(Charsets.UTF_8)
            val newEncryptedVerification = CryptoManager.encrypt(newVerificationBytes, newKey, newIvVerification)

            val updatedUser = currentUser.copy(
                userSalt = CryptoManager.bytesToBase64(newSalt),
                encryptedVerificationValue = newEncryptedVerification,
                ivVerificationValue = CryptoManager.bytesToBase64(newIvVerification)
            )

            val newlyEncryptedAccounts = decryptedAccounts.map { AccountMapper.toEntity(it, newKey) }

            db.withTransaction {
                userDao.insertUser(updatedUser)
                accountDao.updateAll(newlyEncryptedAccounts)
            }

            sessionManager.setKey(newKey)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}