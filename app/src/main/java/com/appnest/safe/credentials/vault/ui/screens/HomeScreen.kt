package com.appnest.safe.credentials.vault.ui.screens

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appnest.safe.credentials.vault.R
import com.appnest.safe.credentials.vault.ui.base.dialogs.EncryptionKeyPromptDialog
import com.appnest.safe.credentials.vault.ui.theme.KeySafeTheme
import com.appnest.safe.credentials.vault.ui.viewmodel.HomeUiState
import com.appnest.safe.credentials.vault.ui.viewmodel.HomeViewModel
import com.appnest.safe.credentials.vault.ui.viewmodel.SiteGroupItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    navController: androidx.navigation.NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onSearchClick = { navController.navigate(com.appnest.safe.credentials.vault.navigation.Screen.Search.route) },
        onAddCredentialClick = { navController.navigate(com.appnest.safe.credentials.vault.navigation.Screen.AddCredential.route) },
        onCredentialItemClick = { siteItem ->
            navController.navigate(com.appnest.safe.credentials.vault.navigation.Screen.CredentialDetails.createRoute(siteItem.siteName))
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
    onAddCredentialClick: () -> Unit = {},
    onCredentialItemClick: (SiteGroupItem) -> Unit = {},
    onSeedMockData: (String) -> Unit = {}
) {
    val categories = listOf("All", "Site", "Mobile", "Laptop", "Network")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var showSeedKeyDialog by remember { mutableStateOf(false) }

    val displayedGroupedCredentials = remember(selectedCategory, uiState.rawCredentials) {
        val filteredRaw = if (selectedCategory == "All") {
            uiState.rawCredentials
        } else {
            uiState.rawCredentials.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
        filteredRaw.groupBy { it.siteOrApp }
            .map { (siteName, items) ->
                SiteGroupItem(
                    siteName = siteName,
                    siteLogo = items.firstOrNull()?.siteLogo,
                    accountCount = if (items.size > 1) items.size else 0,
                    representativeId = items.first().id
                )
            }
    }

    if (showSeedKeyDialog) {
        EncryptionKeyPromptDialog(
            title = "Encrypt & Load Initial Vault Data",
            subtitle = "Enter your secret Encryption Key / PIN to encrypt and load the initial sample credentials. This key will be used to encrypt all vault data and is never stored locally.",
            confirmButtonText = "Load & Encrypt",
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
        TopSearchSection(onSearchClick = onSearchClick)

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                CategoryChip(
                    text = category,
                    isSelected = selectedCategory == category,
                    onClick = { selectedCategory = category }
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
                    AddPasswordRow(onClick = onAddCredentialClick)
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp, end = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                }

                if (uiState.rawCredentials.isEmpty()) {
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
                                    text = "Load & Encrypt Mock Data",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Enter key to transform & populate initial credentials",
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
fun TopSearchSection(onSearchClick: () -> Unit) {
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
                Text(text = "Search", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
            }
        }

        // Profile Icon
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
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
            text = "Add password",
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                 Text(text = credential.siteName.take(1).uppercase(), color = MaterialTheme.colorScheme.primary)
            }
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
                text = "${credential.accountCount} accounts",
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
