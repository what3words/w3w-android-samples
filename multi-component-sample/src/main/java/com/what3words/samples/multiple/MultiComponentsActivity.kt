package com.what3words.samples.multiple

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface.LATIN
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.core.datasource.image.W3WImageDataSource
import com.what3words.core.datasource.text.W3WTextDataSource
import com.what3words.ocr.components.datasource.W3WMLKitImageDataSource
import com.what3words.samples.multiple.ui.screen.MainAppScreen

class MultiComponentsActivity : ComponentActivity() {
    private val viewModel: MultiComponentsViewModel by viewModels()
    private lateinit var w3WImageDataSource: W3WImageDataSource
    private lateinit var w3WTextDataSource: W3WTextDataSource
    private lateinit var dataProvider: What3WordsV3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        w3WTextDataSource = W3WApiTextDataSource.create(this, BuildConfig.W3W_API_KEY)
        w3WImageDataSource = W3WMLKitImageDataSource.create(
            context = this,
            recognizerOptions = LATIN
        )
        dataProvider = What3WordsV3(
            BuildConfig.W3W_API_KEY,
            this
        )

        setContent {
            val selectedSuggestion by viewModel.selectedSuggestion.collectAsState()

            MainAppScreen(
                w3WTextDataSource,
                w3WImageDataSource,
                dataProvider,
                true,
                selectedSuggestion = selectedSuggestion,
                onSuggestionChanged = {
                    viewModel.selectedSuggestion.value = it
                }
            )
        }
    }
}