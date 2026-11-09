package com.appnest.safe.credentials.vault.domain.repository

import com.appnest.safe.credentials.vault.domain.model.Credential
import kotlinx.coroutines.flow.Flow

interface CredentialRepository {
    suspend fun saveCredential(credential: Credential, encryptionKey: String)
    suspend fun getCredentialById(id: String): Credential?
    fun getAllCredentials(): Flow<List<Credential>>
    fun getCredentialsBySite(siteOrApp: String): Flow<List<Credential>>
    fun getFavoriteCredentials(): Flow<List<Credential>>
    suspend fun decryptCredential(credential: Credential, key: String): Credential
    suspend fun seedMockData(encryptionKey: String)
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)
    suspend fun deleteCredential(id: String)
}
