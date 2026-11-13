package com.appnest.safe.credentials.vault.di

import com.appnest.safe.credentials.vault.data.SettingsManager
import com.appnest.safe.credentials.vault.data.db.KeySafeDatabaseHelper
import com.appnest.safe.credentials.vault.data.repository.CredentialRepositoryImpl
import com.appnest.safe.credentials.vault.data.transformer.AESDataTransformer
import com.appnest.safe.credentials.vault.domain.repository.CredentialRepository
import com.appnest.safe.credentials.vault.domain.transformer.DataTransformer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single { SettingsManager(androidContext()) }
    single { KeySafeDatabaseHelper(androidContext()) }
    single<DataTransformer> { AESDataTransformer() }
    single<CredentialRepository> { CredentialRepositoryImpl(get(), get()) }
}
