package com.rubenalba.paxxword.data.mapper

import com.rubenalba.paxxword.data.local.entity.Account
import com.rubenalba.paxxword.data.manager.CryptoManager
import com.rubenalba.paxxword.domain.model.AccountModel
import javax.crypto.spec.SecretKeySpec

object AccountMapper {

    fun toEntity(model: AccountModel, key: SecretKeySpec): Account {
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
            id = model.id,
            folderId = model.folderId,
            serviceName = model.serviceName,
            encryptedUsername = encUser,
            ivUsername = if (encUser != null) CryptoManager.bytesToBase64(ivUser) else null,
            encryptedEmail = encEmail,
            ivEmail = if (encEmail != null) CryptoManager.bytesToBase64(ivEmail) else null,
            encryptedPassword = encPass,
            ivPassword = CryptoManager.bytesToBase64(ivPass),
            encryptedUrl = encUrl,
            ivUrl = if (encUrl != null) CryptoManager.bytesToBase64(ivUrl) else null,
            encryptedNotes = encNotes,
            ivNotes = if (encNotes != null) CryptoManager.bytesToBase64(ivNotes) else null,
            lastModified = System.currentTimeMillis(),
            isDeleted = model.isDeleted
        )
    }

    fun toDomain(entity: Account, key: SecretKeySpec): AccountModel {
        val userRes = safeDecrypt(entity.encryptedUsername, entity.ivUsername, key)
        val emailRes = safeDecrypt(entity.encryptedEmail, entity.ivEmail, key)
        val passRes = safeDecrypt(entity.encryptedPassword, entity.ivPassword, key)
        val urlRes = safeDecrypt(entity.encryptedUrl, entity.ivUrl, key)
        val notesRes = safeDecrypt(entity.encryptedNotes, entity.ivNotes, key)

        val isFailed = userRes.isFailure || emailRes.isFailure || passRes.isFailure || urlRes.isFailure || notesRes.isFailure

        return AccountModel(
            id = entity.id,
            folderId = entity.folderId,
            serviceName = entity.serviceName,
            username = userRes.getOrDefault(""),
            email = emailRes.getOrDefault(""),
            password = passRes.getOrDefault(""),
            url = urlRes.getOrDefault(""),
            notes = notesRes.getOrDefault(""),
            isDeleted = entity.isDeleted,
            isDecryptionFailed = isFailed
        )
    }

    private fun safeDecrypt(encryptedData: String?, ivBase64: String?, key: SecretKeySpec): Result<String> {
        if (encryptedData == null || ivBase64 == null) return Result.success("")
        return try {
            val ivBytes = CryptoManager.base64ToBytes(ivBase64)
            Result.success(CryptoManager.decrypt(encryptedData, key, ivBytes))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}