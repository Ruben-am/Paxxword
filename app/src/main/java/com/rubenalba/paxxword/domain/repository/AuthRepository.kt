package com.rubenalba.paxxword.domain.repository

interface AuthRepository {
    suspend fun changeMasterPassword(newPassword: CharArray): Boolean
    suspend fun login(password: CharArray): Boolean
    suspend fun register(password: CharArray, defaultEmailLabel: String)
    suspend fun restoreFromBackup(uriString: String, password: CharArray, defaultEmailLabel: String): Boolean
    suspend fun verifyMasterPassword(password: CharArray): Boolean
}