package com.appnest.safe.credentials.vault.ui.viewmodel.add

import com.appnest.safe.credentials.vault.R
import com.appnest.safe.credentials.vault.domain.model.CardCredential
import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.usecase.DecryptCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.GetCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.SaveCredentialUseCase
import com.appnest.safe.credentials.vault.utils.UiText
import java.util.UUID

class AddCardViewModel(
    saveCredentialUseCase: SaveCredentialUseCase,
    getCredentialUseCase: GetCredentialUseCase,
    decryptCredentialUseCase: DecryptCredentialUseCase
) : BaseAddCredentialViewModel(
    saveCredentialUseCase, getCredentialUseCase, decryptCredentialUseCase, "Card"
) {

    fun onCardTypeChanged(type: String) {
        _uiState.value = _uiState.value.copy(cardType = type)
    }

    fun onCardIssuerChanged(issuer: String) {
        _uiState.value = _uiState.value.copy(
            cardIssuer = issuer.uppercase(),
            cardIssuerError = null
        )
    }

    fun onCardNameOnCardChanged(name: String) {
        _uiState.value = _uiState.value.copy(cardNameOnCard = name.uppercase())
    }

    fun onCardNumberChanged(number: String) {
        if (number.length <= 19) {
            _uiState.value = _uiState.value.copy(cardNumber = number)
        }
    }

    fun onCardValidityStartChanged(start: String) {
        val (formatted, error) = processSimpleCardDate(start)
        _uiState.value = _uiState.value.copy(cardValidityStart = formatted, cardDateError = error)
    }

    fun onCardValidityEndChanged(end: String) {
        val (formatted, error) = processSimpleCardDate(end)
        _uiState.value = _uiState.value.copy(cardValidityEnd = formatted, cardDateError = error)
    }

    private fun processSimpleCardDate(input: String): Pair<String, UiText?> {
        var digits = input.filter { it.isDigit() }
        if (digits.length > 4) {
            digits = digits.take(4)
        }

        // Auto-prefix 0 for first digit 2-9
        if (digits.length == 1) {
            val firstDigit = digits[0].digitToInt()
            if (firstDigit in 2..9) {
                digits = "0$digits"
            }
        }

        // Check MM range (1-12)
        var error: UiText? = null
        if (digits.length >= 2) {
            val month = digits.substring(0, 2).toIntOrNull() ?: 0
            if (month !in 1..12) {
                error = UiText.StringResource(R.string.error_month_range)
            }
        }

        return Pair(digits, error)
    }

    fun onCardCvvChanged(cvv: String) {
        if (cvv.length <= 4) {
            _uiState.value = _uiState.value.copy(cardCvv = cvv)
        }
    }

    fun toggleCardCvvVisibility() {
        _uiState.value = _uiState.value.copy(isCardCvvVisible = !_uiState.value.isCardCvvVisible)
    }

    override fun validateCategoryInputs(): Boolean {
        var isValid = true
        var state = _uiState.value

        if (state.cardIssuer.isBlank()) {
            state = state.copy(cardIssuerError = UiText.StringResource(R.string.error_issuer_required))
            isValid = false
        }

        val start = state.cardValidityStart
        val end = state.cardValidityEnd

        // Check start month validity
        if (start.length >= 2) {
            val startMonth = start.substring(0, 2).toIntOrNull() ?: 0
            if (startMonth !in 1..12) {
                state = state.copy(cardDateError = UiText.StringResource(R.string.error_start_month_range))
                isValid = false
            }
        }

        // Check end month validity
        if (end.length >= 2) {
            val endMonth = end.substring(0, 2).toIntOrNull() ?: 0
            if (endMonth !in 1..12) {
                state = state.copy(cardDateError = UiText.StringResource(R.string.error_expiry_month_range))
                isValid = false
            }
        }

        if (start.length == 4 && end.length == 4) {
            val startMonth = start.substring(0, 2).toInt()
            val startYear = start.substring(2, 4).toInt()
            val endMonth = end.substring(0, 2).toInt()
            val endYear = end.substring(2, 4).toInt()

            val startTotalMonths = startYear * 12 + startMonth
            val endTotalMonths = endYear * 12 + endMonth

            if (startTotalMonths >= endTotalMonths) {
                state = state.copy(cardDateError = UiText.StringResource(R.string.error_expiry_before_start))
                isValid = false
            }
        } else if (start.isNotBlank() || end.isNotBlank()) {
            if (start.length in 1..3 || end.length in 1..3) {
                state = state.copy(cardDateError = UiText.StringResource(R.string.error_dates_full_format))
                isValid = false
            }
        }

        _uiState.value = state
        return isValid
    }

    override fun createCredential(encryptionKey: String): Credential {
        val state = _uiState.value
        return CardCredential(
            id = state.credentialId ?: UUID.randomUUID().toString(),
            siteOrApp = state.site.trim(),
            category = "Card",
            cardType = state.cardType,
            cardIssuer = state.cardIssuer,
            cardNameOnCard = state.cardNameOnCard,
            cardNumber = state.cardNumber,
            cardValidityStart = state.cardValidityStart,
            cardValidityEnd = state.cardValidityEnd,
            cardCvv = state.cardCvv,
            pin = state.pin.ifBlank { null },
            note = state.note.ifBlank { null },
            isFavorite = state.isFavorite
        )
    }

    override fun mapCredentialToState(credential: Credential) {
        if (credential !is CardCredential) return
        _uiState.value = AddCredentialUiState(
            credentialId = credential.id,
            site = credential.siteOrApp,
            category = credential.category,
            cardType = credential.cardType,
            cardIssuer = credential.cardIssuer ?: "",
            cardNameOnCard = credential.cardNameOnCard ?: "",
            cardNumber = credential.cardNumber ?: "",
            cardValidityStart = credential.cardValidityStart ?: "",
            cardValidityEnd = credential.cardValidityEnd ?: "",
            cardCvv = credential.cardCvv ?: "",
            pin = credential.pin ?: "",
            note = credential.note ?: "",
            isFavorite = credential.isFavorite,
            isEditMode = true,
            isLoading = false
        )
    }
}
