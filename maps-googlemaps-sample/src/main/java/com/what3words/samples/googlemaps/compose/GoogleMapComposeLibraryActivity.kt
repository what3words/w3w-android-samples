package com.what3words.samples.googlemaps.compose

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.components.maps.models.W3WMarkerColor
import com.what3words.components.maps.wrappers.W3WGoogleMapsWrapper
import com.what3words.maps.google_maps_sample.BuildConfig
import com.what3words.samples.googlemaps.ui.theme.W3wandroidcomponentsmapsTheme

class GoogleMapComposeLibraryActivity : ComponentActivity() {
    private lateinit var w3wMapsWrapper: W3WGoogleMapsWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            W3wandroidcomponentsmapsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val singapore = LatLng(1.35, 103.87)
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(singapore, 10f)
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                    ) {
                        //EXPERIMENTAL access to raw GoogleMap, check: https://github.com/googlemaps/android-maps-compose#obtaining-access-to-the-raw-googlemap-experimental
                        MapEffect { map ->
                           enableWhat3wordsFeatures(map)
                        }

                        //Your other Markers/different APIs, i.e GooglePlacesAPI
                        Marker(
                            state = MarkerState(position = singapore),
                            title = "Singapore",
                            snippet = "Marker in Singapore"
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
                    .zoom(16f)
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

        //click even on existing w3w added markers on the map.
        w3wMapsWrapper.onMarkerClicked {
            Log.i("UsingMapWrapperActivity", "clicked: ${it.words}")
        }

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

        map.setOnMapClickListener { latLng ->
            //..

            //example of how to select a 3x3m w3w square using lat/lng
            this.w3wMapsWrapper.selectAtCoordinates(
                latLng.latitude,
                latLng.longitude
            )
        }
    }
}