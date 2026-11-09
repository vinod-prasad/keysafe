package com.appnest.safe.credentials.vault.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.repository.CredentialRepository
import com.appnest.safe.credentials.vault.domain.usecase.DecryptCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.GetCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CredentialDetailsUiState(
    val siteName: String = "",
    val credentials: List<Credential> = emptyList(),
    val isLoading: Boolean = false
)

class CredentialDetailsViewModel(
    private val getCredentialUseCase: GetCredentialUseCase,
    private val decryptCredentialUseCase: DecryptCredentialUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val credentialRepository: CredentialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CredentialDetailsUiState())
    val uiState: StateFlow<CredentialDetailsUiState> = _uiState.asStateFlow()

    fun loadCredentialsForSite(siteNameOrId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val targetSite = getCredentialUseCase.getById(siteNameOrId)?.siteOrApp ?: siteNameOrId
            _uiState.value = _uiState.value.copy(siteName = targetSite)

            getCredentialUseCase.getAll().collect { allList ->
                val matching = allList.filter { it.siteOrApp.equals(targetSite, ignoreCase = true) || it.id == siteNameOrId }
                _uiState.value = _uiState.value.copy(
                    siteName = matching.firstOrNull()?.siteOrApp ?: targetSite,
                    credentials = matching,
                    isLoading = false
                )
            }
        }
    }

    fun decryptCredential(credential: Credential, key: String, onResult: (Credential?, String?) -> Unit) {
        viewModelScope.launch {
            val result = decryptCredentialUseCase(credential, key)
            if (result.isSuccess) {
                onResult(result.getOrNull(), null)
            } else {
                onResult(null, result.exceptionOrNull()?.message ?: "Invalid Encryption Key")
            }
        }
    }

    fun toggleFavorite(credentialId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            toggleFavoriteUseCase(credentialId, !currentStatus)
        }
    }

    fun deleteCredential(credentialId: String) {
        viewModelScope.launch {
            credentialRepository.deleteCredential(credentialId)
        }
    }
}
