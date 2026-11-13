package com.appnest.safe.credentials.vault.di

import com.appnest.safe.credentials.vault.domain.AppLockManager
import com.appnest.safe.credentials.vault.domain.usecase.DecryptCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.GetCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.SaveCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.SeedMockDataUseCase
import com.appnest.safe.credentials.vault.domain.usecase.ToggleFavoriteUseCase
import org.koin.dsl.module

val domainModule = module {
    single { AppLockManager() }
    single { SaveCredentialUseCase(get()) }
    single { GetCredentialUseCase(get()) }
    single { DecryptCredentialUseCase(get()) }
    single { SeedMockDataUseCase(get()) }
    single { ToggleFavoriteUseCase(get()) }
}
