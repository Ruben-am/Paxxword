package com.rubenalba.paxxword.data.manager

import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Singleton
class SessionManager @Inject constructor() {

    private var encryptionKey: SecretKeySpec? = null
    private val SESSION_TIMEOUT_MS = 3 * 60 * 1000L

    private val _sessionActive = MutableStateFlow(false)
    val sessionActive = _sessionActive.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timeoutJob: Job? = null

    fun setKey(key: SecretKeySpec) {
        this.encryptionKey = key
        _sessionActive.value = true
        resetTimer()
    }

    fun getKey(): SecretKeySpec? {
        if (encryptionKey == null) {
            return null
        }
        resetTimer()
        return encryptionKey
    }

    private fun resetTimer() {
        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(SESSION_TIMEOUT_MS)
            clearSession()
        }
    }

    fun clearSession() {
        timeoutJob?.cancel()
        encryptionKey = null
        _sessionActive.value = false
    }

    fun isUserLoggedIn(): Boolean {
        return encryptionKey != null
    }
}