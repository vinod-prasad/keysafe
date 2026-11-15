package com.appnest.safe.credentials.vault.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appnest.safe.credentials.vault.R
import com.appnest.safe.credentials.vault.domain.backup.BackupPayload
import com.appnest.safe.credentials.vault.domain.backup.VaultBackupManager
import com.appnest.safe.credentials.vault.domain.backup.toBackupDto
import com.appnest.safe.credentials.vault.domain.backup.toDomainCredential
import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.repository.CredentialRepository
import com.appnest.safe.credentials.vault.utils.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CredentialSelectable(
    val credential: Credential,
    val isSelected: Boolean = true
)

data class BackupUiState(
    val exportCredentialsList: List<CredentialSelectable> = emptyList(),
    val exportMode: String = "ALL", // "ALL" or "SELECTED"
    val importStrategy: String = "MERGE", // "MERGE" or "OVERRIDE"
    val isLoading: Boolean = false,
    val statusMessage: UiText? = null,
    val isSuccess: Boolean = false
)

class BackupViewModel(
    private val credentialRepository: CredentialRepository,
    private val backupManager: VaultBackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        loadCredentialsForExport("")
    }

    fun loadCredentialsForExport(masterKey: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = null)
            try {
                credentialRepository.getAllCredentials().collect { rawList ->
                    val decryptedList = rawList.map { raw ->
                        if (masterKey.isNotBlank()) {
                            try {
                                credentialRepository.decryptCredential(raw, masterKey)
                            } catch (e: Exception) {
                                raw
                            }
                        } else {
                            raw
                        }
                    }
                    val currentSelectedMap = _uiState.value.exportCredentialsList.associate { it.credential.id to it.isSelected }
                    val isAllMode = _uiState.value.exportMode == "ALL"

                    _uiState.value = _uiState.value.copy(
                        exportCredentialsList = decryptedList.map { cred ->
                            val isSelected = if (isAllMode) true else (currentSelectedMap[cred.id] ?: true)
                            CredentialSelectable(cred, isSelected = isSelected)
                        },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = e.message?.let { UiText.DynamicString(it) }
                )
            }
        }
    }

    fun setExportMode(mode: String) {
        val shouldSelectAll = (mode == "ALL")
        val updated = _uiState.value.exportCredentialsList.map {
            if (shouldSelectAll) it.copy(isSelected = true) else it
        }
        _uiState.value = _uiState.value.copy(exportMode = mode, exportCredentialsList = updated)
    }

    fun setImportStrategy(strategy: String) {
        _uiState.value = _uiState.value.copy(importStrategy = strategy)
    }

    fun toggleCredentialSelection(id: String) {
        val updated = _uiState.value.exportCredentialsList.map {
            if (it.credential.id == id) it.copy(isSelected = !it.isSelected) else it
        }
        _uiState.value = _uiState.value.copy(exportCredentialsList = updated)
    }

    fun toggleSelectAll(selectAll: Boolean) {
        val updated = _uiState.value.exportCredentialsList.map {
            it.copy(isSelected = selectAll)
        }
        _uiState.value = _uiState.value.copy(exportCredentialsList = updated)
    }

    suspend fun generateBackupFileBytes(backupPassword: String): ByteArray? = withContext(Dispatchers.Default) {
        if (backupPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(statusMessage = UiText.StringResource(R.string.error_encryption_key_empty))
            return@withContext null
        }

        val state = _uiState.value
        val itemsToExport = if (state.exportMode == "ALL") {
            state.exportCredentialsList.map { it.credential }
        } else {
            state.exportCredentialsList.filter { it.isSelected }.map { it.credential }
        }

        if (itemsToExport.isEmpty()) {
            _uiState.value = _uiState.value.copy(statusMessage = UiText.StringResource(R.string.error_title_empty))
            return@withContext null
        }

        try {
            val payload = BackupPayload(
                credentials = itemsToExport.map { it.toBackupDto() }
            )
            val fileBytes = backupManager.createBackupFileBytes(payload, backupPassword)
            _uiState.value = _uiState.value.copy(
                statusMessage = UiText.StringResource(R.string.backup_export_success),
                isSuccess = true
            )
            fileBytes
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                statusMessage = e.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.error_unknown_save)
            )
            null
        }
    }

    fun importBackupFileBytes(
        fileBytes: ByteArray,
        backupPassword: String,
        masterKey: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            if (backupPassword.isBlank() || masterKey.isBlank()) {
                _uiState.value = _uiState.value.copy(statusMessage = UiText.StringResource(R.string.error_encryption_key_empty))
                onComplete(false)
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = null)
            try {
                val payload = withContext(Dispatchers.Default) {
                    backupManager.readBackupFileBytes(fileBytes, backupPassword)
                }

                if (payload.credentials.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        statusMessage = UiText.StringResource(R.string.no_credentials_found)
                    )
                    onComplete(false)
                    return@launch
                }

                val domainCredentials = payload.credentials.map { it.toDomainCredential() }

                if (_uiState.value.importStrategy == "OVERRIDE") {
                    credentialRepository.deleteAllCredentials()
                }

                domainCredentials.forEach { cred ->
                    credentialRepository.saveCredential(cred, masterKey)
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = UiText.StringResource(R.string.backup_import_success),
                    isSuccess = true
                )
                onComplete(true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = e.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.error_decryption_failed)
                )
                onComplete(false)
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null, isSuccess = false)
    }
}
