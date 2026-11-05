package com.appnest.safe.credentials.vault.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appnest.safe.credentials.vault.data.SettingsManager
import com.appnest.safe.credentials.vault.domain.AppLockManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    settingsManager: SettingsManager = koinInject(),
    appLockManager: AppLockManager = koinInject()
) {
    val isAppLockEnabled by settingsManager.isAppLockEnabledFlow.collectAsState(initial = true)
    val idleLockTimeoutMins by settingsManager.idleLockTimeoutMinsFlow.collectAsState(initial = 5)
    val scope = rememberCoroutineScope()

    var expanded by remember { mutableStateOf(false) }
    val timeoutOptions = listOf(0, 1, 5, 10, 15, 30) // 0 = Immediately

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(24.dp))

        // App Lock Toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("App Lock", fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                Text("Require Biometric/PIN to unlock", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = isAppLockEnabled,
                onCheckedChange = { 
                    scope.launch { settingsManager.setAppLockEnabled(it) }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Idle Timeout Dropdown
        if (isAppLockEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Idle Lock Time", fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                    Text("Lock app after inactivity", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Box {
                    Text(
                        text = if (idleLockTimeoutMins == 0) "Immediately" else "$idleLockTimeoutMins mins",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { expanded = true }.padding(8.dp)
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        timeoutOptions.forEach { mins ->
                            DropdownMenuItem(
                                text = { Text(if (mins == 0) "Immediately" else "$mins mins") },
                                onClick = {
                                    scope.launch { settingsManager.setIdleLockTimeoutMins(mins) }
                                    appLockManager.updateIdleTimeout(mins)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Theme & Appearance", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        
        val themeMode by settingsManager.themeModeFlow.collectAsState(initial = "SYSTEM")
        val fontStyle by settingsManager.fontStyleFlow.collectAsState(initial = "DEFAULT")
        
        var themeExpanded by remember { mutableStateOf(false) }
        val themeOptions = listOf("SYSTEM", "LIGHT", "DARK")
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("App Theme", fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                Text("Choose dark/light/system mode", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box {
                Text(
                    text = themeMode,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { themeExpanded = true }.padding(8.dp)
                )
                DropdownMenu(
                    expanded = themeExpanded,
                    onDismissRequest = { themeExpanded = false }
                ) {
                    themeOptions.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode) },
                            onClick = {
                                scope.launch { settingsManager.setThemeMode(mode) }
                                themeExpanded = false
                            }
                        )
                    }
                }
            }
        }

        var fontExpanded by remember { mutableStateOf(false) }
        val fontOptions = listOf("DEFAULT", "Lato", "Poppins", "Roboto", "Open Sans", "Montserrat")

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Font Style", fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                Text("App custom font", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box {
                Text(
                    text = fontStyle,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { fontExpanded = true }.padding(8.dp)
                )
                DropdownMenu(
                    expanded = fontExpanded,
                    onDismissRequest = { fontExpanded = false }
                ) {
                    fontOptions.forEach { style ->
                        DropdownMenuItem(
                            text = { Text(style) },
                            onClick = {
                                scope.launch { settingsManager.setFontStyle(style) }
                                fontExpanded = false
                            }
                        )
                    }
                }
            }
        }

        val fontSize by settingsManager.fontSizeFlow.collectAsState(initial = "DEFAULT")
        var fontSizeExpanded by remember { mutableStateOf(false) }
        val fontSizeOptions = listOf("SMALL", "DEFAULT", "MEDIUM", "LARGE")

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Font Size", fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                Text("Scale app text ratio", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box {
                Text(
                    text = fontSize,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { fontSizeExpanded = true }.padding(8.dp)
                )
                DropdownMenu(
                    expanded = fontSizeExpanded,
                    onDismissRequest = { fontSizeExpanded = false }
                ) {
                    fontSizeOptions.forEach { size ->
                        DropdownMenuItem(
                            text = { Text(size) },
                            onClick = {
                                scope.launch { settingsManager.setFontSize(size) }
                                fontSizeExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    com.appnest.safe.credentials.vault.ui.theme.KeySafeTheme {
        // Need to provide dummy injected dependencies for preview
        val dummyContext = androidx.compose.ui.platform.LocalContext.current
        SettingsScreen(
            settingsManager = SettingsManager(dummyContext),
            appLockManager = AppLockManager()
        )
    }
}
