package com.rubenalba.paxxword.data.repository

import androidx.room.withTransaction
import com.rubenalba.paxxword.data.local.AppDatabase
import com.rubenalba.paxxword.data.local.entity.Account
import com.rubenalba.paxxword.data.local.entity.Folder
import com.rubenalba.paxxword.data.manager.CryptoManager
import com.rubenalba.paxxword.data.manager.KeyDerivationUtil
import com.rubenalba.paxxword.data.manager.SessionManager
import com.rubenalba.paxxword.domain.model.AccountModel
import com.rubenalba.paxxword.domain.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class PasswordRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val sessionManager: SessionManager
) : PasswordRepository {

    private val accountDao = db.accountDao()
    private val folderDao = db.folderDao()
    private val userDao = db.userDao()

    private fun getKey(): SecretKeySpec {
        return sessionManager.getKey()
            ?: throw IllegalStateException("¡ERROR CRÍTICO! Intento de acceso a datos sin sesión activa.")
    }

    override fun getAccounts(folderId: Long?): Flow<List<AccountModel>> {
        val sourceFlow = if (folderId == null) {
            accountDao.getAllAccounts()
        } else {
            accountDao.getAccountsForFolder(folderId)
        }
        return sourceFlow.map { encryptedList ->
            encryptedList.map { entity -> decryptAccount(entity) }
        }
    }

    override suspend fun getAccountById(id: Long): AccountModel? {
        val entity = accountDao.getAccountById(id) ?: return null
        return decryptAccount(entity)
    }

    override suspend fun saveAccount(model: AccountModel) {
        val entity = encryptAccount(model)
        if (model.id == 0L) {
            accountDao.insert(entity)
        } else {
            accountDao.update(entity)
        }
    }

    override suspend fun deleteAccount(id: Long) {
        val entity = accountDao.getAccountById(id)
        if (entity != null) accountDao.delete(entity)
    }

    override fun getAllFolders(): Flow<List<Folder>> = folderDao.getAllFolders()
    override suspend fun insertFolder(folder: Folder): Long = folderDao.insert(folder)
    override suspend fun deleteFolder(folder: Folder) = folderDao.delete(folder)

    override suspend fun changeMasterPassword(newPassword: String): Boolean {
        val currentKey = sessionManager.getKey() ?: return false
        val currentUser = userDao.getAppUser() ?: return false

        return try {
            val currentEncryptedAccounts = accountDao.getAllAccounts().first()
            val decryptedAccounts = currentEncryptedAccounts.map { decryptAccount(it, currentKey) }

            val newSalt = KeyDerivationUtil.generateSalt()
            val newKey = KeyDerivationUtil.deriveKey(newPassword.toCharArray(), newSalt)
            val newIvVerification = CryptoManager.generateIv()
            val newVerificationBytes = "PAXXWORD_VERIFIED_USER".toByteArray(Charsets.UTF_8)
            val newEncryptedVerification = CryptoManager.encrypt(newVerificationBytes, newKey, newIvVerification)

            val updatedUser = currentUser.copy(
                userSalt = CryptoManager.bytesToBase64(newSalt),
                encryptedVerificationValue = newEncryptedVerification,
                ivVerificationValue = CryptoManager.bytesToBase64(newIvVerification)
            )

            val newlyEncryptedAccounts = decryptedAccounts.map { encryptAccount(it, newKey) }

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

    private fun encryptAccount(model: AccountModel, key: SecretKeySpec = getKey()): Account {
        val ivUser = CryptoManager.generateIv()
        val ivEmail = CryptoManager.generateIv()
        val ivPass = CryptoManager.generateIv()
        val ivUrl = CryptoManager.generateIv()
        val ivNotes = CryptoManager.generateIv()

        fun encryptField(text: String, iv: ByteArray): String {
            if (text.isEmpty()) return ""
            return CryptoManager.encrypt(text.toByteArray(Charsets.UTF_8), key, iv)
        }

        val encUser = if (model.username.isNotEmpty()) encryptField(model.username, ivUser) else null
        val encEmail = if (model.email.isNotEmpty()) encryptField(model.email, ivEmail) else null
        val encPass = encryptField(model.password, ivPass)
        val encUrl = if (model.url.isNotEmpty()) encryptField(model.url, ivUrl) else null
        val encNotes = if (model.notes.isNotEmpty()) encryptField(model.notes, ivNotes) else null

        return Account(
            id = model.id, folderId = model.folderId, serviceName = model.serviceName,
            encryptedUsername = encUser, ivUsername = if (encUser != null) CryptoManager.bytesToBase64(ivUser) else null,
            encryptedEmail = encEmail, ivEmail = if (encEmail != null) CryptoManager.bytesToBase64(ivEmail) else null,
            encryptedPassword = encPass, ivPassword = CryptoManager.bytesToBase64(ivPass),
            encryptedUrl = encUrl, ivUrl = if (encUrl != null) CryptoManager.bytesToBase64(ivUrl) else null,
            encryptedNotes = encNotes, ivNotes = if (encNotes != null) CryptoManager.bytesToBase64(ivNotes) else null,
            lastModified = System.currentTimeMillis()
        )
    }

    private fun decryptAccount(entity: Account, key: SecretKeySpec = getKey()): AccountModel {
        return AccountModel(
            id = entity.id, folderId = entity.folderId, serviceName = entity.serviceName,
            username = safeDecrypt(entity.encryptedUsername, entity.ivUsername, key),
            email = safeDecrypt(entity.encryptedEmail, entity.ivEmail, key),
            password = safeDecrypt(entity.encryptedPassword, entity.ivPassword, key),
            url = safeDecrypt(entity.encryptedUrl, entity.ivUrl, key),
            notes = safeDecrypt(entity.encryptedNotes, entity.ivNotes, key)
        )
    }

    private fun safeDecrypt(encryptedData: String?, ivBase64: String?, key: SecretKeySpec): String {
        if (encryptedData == null || ivBase64 == null) return ""
        return try {
            val ivBytes = CryptoManager.base64ToBytes(ivBase64)
            CryptoManager.decrypt(encryptedData, key, ivBytes)
        } catch (e: Exception) {
            "Error"
        }
    }
}