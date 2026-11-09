package com.appnest.safe.credentials.vault.domain.model

data class SecurityQuestion(
    val id: String,
    val question: String,
    val answer: String
)
