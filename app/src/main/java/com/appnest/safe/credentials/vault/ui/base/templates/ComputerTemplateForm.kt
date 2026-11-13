package com.appnest.safe.credentials.vault.ui.base.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appnest.safe.credentials.vault.ui.base.components.CustomTextField
import com.appnest.safe.credentials.vault.ui.base.components.SecureTextField

@Composable
fun ComputerTemplateForm(
    computerName: String,
    osType: String,
    username: String,
    password: String,
    isPasswordVisible: Boolean,
    onComputerNameChanged: (String) -> Unit,
    onOsTypeChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onCopy: (String, String) -> Unit
) {
    val availableOsList = listOf("Windows", "Mac", "Linux", "Android")

    Column {
        Text("Computer Name / Label", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField(
            value = computerName,
            onValueChange = onComputerNameChanged,
            placeholder = "e.g. Work MacBook Pro, Home Desktop",
            onCopy = { onCopy(computerName, "Computer Name") }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("Operating System (OS)", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableOsList) { os ->
                val isSelected = osType.equals(os, ignoreCase = true)
                Surface(
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    onClick = { onOsTypeChanged(os) }
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
                            text = os,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(
            value = username,
            onValueChange = onUsernameChanged,
            placeholder = "System Username / Admin",
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomEnd = 0.dp, bottomStart = 0.dp),
            onCopy = { onCopy(username, "Username") }
        )
        SecureTextField(
            value = password,
            onValueChange = onPasswordChanged,
            placeholder = "System Password",
            isTextVisible = isPasswordVisible,
            onToggleVisibility = onTogglePasswordVisibility,
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomEnd = 8.dp, bottomStart = 8.dp),
            onCopy = { onCopy(password, "Password") }
        )
    }
}
