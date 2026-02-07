package com.rubenalba.paxxword.data.repository

import com.rubenalba.paxxword.data.manager.SessionManager
import com.rubenalba.paxxword.data.local.dao.AccountDao
import com.rubenalba.paxxword.data.local.entity.Account
import com.rubenalba.paxxword.data.local.entity.Folder
import com.rubenalba.paxxword.data.local.dao.FolderDao
import com.rubenalba.paxxword.domain.model.AccountModel
import com.rubenalba.paxxword.data.manager.CryptoManager
import com.rubenalba.paxxword.domain.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class PasswordRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val folderDao: FolderDao,
    private val sessionManager: SessionManager
) : PasswordRepository {
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
            encryptedList.map { entity ->
                decryptAccount(entity)
            }
        }
    }

    override suspend fun getAccountById(id: Long): AccountModel? {
        val entity = accountDao.getAccountById(id) ?: return null
        return decryptAccount(entity)
    }

    override suspend fun saveAccount(model: AccountModel) {
        val key = getKey()

        // generate iv (12 bytes)
        val ivUser = CryptoManager.generateIv()
        val ivEmail = CryptoManager.generateIv()
        val ivPass = CryptoManager.generateIv()
        val ivUrl = CryptoManager.generateIv()
        val ivNotes = CryptoManager.generateIv()

        fun encryptField(text: String, iv: ByteArray): String {
            if (text.isEmpty()) return ""
            val bytes = text.toByteArray(Charsets.UTF_8)
            return CryptoManager.encrypt(bytes, key, iv)
        }

        val encUser = if (model.username.isNotEmpty()) encryptField(model.username, ivUser) else null
        val encEmail = if (model.email.isNotEmpty()) encryptField(model.email, ivEmail) else null

        val encPass = encryptField(model.password, ivPass)

        val encUrl = if (model.url.isNotEmpty()) encryptField(model.url, ivUrl) else null
        val encNotes = if (model.notes.isNotEmpty()) encryptField(model.notes, ivNotes) else null

        val entity = Account(
            id = model.id,
            folderId = model.folderId,
            serviceName = model.serviceName,

            // Username
            encryptedUsername = encUser,
            ivUsername = if (encUser != null) CryptoManager.bytesToBase64(ivUser) else null,

            // Email
            encryptedEmail = encEmail,
            ivEmail = if (encEmail != null) CryptoManager.bytesToBase64(ivEmail) else null,

            // Password
            encryptedPassword = encPass,
            ivPassword = CryptoManager.bytesToBase64(ivPass),

            // URL
            encryptedUrl = encUrl,
            ivUrl = if (encUrl != null) CryptoManager.bytesToBase64(ivUrl) else null,

            // Notes
            encryptedNotes = encNotes,
            ivNotes = if (encNotes != null) CryptoManager.bytesToBase64(ivNotes) else null,

            lastModified = System.currentTimeMillis()
        )

        if (model.id == 0L) {
            accountDao.insert(entity)
        } else {
            accountDao.update(entity)
        }
    }

    override suspend fun deleteAccount(id: Long) {
        val entity = accountDao.getAccountById(id)
        if (entity != null) {
            accountDao.delete(entity)
        }
    }

    // DECRYPTED

    private fun decryptAccount(entity: Account): AccountModel {
        val key = getKey()

        return AccountModel(
            id = entity.id,
            folderId = entity.folderId,
            serviceName = entity.serviceName,

            //we use the auxiliary function 'safeDecrypt' for each field
            username = safeDecrypt(entity.encryptedUsername, entity.ivUsername, key),
            email = safeDecrypt(entity.encryptedEmail, entity.ivEmail, key),
            password = safeDecrypt(entity.encryptedPassword, entity.ivPassword, key),
            url = safeDecrypt(entity.encryptedUrl, entity.ivUrl, key),
            notes = safeDecrypt(entity.encryptedNotes, entity.ivNotes, key)
        )
    }

    //Auxiliary function to avoid repeating the try-catch 5 times
    //If the field is null in the DB, it returns "" (empty string) for the UI
    private fun safeDecrypt(encryptedData: String?, ivBase64: String?, key: SecretKeySpec): String {
        if (encryptedData == null || ivBase64 == null) return ""

        return try {
            val ivBytes = CryptoManager.base64ToBytes(ivBase64)
            CryptoManager.decrypt(encryptedData, key, ivBytes)
        } catch (e: Exception) {
            "Error"
        }
    }

    override fun getAllFolders(): Flow<List<Folder>> {
        return folderDao.getAllFolders()
    }

    override suspend fun insertFolder(folder: Folder): Long {
        return folderDao.insert(folder)
    }

    override suspend fun deleteFolder(folder: Folder) {
        folderDao.delete(folder)
    }
}