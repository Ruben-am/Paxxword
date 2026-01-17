package com.rubenalba.myapplication.data

import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {

    // master key variable, app start = null
    private var encryptionKey: SecretKeySpec? = null

    private var lastActivityTime: Long = 0
    private val SESSION_TIMEOUT_MS = 3 * 24 * 60 * 60 * 1000L // 3 days

    // When user log key is stored here
    fun setKey(key: SecretKeySpec) {
        this.encryptionKey = key
        updateActivityTime()
    }

    // ask for the key when we need to decrypt something
    fun getKey(): SecretKeySpec? {
        if (isSessionExpired()) {
            clearSession()
            return null
        }
        updateActivityTime()
        return encryptionKey
    }

    private fun isSessionExpired(): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastActivityTime) > SESSION_TIMEOUT_MS
    }

    private fun updateActivityTime() {
        lastActivityTime = System.currentTimeMillis()
    }

    fun clearSession() {
        encryptionKey = null
        lastActivityTime = 0
    }

    fun isUserLoggedIn(): Boolean {
        return getKey() != null
    }
}