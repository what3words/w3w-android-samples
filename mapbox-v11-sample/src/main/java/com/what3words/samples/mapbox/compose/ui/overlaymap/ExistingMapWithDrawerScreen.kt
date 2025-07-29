package com.what3words.samples.mapbox.compose.ui.overlaymap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotationGroup
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotationGroupState
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.BooleanValue
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.compose.style.Transition
import com.mapbox.maps.extension.compose.style.layers.generated.CircleLayer
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.switchCase
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.interactions.FeatureState
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.what3words.components.compose.maps.MapProvider
import com.what3words.components.compose.maps.W3WMapDefaults
import com.what3words.components.compose.maps.models.W3WMarkerColor
import com.what3words.components.compose.maps.providers.mapbox.W3WMapBoxDrawer
import com.what3words.components.compose.maps.providers.mapbox.updateGridBound
import com.what3words.components.compose.maps.rememberW3WMapManager
import com.what3words.components.compose.maps.state.camera.W3WMapboxCameraState
import com.what3words.core.datasource.text.W3WTextDataSource
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.samples.mapbox.compose.data.london1Coordinate
import com.what3words.samples.mapbox.compose.data.london2Coordinate
import com.what3words.samples.mapbox.compose.data.london2W3WAddress
import com.what3words.samples.mapbox.compose.data.london3W3WAddress
import com.what3words.samples.mapbox.compose.data.london4Coordinate
import com.what3words.samples.mapbox.compose.data.london4W3WAddress
import com.what3words.samples.mapbox.compose.data.london5W3WAddress
import com.what3words.samples.mapbox.v11.R
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val TAG = "ExistingMapWithDrawerScreen"

@Composable
fun ExistingMapWithDrawerScreen(
    modifier: Modifier = Modifier,
    textDataSource: W3WTextDataSource
) {
    // Observing and controlling the camera's state can be done with a MapViewPortState
    val mapViewportState = rememberMapViewportState {
        setCameraOptions(
            CameraOptions.Builder()
                .center(Point.fromLngLat(london1Coordinate.lng, london1Coordinate.lat))
                .zoom(19.0)
                .bearing(0.0)
                .pitch(0.0)
                .build()
        )
    }

    val coroutineScope = rememberCoroutineScope()

    //region What3words map component setup
    // --- Map configuration ---
    val mapConfig = W3WMapDefaults.defaultMapConfig()

    var mapView: MapView? by remember {
        mutableStateOf(null)
    }

    // --- Create map manager ---
    //REQUIRED
    val mapManager = rememberW3WMapManager(
        mapProvider = MapProvider.MAPBOX,
    ).apply {
        //REQUIRED
        setMapConfig(mapConfig)
        setTextDataSource(textDataSource)
    }

    //REQUIRED
    val mapState by mapManager.mapState.collectAsState()

    //REQUIRED - needed to draw the 3x3m grid on the map
    // Update the reference after rotation
    DisposableEffect(mapViewportState) {
        val w3wMapBoxCameraState = mapManager.mapState.value.cameraState as? W3WMapboxCameraState
        w3wMapBoxCameraState?.cameraState = mapViewportState
        onDispose { }
    }

    val w3wMapBoxCameraState = mapState.cameraState as W3WMapboxCameraState
    var lastProcessedCameraState by remember { mutableStateOf(mapViewportState.cameraState) }

    LaunchedEffect(mapViewportState.cameraState) {
        snapshotFlow { mapViewportState.cameraState }
            .filterNotNull()
            .onEach { currentCameraState ->
                mapView?.mapboxMap?.let { mapboxMap ->
                    updateGridBound(
                        mapboxMap,
                        mapConfig.gridLineConfig,
                        onCameraBoundUpdate = { gridBound, visibleBound ->
                            lastProcessedCameraState = currentCameraState
                            w3wMapBoxCameraState.gridBound = gridBound
                            w3wMapBoxCameraState.visibleBound = visibleBound
                            coroutineScope.launch {
                                mapManager.updateCameraState(w3wMapBoxCameraState)
                            }
                        }
                    )
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
        MapBoxView(
            mapViewportState = mapViewportState,
            onMapClick = {
                //region What3words map component setup
                coroutineScope.launch {
                    //REQUIRED - needed to draw selected marker
                    mapManager.setSelectedAt(W3WCoordinates(it.latitude(), it.longitude()))
                }
                //endregion
            },
            content = {
                //region What3words map components setup
                //REQUIRED - needed to draw the 3x3m grid on the map
                MapEffect {
                    mapView = it
                }

                W3WMapBoxDrawer(
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

@OptIn(MapboxExperimental::class)
@Composable
fun MapBoxView(
    modifier: Modifier = Modifier,
    mapViewportState: MapViewportState,
    onMapClick: ((Point) -> Unit)? = null,
    content: @Composable () -> Unit = {}
) {
    val pointList = remember {
        listOf(
            Point.fromLngLat(london1Coordinate.lng, london1Coordinate.lat),
            Point.fromLngLat(london2Coordinate.lng, london2Coordinate.lat),
        )
    }

    // Create polyline options
    val polylines = remember {
        listOf(
            PolylineAnnotationOptions()
                .withPoints(pointList)
                .withLineColor(Color.Red.toArgb())
                .withLineWidth(5.0)
        )
    }

    val markerImage = rememberIconImage(key = R.drawable.ic_marker, painter = painterResource(R.drawable.ic_marker))

    val geoJsonSource = rememberGeoJsonSourceState {
        generateId = BooleanValue(true)
    }
    geoJsonSource.data = GeoJSONData(Point.fromLngLat(london4Coordinate.lng, london4Coordinate.lat))

    com.mapbox.maps.extension.compose.MapboxMap(
        modifier = modifier
            .fillMaxSize(),
        mapState = rememberMapState(),
        mapViewportState = mapViewportState,
        onMapClickListener = {
            onMapClick?.invoke(it)
            true
        },
        style = {
            MapStyle(style = Style.STANDARD)
        }
    ) {
        //Draw marker
        PointAnnotation(
            point = Point.fromLngLat(
                london2Coordinate.lng,
                london2Coordinate.lat
            )
        ) {
            iconImage = markerImage
            iconEmissiveStrength = 1.0
            iconColor = Color.Red
            iconAnchor =
                IconAnchor.BOTTOM // This makes the arrow part of the icon to be at the center of the selected square
        }


        // Draw a polyline connecting the points
        PolylineAnnotationGroup(
            annotations = polylines,
            polylineAnnotationGroupState = remember {
                PolylineAnnotationGroupState().apply {
                    lineOcclusionOpacity = 0.0
                    lineEmissiveStrength = 1.0
                    lineWidth = 1.0
                }
            }
        )

        CircleLayer(
            sourceState = geoJsonSource,
        ) {
            interactionsState.onClicked { featuresetFeature, _ ->
                val selected = featuresetFeature.state.getBooleanState("selected") ?: false
                featuresetFeature.setFeatureState(
                    FeatureState {
                        addBooleanState("selected", !selected)
                    }
                )
                true
            }
            circleColor = ColorValue(
                switchCase {
                    boolean {
                        featureState {
                            literal("selected")
                        }
                        literal(false)
                    }
                    literal("yellow")
                    literal("black")
                }
            )
            circleRadius = DoubleValue(50.0)
            circleRadiusTransition = Transition(durationMillis = 1000L)
        }

        content.invoke()
    }
}