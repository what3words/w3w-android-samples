package com.what3words.compose.sample.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = text, onValueChange = onTextChanged,
            label = {
                Text(text = primaryLabel)
            },
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
            singleLine = true
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            if (secondaryLabel != null) {
                Spacer(modifier = Modifier.height(height = 4.dp))
                Text(
                    text = secondaryLabel,
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}