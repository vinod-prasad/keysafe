package com.appnest.safe.credentials.vault.ui.viewmodel.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appnest.safe.credentials.vault.R
import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.model.SecurityQuestion
import com.appnest.safe.credentials.vault.domain.usecase.DecryptCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.GetCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.SaveCredentialUseCase
import com.appnest.safe.credentials.vault.utils.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

abstract class BaseAddCredentialViewModel(
    protected val saveCredentialUseCase: SaveCredentialUseCase,
    protected val getCredentialUseCase: GetCredentialUseCase,
    protected val decryptCredentialUseCase: DecryptCredentialUseCase,
    initialCategory: String
) : ViewModel() {

    protected val _uiState = MutableStateFlow(AddCredentialUiState(category = initialCategory))
    val uiState: StateFlow<AddCredentialUiState> = _uiState

    protected val _eventChannel = Channel<AddCredentialEvent>(Channel.BUFFERED)
    val eventFlow: Flow<AddCredentialEvent> = _eventChannel.receiveAsFlow()

    open fun resetState(credentialId: String? = null) {
        val currentCategory = _uiState.value.category
        _uiState.value = AddCredentialUiState(credentialId = credentialId, category = currentCategory)
    }

    fun loadCredentialForEditing(id: String?, key: String?) {
        if (id.isNullOrBlank() || key.isNullOrBlank()) {
            resetState()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val cred = getCredentialUseCase.getById(id)
            if (cred != null) {
                val targetCredResult = decryptCredentialUseCase(cred, key)
                targetCredResult.onSuccess { targetCred ->
                    mapCredentialToState(targetCred)
                }.onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = err.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.error_decryption_failed)
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = UiText.StringResource(R.string.error_credential_not_found)
                )
            }
        }
    }

    protected abstract fun mapCredentialToState(credential: Credential)
    protected abstract fun validateCategoryInputs(): Boolean
    protected abstract fun createCredential(encryptionKey: String): Credential

    // Common Field Updaters
    open fun onSiteChanged(site: String) {
        val newState = _uiState.value.copy(
            site = site.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            errorMessage = null,
            siteError = null
        )
        _uiState.value = newState
    }

    open fun onSiteFocusLost() {
        val state = _uiState.value
        if (state.site.isBlank()) return
        _uiState.value = state.copy(site = state.site.trim())
    }

    fun onSiteLogoChanged(logo: String?) {
        _uiState.value = _uiState.value.copy(siteLogo = logo)
    }

    fun toggleFavorite() {
        _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite)
    }

    fun onPinChanged(pin: String) {
        _uiState.value = _uiState.value.copy(pin = pin)
    }

    fun onNoteChanged(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun togglePinVisibility() {
        _uiState.value = _uiState.value.copy(isPinVisible = !_uiState.value.isPinVisible)
    }

    // Security Questions
    fun addSecurityQuestion() {
        val list = _uiState.value.securityQuestions.toMutableList()
        list.add(SecurityQuestionItemUi(id = UUID.randomUUID().toString(), question = "", answer = ""))
        _uiState.value = _uiState.value.copy(securityQuestions = list)
    }

    fun removeSecurityQuestion(id: String) {
        val list = _uiState.value.securityQuestions.filter { it.id != id }
        _uiState.value = _uiState.value.copy(securityQuestions = list)
    }

    fun updateSecurityQuestion(id: String, question: String) {
        val updatedList = _uiState.value.securityQuestions.map {
            if (it.id == id) it.copy(question = question) else it
        }
        _uiState.value = _uiState.value.copy(securityQuestions = updatedList)
    }

    fun updateSecurityAnswer(id: String, answer: String) {
        val updatedList = _uiState.value.securityQuestions.map {
            if (it.id == id) it.copy(answer = answer) else it
        }
        _uiState.value = _uiState.value.copy(securityQuestions = updatedList)
    }

    fun toggleSecurityAnswerVisibility(id: String) {
        val updatedList = _uiState.value.securityQuestions.map {
            if (it.id == id) it.copy(isAnswerVisible = !it.isAnswerVisible) else it
        }
        _uiState.value = _uiState.value.copy(securityQuestions = updatedList)
    }

    fun validateInputs(): Boolean {
        var isValid = true
        var state = _uiState.value

        if (state.site.isBlank()) {
            state = state.copy(siteError = UiText.StringResource(R.string.error_title_empty))
            isValid = false
        }

        _uiState.value = state

        // Let subclasses do category specific validation
        return isValid && validateCategoryInputs()
    }

    fun saveCredential(encryptionKey: String) {
        val state = _uiState.value

        if (!validateInputs()) return

        if (encryptionKey.isBlank()) {
            _uiState.value = state.copy(errorMessage = UiText.StringResource(R.string.error_encryption_key_empty))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = state.copy(isLoading = true)

            try {
                val credential = createCredential(encryptionKey)
                val result = saveCredentialUseCase(credential, encryptionKey)
                _uiState.value = _uiState.value.copy(isLoading = false)
                if (result.isSuccess) {
                    _eventChannel.send(AddCredentialEvent.SaveSuccess)
                } else {
                    val msg = result.exceptionOrNull()?.message ?: "Unknown error saving credential"
                    _uiState.value = _uiState.value.copy(errorMessage = UiText.DynamicString(msg))
                    _eventChannel.send(AddCredentialEvent.SaveError(msg))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message?.let { UiText.DynamicString(it) })
            }
        }
    }
}
