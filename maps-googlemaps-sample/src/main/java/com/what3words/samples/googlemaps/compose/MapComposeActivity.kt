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
import com.google.maps.android.compose.rememberCameraPositionState
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.components.compose.maps.MapProvider
import com.what3words.components.compose.maps.W3WMapComponent
import com.what3words.components.compose.maps.W3WMapDefaults.defaultMapConfig
import com.what3words.components.compose.maps.W3WMapManager
import com.what3words.components.compose.maps.providers.googlemap.W3WGoogleMapDrawer
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

    //In core app
//    @Composable
//    fun W3WMapComponentAppCoreApp() {
//        //ViewModel textDataSource
//
//        val state = W3WMapState()
//
//        W3WMapComponent(
//            mapProvider = MapProvider.GOOGLE_MAP,
//            state = state
//        )
//    }

    //In User with open
//    @Composable
//    fun W3WMapComponentAppUseStateWithExist() {
//        val state = W3WMapState()
//
//        GoogleMap(
//            cameraPositionState = rememberCameraPositionState(),
//            properties = DefaultMapProperties,
//            modifier = Modifier.fillMaxSize(),
//        ) {
//
//            //REQUIRED
//            //needed to draw the 3x3m grid, markers and selected square on the map
//            W3WGoogleMapDrawer(state = state, mapConfig = defaultMapConfig())
//        }
//    }


    @Composable
    fun W3WMapComponentApp() {
        val context = LocalContext.current
        val mapManager by remember {
            mutableStateOf(
                W3WMapManager(
                    textDataSource = W3WApiTextDataSource.create(context, BuildConfig.W3W_API_KEY),
                    mapProvider = MapProvider.GOOGLE_MAP
                )
            )
        }

        W3WMapComponent(
            modifier = Modifier.fillMaxSize(),
            mapManager = mapManager,
        )
    }

    @Composable
    fun W3WMapComponentWithExistingGoogleMap() {
        val context = LocalContext.current


        val mapManager by remember {
            mutableStateOf(
                W3WMapManager(
                    textDataSource = W3WApiTextDataSource.create(
                        context,
                        BuildConfig.W3W_API_KEY),
                    mapProvider = MapProvider.GOOGLE_MAP
                )
            )
        }

        val state by mapManager.state.collectAsState()
//        val cameraPositionState = rememberCameraPositionState {
//            state.cameraPosition?.let {
//                position = it.toGoogleCameraPosition()
//            }
//        }

        //REQUIRED
//        LaunchedEffect(key1 = cameraPositionState.position, cameraPositionState.isMoving) {
//            cameraPositionState.projection
//            //needed to draw the 3x3m grid on the map
//            mapManager.onCameraUpdated(cameraPositionState.toW3WMapStateCameraPosition())
//        }


        GoogleMap(
            cameraPositionState = rememberCameraPositionState(),
            properties = DefaultMapProperties,
            modifier = Modifier.fillMaxSize(),
            onMapClick = { latLng ->
                mapManager.addMarkerAtCoordinates(W3WCoordinates(latLng.latitude, latLng.longitude))
            }
        ) {

            //REQUIRED
            //needed to draw the 3x3m grid, markers and selected square on the map
            W3WGoogleMapDrawer(
                state = state,
                mapConfig = defaultMapConfig())
        }
    }
}