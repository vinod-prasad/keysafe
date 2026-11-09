package com.appnest.safe.credentials.vault.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appnest.safe.credentials.vault.domain.model.Credential
import com.appnest.safe.credentials.vault.domain.model.SecurityQuestion
import com.appnest.safe.credentials.vault.ui.base.dialogs.EncryptionKeyPromptDialog
import com.appnest.safe.credentials.vault.ui.theme.KeySafeTheme
import com.appnest.safe.credentials.vault.ui.viewmodel.CredentialDetailsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CredentialDetailsScreen(
    siteName: String = "accounts.google.com",
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: (String, String) -> Unit = { _, _ -> },
    viewModel: CredentialDetailsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(siteName) {
        viewModel.loadCredentialsForSite(siteName)
    }

    CredentialDetailsContent(
        siteName = uiState.siteName.ifEmpty { siteName },
        credentials = uiState.credentials,
        onNavigateBack = onNavigateBack,
        onToggleFavorite = { id, current -> viewModel.toggleFavorite(id, current) },
        onNavigateToEdit = onNavigateToEdit,
        onDecryptCredential = { cred, key, callback ->
            viewModel.decryptCredential(cred, key, callback)
        },
        onDeleteCredential = { id -> viewModel.deleteCredential(id) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialDetailsContent(
    siteName: String,
    credentials: List<Credential>,
    onNavigateBack: () -> Unit = {},
    onToggleFavorite: (String, Boolean) -> Unit = { _, _ -> },
    onNavigateToEdit: (String, String) -> Unit = { _, _ -> },
    onDecryptCredential: (Credential, String, (Credential?, String?) -> Unit) -> Unit = { _, _, _ -> },
    onDeleteCredential: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = siteName.take(1).uppercase(), color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = siteName, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (credentials.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No credentials found for $siteName", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(credentials) { credential ->
                        CredentialDetailItem(
                            credential = credential,
                            onToggleFavorite = { onToggleFavorite(credential.id, credential.isFavorite) },
                            onEdit = { key -> onNavigateToEdit(credential.id, key) },
                            onDecryptCredential = onDecryptCredential,
                            onDelete = { onDeleteCredential(credential.id) }
                        )
                        if (credential != credentials.last()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CredentialDetailItem(
    credential: Credential,
    onToggleFavorite: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    onDecryptCredential: (Credential, String, (Credential?, String?) -> Unit) -> Unit = { _, _, _ -> },
    onDelete: () -> Unit = {}
) {
    var decryptedCredential by remember(credential) { mutableStateOf<Credential?>(null) }
    var showKeyDialog by remember { mutableStateOf(false) }
    var keyDialogAction by remember { mutableStateOf<KeyAction>(KeyAction.VIEW) }

    var passwordVisible by remember { mutableStateOf(false) }
    var pinVisible by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    fun copyToClipboard(text: String, label: String) {
        if (text.isNotBlank()) {
            clipboardManager.setText(AnnotatedString(text))
            Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    if (showKeyDialog) {
        EncryptionKeyPromptDialog(
            title = "Enter Encryption Key",
            subtitle = "Enter your secret key/PIN for this credential. Entered key is never stored.",
            confirmButtonText = if (keyDialogAction == KeyAction.EDIT) "Unlock to Edit" else "Decrypt",
            onConfirm = { enteredKey ->
                showKeyDialog = false
                onDecryptCredential(credential, enteredKey) { dec, err ->
                    if (dec != null) {
                        decryptedCredential = dec
                        if (keyDialogAction == KeyAction.EDIT) {
                            onEdit(enteredKey)
                        }
                    } else {
                        Toast.makeText(context, err ?: "Invalid Encryption Key", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { showKeyDialog = false }
        )
    }

    val activeCredential = decryptedCredential ?: credential

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Top Item Header Row (Site name + Favorite Star)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = activeCredential.siteOrApp, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = if (activeCredential.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (activeCredential.isFavorite) "Unmark favorite" else "Mark favorite",
                    tint = if (activeCredential.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Username Box
        Surface(
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (decryptedCredential != null) activeCredential.username else "Encrypted Username",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = {
                        if (decryptedCredential != null) {
                            copyToClipboard(activeCredential.username, "Username")
                        } else {
                            keyDialogAction = KeyAction.VIEW
                            showKeyDialog = true
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (decryptedCredential != null) Icons.Default.ContentCopy else Icons.Default.Lock,
                        contentDescription = "Copy username",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Password Box
        Surface(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when {
                        decryptedCredential == null -> "•••••••••••• (Encrypted)"
                        passwordVisible -> activeCredential.password
                        else -> "••••••••••••"
                    },
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (decryptedCredential != null) {
                                passwordVisible = !passwordVisible
                            } else {
                                keyDialogAction = KeyAction.VIEW
                                showKeyDialog = true
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        val icon = if (decryptedCredential == null) Icons.Default.Lock else if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        Icon(icon, contentDescription = "Toggle password visibility", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (decryptedCredential != null) {
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(
                            onClick = { copyToClipboard(activeCredential.password, "Password") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy password", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // PIN Box (Optional)
        if (!activeCredential.pin.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when {
                            decryptedCredential == null -> "PIN: Encrypted"
                            pinVisible -> "PIN: ${activeCredential.pin}"
                            else -> "PIN: ••••"
                        },
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (decryptedCredential != null) {
                                    pinVisible = !pinVisible
                                } else {
                                    keyDialogAction = KeyAction.VIEW
                                    showKeyDialog = true
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            val icon = if (decryptedCredential == null) Icons.Default.Lock else if (pinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                            Icon(icon, contentDescription = "Toggle PIN visibility", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (decryptedCredential != null) {
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(
                                onClick = { copyToClipboard(activeCredential.pin ?: "", "PIN") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy PIN", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Security Questions Section (Optional)
        if (activeCredential.securityQuestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Security Questions",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            activeCredential.securityQuestions.forEachIndexed { index, sq ->
                var answerVisible by remember { mutableStateOf(false) }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Q${index + 1}: ${sq.question}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (decryptedCredential != null) {
                                        copyToClipboard(sq.question, "Question")
                                    } else {
                                        keyDialogAction = KeyAction.VIEW
                                        showKeyDialog = true
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (decryptedCredential != null) Icons.Default.ContentCopy else Icons.Default.Lock,
                                    contentDescription = "Copy question",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when {
                                    decryptedCredential == null -> "A: Encrypted"
                                    answerVisible -> "A: ${sq.answer}"
                                    else -> "A: ••••••••••••"
                                },
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (decryptedCredential != null) {
                                            answerVisible = !answerVisible
                                        } else {
                                            keyDialogAction = KeyAction.VIEW
                                            showKeyDialog = true
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    val icon = if (decryptedCredential == null) Icons.Default.Lock else if (answerVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = "Toggle answer visibility",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (decryptedCredential != null) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    IconButton(
                                        onClick = { copyToClipboard(sq.answer, "Answer") },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy answer",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Note Box
        if (!activeCredential.note.isNullOrEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.CenterStart) {
                    Text(
                        text = if (decryptedCredential != null) activeCredential.note else "Encrypted Note",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Action Buttons (Share on left; Edit & Delete aligned together on right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { /* Share action */ }) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        keyDialogAction = KeyAction.EDIT
                        showKeyDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete", color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}

enum class KeyAction { VIEW, EDIT }

@Preview(showBackground = true)
@Composable
fun CredentialDetailsScreenPreview() {
    KeySafeTheme {
        CredentialDetailsContent(
            siteName = "accounts.google.com",
            credentials = listOf(
                Credential(
                    id = "1",
                    siteOrApp = "accounts.google.com",
                    username = "appnestcare@gmail.com",
                    password = "GoogleSecurePassword123!",
                    pin = "1234",
                    note = "Developer account",
                    securityQuestions = listOf(
                        SecurityQuestion("sq1", "What is your pet's name?", "Fluffy")
                    ),
                    isFavorite = true
                )
            )
        )
    }
}
