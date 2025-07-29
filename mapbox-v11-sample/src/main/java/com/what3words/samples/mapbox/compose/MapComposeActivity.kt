package com.what3words.samples.mapbox.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.design.library.ui.theme.W3WTheme
import com.what3words.samples.mapbox.compose.ui.home.HomeScreen
import com.what3words.samples.mapbox.compose.ui.overlaymap.ExistingMapWithDrawerScreen
import com.what3words.samples.mapbox.compose.ui.standalone.W3WMapComponentScreen
import com.what3words.samples.mapbox.v11.BuildConfig

class MapComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: MapViewModel by viewModels {
            MapViewModelFactory(
                textDataSource = W3WApiTextDataSource.create(this, BuildConfig.W3W_API_KEY)
            )
        }

        setContent {
            W3WTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(navController)
                    }
                    composable("first") {
                        W3WMapComponentScreen(
                            textDataSource = viewModel.w3WTextDataSource
                        )
                    }
                    composable("second") {
                        ExistingMapWithDrawerScreen(
                            textDataSource = viewModel.w3WTextDataSource
                        )
                    }
                }
            }
        }
    }
}

