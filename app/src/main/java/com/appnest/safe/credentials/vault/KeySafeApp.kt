package com.appnest.safe.credentials.vault

import android.app.Application
import com.appnest.safe.credentials.vault.data.SettingsManager
import com.appnest.safe.credentials.vault.data.db.KeySafeDatabaseHelper
import com.appnest.safe.credentials.vault.data.repository.CredentialRepositoryImpl
import com.appnest.safe.credentials.vault.data.transformer.AESDataTransformer
import com.appnest.safe.credentials.vault.domain.AppLockManager
import com.appnest.safe.credentials.vault.domain.repository.CredentialRepository
import com.appnest.safe.credentials.vault.domain.transformer.DataTransformer
import com.appnest.safe.credentials.vault.domain.usecase.DecryptCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.GetCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.SaveCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.SeedMockDataUseCase
import com.appnest.safe.credentials.vault.domain.usecase.ToggleFavoriteUseCase
import com.appnest.safe.credentials.vault.ui.viewmodel.AddCredentialViewModel
import com.appnest.safe.credentials.vault.ui.viewmodel.CredentialDetailsViewModel
import com.appnest.safe.credentials.vault.ui.viewmodel.HomeViewModel
import com.appnest.safe.credentials.vault.ui.viewmodel.SearchViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModule = module {
    single { SettingsManager(androidContext()) }
    single { AppLockManager() }

    // Database & Encryption Transformer
    single { KeySafeDatabaseHelper(androidContext()) }
    single<DataTransformer> { AESDataTransformer() }

    // Repositories
    single<CredentialRepository> { CredentialRepositoryImpl(get(), get()) }

    // Use Cases
    single { SaveCredentialUseCase(get()) }
    single { GetCredentialUseCase(get()) }
    single { DecryptCredentialUseCase(get()) }
    single { SeedMockDataUseCase(get()) }
    single { ToggleFavoriteUseCase(get()) }

    // ViewModels
    viewModel { AddCredentialViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { CredentialDetailsViewModel(get(), get(), get(), get()) }
    viewModel { SearchViewModel(get()) }
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
