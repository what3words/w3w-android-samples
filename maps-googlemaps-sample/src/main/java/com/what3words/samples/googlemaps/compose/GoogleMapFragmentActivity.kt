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
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.design.library.ui.theme.W3WTheme
import com.what3words.samples.googlemaps.BuildConfig
import com.what3words.samples.googlemaps.databinding.ActivityComposeMapFragmentBinding

class GoogleMapFragmentActivity : FragmentActivity(), W3WMapFragment.OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            W3WTheme {
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
        map.setLanguage(W3WRFC5646Language.EN_GB)

        //example how to use W3WMap features (check interface for documentation).
        map.addMarkerAtWords(
            "filled.count.soap",
            W3WMarkerColor.BLUE,
            W3WZoomOption.CENTER_AND_ZOOM,
            {
                Log.i(
                    "UsingMapFragmentActivity",
                    "added $it"
                )
            }, {
                Toast.makeText(
                    this,
                    "$it",
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