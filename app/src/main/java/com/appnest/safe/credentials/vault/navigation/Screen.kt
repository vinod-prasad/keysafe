package com.appnest.safe.credentials.vault.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Screen : NavKey {

    @Serializable
    data object Home : Screen

    @Serializable
    data object Favorites : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object Search : Screen

    @Serializable
    data class AddCredential(
        val credentialId: String? = null,
        val key: String? = null,
        val category: String? = null
    ) : Screen

    @Serializable
    data class CredentialDetails(
        val siteName: String,
        val favoritesOnly: Boolean = false
    ) : Screen
}

sealed class BottomNavItem(
    val key: Screen,
    val title: String,
    val icon: ImageVector
) {
    object Accounts : BottomNavItem(Screen.Home, "Accounts", Icons.Default.Key)
    object Favorites : BottomNavItem(Screen.Favorites, "Favorites", Icons.Default.Star)
    object Settings : BottomNavItem(Screen.Settings, "Settings", Icons.Default.Settings)
}
