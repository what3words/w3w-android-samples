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
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL
import com.what3words.components.maps.models.W3WMarkerColor
import com.what3words.components.maps.models.W3WZoomOption
import com.what3words.components.maps.views.W3WGoogleMapFragment
import com.what3words.components.maps.views.W3WMap
import com.what3words.components.maps.views.W3WMapFragment
import com.what3words.samples.googlemaps.BuildConfig
import com.what3words.samples.googlemaps.databinding.ActivityComposeMapFragmentBinding

class GoogleMapFragmentActivity : FragmentActivity(), W3WMapFragment.OnMapReadyCallback {
    private val TAG = GoogleMapFragmentActivity::class.qualifiedName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AndroidViewBinding(
                        factory = { inflater, parent, attachToParent ->
                            val view = ActivityComposeMapFragmentBinding.inflate(
                                inflater,
                                parent,
                                attachToParent
                            )
                            val googleMapFragment =
                                view.fragmentContainerView.getFragment<W3WGoogleMapFragment>()
                            googleMapFragment.apiKey(
                                BuildConfig.W3W_API_KEY,
                                this@GoogleMapFragmentActivity
                            )
                            view
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    override fun onMapReady(map: W3WMap) {
        //set language to get all the 3wa in the desired language (default english)
        map.setLanguage("en")

        //example how to use W3WMap features (check interface for documentation).
        map.addMarkerAtWords(
            "filled.count.soap",
            W3WMarkerColor.BLUE,
            W3WZoomOption.CENTER_AND_ZOOM,
            onSuccess = {
                Log.i(
                    "UsingMapFragmentActivity",
                    "added ${it.words} at ${it.coordinates.lat}, ${it.coordinates.lng}"
                )
                //optionally select after adding a marker successfully
                map.selectAtSquare(it, W3WZoomOption.NONE)
            },
            onError = {
                Toast.makeText(
                    this,
                    "${it.key}, ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        //set the callback for when a square is selected
        map.onSquareSelected(
            onSuccess = { selectedSquare, selectedByTouch, isMarked ->
                Log.i(TAG, "square selected: ${selectedSquare.words}, byTouch: $selectedByTouch, isMarked: $isMarked")
            },
            onError = {
                Log.e(TAG, "error: ${it.key}, ${it.message}")
            }
        )

        //if you want to access the google map instance inside W3WGoogleMapFragment do the following
        (map as? W3WGoogleMapFragment.Map)?.googleMap()?.let {
            it.mapType = MAP_TYPE_NORMAL
        }
    }
}