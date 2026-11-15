package com.appnest.safe.credentials.vault.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.appnest.safe.credentials.vault.ui.screens.FavoriteScreen
import com.appnest.safe.credentials.vault.ui.screens.HomeScreen
import com.appnest.safe.credentials.vault.ui.screens.SearchScreen
import com.appnest.safe.credentials.vault.ui.screens.SettingsScreen
import com.appnest.safe.credentials.vault.ui.screens.CredentialDetailsScreen

@Composable
fun AppNavDisplay(
    navigationState: NavigationState,
    navigator: Navigator
) {
    val entryProvider = remember(navigator) {
        entryProvider<NavKey> {
            entry<Screen.Home> {
                HomeScreen(navigator = navigator)
            }
            entry<Screen.Favorites> {
                FavoriteScreen(navigator = navigator)
            }
            entry<Screen.Settings> {
                SettingsScreen()
            }
            entry<Screen.BackupPayload> {
                com.appnest.safe.credentials.vault.ui.screens.BackupPayloadScreen(
                    onNavigateBack = { navigator.goBack() }
                )
            }
            entry<Screen.HelpSupport> {
                com.appnest.safe.credentials.vault.ui.screens.HelpSupportScreen()
            }
            entry<Screen.AddCredential> { key ->
                when (key.category?.lowercase()) {
                    "card" -> {
                        com.appnest.safe.credentials.vault.ui.screens.add.AddCardScreen(
                            credentialId = key.credentialId,
                            key = key.key,
                            onNavigateBack = { navigator.goBack() }
                        )
                    }
                    "computer" -> {
                        com.appnest.safe.credentials.vault.ui.screens.add.AddComputerScreen(
                            credentialId = key.credentialId,
                            key = key.key,
                            onNavigateBack = { navigator.goBack() }
                        )
                    }
                    else -> {
                        com.appnest.safe.credentials.vault.ui.screens.add.AddSiteScreen(
                            credentialId = key.credentialId,
                            key = key.key,
                            onNavigateBack = { navigator.goBack() }
                        )
                    }
                }
            }
            entry<Screen.Search> {
                SearchScreen(
                    onNavigateBack = { navigator.goBack() },
                    onCredentialClick = { id ->
                        navigator.navigate(Screen.CredentialDetails(id))
                    }
                )
            }
            entry<Screen.CredentialDetails> { key ->
                CredentialDetailsScreen(
                    siteName = key.siteName,
                    favoritesOnly = key.favoritesOnly,
                    onNavigateBack = { navigator.goBack() },
                    onNavigateToEdit = { credentialId, keyString ->
                        navigator.navigate(
                            Screen.AddCredential(
                                credentialId = credentialId,
                                key = keyString
                            )
                        )
                    }
                )
            }
        }
    }

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() }
    )
}
