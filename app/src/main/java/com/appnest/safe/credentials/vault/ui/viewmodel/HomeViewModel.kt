package com.appnest.safe.credentials.vault.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appnest.safe.credentials.vault.R
import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.usecase.GetCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.SeedMockDataUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

fun String.cleanDomainDisplay(): String {
    return when {
        this.startsWith("https://", ignoreCase = true) -> this.substring(8)
        this.startsWith("http://", ignoreCase = true) -> this.substring(7)
        else -> this
    }
}

fun getCategoryIconRes(category: String, nameOrOs: String): Int {
    val cat = category.lowercase()
    val nameLower = nameOrOs.lowercase()

    return when {
        cat == "card" -> R.drawable.credit_card
        cat == "computer" || nameLower.contains("mac") || nameLower.contains("windows") || nameLower.contains("linux") -> {
            when {
                nameLower.contains("mac") || nameLower.contains("apple") -> R.drawable.apple
                nameLower.contains("windows") || nameLower.contains("win") -> R.drawable.windows
                nameLower.contains("linux") -> R.drawable.linux
                else -> R.drawable.computer
            }
        }
        else -> R.drawable.web
    }
}

data class SiteGroupItem(
    val siteName: String,
    val siteLogo: String? = null,
    val category: String = "Site",
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
