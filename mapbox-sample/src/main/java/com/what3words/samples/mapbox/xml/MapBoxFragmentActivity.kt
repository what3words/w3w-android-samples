package com.what3words.samples.mapbox.xml

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.what3words.components.maps.models.W3WMarkerColor
import com.what3words.components.maps.models.W3WZoomOption
import com.what3words.components.maps.views.W3WMap
import com.what3words.components.maps.views.W3WMapFragment
import com.what3words.components.maps.views.W3WMapboxMapFragment
import com.what3words.samples.mapbox.BuildConfig
import com.what3words.samples.mapbox.R
import com.what3words.samples.mapbox.databinding.ActivityMapFragmentBinding

/**
    This sample demonstrates how to use the [W3WMapboxMapFragment] in a XML activity.

    Note: Since you are creating a new app or a new screen, you can always opt to use our [W3WMapboxMapFragment],
    the main advantage is that all the required events to draw the grid are done under the hood,
    resulting in less boilerplate code and still having access to the [MapboxMap] instance to apply standard customization ( i.e. map types, etc.)
 **/
class MapBoxFragmentActivity : FragmentActivity(), W3WMapFragment.OnMapReadyCallback {
    private lateinit var binding: ActivityMapFragmentBinding
    private val TAG = MapBoxFragmentActivity::class.qualifiedName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapFragmentBinding.inflate(layoutInflater)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as W3WMapboxMapFragment

        //W3WMapboxMapFragment needs W3WMapboxMapFragment.OnFragmentReadyCallback to receive the callback when MapboxMap and W3W features are ready to be used
        mapFragment.apiKey(BuildConfig.W3W_API_KEY, this)
        setContentView(binding.root)
    }

    override fun onMapReady(map: W3WMap) {
        //set language to get all the 3wa in the desired language (default english)
        map.setLanguage("en")

        //set the callback for when a square is selected
        map.onSquareSelected(
            onSuccess = { selectedSquare, selectedByTouch, isMarked ->
                Log.i(TAG, "square selected: ${selectedSquare.words}, byTouch: $selectedByTouch, isMarked: $isMarked")
            },
            onError = { Log.e(TAG, "error: ${it.key}, ${it.message}") })

        //example how to use W3WMap features (check interface for documentation).
        map.addMarkerAtWords(
            "filled.count.soap",
            W3WMarkerColor.BLUE,
            W3WZoomOption.CENTER_AND_ZOOM,
            {
                Log.i(TAG, "added ${it.words} at ${it.coordinates.lat}, ${it.coordinates.lng}")
                //optionally select after adding a marker successfully
                map.selectAtSquare(it, W3WZoomOption.NONE)
            }, {
                Toast.makeText(
                    this@MapBoxFragmentActivity,
                    "${it.key}, ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        //if you want to access the mapbox map instance inside W3WMapboxMapFragment do the following
        (map as? W3WMapboxMapFragment.Map)?.mapBoxMap()?.let {
            it.loadStyleUri(Style.MAPBOX_STREETS)
        }
    }
}