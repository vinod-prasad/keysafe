package com.appnest.safe.credentials.vault.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.appnest.safe.credentials.vault.ui.base.dialogs.KeyInfoDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    var masterKeyError by remember { mutableStateOf(false) }
    var backupPasswordError by remember { mutableStateOf(false) }
    var backupPasswordVisible by remember { mutableStateOf(false) }
    var masterKeyVisible by remember { mutableStateOf(false) }
    var showMasterKeyDialog by remember { mutableStateOf(false) }
    var showOverrideConfirmDialog by remember { mutableStateOf(false) }
    var showKeyInfoDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    if (showKeyInfoDialog) {
        KeyInfoDialog(onDismiss = { showKeyInfoDialog = false })
    }

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
                        backupPassword = ""
                        masterKey = ""
                        backupPasswordVisible = false
                        masterKeyVisible = false
                        viewModel.resetBackupState()
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
            val fileDetails = uri.getFileDetails(context)
            val fileName = fileDetails.first.lowercase()
            if (fileName.endsWith(".ksvault")) {
                pendingImportUri = uri
            } else {
                pendingImportUri = null
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.error_invalid_file_type),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCredentialsForExport("")
    }

    // Toast status notifications & reset on success
    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            Toast.makeText(context, msg.asString(context), Toast.LENGTH_LONG).show()
            viewModel.clearStatusMessage()
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            backupPassword = ""
            masterKey = ""
            backupPasswordVisible = false
            masterKeyVisible = false
            pendingImportUri = null
            viewModel.resetBackupState()
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

            Spacer(modifier = Modifier.height(8.dp))

            if (activeTab == 0) {
                // EXPORT TAB UI
                ExportTabContent(
                    uiState = uiState,
                    masterKey = masterKey,
                    backupPassword = backupPassword,
                    masterKeyVisible = masterKeyVisible,
                    backupPasswordVisible = backupPasswordVisible,
                    isMasterKeyError = masterKeyError,
                    isBackupPasswordError = backupPasswordError,
                    onShowKeyInfo = { showKeyInfoDialog = true },
                    onMasterKeyChanged = {
                        masterKey = it
                        masterKeyError = false
                        viewModel.loadCredentialsForExport(it)
                    },
                    onBackupPasswordChanged = {
                        backupPassword = it
                        backupPasswordError = false
                    },
                    onToggleMasterKeyVisibility = { masterKeyVisible = !masterKeyVisible },
                    onTogglePasswordVisibility = { backupPasswordVisible = !backupPasswordVisible },
                    onSetExportMode = { viewModel.setExportMode(it) },
                    onToggleSelectAll = { viewModel.toggleSelectAll(it) },
                    onToggleCredentialSelection = { viewModel.toggleCredentialSelection(it) },
                    onSaveFile = {
                        if (masterKey.length < 4 || backupPassword.length < 4) {
                            if (masterKey.length < 4) masterKeyError = true
                            if (backupPassword.length < 4) backupPasswordError = true
                            return@ExportTabContent
                        }
                        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
                        val fileName = "keysafe_backup_${timestamp}.ksvault"
                        createDocumentLauncher.launch(fileName)
                    },
                    onShareFile = {
                        if (masterKey.length < 4 || backupPassword.length < 4) {
                            if (masterKey.length < 4) masterKeyError = true
                            if (backupPassword.length < 4) backupPasswordError = true
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
                                    context.startActivity(Intent.createChooser(shareIntent, context.resources.getString(R.string.backup_share)))
                                    backupPassword = ""
                                    masterKey = ""
                                    masterKeyError = false
                                    backupPasswordError = false
                                    backupPasswordVisible = false
                                    masterKeyVisible = false
                                    viewModel.resetBackupState()
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
                    isMasterKeyError = masterKeyError,
                    isBackupPasswordError = backupPasswordError,
                    onShowKeyInfo = { showKeyInfoDialog = true },
                    onBackupPasswordChanged = {
                        backupPassword = it
                        backupPasswordError = false
                    },
                    onMasterKeyChanged = {
                        masterKey = it
                        masterKeyError = false
                    },
                    onToggleBackupPasswordVisibility = { backupPasswordVisible = !backupPasswordVisible },
                    onToggleMasterKeyVisibility = { masterKeyVisible = !masterKeyVisible },
                    onSetImportStrategy = { viewModel.setImportStrategy(it) },
                    onSelectFile = { openDocumentLauncher.launch(arrayOf("*/*")) },
                    onImportAction = {
                        if (backupPassword.length < 4 || masterKey.length < 4) {
                            if (backupPassword.length < 4) backupPasswordError = true
                            if (masterKey.length < 4) masterKeyError = true
                            return@ImportTabContent
                        }
                        if (uiState.importStrategy == "OVERRIDE") {
                            showOverrideConfirmDialog = true
                        } else {
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
                    }
                )
            }
        }
    }
}

@Composable
fun ExportTabContent(
    uiState: BackupUiState,
    masterKey: String,
    backupPassword: String,
    masterKeyVisible: Boolean,
    backupPasswordVisible: Boolean,
    isMasterKeyError: Boolean = false,
    isBackupPasswordError: Boolean = false,
    onShowKeyInfo: () -> Unit = {},
    onMasterKeyChanged: (String) -> Unit,
    onBackupPasswordChanged: (String) -> Unit,
    onToggleMasterKeyVisibility: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSetExportMode: (String) -> Unit,
    onToggleSelectAll: (Boolean) -> Unit,
    onToggleCredentialSelection: (String) -> Unit,
    onSaveFile: () -> Unit,
    onShareFile: () -> Unit
) {
    val isSpecificMode = uiState.exportMode == "SELECTED"

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Text(stringResource(R.string.backup_export_mode_title), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSetExportMode("ALL") }
                        .padding(vertical = 2.dp)
                ) {
                    RadioButton(
                        selected = uiState.exportMode == "ALL",
                        onClick = { onSetExportMode("ALL") }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.backup_export_all), fontSize = 13.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSetExportMode("SELECTED") }
                        .padding(vertical = 2.dp)
                ) {
                    RadioButton(
                        selected = uiState.exportMode == "SELECTED",
                        onClick = { onSetExportMode("SELECTED") }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.backup_export_selected), fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 1. Vault Master Key to read local DB
            OutlinedTextField(
                value = masterKey,
                onValueChange = onMasterKeyChanged,
                label = { Text(stringResource(R.string.backup_master_key_label)) },
                placeholder = { Text(stringResource(R.string.backup_master_key_export_hint)) },
                visualTransformation = if (masterKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onShowKeyInfo) {
                            Icon(Icons.Default.Info, contentDescription = "Key Info", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onToggleMasterKeyVisibility) {
                            Icon(if (masterKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                        }
                    }
                },
                isError = isMasterKeyError,
                supportingText = if (isMasterKeyError) {
                    { Text(if (masterKey.isEmpty()) stringResource(R.string.error_encryption_key_empty) else stringResource(R.string.error_key_min_length)) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Backup Encryption Password to encrypt .ksvault file
            OutlinedTextField(
                value = backupPassword,
                onValueChange = onBackupPasswordChanged,
                label = { Text(stringResource(R.string.backup_password_label)) },
                placeholder = { Text(stringResource(R.string.backup_password_hint)) },
                visualTransformation = if (backupPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onShowKeyInfo) {
                            Icon(Icons.Default.Info, contentDescription = "Key Info", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onTogglePasswordVisibility) {
                            Icon(if (backupPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                        }
                    }
                },
                isError = isBackupPasswordError,
                supportingText = if (isBackupPasswordError) {
                    { Text(if (backupPassword.isEmpty()) stringResource(R.string.backup_password_hint) else stringResource(R.string.error_backup_password_min_length)) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val selectedCount = if (isSpecificMode) uiState.exportCredentialsList.count { it.isSelected } else uiState.exportCredentialsList.size
                Text(
                    text = "Items ($selectedCount selected)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSpecificMode) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Row {
                    TextButton(
                        onClick = { onToggleSelectAll(true) },
                        enabled = isSpecificMode
                    ) {
                        Text(stringResource(R.string.backup_select_all), fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = { onToggleSelectAll(false) },
                        enabled = isSpecificMode
                    ) {
                        Text(stringResource(R.string.backup_deselect_all), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (uiState.exportCredentialsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_credentials_found),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    uiState.exportCredentialsList.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isSpecificMode) { onToggleCredentialSelection(item.credential.id) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = if (isSpecificMode) item.isSelected else true,
                                onCheckedChange = { onToggleCredentialSelection(item.credential.id) },
                                enabled = isSpecificMode
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

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onSaveFile,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.backup_save_device), fontSize = 12.sp, maxLines = 1)
                }

                Button(
                    onClick = onShareFile,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.backup_share), color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 12.sp, maxLines = 1)
                }
            }

            // Bottom spacer for comfortable scrolling
            Spacer(modifier = Modifier.height(16.dp))
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
    isMasterKeyError: Boolean = false,
    isBackupPasswordError: Boolean = false,
    onShowKeyInfo: () -> Unit = {},
    onBackupPasswordChanged: (String) -> Unit,
    onMasterKeyChanged: (String) -> Unit,
    onToggleBackupPasswordVisibility: () -> Unit,
    onToggleMasterKeyVisibility: () -> Unit,
    onSetImportStrategy: (String) -> Unit,
    onSelectFile: () -> Unit,
    onImportAction: () -> Unit
) {
    val context = LocalContext.current
    val fileDetails = remember(selectedUri) {
        selectedUri?.getFileDetails(context)
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Button(
                onClick = onSelectFile,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedUri != null) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (selectedUri != null) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = if (selectedUri != null) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (fileDetails != null) {
                        "${fileDetails.first}${if (fileDetails.second.isNotBlank()) " (${fileDetails.second})" else ""}"
                    } else {
                        stringResource(R.string.backup_select_file)
                    },
                    color = if (selectedUri != null) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (selectedUri == null) {
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Select a .ksvault backup file to begin restoration",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                Text(stringResource(R.string.backup_import_strategy_title), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = uiState.importStrategy == "MERGE",
                            onClick = { onSetImportStrategy("MERGE") }
                        )
                        Column {
                            Text(stringResource(R.string.backup_import_merge), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.backup_import_merge_desc), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = uiState.importStrategy == "OVERRIDE",
                            onClick = { onSetImportStrategy("OVERRIDE") }
                        )
                        Column {
                            Text(stringResource(R.string.backup_import_override), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                            Text(stringResource(R.string.backup_import_override_desc), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Backup Decryption Password
                OutlinedTextField(
                    value = backupPassword,
                    onValueChange = onBackupPasswordChanged,
                    label = { Text(stringResource(R.string.backup_password_label)) },
                    placeholder = { Text(stringResource(R.string.backup_password_hint)) },
                    visualTransformation = if (backupPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onShowKeyInfo) {
                                Icon(Icons.Default.Info, contentDescription = "Key Info", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = onToggleBackupPasswordVisibility) {
                                Icon(if (backupPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                            }
                        }
                    },
                    isError = isBackupPasswordError,
                    supportingText = if (isBackupPasswordError) {
                        { Text(if (backupPassword.isEmpty()) stringResource(R.string.backup_password_hint) else stringResource(R.string.error_backup_password_min_length)) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Master Encryption Key to encrypt imported items into DB
                OutlinedTextField(
                    value = masterKey,
                    onValueChange = onMasterKeyChanged,
                    label = { Text(stringResource(R.string.backup_master_key_label)) },
                    placeholder = { Text(stringResource(R.string.backup_master_key_import_hint)) },
                    visualTransformation = if (masterKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onShowKeyInfo) {
                                Icon(Icons.Default.Info, contentDescription = "Key Info", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = onToggleMasterKeyVisibility) {
                                Icon(if (masterKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                            }
                        }
                    },
                    isError = isMasterKeyError,
                    supportingText = if (isMasterKeyError) {
                        { Text(if (masterKey.isEmpty()) stringResource(R.string.error_encryption_key_empty) else stringResource(R.string.error_key_min_length)) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onImportAction,
                    enabled = selectedUri != null && backupPassword.isNotBlank() && masterKey.isNotBlank() && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        @OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
                        androidx.compose.material3.CircularWavyProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(R.string.backup_import_action))
                    }
                }

                // Bottom spacer for comfortable scrolling
                Spacer(modifier = Modifier.height(16.dp))
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

private fun Uri.getFileDetails(context: Context): Pair<String, String> {
    var fileName = this.lastPathSegment ?: "backup.ksvault"
    if (fileName.contains("/")) fileName = fileName.substringAfterLast("/")
    if (fileName.contains(":")) fileName = fileName.substringAfterLast(":")

    var sizeBytes = 0L

    try {
        context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1 && !cursor.isNull(nameIndex)) {
                    val queriedName = cursor.getString(nameIndex)
                    if (!queriedName.isNullOrBlank()) {
                        fileName = queriedName
                    }
                }
                if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                    sizeBytes = cursor.getLong(sizeIndex)
                }
            }
        }
    } catch (e: Exception) {
        // Fallback
    }

    val formattedSize = when {
        sizeBytes <= 0L -> ""
        sizeBytes < 1024 -> "$sizeBytes B"
        sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
        else -> String.format(java.util.Locale.getDefault(), "%.1f MB", sizeBytes.toDouble() / (1024 * 1024))
    }

    return Pair(fileName, formattedSize)
}
