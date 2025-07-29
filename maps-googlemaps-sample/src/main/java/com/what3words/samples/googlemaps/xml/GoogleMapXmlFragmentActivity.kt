package com.what3words.samples.googlemaps.xml

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.components.maps.models.W3WMarkerColor
import com.what3words.components.maps.models.W3WZoomOption
import com.what3words.components.maps.views.W3WGoogleMapFragment
import com.what3words.components.maps.views.W3WMap
import com.what3words.components.maps.views.W3WMapFragment
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.samples.googlemaps.BuildConfig
import com.what3words.samples.googlemaps.R
import com.what3words.samples.googlemaps.databinding.ActivityMapFragmentBinding

class GoogleMapXmlFragmentActivity : FragmentActivity(), W3WMapFragment.OnMapReadyCallback {
    private lateinit var binding: ActivityMapFragmentBinding
    private val TAG = GoogleMapXmlFragmentActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapFragmentBinding.inflate(layoutInflater)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as W3WGoogleMapFragment

        //W3WGoogleMapFragment needs W3WGoogleMapFragment.OnMapReadyCallback to receive the callback when GoogleMap and W3W features are ready to be used
        mapFragment.initialize(W3WApiTextDataSource.create(this, BuildConfig.W3W_API_KEY),this)
        setContentView(binding.root)
    }

    override fun onMapReady(map: W3WMap) {
        //set language to get all the 3wa in the desired language (default english)
        map.setLanguage(W3WRFC5646Language.EN_GB)

        //set the callback for when a square is selected
        map.onSquareSelected(
            onSuccess = { selectedSquare, selectedByTouch, isMarked ->
                Log.i(TAG, "square selected: ${selectedSquare.words}, byTouch: $selectedByTouch, isMarked: $isMarked")
            },
            onError = {
                Log.e(TAG, "error: ${it.message}")
            }
        )

        //example how to use W3WMap features (check interface for documentation).
        map.addMarkerAtWords(
            "filled.count.soap",
            W3WMarkerColor.BLUE,
            W3WZoomOption.CENTER_AND_ZOOM,
            {
                Log.i(
                    TAG,
                    "added ${it.words} at ${it.center?.lat}, ${it.center?.lng}"
                )
                //optionally select after adding a marker successfully
                map.selectAtAddress(it, W3WZoomOption.NONE)
            }, {
                Toast.makeText(
                    this@GoogleMapXmlFragmentActivity,
                    "${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        //if you want to access the google map instance inside W3WGoogleMapFragment do the following
        (map as? W3WGoogleMapFragment.Map)?.googleMap()?.let {
            it.mapType = MAP_TYPE_NORMAL
        }
    }
}