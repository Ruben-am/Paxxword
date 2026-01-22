package com.rubenalba.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rubenalba.myapplication.ui.addedit.AddAccountScreen
import com.rubenalba.myapplication.ui.auth.AuthScreen
import com.rubenalba.myapplication.ui.splash.SplashScreen
import com.rubenalba.myapplication.ui.vault.VaultScreen

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = "splash") {

        // 1. Splash
        composable("splash") {
            SplashScreen(
                onNavigate = { destination ->
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 2. Auth (Login/Sing up)
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

        // 3. Main
        composable("vault") {
            VaultScreen(
                onAddClick = { navController.navigate("add_account") },
                onItemClick = { /* TODO: Detalles */ }
            )
        }

        composable("add_account") {
            AddAccountScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}