package com.appnest.safe.credentials.vault.ui.base.dialogs

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import com.appnest.safe.credentials.vault.R
import androidx.compose.ui.window.Dialog

@Composable
fun EncryptionKeyPromptDialog(
    title: String = "Vault Master Key Required",
    subtitle: String = "Enter your secret Vault Master Key to proceed. This key is processed in memory and never saved locally.",
    confirmButtonText: String = "Confirm",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var keyInput by remember { mutableStateOf("") }
    var keyVisible by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        KeyInfoDialog(onDismiss = { showInfoDialog = false })
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp).padding(bottom = 4.dp)
                )
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = {
                        keyInput = it
                        isError = false
                    },
                    label = { Text(stringResource(R.string.label_master_key)) },
                    placeholder = { Text(stringResource(R.string.hint_master_key)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showInfoDialog = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Key Info", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { keyVisible = !keyVisible }) {
                                val icon = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                                Icon(icon, contentDescription = "Toggle visibility")
                            }
                        }
                    },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text(if (keyInput.isEmpty()) stringResource(R.string.error_encryption_key_empty) else stringResource(R.string.error_key_min_length)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (keyInput.length >= 4) {
                                onConfirm(keyInput)
                            } else {
                                isError = true
                            }
                        }
                    ) {
                        Text(confirmButtonText)
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun EncryptionKeyPromptDialogPreview() {
    com.appnest.safe.credentials.vault.ui.theme.KeySafeTheme {
        EncryptionKeyPromptDialog(
            title = "Enter Secret Key",
            subtitle = "Please enter your master encryption key to decrypt this credential.",
            confirmButtonText = "Decrypt",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
