package com.rubenalba.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rubenalba.myapplication.data.local.dao.UserDao
import com.rubenalba.myapplication.ui.addedit.AddAccountScreen
import com.rubenalba.myapplication.ui.auth.AuthScreen
import com.rubenalba.myapplication.ui.splash.SplashScreen
import com.rubenalba.myapplication.ui.vault.VaultScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // dao for asking for the user
    @Inject lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "splash") {

                        composable("splash") {
                            SplashScreen(
                                onNavigate = { destination ->
                                    navController.navigate(destination) {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // login
                        composable("login") {
                            AuthScreen(
                                isRegister = false,
                                onAuthSuccess = {
                                    // ok -> vault, erase record of login, back = exit
                                    navController.navigate("vault") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // sing up
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

                        // vault
                        composable("vault") {
                            VaultScreen(
                                onAddClick = { navController.navigate("add_account") },
                                onItemClick = { /* TODO: Detalles */ }
                            )
                        }

                        // add
                        composable("add_account") {
                            AddAccountScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}