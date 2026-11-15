package com.appnest.safe.credentials.vault.ui.screens.add

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.appnest.safe.credentials.vault.ui.base.dialogs.AppSelectorDialog
import com.appnest.safe.credentials.vault.ui.base.templates.SiteTemplateForm
import com.appnest.safe.credentials.vault.ui.viewmodel.add.AddSiteViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddSiteScreen(
    credentialId: String?,
    key: String?,
    onNavigateBack: () -> Unit,
    viewModel: AddSiteViewModel = koinViewModel()
) {
    var showAppSelector by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(credentialId, key) {
        viewModel.loadCredentialForEditing(credentialId, key)
    }

    if (showAppSelector) {
        AppSelectorDialog(
            onDismissRequest = { showAppSelector = false },
            onAppSelected = { appItem ->
                viewModel.onSiteChanged(appItem.name)
                showAppSelector = false
            }
        )
    }

    BaseAddCredentialScreen(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack
    ) { uiState ->
        SiteTemplateForm(
            site = uiState.site,
            username = uiState.username,
            password = uiState.password,
            isPasswordVisible = uiState.isPasswordVisible,
            siteError = uiState.siteError,
            usernameError = uiState.usernameError,
            passwordError = uiState.passwordError,
            onSiteChanged = { viewModel.onSiteChanged(it) },
            onSiteFocusLost = { viewModel.onSiteFocusLost() },
            onShowAppSelector = { showAppSelector = true },
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
private fun AddSiteScreenPreview() {
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
        AddSiteScreen(
            credentialId = null,
            key = null,
            onNavigateBack = {},
            viewModel = androidx.compose.runtime.remember { AddSiteViewModel(saveUseCase, getUseCase, decryptUseCase) }
        )
    }
}
