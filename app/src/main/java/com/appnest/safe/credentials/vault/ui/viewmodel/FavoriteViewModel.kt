package com.appnest.safe.credentials.vault.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.usecase.GetCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FavoriteUiState(
    val favoriteCredentials: List<Credential> = emptyList(),
    val groupedFavorites: List<SiteGroupItem> = emptyList()
)

class FavoriteViewModel(
    getCredentialUseCase: GetCredentialUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    val uiState: StateFlow<FavoriteUiState> = getCredentialUseCase.getFavorites()
        .map { list ->
            val grouped = list.groupBy { cred ->
                val rawName = when (cred.category.lowercase()) {
                    "card" -> cred.cardIssuer.takeIf { !it.isNullOrBlank() } ?: cred.siteOrApp
                    "computer" -> cred.osType.takeIf { !it.isNullOrBlank() } ?: "Computer"
                    else -> cred.siteOrApp
                }
                rawName.cleanDomainDisplay()
            }.map { (groupName, items) ->
                SiteGroupItem(
                    siteName = groupName,
                    siteLogo = items.firstOrNull()?.siteLogo,
                    category = items.firstOrNull()?.category ?: "Site",
                    accountCount = if (items.size > 1) items.size else 0,
                    representativeId = items.first().id
                )
            }
            FavoriteUiState(
                favoriteCredentials = list,
                groupedFavorites = grouped
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoriteUiState()
        )

    fun toggleFavorite(id: String, currentFavorite: Boolean) {
        viewModelScope.launch {
            toggleFavoriteUseCase(id, !currentFavorite)
        }
    }
}
