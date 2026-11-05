package com.appnest.safe.credentials.vault.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val IS_APP_LOCK_ENABLED = booleanPreferencesKey("is_app_lock_enabled")
        val IDLE_LOCK_TIMEOUT_MINS = intPreferencesKey("idle_lock_timeout_mins") // e.g., 0, 5, 10
        val AUTH_METHOD = androidx.datastore.preferences.core.stringPreferencesKey("auth_method") // "UNSET", "DEVICE", "PIN"
        val APP_PIN = androidx.datastore.preferences.core.stringPreferencesKey("app_pin")
        val THEME_MODE = androidx.datastore.preferences.core.stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
        val FONT_STYLE = androidx.datastore.preferences.core.stringPreferencesKey("font_style") // "DEFAULT", "SERIF", "MONOSPACE"
        val FONT_SIZE = androidx.datastore.preferences.core.stringPreferencesKey("font_size") // "SMALL", "DEFAULT", "MEDIUM", "LARGE"
    }

    val isAppLockEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_APP_LOCK_ENABLED] ?: true
    }

    val idleLockTimeoutMinsFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[IDLE_LOCK_TIMEOUT_MINS] ?: 5
    }

    val authMethodFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[AUTH_METHOD] ?: "UNSET"
    }

    val appPinFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_PIN]
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setIdleLockTimeoutMins(mins: Int) {
        context.dataStore.edit { preferences ->
            preferences[IDLE_LOCK_TIMEOUT_MINS] = mins
        }
    }

    suspend fun setAuthMethod(method: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_METHOD] = method
        }
    }

    suspend fun setAppPin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_PIN] = pin
        }
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "SYSTEM"
    }

    val fontStyleFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[FONT_STYLE] ?: "DEFAULT"
    }

    val fontSizeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[FONT_SIZE] ?: "DEFAULT"
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setFontStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[FONT_STYLE] = style
        }
    }

    suspend fun setFontSize(size: String) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE] = size
        }
    }
}
