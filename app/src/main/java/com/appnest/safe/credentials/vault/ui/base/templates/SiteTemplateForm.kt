package com.appnest.safe.credentials.vault.ui.base.templates

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appnest.safe.credentials.vault.ui.base.components.CustomTextField
import com.appnest.safe.credentials.vault.ui.base.components.SecureTextField

@Composable
fun SiteTemplateForm(
    site: String,
    username: String,
    password: String,
    isPasswordVisible: Boolean,
    onSiteChanged: (String) -> Unit,
    onSiteFocusLost: () -> Unit = {},
    onShowAppSelector: () -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onCopy: (String, String) -> Unit
) {
    Column {
        Text("Site or App Domain", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField(
            value = site,
            onValueChange = onSiteChanged,
            placeholder = "e.g. google.com or appnest.in",
            modifier = Modifier.onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                    onSiteFocusLost()
                }
            },
            onCopy = { onCopy(site, "Site") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onShowAppSelector,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
            Text(" Select app", color = MaterialTheme.colorScheme.onPrimaryContainer)
        }

        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(
            value = username,
            onValueChange = onUsernameChanged,
            placeholder = "Username / Email",
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomEnd = 0.dp, bottomStart = 0.dp),
            onCopy = { onCopy(username, "Username") }
        )
        SecureTextField(
            value = password,
            onValueChange = onPasswordChanged,
            placeholder = "Password",
            isTextVisible = isPasswordVisible,
            onToggleVisibility = onTogglePasswordVisibility,
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomEnd = 8.dp, bottomStart = 8.dp),
            onCopy = { onCopy(password, "Password") }
        )
    }
}
