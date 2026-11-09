package com.appnest.safe.credentials.vault.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class KeySafeDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "keysafe_vault.db"
        private const val DATABASE_VERSION = 3

        // Master Table: Credentials
        const val TABLE_CREDENTIALS = "credentials"
        const val COLUMN_CRED_ID = "id"
        const val COLUMN_CRED_SITE = "site_or_app"
        const val COLUMN_CRED_CATEGORY = "category"
        const val COLUMN_CRED_SITE_LOGO = "site_logo"
        const val COLUMN_CRED_USERNAME = "username"
        const val COLUMN_CRED_PASSWORD = "password"
        const val COLUMN_CRED_PIN = "pin"
        const val COLUMN_CRED_NOTE = "note"
        const val COLUMN_CRED_IS_FAVORITE = "is_favorite"
        const val COLUMN_CRED_CREATED_AT = "created_at"
        const val COLUMN_CRED_UPDATED_AT = "updated_at"

        // Child Table: Security Questions
        const val TABLE_SECURITY_QUESTIONS = "security_questions"
        const val COLUMN_SQ_ID = "id"
        const val COLUMN_SQ_CREDENTIAL_ID = "credential_id"
        const val COLUMN_SQ_QUESTION = "question"
        const val COLUMN_SQ_ANSWER = "answer"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createCredentialsTable = """
            CREATE TABLE $TABLE_CREDENTIALS (
                $COLUMN_CRED_ID TEXT PRIMARY KEY,
                $COLUMN_CRED_SITE TEXT NOT NULL,
                $COLUMN_CRED_CATEGORY TEXT NOT NULL DEFAULT 'Site',
                $COLUMN_CRED_SITE_LOGO TEXT,
                $COLUMN_CRED_USERNAME TEXT NOT NULL,
                $COLUMN_CRED_PASSWORD TEXT NOT NULL,
                $COLUMN_CRED_PIN TEXT,
                $COLUMN_CRED_NOTE TEXT,
                $COLUMN_CRED_IS_FAVORITE INTEGER NOT NULL DEFAULT 0,
                $COLUMN_CRED_CREATED_AT INTEGER NOT NULL,
                $COLUMN_CRED_UPDATED_AT INTEGER NOT NULL
            )
        """.trimIndent()

        val createSecurityQuestionsTable = """
            CREATE TABLE $TABLE_SECURITY_QUESTIONS (
                $COLUMN_SQ_ID TEXT PRIMARY KEY,
                $COLUMN_SQ_CREDENTIAL_ID TEXT NOT NULL,
                $COLUMN_SQ_QUESTION TEXT NOT NULL,
                $COLUMN_SQ_ANSWER TEXT NOT NULL,
                FOREIGN KEY ($COLUMN_SQ_CREDENTIAL_ID) REFERENCES $TABLE_CREDENTIALS($COLUMN_CRED_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createCredentialsTable)
        db.execSQL(createSecurityQuestionsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE $TABLE_CREDENTIALS ADD COLUMN $COLUMN_CRED_SITE_LOGO TEXT")
                db.execSQL("ALTER TABLE $TABLE_CREDENTIALS ADD COLUMN $COLUMN_CRED_IS_FAVORITE INTEGER NOT NULL DEFAULT 0")
            } catch (e: Exception) {
                // Ignore fallback
            }
        }
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE $TABLE_CREDENTIALS ADD COLUMN $COLUMN_CRED_CATEGORY TEXT NOT NULL DEFAULT 'Site'")
            } catch (e: Exception) {
                db.execSQL("DROP TABLE IF EXISTS $TABLE_SECURITY_QUESTIONS")
                db.execSQL("DROP TABLE IF EXISTS $TABLE_CREDENTIALS")
                onCreate(db)
            }
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }
}
