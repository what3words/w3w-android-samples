package com.what3words.samples.googlemaps.compose.ui.overlaymap

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.StrokeStyle
import com.google.android.gms.maps.model.StyleSpan
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.what3words.components.compose.maps.MapProvider
import com.what3words.components.compose.maps.W3WMapDefaults
import com.what3words.components.compose.maps.mapper.toGoogleLatLng
import com.what3words.components.compose.maps.models.W3WMarkerColor
import com.what3words.components.compose.maps.providers.googlemap.W3WGoogleMapDrawer
import com.what3words.components.compose.maps.providers.googlemap.updateCameraBound
import com.what3words.components.compose.maps.rememberW3WMapManager
import com.what3words.components.compose.maps.state.camera.W3WGoogleCameraState
import com.what3words.core.datasource.text.W3WTextDataSource
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.samples.googlemaps.compose.data.london1Coordinate
import com.what3words.samples.googlemaps.compose.data.london2Coordinate
import com.what3words.samples.googlemaps.compose.data.london2W3WAddress
import com.what3words.samples.googlemaps.compose.data.london3Coordinate
import com.what3words.samples.googlemaps.compose.data.london3W3WAddress
import com.what3words.samples.googlemaps.compose.data.london4Coordinate
import com.what3words.samples.googlemaps.compose.data.london4W3WAddress
import com.what3words.samples.googlemaps.compose.data.london5Coordinate
import com.what3words.samples.googlemaps.compose.data.london5W3WAddress
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val TAG = "ExistingMapWithDrawerScreen"

val defaultCameraPosition = CameraPosition.fromLatLngZoom(london1Coordinate.toGoogleLatLng(), 11f)

val styleSpan = StyleSpan(
    StrokeStyle.gradientBuilder(
        Color.Red.toArgb(),
        Color.Green.toArgb(),
    ).build(),
)

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun ExistingMapWithDrawerScreen(
    modifier: Modifier = Modifier,
    textDataSource: W3WTextDataSource
) {
    // Observing and controlling the camera's state can be done with a CameraPositionState
    val cameraPositionState = rememberCameraPositionState {
        position = defaultCameraPosition
    }
    val coroutineScope = rememberCoroutineScope()

    //region What3words map component setup
    // --- Map configuration ---
    val mapConfig = W3WMapDefaults.defaultMapConfig()

    // --- Create map manager ---
    //REQUIRED
    val mapManager = rememberW3WMapManager(
        mapProvider = MapProvider.GOOGLE_MAP,
    ).apply {
        //REQUIRED
        setMapConfig(mapConfig)
        setTextDataSource(textDataSource)
    }

    //REQUIRED
    val mapState by mapManager.mapState.collectAsState()

    //REQUIRED - needed to draw the 3x3m grid on the map
    // Update the reference after rotation
    DisposableEffect(cameraPositionState) {
        val w3wGoogleCameraState = mapManager.mapState.value.cameraState as? W3WGoogleCameraState
        w3wGoogleCameraState?.cameraState = cameraPositionState
        onDispose { }
    }

    val w3wGoogleCameraState = mapState.cameraState as W3WGoogleCameraState
    var lastProcessedPosition by remember { mutableStateOf(cameraPositionState.position) }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.position to cameraPositionState.projection }
            .conflate()
            .onEach { (position, projection) ->
                projection?.let {
                    updateCameraBound(
                        projection,
                        mapConfig.gridLineConfig
                    ) { gridBound, visibleBound ->
                        lastProcessedPosition = position
                        w3wGoogleCameraState.gridBound = gridBound
                        w3wGoogleCameraState.visibleBound = visibleBound
                        coroutineScope.launch {
                            mapManager.updateCameraState(w3wGoogleCameraState)
                        }
                    }
                }
            }.launchIn(this)
    }
    //endregion

    //region What3words demo features
    // Add a marker at a specific coordinate
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            mapManager.addMarkerAt(
                coordinates = london1Coordinate,
                markerColor = W3WMarkerColor(background = Color.Red, slash = Color.Yellow)
            )
        }
    }

    // --- Handle selected address changes ---
    LaunchedEffect(mapState.selectedAddress) {
        mapState.selectedAddress?.let {
            // Handle the selected square
        }
    }

    // Add a marker at a specific what3words address
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            mapManager.addMarkerAt(
                words = london2W3WAddress,
                markerColor = W3WMarkerColor(
                    background = Color.Black,
                    slash = Color.Yellow
                ),
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
                markerColor = W3WMarkerColor(background = Color.Blue, slash = Color.White),
            )
        }
    }
    //endregion

    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        GoogleMapView(
            cameraPositionState = cameraPositionState,
            onMapClick = {
                //region What3words map component setup
                coroutineScope.launch {
                    //REQUIRED - needed to draw selected marker
                    mapManager.setSelectedAt(W3WCoordinates(it.latitude, it.longitude))
                }
                //endregion
            },
            content = {
                //region What3words map components setup
                //REQUIRED - needed to draw the 3x3m grid on the map
                W3WGoogleMapDrawer(
                    state = mapState,
                    mapConfig = mapConfig,
                    mapColor = W3WMapDefaults.defaultNormalMapColor(),
                    onMarkerClicked = {
                        coroutineScope.launch {
                            mapManager.setSelectedAt(it.center)
                        }
                    }
                )
                //endregion
            }
        )
    }
}

@Composable
fun GoogleMapView(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    onMapClick: ((LatLng) -> Unit)? = null,
    mapColorScheme: ComposeMapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
    content: @Composable () -> Unit = {}
) {
    val londonState = rememberMarkerState(position = london1Coordinate.toGoogleLatLng())
    val london2State = rememberMarkerState(position = london2Coordinate.toGoogleLatLng())
    val london3State = rememberMarkerState(position = london3Coordinate.toGoogleLatLng())
    val london4State = rememberMarkerState(position = london4Coordinate.toGoogleLatLng())

    var circleCenter by remember { mutableStateOf(london5Coordinate.toGoogleLatLng()) }
    if (!londonState.isDragging) {
        circleCenter = londonState.position
    }

    val polylinePoints = remember { listOf(london1Coordinate.toGoogleLatLng(), london5Coordinate.toGoogleLatLng()) }
    val polylineSpanPoints = remember { listOf(london1Coordinate.toGoogleLatLng()) }
    val styleSpanList = remember { listOf(styleSpan) }

    var uiSettings by remember { mutableStateOf(MapUiSettings(compassEnabled = false)) }
    var mapProperties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL))
    }
    var mapVisible by remember { mutableStateOf(true) }

    var darkMode by remember { mutableStateOf(mapColorScheme) }

    if (mapVisible) {
        GoogleMap(
            modifier = modifier,
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings,
            onMapClick = onMapClick,
            onPOIClick = {
                Log.d(TAG, "POI clicked: ${it.name}")
            },
            mapColorScheme = darkMode
        ) {
            // Drawing on the map is accomplished with a child-based API
            val markerClick: (Marker) -> Boolean = {
                Log.d(TAG, "${it.title} was clicked")
                cameraPositionState.projection?.let { projection ->
                    Log.d(TAG, "The current projection is: $projection")
                }
                false
            }

            MarkerInfoWindowContent(
                state = londonState,
                title = "This marker can be dragged",
                onClick = markerClick,
                draggable = true,
            ) {
                Text(it.title ?: "Title", color = Color.Red)
            }

            MarkerInfoWindowContent(
                state = london2State,
                title = "Marker with custom info window",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                onClick = markerClick,
            ) {
                Text(it.title ?: "Title", color = Color.Blue)
            }

            com.google.maps.android.compose.Marker(
                state = london3State,
                title = "Marker in London",
                onClick = markerClick
            )

            MarkerComposable(
                title = "Marker Composable",
                keys = arrayOf("london4"),
                state = london4State,
                onClick = markerClick,
            ) {
                Box(
                    modifier = Modifier
                        .width(88.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Red),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Compose Marker",
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Circle(
                center = circleCenter,
                fillColor = MaterialTheme.colorScheme.secondary,
                strokeColor = MaterialTheme.colorScheme.onSecondary,
                radius = 1000.0,
            )

            Polyline(
                points = polylinePoints,
                tag = "Polyline A",
            )

            Polyline(
                points = polylineSpanPoints,
                spans = styleSpanList,
                tag = "Polyline B",
            )

            content()
        }
    }
}