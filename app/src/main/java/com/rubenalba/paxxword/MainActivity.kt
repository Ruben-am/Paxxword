package com.rubenalba.paxxword

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.rubenalba.paxxword.ui.navigation.AppNavigation
import com.rubenalba.paxxword.ui.splash.SplashViewModel
import com.rubenalba.paxxword.ui.theme.PaxxwordTheme
import dagger.hilt.android.AndroidEntryPoint
import com.rubenalba.paxxword.ui.settings.SettingsViewModel
import com.rubenalba.paxxword.data.manager.LocaleManager
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            splashViewModel.isLoading.value
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            val settingsState by settingsViewModel.settingsState.collectAsState()
            ApplyLanguage(settingsState.language)
            PaxxwordTheme(appTheme = settingsState.theme) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val startDest by splashViewModel.startDestination.collectAsState()

                    if (startDest != null) {
                        AppNavigation(
                            navController = navController,
                            startDestination = startDest!!
                        )
                    }
                }
            }

        }
    }

    @Composable
    private fun ApplyLanguage(language: com.rubenalba.paxxword.domain.model.AppLanguage) {
        val context = LocalContext.current
        val code = LocaleManager.getLanguageCode(language)

        if (code != null) {
            val locale = Locale(code)
            Locale.setDefault(locale)
            val config = context.resources.configuration
            config.setLocale(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }
}