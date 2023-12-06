package com.what3words.samples.multiple.ui.screen.view

import android.content.Context
import android.provider.Settings
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
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.components.maps.wrappers.W3WGoogleMapsWrapper
import com.what3words.components.maps.wrappers.W3WMapBoxWrapper
import com.what3words.components.maps.wrappers.W3WMapWrapper
import com.what3words.javawrapper.response.SuggestionWithCoordinates

@Composable
fun MapWrapperView(
    wrapper: What3WordsV3,
    modifier: Modifier,
    isGGMap: Boolean,
    suggestion: SuggestionWithCoordinates?,
    onMapClicked: () -> (Unit),
    onWrapperInitialized: (W3WMapWrapper) -> (Unit)
) {
    if (isGGMap) {
        GoogleMapView(
            wrapper,
            modifier = modifier,
            suggestion = suggestion,
            onMapClicked = onMapClicked,
            onWrapperInitialized
        )
    } else {
        MapBoxView(
            wrapper,
            modifier = modifier,
            suggestion = suggestion,
            onMapClicked = onMapClicked,
            onWrapperInitialized
        )
    }
}

@Composable
private fun GoogleMapView(
    wrapper: What3WordsV3,
    modifier: Modifier,
    suggestion: SuggestionWithCoordinates?,
    onMapClicked: () -> (Unit),
    onWrapperInitialized: (W3WMapWrapper) -> (Unit),
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

    var googleMap: GoogleMap? by remember {
        mutableStateOf(null)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
    ) {
        //EXPERIMENTAL access to raw GoogleMap, check: https://github.com/googlemaps/android-maps-compose#obtaining-access-to-the-raw-googlemap-experimental
        MapEffect { map ->
            googleMap = map
            w3wMapsWrapper = W3WGoogleMapsWrapper(
                context,
                map,
                wrapper,
            )

            w3wMapsWrapper?.let {
                onWrapperInitialized(it)

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
                            if (shouldAnimate(context)) {
                                map.animateCamera(
                                    CameraUpdateFactory.newCameraPosition(
                                        cameraPosition
                                    )
                                )
                            } else {
                                map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                            }
                            onMapClicked.invoke()
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
                        if (shouldAnimate(context)) {
                            googleMap?.animateCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    cameraPosition
                                )
                            )
                        } else {
                            googleMap?.moveCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    cameraPosition
                                )
                            )
                        }
                    })
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
    onMapClicked: () -> (Unit),
    onWrapperInitialized: (W3WMapWrapper) -> (Unit),
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
        mapView?.let {
            it.contentDescription = "MapBoxView"
        }
        w3wMapsWrapper = W3WMapBoxWrapper(
            context,
            mapView!!.getMapboxMap(),
            wrapper,
        )

        w3wMapsWrapper?.let { w3wMapsWrapper ->
            onWrapperInitialized(w3wMapsWrapper)

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
                        onMapClicked.invoke()
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
    }
}

fun mapBoxMoveCamera(mapView: MapView, point: Point) {
    // define camera position
    val cameraPosition = CameraOptions.Builder()
        .zoom(19.0)
        .center(point)
        .build()
    // set camera position
    mapView.getMapboxMap().flyTo(
        cameraPosition,
        mapAnimationOptions {
            duration(3000)
        }
    )
}

private fun shouldAnimate(context: Context): Boolean {
    return Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.TRANSITION_ANIMATION_SCALE
    ) > 0 && Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.WINDOW_ANIMATION_SCALE
    ) > 0
}