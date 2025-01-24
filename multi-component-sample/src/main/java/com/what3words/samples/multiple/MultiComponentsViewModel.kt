package com.what3words.samples.multiple

import android.util.Log
import androidx.lifecycle.ViewModel
import com.what3words.components.compose.maps.MapProvider
import com.what3words.core.types.language.W3WLanguage
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MultiComponentsViewModel : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onMapTypeChange() {
        // Switch the map type
        val mapProvider = when (_uiState.value.mapProvider) {
            MapProvider.GOOGLE_MAP -> MapProvider.MAPBOX
            MapProvider.MAPBOX -> MapProvider.GOOGLE_MAP
        }
        _uiState.value = _uiState.value.copy(mapProvider = mapProvider)
    }

    fun onSuggestionChanged(suggestion: SuggestionWithCoordinates?) {
        Log.d("DUY", "HomeViewModel onSuggestionChanged: ${suggestion?.words}")
        _uiState.value = _uiState.value.copy(selectedSuggestion = suggestion)
    }

    fun onMarkerActionEvent(markerActionEvent: UiState.MarkerActionEvent) {
        _uiState.value = _uiState.value.copy(markerActionEvent = markerActionEvent)
    }
}

data class UiState(
    val mapProvider: MapProvider = MapProvider.GOOGLE_MAP,
    val selectedSuggestion: SuggestionWithCoordinates? = null,
    val language: W3WLanguage = W3WRFC5646Language.EN_GB,
    val isSupportedOcr: Boolean = false,
    val markerActionEvent: MarkerActionEvent = MarkerActionEvent.NONE
) {
    enum class MarkerActionEvent {
        ADD_MARKER,
        REMOVE_MARKER,
        NONE
    }
}
