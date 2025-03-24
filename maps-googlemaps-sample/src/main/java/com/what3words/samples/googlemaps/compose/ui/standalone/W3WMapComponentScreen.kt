package com.what3words.samples.googlemaps.compose.ui.standalone

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.what3words.components.compose.maps.MapProvider
import com.what3words.components.compose.maps.W3WMapComponent
import com.what3words.components.compose.maps.W3WMapDefaults
import com.what3words.components.compose.maps.models.W3WMarkerColor
import com.what3words.components.compose.maps.rememberW3WMapManager
import com.what3words.core.datasource.text.W3WTextDataSource
import com.what3words.samples.googlemaps.compose.data.LocationSourceImpl
import com.what3words.samples.googlemaps.compose.data.london1Coordinate
import com.what3words.samples.googlemaps.compose.data.london2W3WAddress
import com.what3words.samples.googlemaps.compose.data.london3Coordinate
import com.what3words.samples.googlemaps.compose.data.london3W3WAddress
import com.what3words.samples.googlemaps.compose.data.london4Coordinate
import com.what3words.samples.googlemaps.compose.data.london4W3WAddress
import com.what3words.samples.googlemaps.compose.data.london5Coordinate
import com.what3words.samples.googlemaps.compose.data.london5W3WAddress
import kotlinx.coroutines.launch


@Composable
fun W3WMapComponentScreen(
    modifier: Modifier = Modifier,
    textDataSource: W3WTextDataSource
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val mapManager = rememberW3WMapManager(
        mapProvider = MapProvider.GOOGLE_MAP
    )

    val locationSource = remember {
        LocationSourceImpl(context)
    }

    // Move the camera to a specific coordinate
    LaunchedEffect(Unit) {
        mapManager.moveToPosition(london1Coordinate)
    }

    // Add a marker at a specific coordinate
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            mapManager.addMarkerAt(
                coordinates = london1Coordinate,
                markerColor = W3WMarkerColor(background = Color.Blue, slash = Color.Green)
            )
        }
    }

    // Add a list of marker at specific coordinates
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            mapManager.addMarkersAt(
                listCoordinates = listOf(
                    london3Coordinate,
                    london4Coordinate,
                    london5Coordinate
                ),
                listName = "London",
                markerColor = W3WMarkerColor(background = Color.Yellow, slash = Color.Red)
            )
        }
    }

    // Add a marker at a specific what3words address
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            mapManager.addMarkerAt(
                words = london2W3WAddress,
                markerColor = W3WMarkerColor(background = Color.Black, slash = Color.Yellow)
            )
        }
    }

    // Add a list of marker at specific coordinates
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            mapManager.addMarkersAt(
                listWords = listOf(
                    london3W3WAddress,
                    london4W3WAddress,
                    london5W3WAddress
                ),
                listName = "London 2",
                markerColor = W3WMarkerColor(background = Color.Blue, slash = Color.White)
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            locationSource.onDestroy()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        W3WMapComponent(
            textDataSource = textDataSource,
            modifier = Modifier.fillMaxSize(),
            locationSource = locationSource,
            mapConfig = W3WMapDefaults.defaultMapConfig(
                buttonConfig = W3WMapDefaults.ButtonConfig(
                    isRecallFeatureEnabled = true,
                    isMapSwitchFeatureEnabled = true,
                    isMyLocationFeatureEnabled = true
                )
            ),
            mapManager = mapManager,
            onSelectedSquareChanged = {
                // Handle the selected square
            }
        )
    }
}