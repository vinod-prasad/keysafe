package com.appnest.safe.credentials.vault.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.appnest.safe.credentials.vault.navigation.BottomNavItem
import com.appnest.safe.credentials.vault.navigation.NavigationState
import com.appnest.safe.credentials.vault.navigation.Navigator

@Composable
fun BottomNavigationBar(
    navigationState: NavigationState,
    navigator: Navigator
) {
    val items = listOf(
        BottomNavItem.Accounts,
        BottomNavItem.Favorites,
        BottomNavItem.Settings
    )

    NavigationBar {
        items.forEach { item ->
            val isSelected = item.key == navigationState.topLevelRoute

            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = stringResource(item.titleRes)) },
                label = { Text(text = stringResource(item.titleRes)) },
                selected = isSelected,
                onClick = {
                    navigator.navigate(item.key)
                }
            )
        }
    }
}
