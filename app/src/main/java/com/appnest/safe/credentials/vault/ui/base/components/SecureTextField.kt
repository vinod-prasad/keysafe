package com.appnest.safe.credentials.vault.ui.base.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SecureTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isTextVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    keyboardType: KeyboardType = KeyboardType.Password,
    isError: Boolean = false,
    errorMessage: String? = null,
    isMandatory: Boolean = false,
    onCopy: (() -> Unit)? = null
) {
    Column {
        TextField(
            value = value,
            onValueChange = onValueChange,
            isError = isError,
            placeholder = { 
                if (isMandatory) {
                    Text(
                        text = buildAnnotatedString {
                            append(placeholder)
                            append(" ")
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
                                append("*")
                            }
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isTextVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onToggleVisibility) {
                    val icon = if (isTextVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    Icon(
                        imageVector = icon,
                        contentDescription = if (isTextVisible) "Hide $placeholder" else "Show $placeholder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (onCopy != null && value.isNotBlank()) {
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy $placeholder",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        supportingText = errorMessage?.let { { Text(it) } },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun SecureTextFieldPreview() {
    com.appnest.safe.credentials.vault.ui.theme.KeySafeTheme {
        SecureTextField(
            value = "SecretPassword123",
            onValueChange = {},
            placeholder = "Password",
            isTextVisible = false,
            onToggleVisibility = {},
            isMandatory = true
        )
    }
}
