package com.what3words.samples.multiple.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.what3words.components.compose.maps.models.W3WMarker
import com.what3words.components.compose.maps.models.W3WMarkerColor
import com.what3words.components.compose.maps.models.W3WMarkerWithList
import com.what3words.components.compose.maps.utils.getPinBitmap
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WRectangle
import com.what3words.design.library.ui.theme.W3WTheme

@Composable
fun RemoveSpecificMarkersDialog(
    modifier: Modifier = Modifier,
    markers: List<W3WMarkerWithList>,
    onDismiss: () -> Unit,
    onConfirm: (List<W3WMarkerWithList>) -> Unit,
) {

    val markersToRemove = remember { mutableStateListOf<W3WMarkerWithList>() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text("Select marker to remove", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        markers.forEach { markerWithList ->
            MarkerWithListContent(
                markerWithList = markerWithList,
                checked = markersToRemove.contains(markerWithList),
                onCheckChange = {
                    if (it) {
                        markersToRemove.add(markerWithList)
                    } else {
                        markersToRemove.remove(markerWithList)
                    }
                }
            )
        }
        Text(
            text = "Tip: To remove all markers, select a square that does not contain any markers.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Button(onClick = {
                onConfirm(markersToRemove)
            }) {
                Text("Confirm")
            }
        }
    }
}

@Composable
private fun MarkerWithListContent(
    markerWithList: W3WMarkerWithList,
    checked: Boolean = false,
    onCheckChange: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val markerPin = getPinBitmap(context, 1f, markerWithList.marker.color)
    Column {
        Text(text = markerWithList.listName, style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "///${markerWithList.marker.words}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                bitmap = markerPin.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckChange
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun RemoveMarkerDialogPreview() {
    W3WTheme {
        RemoveSpecificMarkersDialog(
            markers = listOf(
                W3WMarkerWithList(
                    listName = "Test",
                    marker = W3WMarker(
                        words = "index.home.raft",
                        square = W3WRectangle(
                            southwest = W3WCoordinates(0.0, 0.0),
                            northeast = W3WCoordinates(0.0, 0.0)
                        ),
                        color = W3WMarkerColor(background = Color.Red, Color.Black),
                        center = W3WCoordinates(0.0, 0.0),
                        title = null,
                        snippet = null
                    )
                ),
                W3WMarkerWithList(
                    listName = "Homes",
                    marker = W3WMarker(
                        words = "filled.count.soap",
                        square = W3WRectangle(
                            southwest = W3WCoordinates(0.0, 0.0),
                            northeast = W3WCoordinates(0.0, 0.0)
                        ),
                        color = W3WMarkerColor(background = Color.Yellow, Color.Red),
                        center = W3WCoordinates(0.0, 0.0),
                        title = null,
                        snippet = null
                    )
                )
            ),
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Composable
fun RemoveAllMarkersDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text("Remove all markers?", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Tip: To remove specific markers, select a square containing markers.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Button(onClick = onConfirm) {
                Text("Remove All")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RemoveAllMarkersDialogPreview() {
    W3WTheme {
        RemoveAllMarkersDialog(
            onDismiss = {},
            onConfirm = {}
        )
    }
}