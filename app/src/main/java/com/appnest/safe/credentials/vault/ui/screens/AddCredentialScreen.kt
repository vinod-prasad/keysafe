package com.appnest.safe.credentials.vault.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appnest.safe.credentials.vault.ui.base.components.CustomTextField
import com.appnest.safe.credentials.vault.ui.base.components.SecureTextField
import com.appnest.safe.credentials.vault.ui.base.dialogs.AppSelectorDialog
import com.appnest.safe.credentials.vault.ui.base.dialogs.EncryptionKeyPromptDialog
import com.appnest.safe.credentials.vault.ui.theme.KeySafeTheme
import com.appnest.safe.credentials.vault.ui.viewmodel.AddCredentialEvent
import com.appnest.safe.credentials.vault.ui.viewmodel.AddCredentialUiState
import com.appnest.safe.credentials.vault.ui.viewmodel.AddCredentialViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCredentialScreen(
    credentialId: String? = null,
    encryptionKey: String? = null,
    onNavigateBack: () -> Unit = {},
    viewModel: AddCredentialViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showUnlockDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showAppSelectorDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(credentialId, encryptionKey) {
        if (!credentialId.isNullOrEmpty()) {
            if (!encryptionKey.isNullOrEmpty()) {
                viewModel.loadCredentialForEditing(credentialId, encryptionKey)
            } else {
                showUnlockDialog = true
            }
        } else {
            viewModel.resetState()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AddCredentialEvent.SaveSuccess -> {
                    Toast.makeText(context, "Credential saved successfully", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                is AddCredentialEvent.SaveError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    if (showAppSelectorDialog) {
        AppSelectorDialog(
            onDismissRequest = { showAppSelectorDialog = false },
            onAppSelected = { selectedApp ->
                viewModel.onSiteChanged(selectedApp.name)
                viewModel.onSiteLogoChanged(selectedApp.packageName)
                showAppSelectorDialog = false
            }
        )
    }

    // Dialog for unlocking existing credential on load
    if (showUnlockDialog) {
        EncryptionKeyPromptDialog(
            title = "Unlock Credential",
            subtitle = "Enter your secret Encryption Key / PIN to decrypt this credential for editing.",
            confirmButtonText = "Unlock for Edit",
            onConfirm = { key ->
                showUnlockDialog = false
                viewModel.loadCredentialForEditing(credentialId, key)
            },
            onDismiss = {
                showUnlockDialog = false
                onNavigateBack()
            }
        )
    }

    // Dialog for saving credential (new or edit)
    if (showSaveDialog) {
        EncryptionKeyPromptDialog(
            title = if (uiState.isEditMode) "Save Changes" else "Save Credential",
            subtitle = "Enter your secret Encryption Key / PIN to encrypt values on-the-fly. This key is never stored locally.",
            confirmButtonText = "Save",
            onConfirm = { key ->
                showSaveDialog = false
                viewModel.saveCredential(key)
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    val domainRegex = Regex("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")

    AddCredentialContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onShowAppSelector = { showAppSelectorDialog = true },
        onSiteChanged = viewModel::onSiteChanged,
        onCategoryChanged = viewModel::onCategoryChanged,
        onToggleFavorite = viewModel::toggleFavorite,
        onUsernameChanged = viewModel::onUsernameChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onPinChanged = viewModel::onPinChanged,
        onNoteChanged = viewModel::onNoteChanged,
        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
        onTogglePinVisibility = viewModel::togglePinVisibility,
        onAddSecurityQuestion = viewModel::addSecurityQuestion,
        onRemoveSecurityQuestion = viewModel::removeSecurityQuestion,
        onUpdateSecurityQuestion = viewModel::updateSecurityQuestion,
        onUpdateSecurityAnswer = viewModel::updateSecurityAnswer,
        onToggleSecurityAnswerVisibility = viewModel::toggleSecurityAnswerVisibility,
        onSaveClicked = {
            if (uiState.site.isBlank()) {
                Toast.makeText(context, "Site or app name cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (uiState.category.equals("Site", ignoreCase = true) && !domainRegex.matches(uiState.site.trim())) {
                Toast.makeText(context, "Please enter a valid domain name (e.g. google.com, appnest.in)", Toast.LENGTH_SHORT).show()
            } else {
                showSaveDialog = true
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCredentialContent(
    uiState: AddCredentialUiState,
    onNavigateBack: () -> Unit = {},
    onShowAppSelector: () -> Unit = {},
    onSiteChanged: (String) -> Unit = {},
    onCategoryChanged: (String) -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onUsernameChanged: (String) -> Unit = {},
    onPasswordChanged: (String) -> Unit = {},
    onPinChanged: (String) -> Unit = {},
    onNoteChanged: (String) -> Unit = {},
    onTogglePasswordVisibility: () -> Unit = {},
    onTogglePinVisibility: () -> Unit = {},
    onAddSecurityQuestion: () -> Unit = {},
    onRemoveSecurityQuestion: (String) -> Unit = {},
    onUpdateSecurityQuestion: (String, String) -> Unit = { _, _ -> },
    onUpdateSecurityAnswer: (String, String) -> Unit = { _, _ -> },
    onToggleSecurityAnswerVisibility: (String) -> Unit = {},
    onSaveClicked: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    fun copyToClipboard(text: String, label: String) {
        if (text.isNotBlank()) {
            clipboardManager.setText(AnnotatedString(text))
            Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    val pageTitle = if (uiState.isEditMode) "Edit password" else "Add password"
    val availableCategories = listOf("Site", "Mobile", "Laptop", "Network")
    val isSiteCategory = uiState.category.equals("Site", ignoreCase = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pageTitle, fontWeight = FontWeight.Normal) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
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
                        TextButton(onClick = onSaveClicked) {
                            Text("Save", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Category Selection
            Text("Category", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableCategories) { category ->
                    val isSelected = uiState.category.equals(category, ignoreCase = true)
                    Surface(
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        onClick = { onCategoryChanged(category) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = category,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                if (isSiteCategory) "Site or app" else "Category Label",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(
                value = uiState.site,
                onValueChange = onSiteChanged,
                placeholder = if (isSiteCategory) "e.g. google.com or appnest.in" else uiState.category,
                enabled = isSiteCategory,
                onCopy = { copyToClipboard(uiState.site, "Site") }
            )

            if (isSiteCategory) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onShowAppSelector,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                    Text(" Select app", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Make sure that you're saving your current password", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(
                value = uiState.username,
                onValueChange = onUsernameChanged,
                placeholder = "Username",
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomEnd = 0.dp, bottomStart = 0.dp),
                onCopy = { copyToClipboard(uiState.username, "Username") }
            )

            // Password Field with Hide/Show & Copy options
            SecureTextField(
                value = uiState.password,
                onValueChange = onPasswordChanged,
                placeholder = "Password",
                isTextVisible = uiState.isPasswordVisible,
                onToggleVisibility = onTogglePasswordVisibility,
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomEnd = 8.dp, bottomStart = 8.dp),
                onCopy = { copyToClipboard(uiState.password, "Password") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { /* Suggest Password Logic */ },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Password, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                Text(" Suggest strong password", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }

            // PIN (Optional) with Hide/Show & Copy options
            Spacer(modifier = Modifier.height(24.dp))
            Text("PIN (Optional)", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            SecureTextField(
                value = uiState.pin,
                onValueChange = onPinChanged,
                placeholder = "PIN (e.g. 4-8 digits)",
                isTextVisible = uiState.isPinVisible,
                onToggleVisibility = onTogglePinVisibility,
                keyboardType = KeyboardType.NumberPassword,
                shape = RoundedCornerShape(8.dp),
                onCopy = { copyToClipboard(uiState.pin, "PIN") }
            )

            // Security Questions Section (Optional)
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Security Questions (Optional)",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onAddSecurityQuestion) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Question")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            uiState.securityQuestions.forEachIndexed { index, item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${index + 1}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { onRemoveSecurityQuestion(item.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove question",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = item.question,
                        onValueChange = { newQ -> onUpdateSecurityQuestion(item.id, newQ) },
                        placeholder = "Security Question (e.g. Mother's maiden name)",
                        onCopy = { copyToClipboard(item.question, "Question") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Answer Field with Hide/Show & Copy options
                    SecureTextField(
                        value = item.answer,
                        onValueChange = { newA -> onUpdateSecurityAnswer(item.id, newA) },
                        placeholder = "Answer",
                        isTextVisible = item.isAnswerVisible,
                        onToggleVisibility = { onToggleSecurityAnswerVisibility(item.id) },
                        shape = RoundedCornerShape(8.dp),
                        onCopy = { copyToClipboard(item.answer, "Answer") }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Note (Optional)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Note (Optional)", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            CustomTextField(
                value = uiState.note,
                onValueChange = onNoteChanged,
                placeholder = "Note",
                modifier = Modifier.height(100.dp),
                onCopy = { copyToClipboard(uiState.note, "Note") }
            )
        }
    }
}

@Preview
@Composable
fun AddCredentialScreenPreview() {
    KeySafeTheme {
        AddCredentialContent(
            uiState = AddCredentialUiState(
                site = "example.com",
                category = "Site",
                username = "user@example.com",
                password = "SecretPassword123",
                pin = "1234",
                isFavorite = true,
                securityQuestions = listOf(
                    com.appnest.safe.credentials.vault.ui.viewmodel.SecurityQuestionItemUi(question = "First pet?", answer = "Fluffy")
                )
            )
        )
    }
}
