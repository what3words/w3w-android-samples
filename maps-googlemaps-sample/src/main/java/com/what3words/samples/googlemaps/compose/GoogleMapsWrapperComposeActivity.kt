package com.what3words.samples.googlemaps.compose

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.FragmentActivity
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
import com.what3words.design.library.ui.theme.W3WTheme
import com.what3words.samples.googlemaps.BuildConfig
import com.what3words.samples.googlemaps.databinding.ActivityComposeMapWrapperBinding

class GoogleMapsWrapperComposeActivity : FragmentActivity(), OnMapReadyCallback {
    private lateinit var w3wMapsWrapper: W3WGoogleMapsWrapper
    private val TAG = GoogleMapsWrapperComposeActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            W3WTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AndroidViewBinding(factory = { inflater, parent, attachToParent ->
                        val view =
                            ActivityComposeMapWrapperBinding.inflate(inflater, parent, attachToParent)
                        val supportMapFragment =
                            view.fragmentContainerView.getFragment<SupportMapFragment>()
                        supportMapFragment.getMapAsync(
                            this@GoogleMapsWrapperComposeActivity
                        )
                        view
                    })
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        val textDataSource = W3WApiTextDataSource.create(this, BuildConfig.W3W_API_KEY)
        this.w3wMapsWrapper = W3WGoogleMapsWrapper(
            this,
            map,
            textDataSource,
        ).setLanguage(W3WRFC5646Language.EN_GB)

        w3wMapsWrapper.addMarkerAtWords(
            "index.home.raft",
            W3WMarkerColor.BLUE,
            { address ->
                Log.i(
                    TAG,
                    "added $address"
                )
                address.center?.let { center ->
                    val cameraPosition = CameraPosition.Builder()
                        .target(LatLng(center.lat, center.lng))
                        .zoom(16f)
                        .build()
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }

            }, {
                Toast.makeText(
                    this@GoogleMapsWrapperComposeActivity,
                    "$it",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        //click even on existing w3w added markers on the map.
        w3wMapsWrapper.onMarkerClicked {
            Log.i("TAG", "clicked: ${it.words}")
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