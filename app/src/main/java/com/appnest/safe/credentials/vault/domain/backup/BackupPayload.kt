package com.appnest.safe.credentials.vault.domain.backup

import com.appnest.safe.credentials.vault.domain.model.CardCredential
import com.appnest.safe.credentials.vault.domain.model.ComputerCredential
import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.model.SecurityQuestion
import com.appnest.safe.credentials.vault.domain.model.SiteCredential
import kotlinx.serialization.Serializable

@Serializable
data class BackupPayload(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0.0",
    val credentials: List<CredentialBackupDto>
)

@Serializable
data class CredentialBackupDto(
    val id: String,
    val siteOrApp: String,
    val category: String,
    val siteLogo: String? = null,
    val username: String = "",
    val password: String = "",
    val pin: String? = null,
    val note: String? = null,
    val securityQuestions: List<SecurityQuestionBackupDto> = emptyList(),
    val isFavorite: Boolean = false,
    val cardType: String? = null,
    val cardIssuer: String? = null,
    val cardNameOnCard: String? = null,
    val cardNumber: String? = null,
    val cardValidityStart: String? = null,
    val cardValidityEnd: String? = null,
    val cardCvv: String? = null,
    val osType: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class SecurityQuestionBackupDto(
    val id: String,
    val question: String,
    val answer: String
)

fun Credential.toBackupDto(): CredentialBackupDto {
    return when (this) {
        is SiteCredential -> CredentialBackupDto(
            id = id,
            siteOrApp = siteOrApp,
            category = category,
            siteLogo = siteLogo,
            username = username,
            password = password,
            pin = pin,
            note = note,
            securityQuestions = securityQuestions.map { SecurityQuestionBackupDto(it.id, it.question, it.answer) },
            isFavorite = isFavorite,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
        is CardCredential -> CredentialBackupDto(
            id = id,
            siteOrApp = siteOrApp,
            category = category,
            username = username,
            password = password,
            pin = pin,
            note = note,
            isFavorite = isFavorite,
            cardType = cardType,
            cardIssuer = cardIssuer,
            cardNameOnCard = cardNameOnCard,
            cardNumber = cardNumber,
            cardValidityStart = cardValidityStart,
            cardValidityEnd = cardValidityEnd,
            cardCvv = cardCvv,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
        is ComputerCredential -> CredentialBackupDto(
            id = id,
            siteOrApp = siteOrApp,
            category = category,
            username = username,
            password = password,
            pin = pin,
            note = note,
            securityQuestions = securityQuestions.map { SecurityQuestionBackupDto(it.id, it.question, it.answer) },
            isFavorite = isFavorite,
            osType = osType,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

fun CredentialBackupDto.toDomainCredential(): Credential {
    val sqList = securityQuestions.map { SecurityQuestion(it.id, it.question, it.answer) }
    return when (category.lowercase()) {
        "card" -> CardCredential(
            id = id,
            siteOrApp = siteOrApp,
            category = "Card",
            cardType = cardType ?: "Credit",
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
            pin = pin,
            note = note,
            securityQuestions = sqList,
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
            pin = pin,
            note = note,
            securityQuestions = sqList,
            isFavorite = isFavorite,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
