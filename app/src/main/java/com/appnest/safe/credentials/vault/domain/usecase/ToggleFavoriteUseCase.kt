package com.appnest.safe.credentials.vault.domain.usecase

import com.appnest.safe.credentials.vault.domain.repository.CredentialRepository

class ToggleFavoriteUseCase(
    private val repository: CredentialRepository
) {
    suspend operator fun invoke(id: String, isFavorite: Boolean) {
        repository.toggleFavorite(id, isFavorite)
    }
}
