package com.rubenalba.paxxword.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class PasswordStrength(val color: Color, val fraction: Float, val label: String) {
    NONE(Color.Transparent, 0f, ""),
    WEAK(Color(0xFFEF5350), 0.33f, "Débil"),
    FAIR(Color(0xFFFFA726), 0.66f, "Aceptable"),
    STRONG(Color(0xFF66BB6A), 1f, "Fuerte")
}

fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.NONE
    var score = 0
    if (password.length >= 8) score++
    if (password.length >= 12) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { it.isLowerCase() } && password.any { it.isUpperCase() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when {
        score <= 1 -> PasswordStrength.WEAK
        score in 2..3 -> PasswordStrength.FAIR
        else -> PasswordStrength.STRONG
    }
}

@Composable
fun PasswordStrengthBar(password: String, modifier: Modifier = Modifier) {
    val strength = calculatePasswordStrength(password)

    if (strength != PasswordStrength.NONE) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinearProgressIndicator(
                progress = { strength.fraction },
                modifier = Modifier.weight(1f).height(4.dp),
                color = strength.color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = strength.label,
                style = MaterialTheme.typography.labelSmall,
                color = strength.color
            )
        }
    }
}