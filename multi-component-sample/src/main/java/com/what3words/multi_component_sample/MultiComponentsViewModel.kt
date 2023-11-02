package com.what3words.multi_component_sample

import androidx.lifecycle.ViewModel
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlinx.coroutines.flow.MutableStateFlow

class MultiComponentsViewModel : ViewModel() {
    var selectedSuggestion = MutableStateFlow<SuggestionWithCoordinates?>(null)
}
