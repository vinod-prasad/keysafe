package com.appnest.safe.credentials.vault.domain.usecase

import com.appnest.safe.credentials.vault.domain.repository.CredentialRepository

class SeedMockDataUseCase(
    private val repository: CredentialRepository
) {
    suspend operator fun invoke(encryptionKey: String): Result<Unit> {
        if (encryptionKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Encryption Key cannot be empty"))
        }
        return try {
            repository.seedMockData(encryptionKey)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
