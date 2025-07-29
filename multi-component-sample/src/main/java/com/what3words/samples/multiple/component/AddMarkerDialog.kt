package com.what3words.samples.multiple.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.what3words.design.library.ui.theme.W3WTheme

@Composable
fun AddMarkerDialog(
    onConfirmSingle: (listName: String?, backgroundColor: Color, slashColor: Color) -> Unit,
    onConfirmBatch: (size: Int, listName: String, backgroundColor: Color, slashColor: Color) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Add Single Marker", "Add List Markers")

    Column(modifier = Modifier.padding(16.dp)) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> AddSingleMarkerDialog(
                onConfirm = onConfirmSingle,
                onDismiss = onDismiss
            )

            1 -> AddBatchMarkerDialog(
                onDismiss = onDismiss,
                onConfirm = onConfirmBatch
            )
        }
    }
}

@Composable
fun AddSingleMarkerDialog(
    onConfirm: (listName: String?, backgroundColor: Color, slashColor: Color) -> Unit,
    onDismiss: () -> Unit,
) {
    var listName by remember { mutableStateOf("") }
    var backgroundColor by remember { mutableStateOf(Color.Yellow) }
    var slashColor by remember { mutableStateOf(Color.Red) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = listName,
            onValueChange = { listName = it },
            label = { Text("List name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Background Color",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Normal
        )
        ColorPicker(
            color = backgroundColor,
            onColorChanged = { backgroundColor = it }
        )

        Text(
            "Slash Color",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Normal
        )
        ColorPicker(
            color = slashColor,
            onColorChanged = { slashColor = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Button(onClick = {
                onConfirm(
                    listName.ifEmpty { null },
                    backgroundColor,
                    slashColor
                )
            }) {
                Text("Confirm")
            }
        }
    }
}

@Composable
fun ColorPicker(color: Color, onColorChanged: (Color) -> Unit) {
    val colors = listOf(
        Color.Red,
        Color.Green,
        Color.Blue,
        Color.Yellow,
        Color.Magenta,
        Color.Cyan,
        MaterialTheme.colorScheme.onBackground,
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.errorContainer,
        MaterialTheme.colorScheme.inverseOnSurface,
        MaterialTheme.colorScheme.inversePrimary
    )
    LazyRow {
        items(colors.size) { index ->
            val pickerColor = colors[index]
            ColorBox(
                color = pickerColor,
                isSelected = color == pickerColor,
                onClick = { onColorChanged(pickerColor) }
            )
        }
    }
}

@Composable
fun ColorBox(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(LocalMinimumInteractiveComponentSize.current)
            .clip(CircleShape)
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) Color.Gray else Color.Transparent,
                shape = CircleShape
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(color)
                .clickable(onClick = onClick)
        )
    }
}

@Composable
fun AddBatchMarkerDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: (size: Int, listName: String, backgroundColor: Color, slashColor: Color) -> Unit,
) {
    var size by remember { mutableIntStateOf(0) }

    var listName by remember {
        mutableStateOf("")
    }

    var backgroundColor by remember { mutableStateOf(Color.Yellow) }
    var slashColor by remember { mutableStateOf(Color.Red) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = listName,
            onValueChange = { listName = it },
            label = { Text("List name") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = size.toString(),
            onValueChange = {
                size = try {
                    it.toInt()
                } catch (_: Exception) {
                    0
                }
            },
            label = { Text("Number of marker") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Background Color",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Normal
        )
        ColorPicker(
            color = backgroundColor,
            onColorChanged = { backgroundColor = it }
        )

        Text(
            "Slash Color",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Normal
        )
        ColorPicker(
            color = slashColor,
            onColorChanged = { slashColor = it }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Button(onClick = {
                onConfirm(size, listName, backgroundColor, slashColor)
            }) {
                Text("Confirm")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CombinedMarkerDialogPreview() {
    W3WTheme {
        AddMarkerDialog(
            onConfirmSingle = { _, _, _ -> },
            onConfirmBatch = { _, _, _, _ -> },
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddBatchMarkerDialogPreview() {
    W3WTheme {
        AddBatchMarkerDialog(
            onDismiss = {},
            onConfirm = { _, _, _, _ ->

            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddMarkerDialogPreview() {
    W3WTheme {
        AddSingleMarkerDialog(
            onConfirm = { _, _, _ -> },
            onDismiss = {}
        )
    }
}