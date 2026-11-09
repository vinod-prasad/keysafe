package com.appnest.safe.credentials.vault.domain.model

data class Credential(
    val id: String,
    val siteOrApp: String,
    val category: String = "Site",
    val siteLogo: String? = null,
    val username: String,
    val password: String,
    val pin: String? = null,
    val note: String? = null,
    val securityQuestions: List<SecurityQuestion> = emptyList(),
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
