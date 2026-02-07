package com.rubenalba.paxxword.data.manager

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.rubenalba.paxxword.data.local.entity.Folder
import com.rubenalba.paxxword.domain.model.AccountModel
import com.rubenalba.paxxword.domain.model.BackupAccount
import com.rubenalba.paxxword.domain.model.BackupData
import com.rubenalba.paxxword.domain.model.BackupFolder
import com.rubenalba.paxxword.domain.model.PaxxBackupFile
import com.rubenalba.paxxword.domain.repository.PasswordRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PasswordRepository,
    private val cryptoManager: CryptoManager
) {
    private val gson = Gson()

    // export
    suspend fun exportBackup(uri: Uri, masterPassword: String) = withContext(Dispatchers.IO) {
        // get decrypted data (actual session)
        val folders = repository.getAllFolders().first()
        val accounts = repository.getAccounts(null).first()

        // map to back up model
        val backupData = BackupData(
            timestamp = System.currentTimeMillis(),
            folders = folders.map { BackupFolder(it.id, it.folderName) },
            accounts = accounts.map { acc ->
                val folderName = folders.find { it.id == acc.folderId }?.folderName
                BackupAccount(
                    serviceName = acc.serviceName,
                    username = acc.username,
                    email = acc.email,
                    password = acc.password,
                    url = acc.url,
                    notes = acc.notes,
                    folderName = folderName
                )
            }
        )

        // serialization to json
        val jsonPayload = gson.toJson(backupData)

        // prepare encryption for the file
        val salt = KeyDerivationUtil.generateSalt()
        val iv = CryptoManager.generateIv()
        val key = KeyDerivationUtil.deriveKey(masterPassword.toCharArray(), salt)

        // encrypt the JSON
        val encryptedPayload = CryptoManager.encrypt(
            jsonPayload.toByteArray(Charsets.UTF_8),
            key,
            iv
        )

        // create container
        val paxxFile = PaxxBackupFile(
            saltBase64 = CryptoManager.bytesToBase64(salt),
            ivBase64 = CryptoManager.bytesToBase64(iv),
            encryptedData = encryptedPayload
        )

        // write to URI
        val fileContent = gson.toJson(paxxFile)
        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(fileContent.toByteArray(Charsets.UTF_8))
        }
    }

    // import
    suspend fun importBackup(uri: Uri, masterPassword: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // read file
            val jsonContent = context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader().use { it.readText() }
            } ?: return@withContext false

            val paxxFile = gson.fromJson(jsonContent, PaxxBackupFile::class.java)

            // derive the key with the Salt of the file and the pass provided
            val salt = CryptoManager.base64ToBytes(paxxFile.saltBase64)
            val iv = CryptoManager.base64ToBytes(paxxFile.ivBase64)
            val key = KeyDerivationUtil.deriveKey(masterPassword.toCharArray(), salt)

            // decrypt
            val decryptedJson = try {
                CryptoManager.decrypt(paxxFile.encryptedData, key, iv)
            } catch (e: Exception) {
                // incorrect password or corrupt file
                return@withContext false
            }

            // 4. Deserialize
            val backupData = gson.fromJson(decryptedJson, BackupData::class.java)

            // insert in db
            restoreDataToRepository(backupData)

            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    private suspend fun restoreDataToRepository(data: BackupData) {
        // obtain actual folder
        val currentFolders = repository.getAllFolders().first()
        val folderMap = mutableMapOf<String, Long>()

        // restore folders
        data.folders.forEach { bFolder ->
            val existing = currentFolders.find { it.folderName == bFolder.name }
            if (existing != null) {
                folderMap[bFolder.name] = existing.id
            } else {
                val newFolder = Folder(folderName = bFolder.name)
                repository.insertFolder(newFolder)
                val updatedFolders = repository.getAllFolders().first()
                val createdId = updatedFolders.find { it.folderName == bFolder.name }?.id
                if (createdId != null) folderMap[bFolder.name] = createdId
            }
        }

        // restore accounts
        data.accounts.forEach { bAccount ->
            val targetFolderId = if (bAccount.folderName != null) folderMap[bAccount.folderName] else null

            val newAccount = AccountModel(
                serviceName = bAccount.serviceName,
                username = bAccount.username,
                email = bAccount.email,
                password = bAccount.password,
                url = bAccount.url,
                notes = bAccount.notes,
                folderId = targetFolderId
            )
            repository.saveAccount(newAccount)
        }
    }
}