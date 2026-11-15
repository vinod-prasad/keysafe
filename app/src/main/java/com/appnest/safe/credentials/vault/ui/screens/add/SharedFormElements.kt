package com.appnest.safe.credentials.vault.ui.screens.add

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.appnest.safe.credentials.vault.R
import com.appnest.safe.credentials.vault.ui.base.components.CustomTextField
import com.appnest.safe.credentials.vault.ui.base.components.SecureTextField
import com.appnest.safe.credentials.vault.ui.base.components.SecurityQuestionForm
import com.appnest.safe.credentials.vault.ui.viewmodel.add.AddCredentialUiState
import com.appnest.safe.credentials.vault.ui.viewmodel.add.BaseAddCredentialViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun SharedFormElements(
    uiState: AddCredentialUiState,
    viewModel: BaseAddCredentialViewModel,
    onCopy: (String, String) -> Unit
) {
    var isNoteExpanded by remember(uiState.note) { mutableStateOf(uiState.note.isNotBlank()) }
    var isSecurityQuestionsExpanded by remember(uiState.securityQuestions) { mutableStateOf(uiState.securityQuestions.isNotEmpty()) }

    SecureTextField(
        value = uiState.pin,
        onValueChange = { viewModel.onPinChanged(it) },
        placeholder = stringResource(R.string.label_vault_pin),
        keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword,
        isTextVisible = uiState.isPinVisible,
        onToggleVisibility = { viewModel.togglePinVisibility() },
        onCopy = { onCopy(uiState.pin, "PIN") }
    )
    Spacer(modifier = Modifier.height(16.dp))

    // Note Section (Collapsible / Optional)
    if (isNoteExpanded || uiState.note.isNotBlank()) {
        CustomTextField(
            value = uiState.note,
            onValueChange = { viewModel.onNoteChanged(it) },
            placeholder = stringResource(R.string.label_note_optional),
            modifier = Modifier.height(100.dp),
            onCopy = { onCopy(uiState.note, "Note") }
        )
        Spacer(modifier = Modifier.height(16.dp))
    } else {
        TextButton(
            onClick = { isNoteExpanded = true },
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add_note))
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.action_add_note))
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    // Security Questions Section (Collapsible / Optional)
    if (isSecurityQuestionsExpanded || uiState.securityQuestions.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.label_security_questions), modifier = Modifier.padding(bottom = 8.dp))
        uiState.securityQuestions.forEachIndexed { index, sq ->
            SecurityQuestionForm(
                index = index,
                question = sq.question,
                answer = sq.answer,
                isAnswerVisible = sq.isAnswerVisible,
                onQuestionChanged = { viewModel.updateSecurityQuestion(sq.id, it) },
                onAnswerChanged = { viewModel.updateSecurityAnswer(sq.id, it) },
                onToggleAnswerVisibility = { viewModel.toggleSecurityAnswerVisibility(sq.id) },
                onRemove = {
                    viewModel.removeSecurityQuestion(sq.id)
                    if (uiState.securityQuestions.size <= 1) {
                        isSecurityQuestionsExpanded = false
                    }
                },
                onCopyQuestion = { onCopy(sq.question, "Security Question") },
                onCopyAnswer = { onCopy(sq.answer, "Security Answer") }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        TextButton(onClick = { viewModel.addSecurityQuestion() }) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add_security_question))
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.action_add_security_question))
        }
    } else {
        TextButton(
            onClick = {
                isSecurityQuestionsExpanded = true
                viewModel.addSecurityQuestion()
            },
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add_security_question))
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.action_add_security_question))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun SharedFormElementsPreview() {
    com.appnest.safe.credentials.vault.ui.theme.KeySafeTheme {
        val dummyRepo = object : com.appnest.safe.credentials.vault.domain.repository.CredentialRepository {
            override suspend fun saveCredential(credential: com.appnest.safe.credentials.vault.domain.model.Credential, encryptionKey: String) {}
            override suspend fun getCredentialById(id: String) = null
            override fun getAllCredentials() = kotlinx.coroutines.flow.flowOf(emptyList<com.appnest.safe.credentials.vault.domain.model.Credential>())
            override fun getCredentialsBySite(siteOrApp: String) = kotlinx.coroutines.flow.flowOf(emptyList<com.appnest.safe.credentials.vault.domain.model.Credential>())
            override fun getFavoriteCredentials() = kotlinx.coroutines.flow.flowOf(emptyList<com.appnest.safe.credentials.vault.domain.model.Credential>())
            override suspend fun decryptCredential(credential: com.appnest.safe.credentials.vault.domain.model.Credential, key: String) = credential
            override suspend fun seedMockData(encryptionKey: String) {}
            override suspend fun toggleFavorite(id: String, isFavorite: Boolean) {}
            override suspend fun deleteCredential(id: String) {}
        }
        val saveUseCase = com.appnest.safe.credentials.vault.domain.usecase.SaveCredentialUseCase(dummyRepo)
        val getUseCase = com.appnest.safe.credentials.vault.domain.usecase.GetCredentialUseCase(dummyRepo)
        val decryptUseCase = com.appnest.safe.credentials.vault.domain.usecase.DecryptCredentialUseCase(dummyRepo)
        val dummyVm = androidx.compose.runtime.remember {
            com.appnest.safe.credentials.vault.ui.viewmodel.add.AddSiteViewModel(saveUseCase, getUseCase, decryptUseCase)
        }
        val uiState by dummyVm.uiState.collectAsState()

        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            SharedFormElements(
                uiState = uiState,
                viewModel = dummyVm,
                onCopy = { _, _ -> }
            )
        }
    }
}
