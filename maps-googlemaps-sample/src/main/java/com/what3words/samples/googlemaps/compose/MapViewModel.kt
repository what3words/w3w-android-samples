package com.what3words.samples.googlemaps.compose

import androidx.lifecycle.ViewModel
import com.what3words.core.datasource.text.W3WTextDataSource

class MapViewModel(
    val w3WTextDataSource: W3WTextDataSource,
) : ViewModel()