package com.appnest.safe.credentials.vault.ui.viewmodel.add

import com.appnest.safe.credentials.vault.utils.UiText

data class SecurityQuestionItemUi(
    val id: String,
    val question: String,
    val answer: String,
    val isAnswerVisible: Boolean = false
)

data class AddCredentialUiState(
    val credentialId: String? = null,
    val site: String = "",
    val category: String = "Site", // "Site", "Card", "Computer"
    val siteLogo: String? = null,
    val username: String = "",
    val password: String = "",
    val pin: String = "",
    val note: String = "",
    val isFavorite: Boolean = false,
    val securityQuestions: List<SecurityQuestionItemUi> = emptyList(),
    // Card Fields
    val cardType: String = "Credit",
    val cardIssuer: String = "",
    val cardNameOnCard: String = "",
    val cardNumber: String = "",
    val cardValidityStart: String = "",
    val cardValidityEnd: String = "",
    val cardCvv: String = "",
    val isCardCvvVisible: Boolean = false,
    // Computer Field
    val osType: String = "Windows", // "Windows", "Mac", "Linux", "Android"
    val isPasswordVisible: Boolean = false,
    val isPinVisible: Boolean = false,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,

    // Field Errors
    val siteError: UiText? = null,
    val usernameError: UiText? = null,
    val passwordError: UiText? = null,
    val cardIssuerError: UiText? = null,
    val cardDateError: UiText? = null
)
