package com.what3words.samples.mapbox.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.components.compose.maps.MapProvider
import com.what3words.components.compose.maps.W3WMapComponent
import com.what3words.components.compose.maps.W3WMapDefaults
import com.what3words.components.compose.maps.models.W3WMarkerColor
import com.what3words.components.compose.maps.rememberW3WMapManager
import com.what3words.core.datasource.text.W3WTextDataSource
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.design.library.ui.theme.W3WTheme
import com.what3words.samples.mapbox.v11.BuildConfig

val london1Coordinate = W3WCoordinates(51.513678, -0.133823)
val london2Coordinate = W3WCoordinates(51.494947, -0.093923)
val london3Coordinate = W3WCoordinates(51.473062, -0.142303)
val london4Coordinate = W3WCoordinates(51.472335, -0.183234)
val london5Coordinate = W3WCoordinates(51.471445, -0.239064)
const val london1W3WAddress = "verbs.stacks.ranked"
const val london2W3WAddress = "alarm.cargo.bills"
const val london3W3WAddress = "gold.basin.freed"
const val london4W3WAddress = "known.format.adults"
const val london5W3WAddress = "piles.hedge.logo"

class MapComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            W3WTheme {
                W3WMapComponentApp(
                    textDataSource = W3WApiTextDataSource.create(this, BuildConfig.W3W_API_KEY)
                )
            }
        }
    }
}


@Composable
fun W3WMapComponentApp(
    modifier: Modifier = Modifier,
    textDataSource: W3WTextDataSource
) {

    val context = LocalContext.current

    val mapManager = rememberW3WMapManager(
        mapProvider = MapProvider.MAPBOX
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
        mapManager.addMarkerAt(
            coordinates = london2Coordinate,
            markerColor = W3WMarkerColor(background = Color.Red, slash = Color.Yellow)
        )
    }

    // Add a list of marker at specific coordinates
    LaunchedEffect(Unit) {
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

    // Select a specific address on the map
    LaunchedEffect(Unit) {
        mapManager.setSelectedAt(london1W3WAddress)
    }

    // Add a marker at a specific what3words address
    LaunchedEffect(Unit) {
        mapManager.addMarkerAt(
            words = london2W3WAddress,
            markerColor = W3WMarkerColor(background = Color.Black, slash = Color.Yellow)
        )
    }

    // Add a list of marker at specific coordinates
    LaunchedEffect(Unit) {
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