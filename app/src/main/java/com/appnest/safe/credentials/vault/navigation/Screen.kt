package com.appnest.safe.credentials.vault.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.appnest.safe.credentials.vault.R
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
    data object BackupPayload : Screen

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
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    object Accounts : BottomNavItem(Screen.Home, R.string.nav_accounts, Icons.Default.Key)
    object Favorites : BottomNavItem(Screen.Favorites, R.string.nav_favorites, Icons.Default.Star)
    object Settings : BottomNavItem(Screen.Settings, R.string.nav_settings, Icons.Default.Settings)
}
