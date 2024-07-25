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
        val wrapper = W3WApiTextDataSource.create(this, BuildConfig.W3W_API_KEY)
        this.w3wMapsWrapper = W3WGoogleMapsWrapper(
            this,
            map,
            wrapper,
        ).setLanguage(W3WRFC5646Language.EN_GB)

        w3wMapsWrapper.addMarkerAtWords(
            "filled.count.soap",
            W3WMarkerColor.BLUE,
            { addedAddress ->
                Log.i(
                    TAG,
                    "added $addedAddress"
                )
                addedAddress.center?.let { center ->
                    val cameraPosition = CameraPosition.Builder()
                        .target(LatLng(center.lat, center.lng))
                        .zoom(19f)
                        .build()
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            }, {
                Toast.makeText(
                    this@GoogleMapsWrapperXmlActivity,
                    it.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        //click even on existing w3w added markers on the map.
        w3wMapsWrapper.onMarkerClicked {
            Log.i(TAG, "clicked: ${it.words}")
        }

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

        map.setOnMapClickListener { latLng ->
            //..

            //example of how to select a 3x3m w3w square using lat/lng
            this.w3wMapsWrapper.selectAtCoordinates(
                latLng.latitude,
                latLng.longitude
            )
        }
    }
}
