package com.what3words.samples.googlemaps.compose

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.components.maps.models.W3WMarkerColor
import com.what3words.components.maps.wrappers.W3WGoogleMapsWrapper
import com.what3words.samples.googlemaps.BuildConfig

class GoogleMapComposeLibraryActivity : ComponentActivity() {
    private val TAG = GoogleMapComposeLibraryActivity::class.qualifiedName
    private lateinit var w3wMapsWrapper: W3WGoogleMapsWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val singapore = LatLng(51.520847, -0.195521)
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(singapore, 10f)
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        // grid and zoomed in pins will not show over buildings so we disable them, but this is optional depending on your use case
                        properties = MapProperties(isBuildingEnabled = false)
                    ) {
                        //EXPERIMENTAL access to raw GoogleMap, check: https://github.com/googlemaps/android-maps-compose#obtaining-access-to-the-raw-googlemap-experimental
                        MapEffect { map ->
                           enableWhat3wordsFeatures(map)
                        }

                        //Your other Markers/different APIs, i.e GooglePlacesAPI
                        Marker(
                            state = rememberMarkerState(position = singapore),
                            title = "filled.count.soap",
                            snippet = "office"
                        )
                    }
                }
            }
        }
    }

    private fun enableWhat3wordsFeatures(map: GoogleMap) {
        val wrapper = What3WordsV3(BuildConfig.W3W_API_KEY, this)
        this.w3wMapsWrapper = W3WGoogleMapsWrapper(
            this,
            map,
            wrapper,
        ).setLanguage("en")

//        example grid working with night style json for google maps generate here: https://mapstyle.withgoogle.com/
//        p0.setMapStyle(
//            MapStyleOptions.loadRawResourceStyle(
//                this, R.raw.night_style
//            )
//        )
//
//        w3wMapsWrapper.setGridColor(GridColor.LIGHT)

        w3wMapsWrapper.addMarkerAtWords(
            "index.home.raft",
            W3WMarkerColor.BLUE,
            {
                Log.i(
                    "UsingMapFragmentActivity",
                    "added ${it.words} at ${it.coordinates.lat}, ${it.coordinates.lng}"
                )
                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(it.coordinates.lat, it.coordinates.lng))
                    .zoom(19f)
                    .build()
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }, {
                Toast.makeText(
                    this@GoogleMapComposeLibraryActivity,
                    "${it.key}, ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        //REQUIRED
        map.setOnCameraIdleListener {
            //...

            //needed to draw the 3x3m grid on the map
            this.w3wMapsWrapper.updateMap()
        }

        //REQUIRED
        map.setOnCameraMoveListener {
            //...

            //needed to draw the 3x3m grid on the map
            this.w3wMapsWrapper.updateMove()
        }

        //In GoogleMaps onMapClickListener and MarkerClickListener are separate events that do not propagate to each other,
        //so it needs to be handled separately, when setOnMapClickListener is triggered means that there was not marker clicked.
        //and you can optionally select the unmarked square
        map.setOnMapClickListener { latLng ->
            //..

            //example of how to select a 3x3m w3w square using lat/lng
            this.w3wMapsWrapper.selectAtCoordinates(
                latLng.latitude,
                latLng.longitude,
                onSuccess = {
                    Log.i(TAG, "selected square: ${it.words}, byTouch: true, isMarked: false")
                },
                onError = {
                    Log.e(TAG, "error: ${it.key}, ${it.message}")
                }
            )
        }

        //if there was a marker clicked then it will be handled here and you can optionally select the marked square
        w3wMapsWrapper.onMarkerClicked { clickedMarker ->
            this.w3wMapsWrapper.selectAtSuggestionWithCoordinates(
                clickedMarker, onSuccess = {
                    Log.i(TAG, "selected square: ${clickedMarker.words}, byTouch: true, isMarked: true")
                },
                onError = {
                    Log.e(TAG, "error: ${it.key}, ${it.message}")
                }
            )
        }
    }
}