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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.appnest.safe.credentials.vault.R
import com.appnest.safe.credentials.vault.ui.base.components.CustomTextField
import com.appnest.safe.credentials.vault.utils.UiText
import com.appnest.safe.credentials.vault.ui.base.components.SecureTextField
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions

class CardNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        var out = ""
        for (i in text.indices) {
            out += text[i]
            if (i % 4 == 3 && i < 15) out += " "
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset + 1
                if (offset <= 11) return offset + 2
                if (offset <= 16) return offset + 3
                return 19
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 9) return offset - 1
                if (offset <= 14) return offset - 2
                if (offset <= 19) return offset - 3
                return 16
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        var out = ""
        for (i in text.indices) {
            out += text[i]
            if (i == 1) out += "/"
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 4) return offset + 1
                return 5
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                return 4
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

@Composable
fun CardTemplateForm(
    cardName: String,
    cardType: String,
    cardIssuer: String,
    cardNameOnCard: String,
    cardNumber: String,
    cardValidityStart: String,
    cardValidityEnd: String,
    cardCvv: String,
    isCardCvvVisible: Boolean,
    cardNameError: UiText? = null,
    cardIssuerError: UiText? = null,
    cardDateError: UiText? = null,
    onCardNameChanged: (String) -> Unit,
    onCardTypeChanged: (String) -> Unit,
    onCardIssuerChanged: (String) -> Unit,
    onCardNameOnCardChanged: (String) -> Unit,
    onCardNumberChanged: (String) -> Unit,
    onCardValidityStartChanged: (String) -> Unit,
    onCardValidityEndChanged: (String) -> Unit,
    onCardCvvChanged: (String) -> Unit,
    onToggleCardCvvVisibility: () -> Unit,
    onCopy: (String, String) -> Unit
) {
    Column {
        Text(stringResource(R.string.label_card_name) + " *", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField(
            value = cardName,
            onValueChange = onCardNameChanged,
            placeholder = stringResource(R.string.hint_card_name),
            isMandatory = true,
            isError = cardNameError != null,
            errorMessage = cardNameError?.asString(),
            onCopy = { onCopy(cardName, "Card Name") }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        Text(stringResource(R.string.label_card_type) + " *", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        
        val availableCardTypes = listOf(
            stringResource(R.string.card_type_credit), 
            stringResource(R.string.card_type_debit)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableCardTypes) { type ->
                val isSelected = cardType.equals(type, ignoreCase = true)
                Surface(
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    onClick = { onCardTypeChanged(type) }
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
                            text = type,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(stringResource(R.string.label_card_issuer) + " *", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField(
            value = cardIssuer,
            onValueChange = onCardIssuerChanged,
            placeholder = stringResource(R.string.hint_card_issuer),
            isMandatory = true,
            isError = cardIssuerError != null,
            errorMessage = cardIssuerError?.asString(),
            onCopy = { onCopy(cardIssuer, "Issuer") }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(stringResource(R.string.label_name_on_card), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField(
            value = cardNameOnCard,
            onValueChange = onCardNameOnCardChanged,
            placeholder = stringResource(R.string.hint_name_on_card),
            onCopy = { onCopy(cardNameOnCard, "Name on Card") }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(stringResource(R.string.label_card_number), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField(
            value = cardNumber,
            onValueChange = { if (it.length <= 16 && it.all { char -> char.isDigit() }) onCardNumberChanged(it) },
            placeholder = stringResource(R.string.hint_card_number),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = CardNumberVisualTransformation(),
            onCopy = { onCopy(cardNumber, "Card Number") }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.label_validity_start), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                CustomTextField(
                    value = cardValidityStart,
                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) onCardValidityStartChanged(it) },
                    placeholder = stringResource(R.string.hint_validity_date),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = DateVisualTransformation(),
                    isError = cardDateError != null,
                    errorMessage = cardDateError?.asString()
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.label_validity_expiry), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                CustomTextField(
                    value = cardValidityEnd,
                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) onCardValidityEndChanged(it) },
                    placeholder = stringResource(R.string.hint_validity_date),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = DateVisualTransformation(),
                    isError = cardDateError != null
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(stringResource(R.string.label_card_cvv), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        SecureTextField(
            value = cardCvv,
            onValueChange = onCardCvvChanged,
            placeholder = stringResource(R.string.hint_card_cvv),
            isTextVisible = isCardCvvVisible,
            onToggleVisibility = onToggleCardCvvVisibility,
            keyboardType = KeyboardType.NumberPassword,
            onCopy = { onCopy(cardCvv, "CVV") }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CardTemplateFormPreview() {
    com.appnest.safe.credentials.vault.ui.theme.KeySafeTheme {
        CardTemplateForm(
            cardName = "HDFC Card",
            cardType = "Credit",
            cardIssuer = "HDFC Bank",
            cardNameOnCard = "JOHN DOE",
            cardNumber = "1234567812345678",
            cardValidityStart = "0522",
            cardValidityEnd = "0529",
            cardCvv = "123",
            isCardCvvVisible = false,
            onCardNameChanged = {},
            onCardTypeChanged = {},
            onCardIssuerChanged = {},
            onCardNameOnCardChanged = {},
            onCardNumberChanged = {},
            onCardValidityStartChanged = {},
            onCardValidityEndChanged = {},
            onCardCvvChanged = {},
            onToggleCardCvvVisibility = {},
            onCopy = { _, _ -> }
        )
    }
}
