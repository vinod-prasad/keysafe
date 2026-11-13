package com.appnest.safe.credentials.vault.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.model.SecurityQuestion
import com.appnest.safe.credentials.vault.domain.usecase.DecryptCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.GetCredentialUseCase
import com.appnest.safe.credentials.vault.domain.usecase.SaveCredentialUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

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

    private val _eventChannel = Channel<AddCredentialEvent>(Channel.BUFFERED)
    val eventFlow: Flow<AddCredentialEvent> = _eventChannel.receiveAsFlow()

    fun resetState(initialCategory: String? = null) {
        val defaultCat = initialCategory ?: "Site"
        val defaultSite = if (defaultCat.equals("Site", ignoreCase = true)) "" else defaultCat
        _uiState.value = AddCredentialUiState(
            category = defaultCat,
            site = defaultSite
        )
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
                    cardIssuer = targetCred.cardIssuer ?: "",
                    cardNameOnCard = targetCred.cardNameOnCard ?: "",
                    cardNumber = targetCred.cardNumber ?: "",
                    cardValidityStart = targetCred.cardValidityStart ?: "",
                    cardValidityEnd = targetCred.cardValidityEnd ?: "",
                    cardCvv = targetCred.cardCvv ?: "",
                    osType = targetCred.osType ?: "Windows",
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

    fun onSiteFocusLost() {
        val state = _uiState.value
        if (!state.category.equals("Site", ignoreCase = true) || state.site.isBlank()) return

        val trimmed = state.site.trim()
        val cleanDomain = when {
            trimmed.startsWith("https://", ignoreCase = true) -> trimmed.substring(8)
            trimmed.startsWith("http://", ignoreCase = true) -> trimmed.substring(7)
            else -> trimmed
        }

        val domainRegex = Regex("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$")
        if (!domainRegex.matches(cleanDomain)) {
            _uiState.value = state.copy(errorMessage = "Please enter a valid domain name (e.g. google.com, appnest.in)")
        } else {
            val formatted = if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
                "https://$cleanDomain"
            } else {
                trimmed
            }
            _uiState.value = state.copy(site = formatted, errorMessage = null)
        }
    }

    fun onCategoryChanged(category: String) {
        val isSite = category.equals("Site", ignoreCase = true)
        val newSite = if (!isSite) category else ""

        _uiState.value = _uiState.value.copy(
            category = category,
            site = newSite,
            errorMessage = null
        )
    }

    fun onCardIssuerChanged(issuer: String) {
        _uiState.value = _uiState.value.copy(cardIssuer = issuer)
    }

    fun onCardNameOnCardChanged(name: String) {
        _uiState.value = _uiState.value.copy(cardNameOnCard = name)
    }

    fun onCardNumberChanged(number: String) {
        if (number.length <= 19) {
            _uiState.value = _uiState.value.copy(cardNumber = number)
        }
    }

    fun onCardValidityStartChanged(start: String) {
        if (start.length <= 5) {
            _uiState.value = _uiState.value.copy(cardValidityStart = start)
        }
    }

    fun onCardValidityEndChanged(end: String) {
        if (end.length <= 5) {
            _uiState.value = _uiState.value.copy(cardValidityEnd = end)
        }
    }

    fun onCardCvvChanged(cvv: String) {
        if (cvv.length <= 4) {
            _uiState.value = _uiState.value.copy(cardCvv = cvv)
        }
    }

    fun toggleCardCvvVisibility() {
        _uiState.value = _uiState.value.copy(isCardCvvVisible = !_uiState.value.isCardCvvVisible)
    }

    fun onOsTypeChanged(osType: String) {
        _uiState.value = _uiState.value.copy(osType = osType)
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
            _uiState.value = state.copy(errorMessage = "Title / Label cannot be empty")
            return
        }

        if (state.category.equals("Site", ignoreCase = true)) {
            val trimmed = state.site.trim()
            val cleanDomain = when {
                trimmed.startsWith("https://", ignoreCase = true) -> trimmed.substring(8)
                trimmed.startsWith("http://", ignoreCase = true) -> trimmed.substring(7)
                else -> trimmed
            }
            val domainRegex = Regex("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$")
            if (!domainRegex.matches(cleanDomain)) {
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

            val credential = Credential.create(
                id = state.credentialId ?: UUID.randomUUID().toString(),
                siteOrApp = state.site.trim(),
                category = state.category,
                siteLogo = state.siteLogo,
                username = state.username,
                password = state.password,
                pin = state.pin.ifBlank { null },
                note = state.note.ifBlank { null },
                cardIssuer = state.cardIssuer.ifBlank { null },
                cardNameOnCard = state.cardNameOnCard.ifBlank { null },
                cardNumber = state.cardNumber.ifBlank { null },
                cardValidityStart = state.cardValidityStart.ifBlank { null },
                cardValidityEnd = state.cardValidityEnd.ifBlank { null },
                cardCvv = state.cardCvv.ifBlank { null },
                osType = if (state.category.equals("Computer", ignoreCase = true)) state.osType else null,
                securityQuestions = domainQuestions,
                isFavorite = state.isFavorite,
                updatedAt = System.currentTimeMillis()
            )

            val result = saveCredentialUseCase(credential, encryptionKey)
            _uiState.value = _uiState.value.copy(isLoading = false)

            if (result.isSuccess) {
                _eventChannel.send(AddCredentialEvent.SaveSuccess)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to save credential"
                _uiState.value = _uiState.value.copy(errorMessage = error)
                _eventChannel.send(AddCredentialEvent.SaveError(error))
            }
        }
    }
}
