package com.rubenalba.paxxword.data.manager

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleManager {
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    fun getLanguageCode(appLanguage: com.rubenalba.paxxword.domain.model.AppLanguage): String? {
        return when (appLanguage) {
            com.rubenalba.paxxword.domain.model.AppLanguage.ENGLISH -> "en"
            com.rubenalba.paxxword.domain.model.AppLanguage.SPANISH -> "es"
            else -> null
        }
    }
}