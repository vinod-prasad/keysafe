package com.appnest.safe.credentials.vault.data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.appnest.safe.credentials.vault.data.db.KeySafeDatabaseHelper
import com.appnest.safe.credentials.vault.data.mockdata.MockDataProvider
import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.model.SecurityQuestion
import com.appnest.safe.credentials.vault.domain.repository.CredentialRepository
import com.appnest.safe.credentials.vault.domain.transformer.DataTransformer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class CredentialRepositoryImpl(
    private val dbHelper: KeySafeDatabaseHelper,
    private val transformer: DataTransformer
) : CredentialRepository {

    private val _credentialsFlow = MutableStateFlow<List<Credential>>(emptyList())

    override suspend fun saveCredential(credential: Credential, encryptionKey: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val encSite = credential.siteOrApp
            val encLogo = credential.siteLogo
            val encUsername = transformer.transformForStorage(credential.username, encryptionKey)
            val encPassword = transformer.transformForStorage(credential.password, encryptionKey)
            val encPin = credential.pin?.let { transformer.transformForStorage(it, encryptionKey) }
            val encNote = credential.note?.let { transformer.transformForStorage(it, encryptionKey) }

            val credValues = ContentValues().apply {
                put(KeySafeDatabaseHelper.COLUMN_CRED_ID, credential.id)
                put(KeySafeDatabaseHelper.COLUMN_CRED_SITE, encSite)
                put(KeySafeDatabaseHelper.COLUMN_CRED_CATEGORY, credential.category)
                put(KeySafeDatabaseHelper.COLUMN_CRED_SITE_LOGO, encLogo)
                put(KeySafeDatabaseHelper.COLUMN_CRED_USERNAME, encUsername)
                put(KeySafeDatabaseHelper.COLUMN_CRED_PASSWORD, encPassword)
                put(KeySafeDatabaseHelper.COLUMN_CRED_PIN, encPin)
                put(KeySafeDatabaseHelper.COLUMN_CRED_NOTE, encNote)
                put(KeySafeDatabaseHelper.COLUMN_CRED_IS_FAVORITE, if (credential.isFavorite) 1 else 0)
                put(KeySafeDatabaseHelper.COLUMN_CRED_CREATED_AT, credential.createdAt)
                put(KeySafeDatabaseHelper.COLUMN_CRED_UPDATED_AT, credential.updatedAt)
            }

            db.insertWithOnConflict(
                KeySafeDatabaseHelper.TABLE_CREDENTIALS,
                null,
                credValues,
                SQLiteDatabase.CONFLICT_REPLACE
            )

            // Clear old security questions for this credential
            db.delete(
                KeySafeDatabaseHelper.TABLE_SECURITY_QUESTIONS,
                "${KeySafeDatabaseHelper.COLUMN_SQ_CREDENTIAL_ID} = ?",
                arrayOf(credential.id)
            )

            // Transform & insert security questions
            for (sq in credential.securityQuestions) {
                if (sq.question.isNotBlank() || sq.answer.isNotBlank()) {
                    val encQuestion = transformer.transformForStorage(sq.question, encryptionKey)
                    val encAnswer = transformer.transformForStorage(sq.answer, encryptionKey)

                    val sqValues = ContentValues().apply {
                        put(KeySafeDatabaseHelper.COLUMN_SQ_ID, sq.id.ifEmpty { UUID.randomUUID().toString() })
                        put(KeySafeDatabaseHelper.COLUMN_SQ_CREDENTIAL_ID, credential.id)
                        put(KeySafeDatabaseHelper.COLUMN_SQ_QUESTION, encQuestion)
                        put(KeySafeDatabaseHelper.COLUMN_SQ_ANSWER, encAnswer)
                    }
                    db.insert(KeySafeDatabaseHelper.TABLE_SECURITY_QUESTIONS, null, sqValues)
                }
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        refreshFlow()
    }

    override suspend fun seedMockData(encryptionKey: String) = withContext(Dispatchers.IO) {
        val initialList = MockDataProvider.getInitialCredentials()
        for (cred in initialList) {
            saveCredential(cred, encryptionKey)
        }
    }

    override suspend fun decryptCredential(credential: Credential, key: String): Credential = withContext(Dispatchers.IO) {
        val decUsername = transformer.transformFromStorage(credential.username, key)
        val decPassword = transformer.transformFromStorage(credential.password, key)
        val decPin = credential.pin?.let { transformer.transformFromStorage(it, key) }
        val decNote = credential.note?.let { transformer.transformFromStorage(it, key) }

        val decSecurityQuestions = credential.securityQuestions.map { sq ->
            SecurityQuestion(
                id = sq.id,
                question = transformer.transformFromStorage(sq.question, key),
                answer = transformer.transformFromStorage(sq.answer, key)
            )
        }

        credential.copy(
            username = decUsername,
            password = decPassword,
            pin = decPin,
            note = decNote,
            securityQuestions = decSecurityQuestions
        )
    }

    override suspend fun getCredentialById(id: String): Credential? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        var credential: Credential? = null

        val cursor = db.query(
            KeySafeDatabaseHelper.TABLE_CREDENTIALS,
            null,
            "${KeySafeDatabaseHelper.COLUMN_CRED_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                credential = readCredentialFromCursor(db, c)
            }
        }
        credential
    }

    override fun getAllCredentials(): Flow<List<Credential>> {
        refreshFlow()
        return _credentialsFlow.asStateFlow()
    }

    override fun getCredentialsBySite(siteOrApp: String): Flow<List<Credential>> {
        refreshFlow()
        return _credentialsFlow.map { list ->
            list.filter { it.siteOrApp.equals(siteOrApp, ignoreCase = true) }
        }
    }

    override fun getFavoriteCredentials(): Flow<List<Credential>> {
        refreshFlow()
        return _credentialsFlow.map { list ->
            list.filter { it.isFavorite }
        }
    }

    override suspend fun toggleFavorite(id: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(KeySafeDatabaseHelper.COLUMN_CRED_IS_FAVORITE, if (isFavorite) 1 else 0)
            put(KeySafeDatabaseHelper.COLUMN_CRED_UPDATED_AT, System.currentTimeMillis())
        }
        db.update(
            KeySafeDatabaseHelper.TABLE_CREDENTIALS,
            values,
            "${KeySafeDatabaseHelper.COLUMN_CRED_ID} = ?",
            arrayOf(id)
        )
        refreshFlow()
    }

    override suspend fun deleteCredential(id: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(
            KeySafeDatabaseHelper.TABLE_CREDENTIALS,
            "${KeySafeDatabaseHelper.COLUMN_CRED_ID} = ?",
            arrayOf(id)
        )
        refreshFlow()
    }

    private fun refreshFlow() {
        val list = mutableListOf<Credential>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            KeySafeDatabaseHelper.TABLE_CREDENTIALS,
            null,
            null,
            null,
            null,
            null,
            "${KeySafeDatabaseHelper.COLUMN_CRED_UPDATED_AT} DESC"
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(readCredentialFromCursor(db, c))
            }
        }
        _credentialsFlow.value = list
    }

    private fun readCredentialFromCursor(db: SQLiteDatabase, c: Cursor): Credential {
        val id = c.getString(c.getColumnIndexOrThrow(KeySafeDatabaseHelper.COLUMN_CRED_ID))
        val site = c.getString(c.getColumnIndexOrThrow(KeySafeDatabaseHelper.COLUMN_CRED_SITE)) ?: ""
        val categoryIndex = c.getColumnIndex(KeySafeDatabaseHelper.COLUMN_CRED_CATEGORY)
        val category = if (categoryIndex != -1) c.getString(categoryIndex) ?: "Site" else "Site"
        val siteLogo = c.getString(c.getColumnIndexOrThrow(KeySafeDatabaseHelper.COLUMN_CRED_SITE_LOGO))
        val username = c.getString(c.getColumnIndexOrThrow(KeySafeDatabaseHelper.COLUMN_CRED_USERNAME)) ?: ""
        val password = c.getString(c.getColumnIndexOrThrow(KeySafeDatabaseHelper.COLUMN_CRED_PASSWORD)) ?: ""
        val pin = c.getString(c.getColumnIndexOrThrow(KeySafeDatabaseHelper.COLUMN_CRED_PIN))
        val note = c.getString(c.getColumnIndexOrThrow(KeySafeDatabaseHelper.COLUMN_CRED_NOTE))
        val isFavorite = c.getInt(c.getColumnIndexOrThrow(KeySafeDatabaseHelper.COLUMN_CRED_IS_FAVORITE)) == 1
        val createdAt = c.getLong(c.getColumnIndexOrThrow(KeySafeDatabaseHelper.COLUMN_CRED_CREATED_AT))
        val updatedAt = c.getLong(c.getColumnIndexOrThrow(KeySafeDatabaseHelper.COLUMN_CRED_UPDATED_AT))

        val securityQuestions = mutableListOf<SecurityQuestion>()
        val sqCursor = db.query(
            KeySafeDatabaseHelper.TABLE_SECURITY_QUESTIONS,
            null,
            "${KeySafeDatabaseHelper.COLUMN_SQ_CREDENTIAL_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )
        sqCursor.use { sqC ->
            while (sqC.moveToNext()) {
                val sqId = sqC.getString(sqC.getColumnIndexOrThrow(KeySafeDatabaseHelper.COLUMN_SQ_ID))
                val q = sqC.getString(sqC.getColumnIndexOrThrow(KeySafeDatabaseHelper.COLUMN_SQ_QUESTION)) ?: ""
                val a = sqC.getString(sqC.getColumnIndexOrThrow(KeySafeDatabaseHelper.COLUMN_SQ_ANSWER)) ?: ""

                securityQuestions.add(SecurityQuestion(sqId, q, a))
            }
        }

        return Credential(
            id = id,
            siteOrApp = site,
            category = category,
            siteLogo = siteLogo,
            username = username,
            password = password,
            pin = pin,
            note = note,
            securityQuestions = securityQuestions,
            isFavorite = isFavorite,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
