package com.appnest.safe.credentials.vault

import android.app.Application
import com.appnest.safe.credentials.vault.data.SettingsManager
import com.appnest.safe.credentials.vault.domain.AppLockManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModule = module {
    single { SettingsManager(androidContext()) }
    single { AppLockManager() }
}

class KeySafeApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@KeySafeApp)
            modules(appModule)
        }

        // Attach lifecycle tracking
        val appLockManager: AppLockManager = org.koin.core.context.GlobalContext.get().get()
        appLockManager.attachToProcessLifecycle()
    }
}