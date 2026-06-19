package com.rubenalba.paxxword.data.manager

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.google.gson.Gson
import com.rubenalba.paxxword.domain.model.AccountModel
import com.rubenalba.paxxword.domain.model.BackupAccount
import com.rubenalba.paxxword.domain.model.BackupData
import com.rubenalba.paxxword.domain.model.BackupFolder
import com.rubenalba.paxxword.domain.model.FolderModel
import com.rubenalba.paxxword.domain.model.PaxxBackupFile
import com.rubenalba.paxxword.domain.repository.AccountRepository
import com.rubenalba.paxxword.domain.repository.FolderRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountRepository: AccountRepository,
    private val folderRepository: FolderRepository
) {
    private val gson = Gson()

    // export
    suspend fun exportBackup(uri: Uri, masterPassword: String) = withContext(Dispatchers.IO) {
        // get decrypted data (actual session)
        val folders = folderRepository.getAllFolders().first()
        val accounts = accountRepository.getAccounts(null).first()

        // map to back up model
        val backupData = BackupData(
            timestamp = System.currentTimeMillis(),
            folders = folders.map { BackupFolder(it.id, it.name) },
            accounts = accounts.map { acc ->
                // MODIFICADO: Cambiado ?.folderName a ?.name porque ahora estamos usando FolderModel
                val folderName = folders.find { it.id == acc.folderId }?.name
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

    fun deleteBackupFile(uri: Uri) {
        try {
            DocumentsContract.deleteDocument(context.contentResolver, uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun restoreDataToRepository(data: BackupData) {
        // obtain folders (no duplicates)
        val currentFolders = folderRepository.getAllFolders().first()
        val folderMap = mutableMapOf<String, Long>()

        // restore folders
        data.folders.forEach { bFolder ->
            val cleanName = bFolder.name.trim()
            val existing = currentFolders.find { it.name.trim() == cleanName }

            if (existing != null) {
                folderMap[cleanName] = existing.id
            } else {
                val newFolder = FolderModel(name = cleanName)
                val newId = folderRepository.insertFolder(newFolder)
                if (newId > 0) {
                    folderMap[cleanName] = newId
                }
            }
        }

        // restore accounts
        data.accounts.forEach { bAccount ->
            val targetFolderId = if (bAccount.folderName != null) {
                folderMap[bAccount.folderName.trim()]
            } else null

            val newAccount = AccountModel(
                serviceName = bAccount.serviceName,
                username = bAccount.username,
                email = bAccount.email,
                password = bAccount.password,
                url = bAccount.url,
                notes = bAccount.notes,
                folderId = targetFolderId
            )
            accountRepository.saveAccount(newAccount)
        }
    }
}