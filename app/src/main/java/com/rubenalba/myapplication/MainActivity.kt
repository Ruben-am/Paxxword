package com.rubenalba.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rubenalba.myapplication.ui.addedit.AddAccountScreen
import com.rubenalba.myapplication.ui.vault.VaultScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "vault") {

                        // vault
                        composable("vault") {
                            VaultScreen(
                                onAddClick = {
                                    // + -> add account
                                    navController.navigate("add_account")
                                },
                                onItemClick = { id ->

                                }
                            )
                        }

                        // add account
                        composable("add_account") {
                            AddAccountScreen(
                                onBackClick = {
                                    // save or back arrow -> vault
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}