package com.appnest.safe.credentials.vault.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.appnest.safe.credentials.vault.ui.screens.CheckupScreen
import com.appnest.safe.credentials.vault.ui.screens.HomeScreen
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
        composable(Screen.AddCredential.route) {
            com.appnest.safe.credentials.vault.ui.screens.AddCredentialScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Search.route) {
            com.appnest.safe.credentials.vault.ui.screens.SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onCredentialClick = { id -> 
                    navController.navigate(Screen.CredentialDetails.createRoute(id))
                }
            )
        }
        composable(
            route = Screen.CredentialDetails.route,
            arguments = listOf(androidx.navigation.navArgument("id") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.appnest.safe.credentials.vault.ui.screens.CredentialDetailsScreen(
                siteName = "Item $id Details", // Passing dummy name for now based on ID
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
