package com.what3words.multi_component_sample.ui.screen.view

import android.location.Location
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.components.maps.models.W3WMarkerColor
import com.what3words.components.maps.wrappers.W3WGoogleMapsWrapper
import com.what3words.components.maps.wrappers.W3WMapBoxWrapper
import com.what3words.javawrapper.response.SuggestionWithCoordinates

@Composable
fun MapWrapperView(
    wrapper: What3WordsV3,
    modifier: Modifier,
    isGGMap: Boolean,
    addMarker: Location?,
    suggestion: SuggestionWithCoordinates?,
    onMapClick: (SuggestionWithCoordinates) -> (Unit)
) {
    Log.d("Test", "isGGMap $isGGMap $addMarker ")
    if (isGGMap) {
        GoogleMapView(
            wrapper,
            modifier = modifier,
            suggestion = suggestion,
            onMapClick = onMapClick,
            addMarker
        )
    } else {
        MapBoxView(
            wrapper,
            modifier = modifier,
            suggestion = suggestion,
            onMapClick = onMapClick,
            addMarker
        )
    }
}

@Composable
private fun GoogleMapView(
    wrapper: What3WordsV3,
    modifier: Modifier,
    suggestion: SuggestionWithCoordinates?,
    onMapClick: (SuggestionWithCoordinates) -> (Unit),
    addMarker: Location?
) {
    val TAG = "GoogleMapView"
    val context = LocalContext.current
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 16f)
    }
    var w3wMapsWrapper: W3WGoogleMapsWrapper? by remember {
        mutableStateOf(null)
    }

    var GMap: GoogleMap? by remember {
        mutableStateOf(null)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
    ) {
        //EXPERIMENTAL access to raw GoogleMap, check: https://github.com/googlemaps/android-maps-compose#obtaining-access-to-the-raw-googlemap-experimental
        MapEffect { map ->
            GMap = map
            w3wMapsWrapper = W3WGoogleMapsWrapper(
                context,
                map,
                wrapper,
            ).setLanguage("en")

            w3wMapsWrapper?.let {
                //click even on existing w3w added markers on the map.
                it.onMarkerClicked {
                    Log.d(TAG, "clicked: ${it.words}")
                }

                //REQUIRED
                map.setOnCameraIdleListener {
                    //...

                    //needed to draw the 3x3m grid on the map
                    it.updateMap()
                }

                //REQUIRED
                map.setOnCameraMoveListener {
                    //...

                    //needed to draw the 3x3m grid on the map
                    it.updateMove()
                }

                map.setOnMapClickListener { latLng ->
                    //..

                    //example of how to select a 3x3m w3w square using lat/lng
                    it.selectAtCoordinates(
                        latLng.latitude,
                        latLng.longitude, onSuccess = {
                            val cameraPosition = CameraPosition.Builder()
                                .target(latLng)
                                .zoom(19f)
                                .build()
                            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                            onMapClick.invoke(it)
                        }
                    )
                }
            }
        }

        //Your other Markers/different APIs, i.e GooglePlacesAPI
        Marker(
            state = MarkerState(position = singapore),
        )

        //Update search w3w marker
        w3wMapsWrapper?.let { w3wMapsWrapper ->
            suggestion?.let {
                if (w3wMapsWrapper.getSelectedMarker()?.coordinates?.lat != it.coordinates.lat &&
                    w3wMapsWrapper.getSelectedMarker()?.coordinates?.lng != it.coordinates.lng
                ) {
                    w3wMapsWrapper.selectAtSuggestionWithCoordinates(it, onSuccess = {
                        val cameraPosition = CameraPosition.Builder()
                            .target(LatLng(it.coordinates.lat, it.coordinates.lng))
                            .zoom(19f)
                            .build()
                        GMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    })
                }
            }

            addMarker?.let {
                if (w3wMapsWrapper.findMarkerByCoordinates(it.latitude, it.longitude) == null) {
                    w3wMapsWrapper.addMarkerAtCoordinates(
                        it.latitude,
                        it.longitude,
                        W3WMarkerColor.RED
                    )
                }
            }
        }
    }
}

@Composable
fun MapBoxView(
    wrapper: What3WordsV3,
    modifier: Modifier,
    suggestion: SuggestionWithCoordinates?,
    onMapClick: (SuggestionWithCoordinates) -> (Unit),
    addMarker: Location?
) {
    val TAG = "MapBoxView"
    var w3wMapsWrapper: W3WMapBoxWrapper? by remember {
        mutableStateOf(null)
    }
    val context = LocalContext.current
    var mapView: MapView? by remember {
        mutableStateOf(null)
    }

    AndroidView(modifier = modifier, factory = { it ->
        mapView = MapView(it)
        w3wMapsWrapper = W3WMapBoxWrapper(
            context,
            mapView!!.getMapboxMap(),
            wrapper,
        ).setLanguage("en")

        w3wMapsWrapper?.let { w3wMapsWrapper ->
            //click even on existing w3w added markers on the map.
            w3wMapsWrapper.onMarkerClicked {
                Log.i(TAG, "clicked: ${it.words}")
            }

            //REQUIRED
            mapView!!.getMapboxMap().addOnMapIdleListener {
                //...

                //needed to draw the 3x3m grid on the map
                w3wMapsWrapper.updateMap()
            }

            //REQUIRED
            mapView!!.getMapboxMap().addOnCameraChangeListener {
                //...

                //needed to draw the 3x3m grid on the map
                w3wMapsWrapper.updateMove()
            }

            mapView!!.getMapboxMap().addOnMapClickListener { latLng ->
                //..

                //example of how to select a 3x3m w3w square using lat/lng
                w3wMapsWrapper.selectAtCoordinates(
                    latLng.latitude(),
                    latLng.longitude(), onSuccess = {
                        onMapClick.invoke(it)
                        mapBoxMoveCamera(
                            mapView!!,
                            Point.fromLngLat(it.coordinates.lng, it.coordinates.lat)
                        )
                    }
                )
                true
            }
        }

        mapView!!
    })

    w3wMapsWrapper?.let { wrapper ->
        suggestion?.let { suggestion ->
            //Update search w3w marker
            if (wrapper.getSelectedMarker()?.coordinates?.lat != suggestion.coordinates.lat &&
                wrapper.getSelectedMarker()?.coordinates?.lng != suggestion.coordinates.lng
            ) {
                wrapper.selectAtSuggestionWithCoordinates(suggestion, onSuccess = {
                    mapView?.let { mapView ->
                        mapBoxMoveCamera(
                            mapView,
                            Point.fromLngLat(it.coordinates.lng, it.coordinates.lat)
                        )
                    }
                })
            }
        }

        addMarker?.let {
            if (wrapper.findMarkerByCoordinates(it.latitude, it.longitude) == null) {
                wrapper.addMarkerAtCoordinates(it.latitude, it.longitude, W3WMarkerColor.RED)
            }
        }
    }
}

fun mapBoxMoveCamera(mapView: MapView, point: Point) {
    // define camera position
    val cameraPosition = CameraOptions.Builder()
        .zoom(19.0)
        .center(point)
        .build()
    // set camera position
    mapView.getMapboxMap().setCamera(cameraPosition)
}