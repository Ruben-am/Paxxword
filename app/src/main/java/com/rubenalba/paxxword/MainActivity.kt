package com.rubenalba.paxxword

import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.rubenalba.paxxword.data.manager.LocaleManager
import com.rubenalba.paxxword.data.manager.SessionManager
import com.rubenalba.paxxword.ui.navigation.AppNavigation
import com.rubenalba.paxxword.ui.settings.SettingsViewModel
import com.rubenalba.paxxword.ui.splash.SplashViewModel
import com.rubenalba.paxxword.ui.theme.PaxxwordTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var sessionManager: SessionManager

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        sessionManager.resetTimer()
        return super.dispatchTouchEvent(ev)
    }

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

            ApplyLanguage(language = settingsState.language) {
                PaxxwordTheme(appTheme = settingsState.theme) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        val navController = rememberNavController()
                        val startDest by splashViewModel.startDestination.collectAsState()
                        val isSessionActive by sessionManager.sessionActive.collectAsState()

                        LaunchedEffect(isSessionActive) {
                            if (!isSessionActive && startDest != null) {
                                val currentRoute = navController.currentDestination?.route
                                if (currentRoute != "login" && currentRoute != "signup") {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        }

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
    }
}

@Composable
fun ApplyLanguage(
    language: com.rubenalba.paxxword.domain.model.AppLanguage,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentConfig = LocalConfiguration.current
    val code = LocaleManager.getLanguageCode(language)

    val (updatedContext, finalConfig) = remember(context, currentConfig, code) {
        if (code != null) {
            val locale = Locale.forLanguageTag(code)
            Locale.setDefault(locale)

            val newConfig = Configuration(currentConfig)
            newConfig.setLocale(locale)
            newConfig.setLayoutDirection(locale)

            val configContext = context.createConfigurationContext(newConfig)

            val wrapper = object : ContextWrapper(context) {
                override fun getResources(): android.content.res.Resources {
                    return configContext.resources
                }
            }

            wrapper to newConfig
        } else {
            context to currentConfig
        }
    }

    CompositionLocalProvider(
        LocalContext provides updatedContext,
        LocalConfiguration provides finalConfig
    ) {
        content()
    }
}