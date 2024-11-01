package com.what3words.samples.mapbox.compose

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.components.maps.models.W3WMarkerColor
import com.what3words.components.maps.wrappers.W3WMapBoxWrapper
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.samples.mapbox.BuildConfig

/**
This sample demonstrates how to use the [W3WMapBoxWrapper] in a Compose activity.

Note: Since you are trying to add what3words support to an existing map app/screen, you can shoulder always opt to use our [W3WMapBoxWrapper],
which will work along side any other location APIs that you may be using, it needs a bit more set uo
but it's more flexible and you can have more control over the map and the what3words features.
 **/
class MapBoxComposeWrapperActivity : ComponentActivity() {
    private lateinit var w3wMapsWrapper: W3WMapBoxWrapper
    private val TAG = MapBoxComposeWrapperActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AndroidView(factory = {
                        val view = MapView(it)
                        enableWhat3wordsFeatures(view)
                        view
                    })
                }
            }
        }
    }

    private fun enableWhat3wordsFeatures(mapView: MapView) {
        val textDataSource = W3WApiTextDataSource.create(this, BuildConfig.W3W_API_KEY)
        this.w3wMapsWrapper = W3WMapBoxWrapper(
            this,
            mapView.getMapboxMap(),
            textDataSource,
        ).setLanguage(W3WRFC5646Language.EN_GB)

        w3wMapsWrapper.addMarkerAtWords(
            "filled.count.soap",
            W3WMarkerColor.BLUE,
            {
                Log.i(
                    TAG,
                    "added ${it.words} at ${it.center?.lat}, ${it.center?.lng}"
                )
                val cameraOptions = CameraOptions.Builder()
                    .center(Point.fromLngLat(it.center?.lng ?: 0.0, it.center?.lat ?: 0.0))
                    .zoom(19.0)
                    .build()
                mapView.getMapboxMap().setCamera(cameraOptions)
            }, {
                Toast.makeText(
                    this@MapBoxComposeWrapperActivity,
                    "${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        //REQUIRED
        mapView.getMapboxMap().addOnMapIdleListener {
            //...

            //needed to draw the 3x3m grid on the map
            this.w3wMapsWrapper.updateMap()
        }

        //REQUIRED
        mapView.getMapboxMap().addOnCameraChangeListener {
            //...

            //needed to draw the 3x3m grid on the map
            this.w3wMapsWrapper.updateMove()
        }

        mapView.getMapboxMap().addOnMapClickListener { latLng ->
            //check if a marker was clicked, if so you can have the option to select the square that's marked or
            //select a new square at the clicked lat/lng
            this.w3wMapsWrapper.checkIfMarkerClicked(latLng) { clickedMarker ->
                if (clickedMarker == null) {
                    //example of how to select a 3x3m w3w square using lat/lng
                    this.w3wMapsWrapper.selectAtCoordinates(
                        W3WCoordinates(
                            latLng.latitude(),
                            latLng.longitude()
                        ),
                        onSuccess = {
                            Log.i(
                                TAG,
                                "selected square: ${it.words}, byTouch: true, isMarked: false"
                            )
                        },
                        onError = {
                            Log.e(TAG, "error: ${it.message}")
                        }
                    )
                } else {
                    this.w3wMapsWrapper.selectAtAddress(
                        clickedMarker,
                        onSuccess = {
                            Log.i(
                                TAG,
                                "selected square: ${clickedMarker.words}, byTouch: true, isMarked: true"
                            )
                        },
                        onError = {
                            Log.e(TAG, "error: ${it.message}")
                        }
                    )
                }
            }
            true
        }
    }
}
