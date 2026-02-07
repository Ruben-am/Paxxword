package com.rubenalba.paxxword.domain.model

import com.google.gson.annotations.SerializedName

data class PaxxBackupFile(
    @SerializedName("v") val version: Int = 1,
    @SerializedName("s") val saltBase64: String,
    @SerializedName("iv") val ivBase64: String,
    @SerializedName("d") val encryptedData: String
)

data class BackupData(
    val timestamp: Long,
    val folders: List<BackupFolder>,
    val accounts: List<BackupAccount>
)

data class BackupFolder(
    val id: Long,
    val name: String
)

data class BackupAccount(
    val serviceName: String,
    val username: String,
    val email: String,
    val password: String,
    val url: String,
    val notes: String,
    val folderName: String?
)