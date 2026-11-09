package com.appnest.safe.credentials.vault.domain.transformer

/**
 * Interface for on-the-fly encryption / decryption transformation.
 * The encryption key is provided dynamically per operation and is NEVER stored locally.
 */
interface DataTransformer {
    fun transformForStorage(plainText: String, key: String): String
    fun transformFromStorage(cipherText: String, key: String): String
}
