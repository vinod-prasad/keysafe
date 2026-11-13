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
        private const val DATABASE_VERSION = 4

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

        // Card Template Columns
        const val COLUMN_CARD_ISSUER = "card_issuer"
        const val COLUMN_CARD_NAME_ON_CARD = "card_name_on_card"
        const val COLUMN_CARD_NUMBER = "card_number"
        const val COLUMN_CARD_VALIDITY_START = "card_validity_start"
        const val COLUMN_CARD_VALIDITY_END = "card_validity_end"
        const val COLUMN_CARD_CVV = "card_cvv"

        // Computer Template Columns
        const val COLUMN_OS_TYPE = "os_type"

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
                $COLUMN_CRED_USERNAME TEXT,
                $COLUMN_CRED_PASSWORD TEXT,
                $COLUMN_CRED_PIN TEXT,
                $COLUMN_CRED_NOTE TEXT,
                $COLUMN_CRED_IS_FAVORITE INTEGER NOT NULL DEFAULT 0,
                $COLUMN_CARD_ISSUER TEXT,
                $COLUMN_CARD_NAME_ON_CARD TEXT,
                $COLUMN_CARD_NUMBER TEXT,
                $COLUMN_CARD_VALIDITY_START TEXT,
                $COLUMN_CARD_VALIDITY_END TEXT,
                $COLUMN_CARD_CVV TEXT,
                $COLUMN_OS_TYPE TEXT,
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
            runCatching { db.execSQL("ALTER TABLE $TABLE_CREDENTIALS ADD COLUMN $COLUMN_CRED_SITE_LOGO TEXT") }
            runCatching { db.execSQL("ALTER TABLE $TABLE_CREDENTIALS ADD COLUMN $COLUMN_CRED_IS_FAVORITE INTEGER NOT NULL DEFAULT 0") }
        }
        if (oldVersion < 3) {
            runCatching { db.execSQL("ALTER TABLE $TABLE_CREDENTIALS ADD COLUMN $COLUMN_CRED_CATEGORY TEXT NOT NULL DEFAULT 'Site'") }
        }
        if (oldVersion < 4) {
            runCatching { db.execSQL("ALTER TABLE $TABLE_CREDENTIALS ADD COLUMN $COLUMN_CARD_ISSUER TEXT") }
            runCatching { db.execSQL("ALTER TABLE $TABLE_CREDENTIALS ADD COLUMN $COLUMN_CARD_NAME_ON_CARD TEXT") }
            runCatching { db.execSQL("ALTER TABLE $TABLE_CREDENTIALS ADD COLUMN $COLUMN_CARD_NUMBER TEXT") }
            runCatching { db.execSQL("ALTER TABLE $TABLE_CREDENTIALS ADD COLUMN $COLUMN_CARD_VALIDITY_START TEXT") }
            runCatching { db.execSQL("ALTER TABLE $TABLE_CREDENTIALS ADD COLUMN $COLUMN_CARD_VALIDITY_END TEXT") }
            runCatching { db.execSQL("ALTER TABLE $TABLE_CREDENTIALS ADD COLUMN $COLUMN_CARD_CVV TEXT") }
            runCatching { db.execSQL("ALTER TABLE $TABLE_CREDENTIALS ADD COLUMN $COLUMN_OS_TYPE TEXT") }
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }
}
