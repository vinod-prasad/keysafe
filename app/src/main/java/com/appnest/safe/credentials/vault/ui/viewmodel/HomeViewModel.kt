package com.appnest.safe.credentials.vault.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.usecase.GetCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.SeedMockDataUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SiteGroupItem(
    val siteName: String,
    val siteLogo: String? = null,
    val accountCount: Int,
    val representativeId: String
)

data class HomeUiState(
    val groupedCredentials: List<SiteGroupItem> = emptyList(),
    val favoriteCredentials: List<Credential> = emptyList(),
    val rawCredentials: List<Credential> = emptyList()
)

class HomeViewModel(
    getCredentialUseCase: GetCredentialUseCase,
    private val seedMockDataUseCase: SeedMockDataUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getCredentialUseCase.getAll()
        .map { list ->
            val grouped = list.groupBy { it.siteOrApp }
                .map { (siteName, items) ->
                    SiteGroupItem(
                        siteName = siteName,
                        siteLogo = items.firstOrNull()?.siteLogo,
                        accountCount = if (items.size > 1) items.size else 0,
                        representativeId = items.first().id
                    )
                }
            val favorites = list.filter { it.isFavorite }
            HomeUiState(
                groupedCredentials = grouped,
                favoriteCredentials = favorites,
                rawCredentials = list
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    fun seedInitialMockData(encryptionKey: String) {
        viewModelScope.launch {
            seedMockDataUseCase(encryptionKey)
        }
    }
}
