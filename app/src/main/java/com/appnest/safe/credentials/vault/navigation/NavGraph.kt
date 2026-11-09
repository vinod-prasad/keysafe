package com.appnest.safe.credentials.vault.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.appnest.safe.credentials.vault.ui.screens.AddCredentialScreen
import com.appnest.safe.credentials.vault.ui.screens.CheckupScreen
import com.appnest.safe.credentials.vault.ui.screens.CredentialDetailsScreen
import com.appnest.safe.credentials.vault.ui.screens.HomeScreen
import com.appnest.safe.credentials.vault.ui.screens.SearchScreen
import com.appnest.safe.credentials.vault.ui.screens.SettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Checkup.route) {
            CheckupScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(
            route = Screen.AddCredential.route,
            arguments = listOf(
                navArgument("credentialId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("key") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val credentialId = backStackEntry.arguments?.getString("credentialId")
            val key = backStackEntry.arguments?.getString("key")
            AddCredentialScreen(
                credentialId = credentialId,
                encryptionKey = key,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onCredentialClick = { id ->
                    navController.navigate(Screen.CredentialDetails.createRoute(id))
                }
            )
        }
        composable(
            route = Screen.CredentialDetails.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            CredentialDetailsScreen(
                siteName = id,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { credentialId, key ->
                    navController.navigate(Screen.AddCredential.createRoute(credentialId, key))
                }
            )
        }
    }
}
