package com.appnest.safe.credentials.vault.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.appnest.safe.credentials.vault.R
import com.appnest.safe.credentials.vault.data.SettingsManager
import com.appnest.safe.credentials.vault.navigation.Navigator
import com.appnest.safe.credentials.vault.navigation.Screen
import com.appnest.safe.credentials.vault.ui.base.components.CategoryOrDomainLogo
import com.appnest.safe.credentials.vault.ui.base.dialogs.CategorySelectionDialog
import com.appnest.safe.credentials.vault.ui.base.dialogs.EncryptionKeyPromptDialog
import com.appnest.safe.credentials.vault.ui.base.dialogs.ProfileImageCropDialog
import com.appnest.safe.credentials.vault.ui.theme.KeySafeTheme
import com.appnest.safe.credentials.vault.ui.viewmodel.HomeUiState
import com.appnest.safe.credentials.vault.ui.viewmodel.HomeViewModel
import com.appnest.safe.credentials.vault.ui.viewmodel.SiteGroupItem
import com.appnest.safe.credentials.vault.ui.viewmodel.cleanDomainDisplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.io.File
import java.io.FileOutputStream

@Composable
fun HomeScreen(
    navigator: Navigator,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCategoryDialog by remember { mutableStateOf(false) }

    if (showCategoryDialog) {
        CategorySelectionDialog(
            onDismissRequest = { showCategoryDialog = false },
            onCategorySelected = { category ->
                showCategoryDialog = false
                navigator.navigate(Screen.AddCredential(category = category))
            }
        )
    }

    HomeScreenContent(
        uiState = uiState,
        onSearchClick = { navigator.navigate(Screen.Search) },
        onAddCredentialClick = { activeCategory -> 
            if (activeCategory == "All") {
                showCategoryDialog = true
            } else {
                navigator.navigate(Screen.AddCredential(category = activeCategory))
            }
        },
        onCredentialItemClick = { siteItem ->
            navigator.navigate(Screen.CredentialDetails(siteItem.siteName))
        },
        onSeedMockData = { key ->
            viewModel.seedInitialMockData(key)
        }
    )
}


@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onSearchClick: () -> Unit = {},
    onAddCredentialClick: (String) -> Unit = {},
    onCredentialItemClick: (SiteGroupItem) -> Unit = {},
    onSeedMockData: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsManager: SettingsManager = koinInject()
    val profileImagePath by settingsManager.profileImagePathFlow.collectAsState(initial = null)
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    selectedImageUri?.let { uri ->
        ProfileImageCropDialog(
            imageUri = uri,
            onConfirm = { croppedBitmap ->
                selectedImageUri = null
                scope.launch(Dispatchers.IO) {
                    try {
                        val avatarFile = File(context.filesDir, "profile_avatar.png")
                        FileOutputStream(avatarFile).use { out ->
                            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                        }
                        settingsManager.setProfileImagePath(avatarFile.absolutePath)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            onDismiss = { selectedImageUri = null }
        )
    }

    val categoryKeys = listOf("All", "Site", "Card", "Computer")
    var selectedCategoryKey by remember { mutableStateOf(categoryKeys[0]) }
    var showSeedKeyDialog by remember { mutableStateOf(false) }

    val displayedGroupedCredentials = remember(selectedCategoryKey, uiState.rawCredentials) {
        val filteredRaw = if (selectedCategoryKey == "All") {
            uiState.rawCredentials
        } else {
            uiState.rawCredentials.filter { it.category.equals(selectedCategoryKey, ignoreCase = true) }
        }
        filteredRaw.groupBy { cred ->
            val rawName = when (cred.category.lowercase()) {
                "card" -> {
                    val issuer = cred.cardIssuer.takeIf { !it.isNullOrBlank() } ?: "Bank"
                    val type = cred.cardType.takeIf { !it.isNullOrBlank() } ?: "Credit"
                    "$issuer $type"
                }
                "computer" -> cred.osType.takeIf { !it.isNullOrBlank() } ?: "Computer"
                else -> cred.siteOrApp
            }
            rawName.cleanDomainDisplay()
        }.map { (groupName, items) ->
            SiteGroupItem(
                siteName = groupName,
                siteLogo = items.firstOrNull()?.siteLogo,
                category = items.firstOrNull()?.category ?: "Site",
                accountCount = if (items.size > 1) items.size else 0,
                representativeId = items.first().id
            )
        }
    }

    if (showSeedKeyDialog) {
        EncryptionKeyPromptDialog(
            title = stringResource(R.string.seed_mock_data_title),
            subtitle = stringResource(R.string.seed_mock_data_subtitle),
            confirmButtonText = stringResource(R.string.seed_mock_data_button),
            onConfirm = { key ->
                showSeedKeyDialog = false
                onSeedMockData(key)
            },
            onDismiss = { showSeedKeyDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar Area (Search & Profile)
        TopSearchSection(
            profileImagePath = profileImagePath,
            onSearchClick = onSearchClick,
            onProfileClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categoryKeys) { key ->
                val label = when (key) {
                    "Site" -> stringResource(R.string.category_site)
                    "Card" -> stringResource(R.string.category_card)
                    "Computer" -> stringResource(R.string.category_computer)
                    else -> stringResource(R.string.category_all)
                }
                CategoryChip(
                    text = label,
                    isSelected = selectedCategoryKey == key,
                    onClick = { selectedCategoryKey = key }
                )
            }
        }

        // Credentials List
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    AddPasswordRow(onClick = { onAddCredentialClick(selectedCategoryKey) })
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp, end = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                }

                if (uiState.rawCredentials.isEmpty() && com.appnest.safe.credentials.vault.BuildConfig.DEBUG) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSeedKeyDialog = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Load Mock Data", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.seed_mock_data_row_title),
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = stringResource(R.string.seed_mock_data_row_subtitle),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp, end = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }

                items(displayedGroupedCredentials) { credential ->
                    CredentialListItem(credential) {
                        onCredentialItemClick(credential)
                    }
                    if (credential != displayedGroupedCredentials.last()) {
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp, end = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSearchSection(
    profileImagePath: String? = null,
    onSearchClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.keysafe),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        }

        // Search Bar
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .padding(horizontal = 12.dp)
                .clickable(onClick = onSearchClick)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = stringResource(R.string.action_search), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
            }
        }

        // Profile Avatar
        val profileFile = remember(profileImagePath) {
            profileImagePath?.let { File(it) }?.takeIf { it.exists() }
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            if (profileFile != null) {
                AsyncImage(
                    model = profileFile,
                    contentDescription = "Profile Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun CategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.height(36.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        onClick = onClick
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
                text = text,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AddPasswordRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.title_add_credential),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CredentialListItem(credential: SiteGroupItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            CategoryOrDomainLogo(
                category = credential.category,
                nameOrDomain = credential.siteName
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = credential.siteName,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }

        if (credential.accountCount > 0) {
            Text(
                text = stringResource(R.string.accounts_count, credential.accountCount),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    KeySafeTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                groupedCredentials = listOf(
                    SiteGroupItem("7stardigitalnetwork.com", accountCount = 0, representativeId = "1"),
                    SiteGroupItem("accounts.google.com", accountCount = 18, representativeId = "2"),
                    SiteGroupItem("adobe.com", accountCount = 3, representativeId = "3")
                )
            )
        )
    }
}
