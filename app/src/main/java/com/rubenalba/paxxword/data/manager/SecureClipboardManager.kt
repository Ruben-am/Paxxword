package com.rubenalba.paxxword.data.manager

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureClipboardManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var clearJob: Job? = null

    fun copySensitiveText(label: String, text: String) {
        if (text.isBlank()) return

        val clip = ClipData.newPlainText(label, text)

        // Android 13+ : Oculta el contenido en la alerta visual del sistema
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }

        clipboard.setPrimaryClip(clip)

        // Limpieza tras 45 segundos
        clearJob?.cancel()
        clearJob = scope.launch {
            delay(45 * 1000L)
            clearClipboardIfMatches(text)
        }
    }

    fun copyStandardText(label: String, text: String) {
        if (text.isBlank()) return

        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)

        clearJob?.cancel()
    }

    private fun clearClipboardIfMatches(copiedText: String) {
        val currentClip = clipboard.primaryClip
        if (currentClip != null && currentClip.itemCount > 0) {
            val currentText = currentClip.getItemAt(0).text?.toString()

            if (currentText == copiedText) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    clipboard.clearPrimaryClip()
                } else {
                    clipboard.setPrimaryClip(ClipData.newPlainText("", "")) // Hack para Android < 9
                }
            }
        }
    }
}