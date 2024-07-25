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
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.design.library.ui.theme.W3WTheme
import com.what3words.samples.mapbox.BuildConfig

class MapBoxComposeWrapperActivity : ComponentActivity() {
    private lateinit var w3wMapsWrapper: W3WMapBoxWrapper
    private val TAG = MapBoxComposeWrapperActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            W3WTheme {
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
            { address ->

                Log.i(
                    TAG,
                    "added $address"
                )
                address.center?.let { center ->
                    val cameraOptions = CameraOptions.Builder()
                        .center(Point.fromLngLat(center.lng, center.lat))
                        .zoom(18.5)
                        .build()
                    mapView.getMapboxMap().setCamera(cameraOptions)
                }
            }, {
                Toast.makeText(
                    this@MapBoxComposeWrapperActivity,
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
            //..

            //example of how to select a 3x3m w3w square using lat/lng
            this.w3wMapsWrapper.selectAtCoordinates(
                latLng.latitude(),
                latLng.longitude()
            )
            true
        }
    }
}
