package com.appnest.safe.credentials.vault.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.usecase.GetCredentialUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SearchUiState(
    val query: String = "",
    val results: List<Credential> = emptyList()
)

class SearchViewModel(
    getCredentialUseCase: GetCredentialUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val uiState: StateFlow<SearchUiState> = combine(_query, getCredentialUseCase.getAll()) { q, list ->
        val filtered = if (q.isBlank()) list else list.filter {
            it.siteOrApp.contains(q, ignoreCase = true) || it.username.contains(q, ignoreCase = true)
        }
        SearchUiState(query = q, results = filtered)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    fun onQueryChanged(q: String) {
        _query.value = q
    }
}
