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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rubenalba.paxxword.R
import com.rubenalba.paxxword.ui.generator.GeneratorScreen
import com.rubenalba.paxxword.ui.settings.SettingsScreen
import com.rubenalba.paxxword.ui.vault.VaultScreen
import com.rubenalba.paxxword.ui.trash.TrashScreen
import com.rubenalba.paxxword.ui.navigation.Screen

sealed class BottomNavItem(val route: String, val titleResId: Int, val icon: ImageVector) {
    object Vault : BottomNavItem(Screen.VaultTab.route, R.string.tab_vault, Icons.Default.Lock)
    object Generator : BottomNavItem(Screen.GeneratorTab.route, R.string.tab_generator, Icons.Default.Password)
    object Settings : BottomNavItem(Screen.SettingsTab.route, R.string.tab_settings, Icons.Default.Settings)
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
                        icon = { Icon(item.icon, contentDescription = stringResource(item.titleResId)) },
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
                    bottomNavController.navigate(Screen.Trash.route)
                })
            }
            composable(BottomNavItem.Generator.route) {
                GeneratorScreen()
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen()
            }

            composable(Screen.Trash.route) {
                TrashScreen(onBack = { bottomNavController.popBackStack() })
            }
        }
    }
}