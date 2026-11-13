package com.appnest.safe.credentials.vault.ui.base.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appnest.safe.credentials.vault.domain.model.SiteCredential

@Composable
fun SiteDetailTemplate(
    activeCredential: SiteCredential,
    isDecrypted: Boolean,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onRequestUnlock: () -> Unit,
    onCopy: (String, String) -> Unit
) {
    Column {
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
                    text = if (isDecrypted) activeCredential.username else "Encrypted Username",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = {
                        if (isDecrypted) {
                            onCopy(activeCredential.username, "Username")
                        } else {
                            onRequestUnlock()
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isDecrypted) Icons.Default.ContentCopy else Icons.Default.Lock,
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
                        !isDecrypted -> "•••••••••••• (Encrypted)"
                        isPasswordVisible -> activeCredential.password
                        else -> "••••••••••••"
                    },
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (isDecrypted) {
                                onTogglePasswordVisibility()
                            } else {
                                onRequestUnlock()
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        val icon = if (!isDecrypted) Icons.Default.Lock else if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        Icon(icon, contentDescription = "Toggle password visibility", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (isDecrypted) {
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(
                            onClick = { onCopy(activeCredential.password, "Password") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy password", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
