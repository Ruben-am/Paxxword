package com.rubenalba.paxxword.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rubenalba.paxxword.R

enum class PasswordStrength(val fraction: Float, @StringRes val labelRes: Int?) {
    NONE(0f, null),
    WEAK(0.33f, R.string.strength_weak),
    FAIR(0.66f, R.string.strength_fair),
    STRONG(1f, R.string.strength_strong)
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
        val isDark = isSystemInDarkTheme()

        val targetColor = when (strength) {
            PasswordStrength.NONE -> Color.Transparent
            PasswordStrength.WEAK -> if (isDark) Color(0xFFEF5350) else Color(0xFFD32F2F)
            PasswordStrength.FAIR -> if (isDark) Color(0xFFFFB74D) else Color(0xFFF57C00)
            PasswordStrength.STRONG -> if (isDark) Color(0xFF81C784) else Color(0xFF388E3C)
        }

        val animatedProgress by animateFloatAsState(
            targetValue = strength.fraction,
            label = "strength_progress"
        )

        val animatedColor by animateColorAsState(
            targetValue = targetColor,
            label = "strength_color"
        )

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val trackColor = MaterialTheme.colorScheme.surfaceVariant

            Canvas(modifier = Modifier.weight(1f).height(4.dp)) {
                drawLine(
                    color = trackColor,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round
                )

                if (animatedProgress > 0f) {
                    drawLine(
                        color = animatedColor,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width * animatedProgress, size.height / 2),
                        strokeWidth = size.height,
                        cap = StrokeCap.Round
                    )
                }
            }

            strength.labelRes?.let { labelId ->
                val rawText = stringResource(id = labelId)
                val cleanText = rawText.dropWhile { !it.isLetter() }.trim()

                Text(
                    text = cleanText,
                    style = MaterialTheme.typography.labelSmall,
                    color = animatedColor
                )
            }
        }
    }
}