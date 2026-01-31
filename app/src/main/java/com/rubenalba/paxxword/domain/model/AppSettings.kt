package com.rubenalba.paxxword.domain.model

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

enum class AppLanguage {
    SYSTEM, ENGLISH, SPANISH
}

data class SettingsState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM
)