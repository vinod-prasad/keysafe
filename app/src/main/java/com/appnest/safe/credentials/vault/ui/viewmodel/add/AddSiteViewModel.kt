package com.appnest.safe.credentials.vault.ui.viewmodel.add

import com.appnest.safe.credentials.vault.R
import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.model.SiteCredential
import com.appnest.safe.credentials.vault.domain.usecase.DecryptCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.GetCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.SaveCredentialUseCase
import com.appnest.safe.credentials.vault.utils.UiText
import java.util.UUID

class AddSiteViewModel(
    saveCredentialUseCase: SaveCredentialUseCase,
    getCredentialUseCase: GetCredentialUseCase,
    decryptCredentialUseCase: DecryptCredentialUseCase
) : BaseAddCredentialViewModel(
    saveCredentialUseCase, getCredentialUseCase, decryptCredentialUseCase, "Site"
) {

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(username = username, usernameError = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, passwordError = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    override fun onSiteChanged(site: String) {
        val newState = _uiState.value.copy(
            site = site.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            errorMessage = null,
            siteError = null
        )
        _uiState.value = newState

        if (site.isNotBlank()) {
            val cleanDomain = site.trim().lowercase().removePrefix("https://").removePrefix("http://")
            val domainRegex = Regex("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$")
            if (!domainRegex.matches(cleanDomain)) {
                _uiState.value = _uiState.value.copy(siteError = UiText.StringResource(R.string.error_domain_invalid))
            } else {
                _uiState.value = _uiState.value.copy(siteError = null)
            }
        }
    }

    override fun onSiteFocusLost() {
        val state = _uiState.value
        if (state.site.isBlank()) return

        val trimmed = state.site.trim()
        val cleanDomain = when {
            trimmed.startsWith("https://", ignoreCase = true) -> trimmed.substring(8)
            trimmed.startsWith("http://", ignoreCase = true) -> trimmed.substring(7)
            else -> trimmed
        }

        val domainRegex = Regex("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$")
        if (!domainRegex.matches(cleanDomain.lowercase())) {
            _uiState.value = state.copy(siteError = UiText.StringResource(R.string.error_domain_invalid))
        } else {
            _uiState.value = state.copy(site = cleanDomain.lowercase(), siteError = null, errorMessage = null)
        }
    }

    override fun validateCategoryInputs(): Boolean {
        var isValid = true
        var state = _uiState.value

        if (state.username.isBlank()) {
            state = state.copy(usernameError = UiText.StringResource(R.string.error_username_required))
            isValid = false
        }
        if (state.password.isBlank()) {
            state = state.copy(passwordError = UiText.StringResource(R.string.error_password_required))
            isValid = false
        }

        val trimmed = state.site.trim()
        val cleanDomain = trimmed.lowercase().removePrefix("https://").removePrefix("http://")
        val domainRegex = Regex("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$")
        if (!domainRegex.matches(cleanDomain)) {
            state = state.copy(siteError = UiText.StringResource(R.string.error_domain_invalid))
            isValid = false
        }

        _uiState.value = state
        return isValid
    }

    override fun createCredential(encryptionKey: String): Credential {
        val state = _uiState.value
        val domainQuestions = state.securityQuestions
            .filter { it.question.isNotBlank() || it.answer.isNotBlank() }
            .map { com.appnest.safe.credentials.vault.domain.model.SecurityQuestion(it.id, it.question, it.answer) }

        return SiteCredential(
            id = state.credentialId ?: UUID.randomUUID().toString(),
            siteOrApp = state.site.trim(),
            category = "Site",
            siteLogo = state.siteLogo,
            username = state.username,
            password = state.password,
            pin = state.pin.ifBlank { null },
            note = state.note.ifBlank { null },
            securityQuestions = domainQuestions,
            isFavorite = state.isFavorite
        )
    }

    override fun mapCredentialToState(credential: Credential) {
        if (credential !is SiteCredential) return
        _uiState.value = AddCredentialUiState(
            credentialId = credential.id,
            site = credential.siteOrApp,
            category = credential.category,
            siteLogo = credential.siteLogo,
            username = credential.username,
            password = credential.password,
            pin = credential.pin ?: "",
            note = credential.note ?: "",
            isFavorite = credential.isFavorite,
            securityQuestions = credential.securityQuestions.map {
                SecurityQuestionItemUi(id = it.id, question = it.question, answer = it.answer)
            },
            isEditMode = true,
            isLoading = false
        )
    }
}
