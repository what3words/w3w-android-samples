package com.what3words.samples.autosuggest.compose.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MultiLabelTextField(
    text: String,
    onTextChanged: (String) -> Unit,
    primaryLabel: String,
    modifier: Modifier = Modifier,
    secondaryLabel: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = text, onValueChange = onTextChanged,
            label = {
                Text(text = primaryLabel)
            },
            singleLine = true
        )
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            if (secondaryLabel != null) {
                Spacer(modifier = Modifier.height(height = 4.dp))
                Text(
                    text = secondaryLabel,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}