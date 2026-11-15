package com.appnest.safe.credentials.vault.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.appnest.safe.credentials.vault.R
import com.appnest.safe.credentials.vault.ui.base.components.CategoryOrDomainLogo
import com.appnest.safe.credentials.vault.ui.base.dialogs.EncryptionKeyPromptDialog
import com.appnest.safe.credentials.vault.ui.viewmodel.BackupUiState
import com.appnest.safe.credentials.vault.ui.viewmodel.BackupViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupPayloadScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: BackupViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(0) } // 0 = Export, 1 = Import
    var backupPassword by remember { mutableStateOf("") }
    var masterKey by remember { mutableStateOf("") }
    var backupPasswordVisible by remember { mutableStateOf(false) }
    var masterKeyVisible by remember { mutableStateOf(false) }
    var showMasterKeyDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher for Export (Save File)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null && backupPassword.isNotBlank()) {
            scope.launch {
                val bytes = viewModel.generateBackupFileBytes(backupPassword)
                if (bytes != null) {
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { os ->
                            os.write(bytes)
                        }
                        Toast.makeText(context, context.resources.getString(R.string.backup_export_success), Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message ?: "Failed to write file", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Launcher for Import (Open File)
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCredentialsForExport("")
    }

    // Toast status notifications
    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            Toast.makeText(context, msg.asString(context), Toast.LENGTH_LONG).show()
            viewModel.clearStatusMessage()
        }
    }

    if (showMasterKeyDialog) {
        EncryptionKeyPromptDialog(
            title = stringResource(R.string.dialog_encrypt_save_title),
            subtitle = stringResource(R.string.dialog_encrypt_save_subtitle),
            confirmButtonText = stringResource(R.string.action_confirm),
            onConfirm = { key ->
                masterKey = key
                showMasterKeyDialog = false
                viewModel.loadCredentialsForExport(key)
            },
            onDismiss = { showMasterKeyDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_backup_restore), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_close))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text(stringResource(R.string.tab_export)) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text(stringResource(R.string.tab_import)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeTab == 0) {
                // EXPORT TAB UI
                ExportTabContent(
                    uiState = uiState,
                    backupPassword = backupPassword,
                    backupPasswordVisible = backupPasswordVisible,
                    onBackupPasswordChanged = { backupPassword = it },
                    onTogglePasswordVisibility = { backupPasswordVisible = !backupPasswordVisible },
                    onSetExportMode = { viewModel.setExportMode(it) },
                    onToggleSelectAll = { viewModel.toggleSelectAll(it) },
                    onToggleCredentialSelection = { viewModel.toggleCredentialSelection(it) },
                    onSaveFile = {
                        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
                        val fileName = "keysafe_backup_${timestamp}.ksvault"
                        createDocumentLauncher.launch(fileName)
                    },
                    onShareFile = {
                        if (backupPassword.isBlank()) {
                            Toast.makeText(context, context.getString(R.string.error_encryption_key_empty), Toast.LENGTH_SHORT).show()
                            return@ExportTabContent
                        }
                        scope.launch {
                            val bytes = viewModel.generateBackupFileBytes(backupPassword)
                            if (bytes != null) {
                                try {
                                    val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
                                    val fileName = "keysafe_backup_${timestamp}.ksvault"
                                    val cacheFile = File(context.cacheDir, fileName)
                                    FileOutputStream(cacheFile).use { os -> os.write(bytes) }

                                    val contentUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        cacheFile
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/octet-stream"
                                        putExtra(Intent.EXTRA_STREAM, contentUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.backup_share)))
                                } catch (e: Exception) {
                                    Toast.makeText(context, e.message ?: "Failed to share file", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
            } else {
                // IMPORT TAB UI
                ImportTabContent(
                    uiState = uiState,
                    selectedUri = pendingImportUri,
                    backupPassword = backupPassword,
                    masterKey = masterKey,
                    backupPasswordVisible = backupPasswordVisible,
                    masterKeyVisible = masterKeyVisible,
                    onBackupPasswordChanged = { backupPassword = it },
                    onMasterKeyChanged = { masterKey = it },
                    onToggleBackupPasswordVisibility = { backupPasswordVisible = !backupPasswordVisible },
                    onToggleMasterKeyVisibility = { masterKeyVisible = !masterKeyVisible },
                    onSetImportStrategy = { viewModel.setImportStrategy(it) },
                    onSelectFile = { openDocumentLauncher.launch(arrayOf("*/*")) },
                    onImportAction = {
                        val uri = pendingImportUri
                        if (uri != null) {
                            try {
                                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                if (bytes != null) {
                                    viewModel.importBackupFileBytes(bytes, backupPassword, masterKey) { success ->
                                        if (success) {
                                            pendingImportUri = null
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, e.message ?: "Failed to read import file", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ExportTabContent(
    uiState: BackupUiState,
    backupPassword: String,
    backupPasswordVisible: Boolean,
    onBackupPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSetExportMode: (String) -> Unit,
    onToggleSelectAll: (Boolean) -> Unit,
    onToggleCredentialSelection: (String) -> Unit,
    onSaveFile: () -> Unit,
    onShareFile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.backup_export_mode_title), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = uiState.exportMode == "ALL",
                    onClick = { onSetExportMode("ALL") }
                )
                Text(stringResource(R.string.backup_export_all), fontSize = 14.sp)

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = uiState.exportMode == "SELECTED",
                    onClick = { onSetExportMode("SELECTED") }
                )
                Text(stringResource(R.string.backup_export_selected), fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Backup Encryption Password
            OutlinedTextField(
                value = backupPassword,
                onValueChange = onBackupPasswordChanged,
                label = { Text(stringResource(R.string.backup_password_label)) },
                placeholder = { Text(stringResource(R.string.backup_password_hint)) },
                visualTransformation = if (backupPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(if (backupPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val selectedCount = uiState.exportCredentialsList.count { it.isSelected }
                Text(
                    text = "Items ($selectedCount selected)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    TextButton(onClick = { onToggleSelectAll(true) }) {
                        Text(stringResource(R.string.backup_select_all), fontSize = 12.sp)
                    }
                    TextButton(onClick = { onToggleSelectAll(false) }) {
                        Text(stringResource(R.string.backup_deselect_all), fontSize = 12.sp)
                    }
                }
            }

            if (uiState.exportCredentialsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_credentials_found),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.exportCredentialsList) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleCredentialSelection(item.credential.id) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isSelected,
                                onCheckedChange = { onToggleCredentialSelection(item.credential.id) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CategoryOrDomainLogo(category = item.credential.category, nameOrDomain = item.credential.siteOrApp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(item.credential.siteOrApp, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                if (item.credential.username.isNotBlank()) {
                                    Text(item.credential.username, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onSaveFile,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.backup_save_device), fontSize = 13.sp)
                }

                Button(
                    onClick = onShareFile,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.backup_share), color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ImportTabContent(
    uiState: BackupUiState,
    selectedUri: Uri?,
    backupPassword: String,
    masterKey: String,
    backupPasswordVisible: Boolean,
    masterKeyVisible: Boolean,
    onBackupPasswordChanged: (String) -> Unit,
    onMasterKeyChanged: (String) -> Unit,
    onToggleBackupPasswordVisibility: () -> Unit,
    onToggleMasterKeyVisibility: () -> Unit,
    onSetImportStrategy: (String) -> Unit,
    onSelectFile: () -> Unit,
    onImportAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = onSelectFile,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedUri?.lastPathSegment ?: stringResource(R.string.backup_select_file),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.backup_import_strategy_title), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.importStrategy == "MERGE",
                        onClick = { onSetImportStrategy("MERGE") }
                    )
                    Column {
                        Text(stringResource(R.string.backup_import_merge), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.backup_import_merge_desc), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.importStrategy == "OVERRIDE",
                        onClick = { onSetImportStrategy("OVERRIDE") }
                    )
                    Column {
                        Text(stringResource(R.string.backup_import_override), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                        Text(stringResource(R.string.backup_import_override_desc), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Backup Decryption Password
            OutlinedTextField(
                value = backupPassword,
                onValueChange = onBackupPasswordChanged,
                label = { Text(stringResource(R.string.backup_password_label)) },
                placeholder = { Text(stringResource(R.string.backup_password_hint)) },
                visualTransformation = if (backupPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onToggleBackupPasswordVisibility) {
                        Icon(if (backupPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Master Encryption Key to encrypt imported items into DB
            OutlinedTextField(
                value = masterKey,
                onValueChange = onMasterKeyChanged,
                label = { Text(stringResource(R.string.backup_master_key_label)) },
                placeholder = { Text(stringResource(R.string.backup_master_key_hint)) },
                visualTransformation = if (masterKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onToggleMasterKeyVisibility) {
                        Icon(if (masterKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onImportAction,
                enabled = selectedUri != null && backupPassword.isNotBlank() && masterKey.isNotBlank() && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.backup_import_action))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BackupPayloadScreenPreview() {
    com.appnest.safe.credentials.vault.ui.theme.KeySafeTheme {
        BackupPayloadScreen()
    }
}
