package com.what3words.samples.ocr

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface.LATIN
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface.LATIN_AND_CHINESE
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface.LATIN_AND_DEVANAGARI
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface.LATIN_AND_JAPANESE
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface.LATIN_AND_KOREAN

class ComposeOcrScanSamplePopupViewModel : ViewModel() {

    var results: String? by mutableStateOf(null)
    var selectedMLKitLibrary: Int by mutableIntStateOf(LATIN)
    var availableMLKitLanguages: List<Int> by mutableStateOf(
        listOf(LATIN, LATIN_AND_CHINESE, LATIN_AND_DEVANAGARI, LATIN_AND_JAPANESE, LATIN_AND_KOREAN)
    )

    fun getLibName(lib: Int) : String {
        return when(lib) {
            LATIN_AND_CHINESE -> "Latin and Chinese"
            LATIN_AND_DEVANAGARI -> "Latin and Devanagari"
            LATIN_AND_JAPANESE -> "Latin and Japanese"
            LATIN_AND_KOREAN -> "Latin and Korean"
            else -> "Latin"
        }
    }
}
