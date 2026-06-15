package com.rubenalba.paxxword.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rubenalba.paxxword.ui.generator.GeneratorScreen
import com.rubenalba.paxxword.ui.settings.SettingsScreen
import com.rubenalba.paxxword.ui.vault.VaultScreen
import com.rubenalba.paxxword.ui.trash.TrashScreen

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Vault : BottomNavItem("vault_tab", "Bóveda", Icons.Default.Lock)
    object Generator : BottomNavItem("generator_tab", "Generador", Icons.Default.Password)
    object Settings : BottomNavItem("settings_tab", "Ajustes", Icons.Default.Settings)
}

@Composable
fun MainScreen() {
    val bottomNavController = rememberNavController()
    val items = listOf(
        BottomNavItem.Vault,
        BottomNavItem.Generator,
        BottomNavItem.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Vault.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(BottomNavItem.Vault.route) {
                VaultScreen(onNavigateToTrash = {
                    bottomNavController.navigate("trash")
                })
            }
            composable(BottomNavItem.Generator.route) {
                GeneratorScreen()
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen()
            }

            composable("trash") {
                TrashScreen(onBack = { bottomNavController.popBackStack() })
            }
        }
    }
}