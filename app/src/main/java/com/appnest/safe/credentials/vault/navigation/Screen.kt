package com.appnest.safe.credentials.vault.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Checkup : Screen("checkup")
    object Settings : Screen("settings")
    object Search : Screen("search")
    object AddCredential : Screen("add_credential")
    object CredentialDetails : Screen("credential_details/{id}") {
        fun createRoute(id: String) = "credential_details/$id"
    }
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Accounts : BottomNavItem(Screen.Home.route, "Accounts", Icons.Default.Key)
    object Checkup : BottomNavItem(Screen.Checkup.route, "Checkup", Icons.Default.CheckCircle)
    object Settings : BottomNavItem(Screen.Settings.route, "Settings", Icons.Default.Settings)
}
