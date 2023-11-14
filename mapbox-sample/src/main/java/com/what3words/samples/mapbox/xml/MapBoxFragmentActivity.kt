package com.what3words.samples.mapbox.xml

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.maps.Style
import com.what3words.components.maps.models.W3WMarkerColor
import com.what3words.components.maps.models.W3WZoomOption
import com.what3words.components.maps.views.W3WMap
import com.what3words.components.maps.views.W3WMapboxMapFragment
import com.what3words.map.components.mapboxsample.BuildConfig
import com.what3words.map.components.mapboxsample.R
import com.what3words.map.components.mapboxsample.databinding.ActivityMapFragmentBinding

class MapBoxFragmentActivity : AppCompatActivity() , W3WMapboxMapFragment.OnMapReadyCallback {
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

        //example how to use W3WMap features (check interface for documentation).
        map.addMarkerAtWords(
            "filled.count.soap",
            W3WMarkerColor.BLUE,
            W3WZoomOption.CENTER_AND_ZOOM,
            {
                Log.i(
                    TAG,
                    "added ${it.words} at ${it.coordinates.lat}, ${it.coordinates.lng}"
                )
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