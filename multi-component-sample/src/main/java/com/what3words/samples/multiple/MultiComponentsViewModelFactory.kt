package com.what3words.samples.multiple

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.what3words.core.datasource.text.W3WTextDataSource

class MultiComponentsViewModelFactory(
    private val textDataSource: W3WTextDataSource,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MultiComponentsViewModel::class.java)) {
            return MultiComponentsViewModel(textDataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}