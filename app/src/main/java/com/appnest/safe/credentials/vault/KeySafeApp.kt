package com.appnest.safe.credentials.vault

import android.app.Application
import com.appnest.safe.credentials.vault.domain.AppLockManager
import com.appnest.safe.credentials.vault.di.dataModule
import com.appnest.safe.credentials.vault.di.domainModule
import com.appnest.safe.credentials.vault.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import coil.ImageLoaderFactory
import coil.ImageLoader
import com.appnest.safe.credentials.vault.utils.ImageLoaderUtils

class KeySafeApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@KeySafeApp)
            modules(listOf(dataModule, domainModule, viewModelModule))
        }

        // Attach lifecycle tracking
        val appLockManager: AppLockManager = org.koin.core.context.GlobalContext.get().get()
        appLockManager.attachToProcessLifecycle()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoaderUtils.createImageLoader(this)
    }
}
