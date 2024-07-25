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
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.components.maps.models.W3WMarkerColor
import com.what3words.components.maps.wrappers.W3WGoogleMapsWrapper
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.design.library.ui.theme.W3WTheme
import com.what3words.samples.googlemaps.BuildConfig

class GoogleMapComposeLibraryActivity : ComponentActivity() {
    private lateinit var w3wMapsWrapper: W3WGoogleMapsWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            W3WTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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
        val textDataSource = W3WApiTextDataSource.create(this, BuildConfig.W3W_API_KEY)
        this.w3wMapsWrapper = W3WGoogleMapsWrapper(
            this,
            map,
            textDataSource,
        ).setLanguage(W3WRFC5646Language.EN_GB)

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
            { address ->
                Log.i(
                    "UsingMapFragmentActivity",
                    "added $address"
                )
                address.center?.let { center ->
                    val cameraPosition = CameraPosition.Builder()
                        .target(LatLng(center.lat, center.lng))
                        .zoom(16f)
                        .build()
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }

            }, {
                Toast.makeText(
                    this@GoogleMapComposeLibraryActivity,
                    "$it",
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