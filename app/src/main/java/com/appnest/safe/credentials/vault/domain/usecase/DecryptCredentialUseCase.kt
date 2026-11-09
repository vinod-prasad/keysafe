package com.appnest.safe.credentials.vault.domain.usecase

import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.repository.CredentialRepository

class DecryptCredentialUseCase(
    private val repository: CredentialRepository
) {
    suspend operator fun invoke(credential: Credential, encryptionKey: String): Result<Credential> {
        if (encryptionKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Encryption Key cannot be empty"))
        }
        return try {
            val decrypted = repository.decryptCredential(credential, encryptionKey)
            Result.success(decrypted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
