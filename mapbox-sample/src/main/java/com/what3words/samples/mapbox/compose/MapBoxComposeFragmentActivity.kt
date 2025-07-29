package com.what3words.samples.mapbox.compose

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
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.components.maps.models.W3WMarkerColor
import com.what3words.components.maps.models.W3WZoomOption
import com.what3words.components.maps.views.W3WMap
import com.what3words.components.maps.views.W3WMapFragment
import com.what3words.components.maps.views.W3WMapboxMapFragment
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.samples.mapbox.BuildConfig
import com.what3words.samples.mapbox.databinding.ActivityComposeMapFragmentBinding

/**
    This sample demonstrates how to use the [W3WMapboxMapFragment] in a Compose activity.

    Note: Since you are creating a new app or a new screen, you can always opt to use our [W3WMapboxMapFragment],
    the main advantage is that all the required events to draw the grid are done under the hood,
    resulting in less boilerplate code and still having access to the [MapboxMap] instance to apply standard customization ( i.e. map types, etc.)
 **/
class MapBoxComposeFragmentActivity : FragmentActivity(), W3WMapFragment.OnMapReadyCallback {
    private val TAG = MapBoxComposeFragmentActivity::class.qualifiedName

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
                            val mapboxFragment =
                                view.fragmentContainerView.getFragment<W3WMapboxMapFragment>()
                            mapboxFragment.initialize(W3WApiTextDataSource.create(this, BuildConfig.W3W_API_KEY),this)
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

        //set the callback for when a square is selected
        map.onSquareSelected({ selectedSquare, selectedByTouch, isMarked ->
            Log.i(TAG, "square selected: ${selectedSquare.words}, byTouch: $selectedByTouch, isMarked: $isMarked")
        }, {
            Log.e(TAG, "error: ${it.message}")
        })

        //example how to use W3WMap features (check interface for documentation).
        map.addMarkerAtWords(
            "filled.count.soap",
            W3WMarkerColor.BLUE,
            W3WZoomOption.CENTER_AND_ZOOM,
            {
                Log.i(TAG, "added ${it.words} at ${it.center?.lat}, ${it.center?.lng}")
                //select after adding a marker successfully
                map.selectAtAddress(it, W3WZoomOption.NONE)
            }, {
                Toast.makeText(this@MapBoxComposeFragmentActivity, "${it.message}", Toast.LENGTH_LONG).show()
            }
        )

        //if you want to access the mapbox map instance inside W3WMapboxMapFragment do the following
        (map as? W3WMapboxMapFragment.Map)?.mapBoxMap()?.loadStyleUri(Style.MAPBOX_STREETS)
    }
}