package com.appnest.safe.credentials.vault.data.transformer

import android.util.Base64
import com.appnest.safe.credentials.vault.domain.transformer.DataTransformer
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Military-grade AES-256-GCM + PBKDF2 implementation of DataTransformer for local DB storage.
 * Uses AES/GCM/NoPadding with PBKDF2-HMAC-SHA256 key derivation.
 * Each entry is encrypted with a unique random Salt and IV.
 * The key is processed on-the-fly and never stored anywhere.
 */
class AESDataTransformer : DataTransformer {

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val SALT_SIZE = 16
        private const val IV_SIZE = 12
        private const val TAG_SIZE_BITS = 128
        private const val KEY_SIZE_BITS = 256
        private const val PBKDF2_ITERATIONS = 10_000 // Fast per-field DB operations
    }

    private fun deriveKey(key: String, salt: ByteArray): SecretKeySpec {
        val keySpec = PBEKeySpec(key.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_SIZE_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(keySpec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    override fun transformForStorage(plainText: String, key: String): String {
        if (plainText.isEmpty() || key.isEmpty()) return plainText
        return try {
            val random = SecureRandom()
            val salt = ByteArray(SALT_SIZE)
            random.nextBytes(salt)

            val iv = ByteArray(IV_SIZE)
            random.nextBytes(iv)

            val secretKey = deriveKey(key, salt)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TAG_SIZE_BITS, iv))
            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            // Pack: Salt (16b) + IV (12b) + Ciphertext
            val combined = ByteArray(SALT_SIZE + IV_SIZE + cipherBytes.size)
            System.arraycopy(salt, 0, combined, 0, SALT_SIZE)
            System.arraycopy(iv, 0, combined, SALT_SIZE, IV_SIZE)
            System.arraycopy(cipherBytes, 0, combined, SALT_SIZE + IV_SIZE, cipherBytes.size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText
        }
    }

    override fun transformFromStorage(cipherText: String, key: String): String {
        if (cipherText.isEmpty() || key.isEmpty()) return cipherText
        return try {
            val combined = Base64.decode(cipherText, Base64.NO_WRAP)
            if (combined.size < SALT_SIZE + IV_SIZE) return decryptLegacyCbc(cipherText, key)

            val salt = ByteArray(SALT_SIZE)
            System.arraycopy(combined, 0, salt, 0, SALT_SIZE)

            val iv = ByteArray(IV_SIZE)
            System.arraycopy(combined, SALT_SIZE, iv, 0, IV_SIZE)

            val cipherBytes = ByteArray(combined.size - (SALT_SIZE + IV_SIZE))
            System.arraycopy(combined, SALT_SIZE + IV_SIZE, cipherBytes, 0, cipherBytes.size)

            val secretKey = deriveKey(key, salt)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_SIZE_BITS, iv))

            val plainBytes = cipher.doFinal(cipherBytes)
            String(plainBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // Fallback for legacy AES-256-CBC entries
            try {
                decryptLegacyCbc(cipherText, key)
            } catch (fallbackEx: Exception) {
                throw IllegalArgumentException("Invalid Encryption Key")
            }
        }
    }

    private fun decryptLegacyCbc(cipherText: String, key: String): String {
        val sha256 = MessageDigest.getInstance("SHA-256")
        val keyBytes = sha256.digest(key.toByteArray(Charsets.UTF_8))
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val ivBytes = "KeySafeInitVector".toByteArray(Charsets.UTF_8).copyOf(16)
        val ivSpec = IvParameterSpec(ivBytes)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        val decodedBytes = Base64.decode(cipherText, Base64.NO_WRAP)
        return String(cipher.doFinal(decodedBytes), Charsets.UTF_8)
    }
}
