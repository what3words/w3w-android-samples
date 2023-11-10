package com.what3words.multicomponentsample

import androidx.lifecycle.ViewModel
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlinx.coroutines.flow.MutableStateFlow

class MultiComponentsViewModel : ViewModel() {
    var selectedSuggestion = MutableStateFlow<SuggestionWithCoordinates?>(null)
}
