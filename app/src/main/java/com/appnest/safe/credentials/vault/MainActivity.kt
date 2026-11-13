package com.appnest.safe.credentials.vault

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.appnest.safe.credentials.vault.domain.AppLockManager
import com.appnest.safe.credentials.vault.navigation.AppNavDisplay
import com.appnest.safe.credentials.vault.navigation.Navigator
import com.appnest.safe.credentials.vault.navigation.Screen
import com.appnest.safe.credentials.vault.navigation.rememberNavigationState
import com.appnest.safe.credentials.vault.ui.components.BottomNavigationBar
import com.appnest.safe.credentials.vault.ui.screens.AuthScreen
import com.appnest.safe.credentials.vault.ui.theme.KeySafeTheme
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {

    private val appLockManager: AppLockManager by inject()
    private val APP_UPDATE_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkForAppUpdate()

        setContent {
            val settingsManager: com.appnest.safe.credentials.vault.data.SettingsManager by inject()
            val isAppLockEnabled by settingsManager.isAppLockEnabledFlow.collectAsState(initial = true)
            val isLocked by appLockManager.isLocked.collectAsState()
            val idleTimeout by settingsManager.idleLockTimeoutMinsFlow.collectAsState(initial = 5)

            val authMethod by settingsManager.authMethodFlow.collectAsState(initial = "UNSET")
            val appPin by settingsManager.appPinFlow.collectAsState(initial = null)
            val themeMode by settingsManager.themeModeFlow.collectAsState(initial = "SYSTEM")
            val fontStyle by settingsManager.fontStyleFlow.collectAsState(initial = "DEFAULT")
            val fontSize by settingsManager.fontSizeFlow.collectAsState(initial = "DEFAULT")

            val scope = androidx.compose.runtime.rememberCoroutineScope()

            // Keep AppLockManager in sync with settings
            LaunchedEffect(idleTimeout) {
                appLockManager.updateIdleTimeout(idleTimeout)
            }

            KeySafeTheme(themeMode = themeMode, fontStyle = fontStyle, fontSize = fontSize) {
                when {
                    authMethod == "UNSET" -> {
                        com.appnest.safe.credentials.vault.ui.screens.SetupAuthScreen(
                            onSelectDeviceLock = {
                                scope.launch {
                                    settingsManager.setAuthMethod("DEVICE")
                                }
                            },
                            onPinCreated = { pin ->
                                scope.launch {
                                    settingsManager.setAppPin(pin) // Store PIN
                                    settingsManager.setAuthMethod("PIN")
                                }
                            }
                        )
                    }
                    isAppLockEnabled && isLocked -> {
                        if (authMethod == "DEVICE") {
                            AuthScreen(onAuthenticated = {
                                appLockManager.unlock()
                            })
                        } else if (authMethod == "PIN" && appPin != null) {
                            com.appnest.safe.credentials.vault.ui.screens.PinAuthScreen(
                                expectedPin = appPin!!,
                                onAuthenticated = {
                                    appLockManager.unlock()
                                }
                            )
                        } else {
                            // Fallback if state gets corrupted
                            AuthScreen(onAuthenticated = { appLockManager.unlock() })
                        }
                    }
                    else -> {
                        val navigationState = rememberNavigationState(
                            startRoute = Screen.Home,
                            topLevelRoutes = setOf(Screen.Home, Screen.Favorites, Screen.Settings)
                        )
                        val navigator = remember(navigationState) { Navigator(navigationState) }

                        val currentTopLevel = navigationState.topLevelRoute
                        val activeStack = navigationState.backStacks[currentTopLevel]
                        val currentKey = activeStack?.lastOrNull() ?: currentTopLevel

                        val adPlacement = when (currentKey) {
                            is Screen.Home, is Screen.Favorites, is Screen.Settings -> AdPlacement.BOTTOM
                            is Screen.AddCredential -> AdPlacement.TOP
                            is Screen.CredentialDetails -> AdPlacement.BOTTOM
                            else -> AdPlacement.NONE
                        }

                        val showBottomBar = currentKey in listOf(Screen.Home, Screen.Favorites, Screen.Settings)

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                if (showBottomBar) {
                                    BottomNavigationBar(
                                        navigationState = navigationState,
                                        navigator = navigator
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                if (adPlacement == AdPlacement.TOP || adPlacement == AdPlacement.BOTH) {
                                    AdBannerPlaceholder("Ad Banner Space (Top)")
                                }

                                // Main Content
                                Box(modifier = Modifier.weight(1f)) {
                                    AppNavDisplay(
                                        navigationState = navigationState,
                                        navigator = navigator
                                    )
                                }

                                if (adPlacement == AdPlacement.BOTTOM || adPlacement == AdPlacement.BOTH) {
                                    AdBannerPlaceholder("Ad Banner Space (Bottom)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkForAppUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    APP_UPDATE_REQUEST_CODE
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    APP_UPDATE_REQUEST_CODE
                )
            }
        }
    }
}

enum class AdPlacement { TOP, BOTTOM, BOTH, NONE }

@Composable
fun AdBannerPlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}
