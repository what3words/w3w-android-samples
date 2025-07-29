package com.what3words.samples.multiple

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.core.datasource.image.W3WImageDataSource
import com.what3words.core.datasource.text.W3WTextDataSource
import com.what3words.samples.multiple.data.LocationSourceImpl

class MultiComponentsViewModelFactory(
    private val textDataSource: W3WTextDataSource,
    private val locationSourceImpl: LocationSourceImpl,
    private val w3WImageDataSource: W3WImageDataSource,
    private val dataProvider: What3WordsV3
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MultiComponentsViewModel::class.java)) {
            return MultiComponentsViewModel(
                textDataSource,
                locationSourceImpl,
                w3WImageDataSource,
                dataProvider
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}