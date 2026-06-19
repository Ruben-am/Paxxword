package com.rubenalba.paxxword.ui.generator

import androidx.lifecycle.ViewModel
import com.rubenalba.paxxword.data.manager.SecureClipboardManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class GeneratorState(
    val length: Float = 16f,
    val useLower: Boolean = true,
    val useUpper: Boolean = true,
    val useDigits: Boolean = true,
    val useSymbols: Boolean = true,
    val generatedPassword: CharArray = CharArray(0)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GeneratorState) return false
        if (length != other.length || useLower != other.useLower ||
            useUpper != other.useUpper || useDigits != other.useDigits ||
            useSymbols != other.useSymbols) return false
        return generatedPassword.contentEquals(other.generatedPassword)
    }

    override fun hashCode(): Int {
        var result = length.hashCode()
        result = 31 * result + useLower.hashCode()
        result = 31 * result + useUpper.hashCode()
        result = 31 * result + useDigits.hashCode()
        result = 31 * result + useSymbols.hashCode()
        result = 31 * result + generatedPassword.contentHashCode()
        return result
    }
}

@HiltViewModel
class GeneratorViewModel @Inject constructor(
    private val secureClipboardManager: SecureClipboardManager
) : ViewModel() {

    companion object {
        private const val LOWER_CHARS = "abcdefghijklmnopqrstuvwxyz"
        private const val UPPER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val DIGIT_CHARS = "0123456789"
        private const val SYMBOL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?"
    }

    private val _state = MutableStateFlow(GeneratorState())
    val state: StateFlow<GeneratorState> = _state.asStateFlow()

    init {
        generatePassword()
    }

    fun updateLength(newLength: Float) {
        _state.update { it.copy(length = newLength) }
        generatePassword()
    }

    fun toggleLower(checked: Boolean) = toggleOption(checked) { it.copy(useLower = checked) }
    fun toggleUpper(checked: Boolean) = toggleOption(checked) { it.copy(useUpper = checked) }
    fun toggleDigits(checked: Boolean) = toggleOption(checked) { it.copy(useDigits = checked) }
    fun toggleSymbols(checked: Boolean) = toggleOption(checked) { it.copy(useSymbols = checked) }

    // Lógica para evitar que el usuario desmarque todas las casillas
    private fun toggleOption(checked: Boolean, updateBlock: (GeneratorState) -> GeneratorState) {
        val currentState = _state.value
        val activeCount = listOf(
            currentState.useLower, currentState.useUpper,
            currentState.useDigits, currentState.useSymbols
        ).count { it }

        if (!checked && activeCount <= 1) {
            return
        }

        _state.update(updateBlock)
        generatePassword()
    }

    // Lógica de generación
    fun generatePassword() {
        val currentState = _state.value

        var pool = ""
        val forcedChars = mutableListOf<Char>()

        if (currentState.useLower) { pool += LOWER_CHARS; forcedChars.add(LOWER_CHARS.random()) }
        if (currentState.useUpper) { pool += UPPER_CHARS; forcedChars.add(UPPER_CHARS.random()) }
        if (currentState.useDigits) { pool += DIGIT_CHARS; forcedChars.add(DIGIT_CHARS.random()) }
        if (currentState.useSymbols) { pool += SYMBOL_CHARS; forcedChars.add(SYMBOL_CHARS.random()) }

        if (pool.isEmpty()) return

        val len = currentState.length.toInt()
        val chars = forcedChars.toMutableList()

        while (chars.size < len) { chars.add(pool.random()) }
        chars.shuffle()

        val oldPassword = _state.value.generatedPassword
        val newPassword = chars.toCharArray()

        _state.update { it.copy(generatedPassword = newPassword) }

        oldPassword.fill('\u0000')
    }

    // Acción de copiado
    fun copyToClipboard(label: String) {
        val passStr = String(_state.value.generatedPassword)
        secureClipboardManager.copySensitiveText(label, passStr)
    }

    override fun onCleared() {
        super.onCleared()
        _state.value.generatedPassword.fill('\u0000')
    }
}