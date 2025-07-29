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
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.components.maps.models.W3WMarkerColor
import com.what3words.components.maps.wrappers.W3WGoogleMapsWrapper
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.language.W3WRFC5646Language
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
        val textDataSource = W3WApiTextDataSource.create(this, BuildConfig.W3W_API_KEY)
        this.w3wMapsWrapper = W3WGoogleMapsWrapper(
            this,
            map,
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
                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(it.center?.lat ?: 0.0, it.center?.lng ?: 0.0))
                    .zoom(19f)
                    .build()
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }, {
                Toast.makeText(
                    this@GoogleMapsWrapperXmlActivity,
                    "${it.message}",
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
                W3WCoordinates(latLng.latitude, latLng.longitude),
                onSuccess = {
                    Log.i(TAG, "selected square: ${it.words}, byTouch: true, isMarked: false")
                },
                onError = {
                    Log.e(TAG, "error: ${it.message}")
                }
            )
        }

        //if there was a marker clicked then it will be handled here and you can optionally select the marked square
        w3wMapsWrapper.onMarkerClicked { clickedMarker ->
            this.w3wMapsWrapper.selectAtAddress(
                clickedMarker, onSuccess = {
                    Log.i(TAG, "selected square: ${clickedMarker.words}, byTouch: true, isMarked: true")
                },
                onError = {
                    Log.e(TAG, "error: ${it.message}")
                }
            )
        }
    }
}
