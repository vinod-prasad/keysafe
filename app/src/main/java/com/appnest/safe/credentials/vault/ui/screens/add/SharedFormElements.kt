package com.appnest.safe.credentials.vault.ui.screens.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appnest.safe.credentials.vault.R
import com.appnest.safe.credentials.vault.ui.base.components.CustomTextField
import com.appnest.safe.credentials.vault.ui.base.components.SecureTextField
import com.appnest.safe.credentials.vault.ui.base.components.SecurityQuestionForm
import com.appnest.safe.credentials.vault.ui.viewmodel.add.AddCredentialUiState
import com.appnest.safe.credentials.vault.ui.viewmodel.add.BaseAddCredentialViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SharedFormElements(
    uiState: AddCredentialUiState,
    viewModel: BaseAddCredentialViewModel,
    onCopy: (String, String) -> Unit
) {
    var isPinExpanded by remember(uiState.pin) { mutableStateOf(uiState.pin.isNotBlank()) }
    var isNoteExpanded by remember(uiState.note) { mutableStateOf(uiState.note.isNotBlank()) }
    var isSecurityQuestionsExpanded by remember(uiState.securityQuestions) { mutableStateOf(uiState.securityQuestions.isNotEmpty()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Optional Item Security PIN
        if (isPinExpanded || uiState.pin.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.label_item_pin), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(onClick = {
                    isPinExpanded = false
                    viewModel.onPinChanged("")
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Remove PIN", modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            SecureTextField(
                value = uiState.pin,
                onValueChange = { viewModel.onPinChanged(it) },
                placeholder = stringResource(R.string.hint_item_pin),
                keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword,
                isTextVisible = uiState.isPinVisible,
                onToggleVisibility = { viewModel.togglePinVisibility() },
                onCopy = { onCopy(uiState.pin, "Item PIN") }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2. Optional Note Section
        if (isNoteExpanded || uiState.note.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.label_note_optional), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(onClick = {
                    isNoteExpanded = false
                    viewModel.onNoteChanged("")
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Remove Note", modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            CustomTextField(
                value = uiState.note,
                onValueChange = { viewModel.onNoteChanged(it) },
                placeholder = stringResource(R.string.label_note_optional),
                modifier = Modifier.height(100.dp),
                onCopy = { onCopy(uiState.note, "Note") }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. Optional Security Questions Section
        if (isSecurityQuestionsExpanded || uiState.securityQuestions.isNotEmpty()) {
            Text(stringResource(R.string.label_security_questions), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
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
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 4. "Add more info" Section with Pill Chips (Toggleable)
        var isAddMoreExpanded by remember { mutableStateOf(false) }

        val hasUnexpandedItems = (!isPinExpanded && uiState.pin.isBlank()) ||
                (!isNoteExpanded && uiState.note.isBlank()) ||
                (!isSecurityQuestionsExpanded && uiState.securityQuestions.isEmpty())

        if (hasUnexpandedItems) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { isAddMoreExpanded = !isAddMoreExpanded }
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.action_add_more_info),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isAddMoreExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle Add More Info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isAddMoreExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isPinExpanded && uiState.pin.isBlank()) {
                        AddMorePillChip(
                            icon = Icons.Default.Pin,
                            label = stringResource(R.string.label_item_pin),
                            onClick = {
                                isPinExpanded = true
                                isAddMoreExpanded = false
                            }
                        )
                    }

                    if (!isNoteExpanded && uiState.note.isBlank()) {
                        AddMorePillChip(
                            icon = Icons.Default.Description,
                            label = stringResource(R.string.label_note_optional),
                            onClick = {
                                isNoteExpanded = true
                                isAddMoreExpanded = false
                            }
                        )
                    }

                    if (!isSecurityQuestionsExpanded && uiState.securityQuestions.isEmpty()) {
                        AddMorePillChip(
                            icon = Icons.Default.Quiz,
                            label = stringResource(R.string.label_security_questions),
                            onClick = {
                                isSecurityQuestionsExpanded = true
                                isAddMoreExpanded = false
                                viewModel.addSecurityQuestion()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddMorePillChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.height(44.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
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
            override suspend fun deleteAllCredentials() {}
        }
        val saveUseCase = com.appnest.safe.credentials.vault.domain.usecase.SaveCredentialUseCase(dummyRepo)
        val getUseCase = com.appnest.safe.credentials.vault.domain.usecase.GetCredentialUseCase(dummyRepo)
        val decryptUseCase = com.appnest.safe.credentials.vault.domain.usecase.DecryptCredentialUseCase(dummyRepo)
        val dummyVm = androidx.compose.runtime.remember {
            com.appnest.safe.credentials.vault.ui.viewmodel.add.AddSiteViewModel(saveUseCase, getUseCase, decryptUseCase)
        }
        val uiState by dummyVm.uiState.collectAsState()

        Column(
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
