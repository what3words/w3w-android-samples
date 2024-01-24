package com.what3words.samples.multiple

import androidx.lifecycle.ViewModel
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlinx.coroutines.flow.MutableStateFlow

class MultiComponentsViewModel : ViewModel() {
    var selectedSuggestion = MutableStateFlow<SuggestionWithCoordinates?>(null)
}
