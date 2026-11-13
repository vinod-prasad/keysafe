package com.appnest.safe.credentials.vault.domain.model

import java.util.UUID

/**
 * Base sealed class for all credential templates using inheritance.
 * Common fields and optional extensions are declared at the base level so
 * repositories, use cases, view models, and lists can operate on the base Credential type seamlessly.
 */
sealed class Credential {
    abstract val id: String
    abstract val siteOrApp: String
    abstract val category: String
    open val siteLogo: String? = null
    open val username: String = ""
    open val password: String = ""
    open val pin: String? = null
    open val note: String? = null
    open val isFavorite: Boolean = false
    open val createdAt: Long = System.currentTimeMillis()
    open val updatedAt: Long = System.currentTimeMillis()

    // Card Specific Template Fields
    open val cardIssuer: String? = null
    open val cardNameOnCard: String? = null
    open val cardNumber: String? = null
    open val cardValidityStart: String? = null
    open val cardValidityEnd: String? = null
    open val cardCvv: String? = null

    // Computer Specific Template Fields
    open val osType: String? = null

    // Security Questions
    open val securityQuestions: List<SecurityQuestion> = emptyList()

    companion object {
        fun create(
            id: String = UUID.randomUUID().toString(),
            siteOrApp: String,
            category: String = "Site",
            siteLogo: String? = null,
            username: String = "",
            password: String = "",
            pin: String? = null,
            note: String? = null,
            securityQuestions: List<SecurityQuestion> = emptyList(),
            isFavorite: Boolean = false,
            cardIssuer: String? = null,
            cardNameOnCard: String? = null,
            cardNumber: String? = null,
            cardValidityStart: String? = null,
            cardValidityEnd: String? = null,
            cardCvv: String? = null,
            osType: String? = null,
            createdAt: Long = System.currentTimeMillis(),
            updatedAt: Long = System.currentTimeMillis()
        ): Credential {
            return when (category.lowercase()) {
                "card" -> CardCredential(
                    id = id,
                    siteOrApp = siteOrApp,
                    category = "Card",
                    cardIssuer = cardIssuer ?: "",
                    cardNameOnCard = cardNameOnCard ?: "",
                    cardNumber = cardNumber ?: "",
                    cardValidityStart = cardValidityStart ?: "",
                    cardValidityEnd = cardValidityEnd ?: "",
                    cardCvv = cardCvv ?: "",
                    pin = pin,
                    note = note,
                    isFavorite = isFavorite,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
                "computer" -> ComputerCredential(
                    id = id,
                    siteOrApp = siteOrApp,
                    category = "Computer",
                    osType = osType ?: "Windows",
                    username = username,
                    password = password,
                    securityQuestions = securityQuestions,
                    pin = pin,
                    note = note,
                    isFavorite = isFavorite,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
                else -> SiteCredential(
                    id = id,
                    siteOrApp = siteOrApp,
                    category = "Site",
                    siteLogo = siteLogo,
                    username = username,
                    password = password,
                    securityQuestions = securityQuestions,
                    pin = pin,
                    note = note,
                    isFavorite = isFavorite,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
            }
        }
    }
}

/**
 * Template 1: Site / Web Login Credential
 */
data class SiteCredential(
    override val id: String,
    override val siteOrApp: String,
    override val category: String = "Site",
    override val siteLogo: String? = null,
    override val username: String = "",
    override val password: String = "",
    override val securityQuestions: List<SecurityQuestion> = emptyList(),
    override val pin: String? = null,
    override val note: String? = null,
    override val isFavorite: Boolean = false,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis()
) : Credential()

/**
 * Template 2: Credit / Debit Card Credential
 */
data class CardCredential(
    override val id: String,
    override val siteOrApp: String,
    override val category: String = "Card",
    override val cardIssuer: String = "",
    override val cardNameOnCard: String = "",
    override val cardNumber: String = "",
    override val cardValidityStart: String = "",
    override val cardValidityEnd: String = "",
    override val cardCvv: String = "",
    override val pin: String? = null,
    override val note: String? = null,
    override val isFavorite: Boolean = false,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis()
) : Credential()

/**
 * Template 3: Computer / System Credential (PC, Mac, Linux, Android)
 */
data class ComputerCredential(
    override val id: String,
    override val siteOrApp: String,
    override val category: String = "Computer",
    override val osType: String = "Windows",
    override val username: String = "",
    override val password: String = "",
    override val securityQuestions: List<SecurityQuestion> = emptyList(),
    override val pin: String? = null,
    override val note: String? = null,
    override val isFavorite: Boolean = false,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis()
) : Credential()
