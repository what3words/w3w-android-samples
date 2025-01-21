package com.what3words.samples.ocr

import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface.LATIN
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface.LATIN_AND_CHINESE
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface.LATIN_AND_DEVANAGARI
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface.LATIN_AND_JAPANESE
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface.LATIN_AND_KOREAN
import com.what3words.core.types.common.W3WError
import com.what3words.core.types.domain.W3WSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OcrSampleViewModel : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun changeMlKit(mlKit: MlKit) {
        _uiState.update {
            it.copy(mlKit = mlKit)
        }
    }

    fun selectSuggestion(suggestion: W3WSuggestion) {
        _uiState.update {
            it.copy(selectedSuggestion = suggestion, isScanning = false)
        }
    }

    fun showScanner(shouldShow: Boolean) {
        _uiState.update {
            it.copy(isScanning = shouldShow)
        }
    }

    fun onError(error: W3WError) {
        _uiState.update {
            it.copy(error = error, isScanning = false)
        }
    }
}

data class UiState(
    val availableMlKits: List<MlKit> = listOf(
        MlKit(LATIN, "Latin"),
        MlKit(LATIN_AND_CHINESE, "Latin and Chinese"),
        MlKit(LATIN_AND_DEVANAGARI, "Latin and Devanagari"),
        MlKit(LATIN_AND_JAPANESE, "Latin and Japanese"),
        MlKit(LATIN_AND_KOREAN, "Latin and Korean")
    ),
    val mlKit: MlKit = availableMlKits[0],
    val isScanning: Boolean = false,
    val selectedSuggestion: W3WSuggestion? = null,
    val error: W3WError? = null
)

data class MlKit(val value: Int, val mlKitName: String)