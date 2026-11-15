package com.appnest.safe.credentials.vault.ui.screens.add

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.appnest.safe.credentials.vault.ui.base.templates.ComputerTemplateForm
import com.appnest.safe.credentials.vault.ui.viewmodel.add.AddComputerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddComputerScreen(
    credentialId: String?,
    key: String?,
    onNavigateBack: () -> Unit,
    viewModel: AddComputerViewModel = koinViewModel()
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(credentialId, key) {
        viewModel.loadCredentialForEditing(credentialId, key)
    }

    BaseAddCredentialScreen(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack
    ) { uiState ->
        ComputerTemplateForm(
            computerName = uiState.site,
            osType = uiState.osType,
            username = uiState.username,
            password = uiState.password,
            isPasswordVisible = uiState.isPasswordVisible,
            computerNameError = uiState.siteError,
            usernameError = uiState.usernameError,
            passwordError = uiState.passwordError,
            onComputerNameChanged = { viewModel.onSiteChanged(it) },
            onOsTypeChanged = { viewModel.onOsTypeChanged(it) },
            onUsernameChanged = { viewModel.onUsernameChanged(it) },
            onPasswordChanged = { viewModel.onPasswordChanged(it) },
            onTogglePasswordVisibility = { viewModel.togglePasswordVisibility() },
            onCopy = { text, label ->
                clipboardManager.setText(AnnotatedString(text))
                Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        SharedFormElements(
            uiState = uiState,
            viewModel = viewModel,
            onCopy = { text, label ->
                clipboardManager.setText(AnnotatedString(text))
                Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun AddComputerScreenPreview() {
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
        AddComputerScreen(
            credentialId = null,
            key = null,
            onNavigateBack = {},
            viewModel = androidx.compose.runtime.remember { AddComputerViewModel(saveUseCase, getUseCase, decryptUseCase) }
        )
    }
}
