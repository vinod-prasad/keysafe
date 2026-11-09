package com.appnest.safe.credentials.vault.domain.usecase

import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.repository.CredentialRepository

class SaveCredentialUseCase(
    private val repository: CredentialRepository
) {
    suspend operator fun invoke(credential: Credential, encryptionKey: String): Result<Unit> {
        if (credential.siteOrApp.isBlank()) {
            return Result.failure(IllegalArgumentException("Site or app name cannot be empty"))
        }
        if (encryptionKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Encryption Key cannot be empty"))
        }
        return try {
            repository.saveCredential(credential, encryptionKey)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
