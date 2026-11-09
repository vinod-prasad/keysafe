package com.appnest.safe.credentials.vault.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.model.SecurityQuestion
import com.appnest.safe.credentials.vault.domain.usecase.DecryptCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.GetCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.SaveCredentialUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class AddCredentialUiState(
    val credentialId: String? = null,
    val site: String = "",
    val category: String = "Site",
    val siteLogo: String? = null,
    val username: String = "",
    val password: String = "",
    val pin: String = "",
    val note: String = "",
    val isFavorite: Boolean = false,
    val securityQuestions: List<SecurityQuestionItemUi> = emptyList(),
    val isPasswordVisible: Boolean = false,
    val isPinVisible: Boolean = false,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class SecurityQuestionItemUi(
    val id: String = UUID.randomUUID().toString(),
    val question: String = "",
    val answer: String = "",
    val isAnswerVisible: Boolean = false
)

sealed interface AddCredentialEvent {
    object SaveSuccess : AddCredentialEvent
    data class SaveError(val message: String) : AddCredentialEvent
}

class AddCredentialViewModel(
    private val saveCredentialUseCase: SaveCredentialUseCase,
    private val getCredentialUseCase: GetCredentialUseCase,
    private val decryptCredentialUseCase: DecryptCredentialUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCredentialUiState())
    val uiState: StateFlow<AddCredentialUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddCredentialEvent>(replay = 1, extraBufferCapacity = 1)
    val eventFlow: SharedFlow<AddCredentialEvent> = _eventFlow.asSharedFlow()

    fun resetState() {
        _uiState.value = AddCredentialUiState()
    }

    fun loadCredentialForEditing(credentialId: String?, key: String?) {
        if (credentialId.isNullOrEmpty()) {
            resetState()
            return
        }
        viewModelScope.launch {
            val existing = getCredentialUseCase.getById(credentialId)
            if (existing != null) {
                val targetCred = if (!key.isNullOrEmpty()) {
                    decryptCredentialUseCase(existing, key).getOrDefault(existing)
                } else {
                    existing
                }
                _uiState.value = AddCredentialUiState(
                    credentialId = targetCred.id,
                    site = targetCred.siteOrApp,
                    category = targetCred.category,
                    siteLogo = targetCred.siteLogo,
                    username = targetCred.username,
                    password = targetCred.password,
                    pin = targetCred.pin ?: "",
                    note = targetCred.note ?: "",
                    isFavorite = targetCred.isFavorite,
                    securityQuestions = targetCred.securityQuestions.map {
                        SecurityQuestionItemUi(id = it.id, question = it.question, answer = it.answer)
                    },
                    isEditMode = true
                )
            }
        }
    }

    fun onSiteChanged(site: String) {
        _uiState.value = _uiState.value.copy(site = site, errorMessage = null)
    }

    fun onCategoryChanged(category: String) {
        val isSite = category.equals("Site", ignoreCase = true)
        val currentSite = _uiState.value.site
        val isAutoFilledCategory = listOf("Mobile", "Laptop", "Network").any { it.equals(currentSite, ignoreCase = true) }

        val newSite = if (!isSite) {
            category
        } else if (isAutoFilledCategory) {
            ""
        } else {
            currentSite
        }

        _uiState.value = _uiState.value.copy(
            category = category,
            site = newSite,
            errorMessage = null
        )
    }

    fun onSiteLogoChanged(logo: String?) {
        _uiState.value = _uiState.value.copy(siteLogo = logo)
    }

    fun toggleFavorite() {
        _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite)
    }

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onPinChanged(pin: String) {
        if (pin.length <= 8) {
            _uiState.value = _uiState.value.copy(pin = pin)
        }
    }

    fun onNoteChanged(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun togglePinVisibility() {
        _uiState.value = _uiState.value.copy(isPinVisible = !_uiState.value.isPinVisible)
    }

    fun addSecurityQuestion() {
        val currentList = _uiState.value.securityQuestions
        _uiState.value = _uiState.value.copy(
            securityQuestions = currentList + SecurityQuestionItemUi()
        )
    }

    fun removeSecurityQuestion(id: String) {
        val currentList = _uiState.value.securityQuestions
        _uiState.value = _uiState.value.copy(
            securityQuestions = currentList.filterNot { it.id == id }
        )
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

    fun saveCredential(encryptionKey: String) {
        val state = _uiState.value
        if (state.site.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Site or app cannot be empty")
            return
        }

        if (state.category.equals("Site", ignoreCase = true)) {
            val domainRegex = Regex("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
            if (!domainRegex.matches(state.site.trim())) {
                _uiState.value = state.copy(errorMessage = "Please enter a valid domain name (e.g. google.com, appnest.in)")
                return
            }
        }

        if (encryptionKey.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Encryption Key cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)

            val domainQuestions = state.securityQuestions
                .filter { it.question.isNotBlank() || it.answer.isNotBlank() }
                .map { SecurityQuestion(it.id, it.question, it.answer) }

            val credential = Credential(
                id = state.credentialId ?: UUID.randomUUID().toString(),
                siteOrApp = state.site.trim(),
                category = state.category,
                siteLogo = state.siteLogo,
                username = state.username,
                password = state.password,
                pin = state.pin.ifBlank { null },
                note = state.note.ifBlank { null },
                securityQuestions = domainQuestions,
                isFavorite = state.isFavorite,
                updatedAt = System.currentTimeMillis()
            )

            val result = saveCredentialUseCase(credential, encryptionKey)
            _uiState.value = _uiState.value.copy(isLoading = false)

            if (result.isSuccess) {
                _eventFlow.emit(AddCredentialEvent.SaveSuccess)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to save credential"
                _uiState.value = _uiState.value.copy(errorMessage = error)
                _eventFlow.emit(AddCredentialEvent.SaveError(error))
            }
        }
    }
}
