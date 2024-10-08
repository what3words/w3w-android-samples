package com.what3words.samples.mapbox.xml

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.components.maps.models.W3WMarkerColor
import com.what3words.components.maps.wrappers.W3WMapBoxWrapper
import com.what3words.samples.mapbox.BuildConfig
import com.what3words.samples.mapbox.databinding.ActivityMapWrapperBinding

/**
    This sample demonstrates how to use the [W3WMapBoxWrapper] in a XML activity.

    Note: Since you are trying to add what3words support to an existing map app/screen, you can shoulder always opt to use our [W3WMapBoxWrapper],
    which will work along side any other location APIs that you may be using, it needs a bit more set uo
    but it's more flexible and you can have more control over the map and the what3words features.
 **/
class MapBoxXmlWrapperActivity : AppCompatActivity() {
    private lateinit var w3wMapsWrapper: W3WMapBoxWrapper
    private lateinit var binding: ActivityMapWrapperBinding
    private val TAG = MapBoxXmlWrapperActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapWrapperBinding.inflate(layoutInflater)
        binding.mapView.getMapboxMap().loadStyleUri(Style.OUTDOORS)
        setContentView(binding.root)

        val wrapper = What3WordsV3(BuildConfig.W3W_API_KEY, this)
        this.w3wMapsWrapper = W3WMapBoxWrapper(
            this,
            binding.mapView.getMapboxMap(),
            wrapper,
        ).setLanguage("en")

        w3wMapsWrapper.addMarkerAtWords(
            "filled.count.soap",
            W3WMarkerColor.BLUE,
            {
                Log.i(
                    TAG,
                    "added ${it.words} at ${it.coordinates.lat}, ${it.coordinates.lng}"
                )
                val cameraOptions = CameraOptions.Builder()
                    .center(Point.fromLngLat(it.coordinates.lng, it.coordinates.lat))
                    .zoom(19.0)
                    .build()
                binding.mapView.getMapboxMap().setCamera(cameraOptions)
            }, {
                Toast.makeText(
                    this@MapBoxXmlWrapperActivity,
                    "${it.key}, ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        //REQUIRED
        binding.mapView.getMapboxMap().addOnMapIdleListener {
            //...

            //needed to draw the 3x3m grid on the map
            this.w3wMapsWrapper.updateMap()
        }

        //REQUIRED
        binding.mapView.getMapboxMap().addOnCameraChangeListener {
            //...

            //needed to draw the 3x3m grid on the map
            this.w3wMapsWrapper.updateMove()
        }

        binding.mapView.getMapboxMap().addOnMapClickListener { latLng ->
            //check if a marker was clicked, if so you can have the option to select the square that's marked or
            //select a new square at the clicked lat/lng
            this.w3wMapsWrapper.checkIfMarkerClicked(latLng) { clickedMarker ->
                if (clickedMarker == null) {
                    //example of how to select a 3x3m w3w square using lat/lng
                    this.w3wMapsWrapper.selectAtCoordinates(
                        latLng.latitude(),
                        latLng.longitude(),
                        onSuccess = {
                            Log.i(TAG, "selected square: ${it.words}, byTouch: true, isMarked: false")
                        },
                        onError = {
                            Log.e(TAG, "error: ${it.key}, ${it.message}")
                        }
                    )
                } else {
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
            true
        }
    }
}
