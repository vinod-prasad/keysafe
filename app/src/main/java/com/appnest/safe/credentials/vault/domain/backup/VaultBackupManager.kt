package com.appnest.safe.credentials.vault.domain.backup

import kotlinx.serialization.json.Json
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class VaultBackupManager {

    companion object {
        private const val MAGIC_HEADER = "KSVAULT1" // 8 bytes header
        private const val SALT_SIZE = 16
        private const val IV_SIZE = 12
        private const val TAG_SIZE_BITS = 128
        private const val KEY_SIZE_BITS = 256
        private const val PBKDF2_ITERATIONS = 100_000

        private val jsonFormat = Json {
            ignoreUnknownKeys = true
            prettyPrint = false
        }
    }

    /**
     * Encrypts BackupPayload to byte array formatted for .ksvault file.
     */
    fun createBackupFileBytes(payload: BackupPayload, backupPassword: String): ByteArray {
        val jsonString = jsonFormat.encodeToString(BackupPayload.serializer(), payload)
        val plainBytes = jsonString.toByteArray(Charsets.UTF_8)

        val random = SecureRandom()
        val salt = ByteArray(SALT_SIZE)
        random.nextBytes(salt)

        val iv = ByteArray(IV_SIZE)
        random.nextBytes(iv)

        val secretKey = deriveKey(backupPassword, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TAG_SIZE_BITS, iv))
        val ciphertext = cipher.doFinal(plainBytes)

        val headerBytes = MAGIC_HEADER.toByteArray(Charsets.UTF_8)
        val result = ByteArray(headerBytes.size + SALT_SIZE + IV_SIZE + ciphertext.size)

        var offset = 0
        System.arraycopy(headerBytes, 0, result, offset, headerBytes.size)
        offset += headerBytes.size

        System.arraycopy(salt, 0, result, offset, SALT_SIZE)
        offset += SALT_SIZE

        System.arraycopy(iv, 0, result, offset, IV_SIZE)
        offset += IV_SIZE

        System.arraycopy(ciphertext, 0, result, offset, ciphertext.size)

        return result
    }

    /**
     * Decrypts byte array from .ksvault file into BackupPayload.
     */
    fun readBackupFileBytes(fileBytes: ByteArray, backupPassword: String): BackupPayload {
        val headerBytes = MAGIC_HEADER.toByteArray(Charsets.UTF_8)
        if (fileBytes.size < headerBytes.size + SALT_SIZE + IV_SIZE) {
            throw IllegalArgumentException("Invalid backup file: File too short")
        }

        val magic = String(fileBytes, 0, headerBytes.size, Charsets.UTF_8)
        if (magic != MAGIC_HEADER) {
            throw IllegalArgumentException("Invalid backup file format or version")
        }

        var offset = headerBytes.size
        val salt = ByteArray(SALT_SIZE)
        System.arraycopy(fileBytes, offset, salt, 0, SALT_SIZE)
        offset += SALT_SIZE

        val iv = ByteArray(IV_SIZE)
        System.arraycopy(fileBytes, offset, iv, 0, IV_SIZE)
        offset += IV_SIZE

        val ciphertext = ByteArray(fileBytes.size - offset)
        System.arraycopy(fileBytes, offset, ciphertext, 0, ciphertext.size)

        val secretKey = deriveKey(backupPassword, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_SIZE_BITS, iv))

        val plainBytes = try {
            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid Backup Password or corrupted file")
        }

        val jsonString = String(plainBytes, Charsets.UTF_8)
        return jsonFormat.decodeFromString(BackupPayload.serializer(), jsonString)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val keySpec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_SIZE_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(keySpec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }
}
