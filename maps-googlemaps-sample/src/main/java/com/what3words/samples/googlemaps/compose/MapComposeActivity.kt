package com.what3words.samples.googlemaps.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.maps.android.compose.DefaultMapProperties
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.components.compose.maps.W3WMapComponent
import com.what3words.components.compose.maps.W3WMapManager
import com.what3words.components.compose.maps.providers.googlemap.GoogleMapDrawer
import com.what3words.components.compose.maps.providers.googlemap.GoogleMapProvider
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.samples.googlemaps.BuildConfig

class MapComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Sample for using W3WMapComponent fully
                    W3WMapComponentApp()

                    // Sample for using W3WMapComponent with an existing GoogleMap
                    // Uncomment the following line to use
//                    W3WMapComponentWithExistingGoogleMap()
                }
            }
        }
    }


    @Composable
    fun W3WMapComponentApp() {
        val context = LocalContext.current
        val mapManager by remember {
            mutableStateOf(
                W3WMapManager(
                    textDataSource = W3WApiTextDataSource.create(context, BuildConfig.W3W_API_KEY)
                )
            )
        }

        W3WMapComponent(
            modifier = Modifier.fillMaxSize(),
            mapManager = mapManager,
            mapProvider = GoogleMapProvider()
        )
    }

    @OptIn(MapsComposeExperimentalApi::class)
    @Composable
    fun W3WMapComponentWithExistingGoogleMap() {
        val context = LocalContext.current

        val mapManager by remember {
            mutableStateOf(
                W3WMapManager(
                    textDataSource = W3WApiTextDataSource.create(context, BuildConfig.W3W_API_KEY)
                )
            )
        }

        val state by mapManager.state.collectAsState()

        GoogleMap(
            cameraPositionState = rememberCameraPositionState(),
            properties = DefaultMapProperties,
            modifier = Modifier.fillMaxSize(),
            onMapClick = { latLng ->
                mapManager.selectAtCoordinates(W3WCoordinates(latLng.latitude, latLng.longitude))
            }
        ) {
            MapEffect { map ->
                //REQUIRED
                map.setOnCameraIdleListener {
                    //needed to draw the 3x3m grid on the map
                    mapManager.updateMap()
                }

                //REQUIRED
                map.setOnCameraMoveListener {
                    //...

                    //needed to draw the 3x3m grid on the map
                    mapManager.updateMove()
                }
            }

            //REQUIRED
            //needed to draw the 3x3m grid, markers and selected square on the map
            GoogleMapDrawer(state = state)
        }
    }
}