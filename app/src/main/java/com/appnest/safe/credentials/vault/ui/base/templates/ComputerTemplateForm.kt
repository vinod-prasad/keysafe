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
import androidx.compose.ui.res.stringResource
import com.appnest.safe.credentials.vault.R
import com.appnest.safe.credentials.vault.ui.base.components.CustomTextField
import com.appnest.safe.credentials.vault.utils.UiText
import com.appnest.safe.credentials.vault.ui.base.components.SecureTextField

@Composable
fun ComputerTemplateForm(
    computerName: String,
    osType: String,
    username: String,
    password: String,
    isPasswordVisible: Boolean,
    computerNameError: UiText? = null,
    usernameError: UiText? = null,
    passwordError: UiText? = null,
    onComputerNameChanged: (String) -> Unit,
    onOsTypeChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onCopy: (String, String) -> Unit
) {
    val availableOsList = listOf("Windows", "Mac", "Linux", "Android")

    Column {
        Text(stringResource(R.string.label_computer_name) + " *", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField(
            value = computerName,
            onValueChange = onComputerNameChanged,
            placeholder = stringResource(R.string.hint_computer_name),
            isMandatory = true,
            isError = computerNameError != null,
            errorMessage = computerNameError?.asString(),
            onCopy = { onCopy(computerName, "Computer Name") }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(stringResource(R.string.label_os_type) + " *", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            placeholder = stringResource(R.string.label_system_username),
            isMandatory = true,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomEnd = 0.dp, bottomStart = 0.dp),
            isError = usernameError != null,
            errorMessage = usernameError?.asString(),
            onCopy = { onCopy(username, "Username") }
        )
        SecureTextField(
            value = password,
            onValueChange = onPasswordChanged,
            placeholder = stringResource(R.string.label_system_password),
            isMandatory = true,
            isTextVisible = isPasswordVisible,
            onToggleVisibility = onTogglePasswordVisibility,
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomEnd = 8.dp, bottomStart = 8.dp),
            isError = passwordError != null,
            errorMessage = passwordError?.asString(),
            onCopy = { onCopy(password, "Password") }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ComputerTemplateFormPreview() {
    com.appnest.safe.credentials.vault.ui.theme.KeySafeTheme {
        ComputerTemplateForm(
            computerName = "Work MacBook Pro",
            osType = "Mac",
            username = "admin",
            password = "Password123",
            isPasswordVisible = false,
            onComputerNameChanged = {},
            onOsTypeChanged = {},
            onUsernameChanged = {},
            onPasswordChanged = {},
            onTogglePasswordVisibility = {},
            onCopy = { _, _ -> }
        )
    }
}
