package com.appnest.safe.credentials.vault.ui.viewmodel.add

import com.appnest.safe.credentials.vault.R
import com.appnest.safe.credentials.vault.domain.model.ComputerCredential
import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.usecase.DecryptCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.GetCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.SaveCredentialUseCase
import com.appnest.safe.credentials.vault.utils.UiText
import java.util.UUID

class AddComputerViewModel(
    saveCredentialUseCase: SaveCredentialUseCase,
    getCredentialUseCase: GetCredentialUseCase,
    decryptCredentialUseCase: DecryptCredentialUseCase
) : BaseAddCredentialViewModel(
    saveCredentialUseCase, getCredentialUseCase, decryptCredentialUseCase, "Computer"
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

    fun onOsTypeChanged(osType: String) {
        _uiState.value = _uiState.value.copy(osType = osType)
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

        _uiState.value = state
        return isValid
    }

    override fun createCredential(encryptionKey: String): Credential {
        val state = _uiState.value
        val domainQuestions = state.securityQuestions
            .filter { it.question.isNotBlank() || it.answer.isNotBlank() }
            .map { com.appnest.safe.credentials.vault.domain.model.SecurityQuestion(it.id, it.question, it.answer) }

        return ComputerCredential(
            id = state.credentialId ?: UUID.randomUUID().toString(),
            siteOrApp = state.site.trim(),
            category = "Computer",
            osType = state.osType,
            username = state.username,
            password = state.password,
            pin = state.pin.ifBlank { null },
            note = state.note.ifBlank { null },
            securityQuestions = domainQuestions,
            isFavorite = state.isFavorite
        )
    }

    override fun mapCredentialToState(credential: Credential) {
        if (credential !is ComputerCredential) return
        _uiState.value = AddCredentialUiState(
            credentialId = credential.id,
            site = credential.siteOrApp,
            category = credential.category,
            osType = credential.osType,
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
