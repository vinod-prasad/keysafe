package com.appnest.safe.credentials.vault.ui.screens.add

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.appnest.safe.credentials.vault.R
import com.appnest.safe.credentials.vault.ui.base.dialogs.EncryptionKeyPromptDialog
import com.appnest.safe.credentials.vault.ui.viewmodel.add.AddCredentialUiState
import com.appnest.safe.credentials.vault.ui.viewmodel.add.BaseAddCredentialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseAddCredentialScreen(
    viewModel: BaseAddCredentialViewModel,
    onNavigateBack: () -> Unit,
    content: @Composable (AddCredentialUiState) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showSaveDialog by remember { mutableStateOf(false) }

    val pageTitle = if (uiState.isEditMode) stringResource(R.string.title_edit_credential) else stringResource(R.string.title_add_credential)

    if (showSaveDialog) {
        EncryptionKeyPromptDialog(
            title = stringResource(R.string.dialog_encrypt_save_title),
            subtitle = stringResource(R.string.dialog_encrypt_save_subtitle),
            confirmButtonText = stringResource(R.string.action_save),
            onConfirm = { key ->
                showSaveDialog = false
                viewModel.saveCredential(key)
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pageTitle, fontWeight = FontWeight.Normal) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (uiState.isFavorite) "Unmark as Favorite" else "Mark as Favorite",
                            tint = if (uiState.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        TextButton(onClick = {
                            if (viewModel.validateInputs()) {
                                showSaveDialog = true
                            }
                        }) {
                            Text(stringResource(R.string.action_save), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!.asString(),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            content(uiState)
        }
    }
}
