package com.rubenalba.paxxword.domain.repository

interface AuthRepository {
    suspend fun changeMasterPassword(newPassword: String): Boolean
}