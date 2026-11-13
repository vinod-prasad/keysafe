package com.appnest.safe.credentials.vault.ui.base.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appnest.safe.credentials.vault.ui.base.components.CustomTextField
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
    cardIssuer: String,
    cardNameOnCard: String,
    cardNumber: String,
    cardValidityStart: String,
    cardValidityEnd: String,
    cardCvv: String,
    isCardCvvVisible: Boolean,
    onCardNameChanged: (String) -> Unit,
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
        Text("Card Name / Label", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField(
            value = cardName,
            onValueChange = onCardNameChanged,
            placeholder = "e.g. Primary HDFC Visa Card",
            onCopy = { onCopy(cardName, "Card Name") }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("Issuer (Bank Name)", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField(
            value = cardIssuer,
            onValueChange = onCardIssuerChanged,
            placeholder = "e.g. HDFC Bank, Chase, ICICI",
            onCopy = { onCopy(cardIssuer, "Issuer") }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("Name on Card", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField(
            value = cardNameOnCard,
            onValueChange = onCardNameOnCardChanged,
            placeholder = "e.g. JOHN DOE",
            onCopy = { onCopy(cardNameOnCard, "Name on Card") }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("Card Number", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField(
            value = cardNumber,
            onValueChange = { if (it.length <= 16 && it.all { char -> char.isDigit() }) onCardNumberChanged(it) },
            placeholder = "16-digit card number",
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
                Text("Validity Start", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                CustomTextField(
                    value = cardValidityStart,
                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) onCardValidityStartChanged(it) },
                    placeholder = "MM/YY (e.g. 0522)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = DateVisualTransformation()
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Validity Expiry", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                CustomTextField(
                    value = cardValidityEnd,
                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) onCardValidityEndChanged(it) },
                    placeholder = "MM/YY (e.g. 0529)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = DateVisualTransformation()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("CVV / CVC", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        SecureTextField(
            value = cardCvv,
            onValueChange = onCardCvvChanged,
            placeholder = "3-4 digit CVV",
            isTextVisible = isCardCvvVisible,
            onToggleVisibility = onToggleCardCvvVisibility,
            keyboardType = KeyboardType.NumberPassword,
            onCopy = { onCopy(cardCvv, "CVV") }
        )
    }
}
