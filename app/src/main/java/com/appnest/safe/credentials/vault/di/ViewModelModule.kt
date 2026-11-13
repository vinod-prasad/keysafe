package com.appnest.safe.credentials.vault.di

import com.appnest.safe.credentials.vault.ui.viewmodel.AddCredentialViewModel
import com.appnest.safe.credentials.vault.ui.viewmodel.CredentialDetailsViewModel
import com.appnest.safe.credentials.vault.ui.viewmodel.FavoriteViewModel
import com.appnest.safe.credentials.vault.ui.viewmodel.HomeViewModel
import com.appnest.safe.credentials.vault.ui.viewmodel.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { AddCredentialViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { FavoriteViewModel(get(), get()) }
    viewModel { CredentialDetailsViewModel(get(), get(), get(), get()) }
    viewModel { SearchViewModel(get()) }
}
