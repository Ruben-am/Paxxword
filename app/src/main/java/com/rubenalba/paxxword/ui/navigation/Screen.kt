package com.rubenalba.paxxword.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Main : Screen("main")
    object VaultTab : Screen("vault_tab")
    object GeneratorTab : Screen("generator_tab")
    object SettingsTab : Screen("settings_tab")
    object Trash : Screen("trash")
}