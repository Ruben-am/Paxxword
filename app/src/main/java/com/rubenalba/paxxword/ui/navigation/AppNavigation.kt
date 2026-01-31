package com.rubenalba.paxxword.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rubenalba.paxxword.ui.auth.AuthScreen
import com.rubenalba.paxxword.ui.settings.SettingsScreen
import com.rubenalba.paxxword.ui.vault.VaultScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // Auth (Login/Sing up)
        composable("login") {
            AuthScreen(
                isRegister = false,
                onAuthSuccess = {
                    navController.navigate("vault") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("signup") {
            AuthScreen(
                isRegister = true,
                onAuthSuccess = {
                    navController.navigate("vault") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }

        // Main
        composable("vault") {
            VaultScreen(
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}