package com.rubenalba.myapplication.data.repository

import com.rubenalba.myapplication.data.SessionManager
import com.rubenalba.myapplication.data.dao.AccountDao
import com.rubenalba.myapplication.data.model.Account
import com.rubenalba.myapplication.data.model.AccountModel
import com.rubenalba.myapplication.utils.crypto.CryptoManager
import com.rubenalba.myapplication.utils.crypto.KeyDerivationUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import com.rubenalba.myapplication.BuildConfig

class PasswordRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val sessionManager: SessionManager
) : PasswordRepository {

    /*
     * SECURITY NOTE / NOTA DE SEGURIDAD
     *
     * [EN] TEMPORARY DEV SHORTCUT:
     * The dev key is injected via BuildConfig. It attempts to read from 'local.properties' (ignored by Git).
     * If not found, it defaults to "123456" to ensure the project builds and runs out-of-the-box
     * without requiring manual configuration from the reviewer.
     * This bypasses the auth flow until the Login Screen is fully implemented (Ref: Issue #7).
     *
     * [ES] ATAJO TEMPORAL DE DESARROLLO:
     * La clave de desarrollo se inyecta vía BuildConfig. Intenta leer de 'local.properties' (ignorado por Git).
     * Si no se encuentra, usa "123456" por defecto para asegurar que el proyecto compile y funcione
     * inmediatamente sin requerir configuración manual por parte del revisor.
     * Esto omite el flujo de autenticación hasta que la Pantalla de Login esté implementada (Ref: Issue #7).
     */

    private val devKey: SecretKeySpec by lazy {
        val passwordChars = BuildConfig.DEV_KEY_PASS.toCharArray()
        val fixedSalt = ByteArray(16) { 0 }
        KeyDerivationUtil.deriveKey(passwordChars, fixedSalt)
    }

    private fun getKey(): SecretKeySpec {
        return sessionManager.getKey() ?: devKey
    }

    override fun getAccounts(folderId: Long?): Flow<List<AccountModel>> {
        val sourceFlow = if (folderId == null) {
            accountDao.getAccountsWithoutFolder()
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

        val ivUser = CryptoManager.generateIv()
        val ivEmail = CryptoManager.generateIv()
        val ivPass = CryptoManager.generateIv()
        val ivUrl = CryptoManager.generateIv()
        val ivNotes = CryptoManager.generateIv()

        val encUser = if (model.username.isNotEmpty()) CryptoManager.encrypt(model.username, key, ivUser) else null
        val encEmail = if (model.email.isNotEmpty()) CryptoManager.encrypt(model.email, key, ivEmail) else null
        val encPass = CryptoManager.encrypt(model.password, key, ivPass)
        val encUrl = if (model.url.isNotEmpty()) CryptoManager.encrypt(model.url, key, ivUrl) else null
        val encNotes = if (model.notes.isNotEmpty()) CryptoManager.encrypt(model.notes, key, ivNotes) else null

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
}