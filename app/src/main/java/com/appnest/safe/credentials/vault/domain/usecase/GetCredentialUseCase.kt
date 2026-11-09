package com.appnest.safe.credentials.vault.domain.usecase

import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.repository.CredentialRepository
import kotlinx.coroutines.flow.Flow

class GetCredentialUseCase(
    private val repository: CredentialRepository
) {
    suspend fun getById(id: String): Credential? {
        return repository.getCredentialById(id)
    }

    fun getAll(): Flow<List<Credential>> {
        return repository.getAllCredentials()
    }
}
