package com.rubenalba.paxxword.data.manager

import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SessionManager @Inject constructor() {

    private var encryptionKey: SecretKeySpec? = null
    private var lastActivityTime: Long = 0

    private val SESSION_TIMEOUT_MS = 3 * 60 * 1000L

    private val _sessionActive = MutableStateFlow(false)
    val sessionActive = _sessionActive.asStateFlow()

    fun setKey(key: SecretKeySpec) {
        this.encryptionKey = key
        updateActivityTime()
        _sessionActive.value = true
    }

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
        _sessionActive.value = false
    }

    fun isUserLoggedIn(): Boolean {
        return getKey() != null
    }
}