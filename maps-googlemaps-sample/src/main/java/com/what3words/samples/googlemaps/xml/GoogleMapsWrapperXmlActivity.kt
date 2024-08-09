package com.what3words.samples.googlemaps.xml

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.components.maps.models.W3WMarkerColor
import com.what3words.components.maps.wrappers.W3WGoogleMapsWrapper
import com.what3words.samples.googlemaps.BuildConfig
import com.what3words.samples.googlemaps.R
import com.what3words.samples.googlemaps.databinding.ActivityMapWrapperBinding


class GoogleMapsWrapperXmlActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var w3wMapsWrapper: W3WGoogleMapsWrapper
    private lateinit var binding: ActivityMapWrapperBinding
    private val TAG = GoogleMapsWrapperXmlActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapWrapperBinding.inflate(layoutInflater)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setContentView(binding.root)
    }

    override fun onMapReady(map: GoogleMap) {
        // grid and zoomed in pins will not show over buildings so we disable them, but this is optional depending on your use case
        map.isBuildingsEnabled = false
        val wrapper = What3WordsV3(BuildConfig.W3W_API_KEY, this)
        this.w3wMapsWrapper = W3WGoogleMapsWrapper(
            this,
            map,
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
                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(it.coordinates.lat, it.coordinates.lng))
                    .zoom(19f)
                    .build()
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }, {
                Toast.makeText(
                    this@GoogleMapsWrapperXmlActivity,
                    "${it.key}, ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        //REQUIRED
        map.setOnCameraIdleListener {
            //...

            //needed to draw the 3x3m grid on the map
            this.w3wMapsWrapper.updateMap()
        }

        //REQUIRED
        map.setOnCameraMoveListener {
            //...

            //needed to draw the 3x3m grid on the map
            this.w3wMapsWrapper.updateMove()
        }

        //In GoogleMaps onMapClickListener and MarkerClickListener are separate events that do not propagate to each other,
        //so it needs to be handled separately, when setOnMapClickListener is triggered means that there was not marker clicked.
        //and you can optionally select the unmarked square
        map.setOnMapClickListener { latLng ->
            //..

            //example of how to select a 3x3m w3w square using lat/lng
            this.w3wMapsWrapper.selectAtCoordinates(
                latLng.latitude,
                latLng.longitude,
                onSuccess = {
                    Log.i(TAG, "selected square: ${it.words}, byTouch: true, isMarked: false")
                },
                onError = {
                    Log.e(TAG, "error: ${it.key}, ${it.message}")
                }
            )
        }

        //if there was a marker clicked then it will be handled here and you can optionally select the marked square
        w3wMapsWrapper.onMarkerClicked { clickedMarker ->
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
}
