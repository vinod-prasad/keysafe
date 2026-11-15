package com.appnest.safe.credentials.vault.ui.base.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SecurityQuestionForm(
    index: Int,
    question: String,
    answer: String,
    isAnswerVisible: Boolean,
    onQuestionChanged: (String) -> Unit,
    onAnswerChanged: (String) -> Unit,
    onToggleAnswerVisibility: () -> Unit,
    onRemove: () -> Unit,
    onCopyQuestion: (() -> Unit)? = null,
    onCopyAnswer: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question ${index + 1}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove question",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField(
            value = question,
            onValueChange = onQuestionChanged,
            placeholder = "Security Question (e.g. Mother's maiden name)",
            onCopy = onCopyQuestion
        )
        Spacer(modifier = Modifier.height(8.dp))

        SecureTextField(
            value = answer,
            onValueChange = onAnswerChanged,
            placeholder = "Answer",
            isTextVisible = isAnswerVisible,
            onToggleVisibility = onToggleAnswerVisibility,
            shape = RoundedCornerShape(8.dp),
            onCopy = onCopyAnswer
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun SecurityQuestionFormPreview() {
    com.appnest.safe.credentials.vault.ui.theme.KeySafeTheme {
        SecurityQuestionForm(
            index = 0,
            question = "What is your pet's name?",
            answer = "Fluffy",
            isAnswerVisible = false,
            onQuestionChanged = {},
            onAnswerChanged = {},
            onToggleAnswerVisibility = {},
            onRemove = {}
        )
    }
}