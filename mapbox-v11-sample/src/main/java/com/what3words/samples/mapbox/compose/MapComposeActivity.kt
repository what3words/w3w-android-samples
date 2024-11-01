package com.what3words.samples.mapbox.compose

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
import com.mapbox.maps.extension.compose.MapboxMap
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.components.compose.maps.MapProvider
import com.what3words.components.compose.maps.W3WMapComponent
import com.what3words.components.compose.maps.W3WMapDefaults
import com.what3words.components.compose.maps.W3WMapManager
import com.what3words.components.compose.maps.providers.mapbox.W3WMapBoxDrawer
import com.what3words.samples.mapbox.v11.BuildConfig

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

                    // Sample for using W3WMapComponent with an existing MapBox
                    // Uncomment the following line to use
//                    W3WMapComponentWithExistingMapBox()
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
                    textDataSource = W3WApiTextDataSource.create(context, BuildConfig.W3W_API_KEY),
                )
            )
        }

        W3WMapComponent(
            modifier = Modifier.fillMaxSize(),
            mapManager = mapManager,
            mapProvider = MapProvider.MAPBOX
        )
    }

    @Composable
    fun W3WMapComponentWithExistingMapBox() {
        val context = LocalContext.current

        val mapManager by remember {
            mutableStateOf(
                W3WMapManager(
                    textDataSource = W3WApiTextDataSource.create(context, BuildConfig.W3W_API_KEY),
                )
            )
        }

        val state by mapManager.state.collectAsState()

        MapboxMap(
            modifier = Modifier.fillMaxSize(),
        ) {
            W3WMapBoxDrawer(state, W3WMapDefaults.defaultMapConfig())
        }
    }

}