package com.appnest.safe.credentials.vault.data.transformer

import android.util.Base64
import com.appnest.safe.credentials.vault.domain.transformer.DataTransformer
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-256 implementation of DataTransformer.
 * Uses a user-supplied key dynamically derived via SHA-256.
 * The key is processed on-the-fly and never stored anywhere.
 */
class AESDataTransformer : DataTransformer {

    private val transformation = "AES/CBC/PKCS5Padding"
    private val ivBytes = "KeySafeInitVector".toByteArray(Charsets.UTF_8).copyOf(16)
    private val ivSpec = IvParameterSpec(ivBytes)

    private fun deriveKey(key: String): SecretKeySpec {
        val sha256 = MessageDigest.getInstance("SHA-256")
        val keyBytes = sha256.digest(key.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    override fun transformForStorage(plainText: String, key: String): String {
        if (plainText.isEmpty() || key.isEmpty()) return plainText
        return try {
            val secretKey = deriveKey(key)
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText
        }
    }

    override fun transformFromStorage(cipherText: String, key: String): String {
        if (cipherText.isEmpty() || key.isEmpty()) return cipherText
        return try {
            val secretKey = deriveKey(key)
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            val decodedBytes = Base64.decode(cipherText, Base64.NO_WRAP)
            String(cipher.doFinal(decodedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid Encryption Key")
        }
    }
}
