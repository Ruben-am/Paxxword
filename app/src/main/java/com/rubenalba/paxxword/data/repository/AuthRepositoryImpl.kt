package com.rubenalba.paxxword.data.repository

import android.net.Uri
import androidx.room.withTransaction
import com.rubenalba.paxxword.data.local.AppDatabase
import com.rubenalba.paxxword.data.local.entity.User
import com.rubenalba.paxxword.data.manager.BackupManager
import com.rubenalba.paxxword.data.manager.CryptoManager
import com.rubenalba.paxxword.data.manager.KeyDerivationUtil
import com.rubenalba.paxxword.data.manager.SessionManager
import com.rubenalba.paxxword.data.mapper.AccountMapper
import com.rubenalba.paxxword.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val sessionManager: SessionManager,
    private val backupManager: BackupManager
) : AuthRepository {

    private val accountDao = db.accountDao()
    private val userDao = db.userDao()

    override suspend fun changeMasterPassword(newPassword: String): Boolean {
        val currentKey = sessionManager.getKey() ?: return false
        val currentUser = userDao.getAppUser() ?: return false

        return try {
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

            db.withTransaction {
                userDao.insertUser(updatedUser)

                val batchSize = 50
                var offset = 0
                while (true) {
                    val batch = accountDao.getAccountsBatch(batchSize, offset)
                    if (batch.isEmpty()) break

                    val decryptedAccounts = batch.map { AccountMapper.toDomain(it, currentKey) }
                    val newlyEncryptedAccounts = decryptedAccounts.map { AccountMapper.toEntity(it, newKey) }

                    accountDao.updateAll(newlyEncryptedAccounts)
                    offset += batchSize
                }
            }

            sessionManager.setKey(newKey)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun login(password: CharArray): Boolean {
        val user = userDao.getAppUser() ?: return false
        return try {
            val salt = CryptoManager.base64ToBytes(user.userSalt)
            val keyCandidate = KeyDerivationUtil.deriveKey(password, salt)
            val iv = CryptoManager.base64ToBytes(user.ivVerificationValue)
            val decryptedPhrase = CryptoManager.decrypt(user.encryptedVerificationValue, keyCandidate, iv)

            if (decryptedPhrase == user.verificationToken) {
                sessionManager.setKey(keyCandidate)
                true
            } else false
        } catch (e: Exception) { false }
    }

    override suspend fun register(password: CharArray, defaultEmailLabel: String) {
        val salt = KeyDerivationUtil.generateSalt()
        val key = KeyDerivationUtil.deriveKey(password, salt)
        val iv = CryptoManager.generateIv()
        val uniqueToken = java.util.UUID.randomUUID().toString()
        val verificationBytes = uniqueToken.toByteArray(Charsets.UTF_8)
        val encryptedVerification = CryptoManager.encrypt(verificationBytes, key, iv)

        val user = User(
            userEmail = defaultEmailLabel,
            userSalt = CryptoManager.bytesToBase64(salt),
            verificationToken = uniqueToken,
            encryptedVerificationValue = encryptedVerification,
            ivVerificationValue = CryptoManager.bytesToBase64(iv)
        )
        userDao.deleteAllUsers()
        userDao.insertUser(user)
        sessionManager.setKey(key)
    }

    override suspend fun restoreFromBackup(uriString: String, password: CharArray, defaultEmailLabel: String): Boolean {
        val salt = KeyDerivationUtil.generateSalt()
        val key = KeyDerivationUtil.deriveKey(password, salt)
        sessionManager.setKey(key)

        val uri = Uri.parse(uriString)
        val success = backupManager.importBackup(uri, String(password))

        if (success) {
            val uniqueToken = java.util.UUID.randomUUID().toString()
            val iv = CryptoManager.generateIv()
            val verificationBytes = uniqueToken.toByteArray(Charsets.UTF_8)
            val encryptedVerification = CryptoManager.encrypt(verificationBytes, key, iv)

            val user = User(
                userEmail = defaultEmailLabel,
                userSalt = CryptoManager.bytesToBase64(salt),
                verificationToken = uniqueToken,
                encryptedVerificationValue = encryptedVerification,
                ivVerificationValue = CryptoManager.bytesToBase64(iv)
            )
            userDao.deleteAllUsers()
            userDao.insertUser(user)
            return true
        } else {
            sessionManager.clearSession()
            return false
        }
    }

    override suspend fun verifyMasterPassword(password: CharArray): Boolean {
        val user = userDao.getAppUser() ?: return false
        return try {
            val salt = CryptoManager.base64ToBytes(user.userSalt)
            val keyCandidate = KeyDerivationUtil.deriveKey(password, salt)
            val iv = CryptoManager.base64ToBytes(user.ivVerificationValue)
            val decryptedPhrase = CryptoManager.decrypt(user.encryptedVerificationValue, keyCandidate, iv)
            decryptedPhrase == user.verificationToken
        } catch (e: Exception) { false }
    }
}