package com.what3words.samples.googlemaps.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.what3words.core.datasource.text.W3WTextDataSource

class MapViewModelFactory(
    private val textDataSource: W3WTextDataSource,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(
                textDataSource,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}