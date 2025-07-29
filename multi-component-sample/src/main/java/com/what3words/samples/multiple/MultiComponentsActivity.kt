package com.what3words.samples.multiple

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface.LATIN
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.design.library.ui.theme.W3WTheme
import com.what3words.ocr.components.datasource.W3WMLKitImageDataSource
import com.what3words.samples.multiple.data.LocationSourceImpl
import com.what3words.samples.multiple.home.HomeScreen

class MultiComponentsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel: MultiComponentsViewModel by viewModels {
            MultiComponentsViewModelFactory(
                textDataSource = W3WApiTextDataSource.create(this, BuildConfig.W3W_API_KEY),
                locationSourceImpl = LocationSourceImpl(this),
                w3WImageDataSource = W3WMLKitImageDataSource.create(
                    context = this,
                    recognizerOptions = LATIN
                ),
                dataProvider = What3WordsV3(
                    BuildConfig.W3W_API_KEY,
                    this
                )
            )
        }

        setContent {
            W3WTheme {
                val uiState by viewModel.uiState.collectAsState()

                HomeScreen(
                    textDataSource = viewModel.w3WTextDataSource,
                    imageDataSource = viewModel.w3WImageDataSource,
                    locationSource = viewModel.locationSource,
                    dataProvider = viewModel.dataProvider,
                    uiState = uiState,
                    onSuggestionChanged = viewModel::onSuggestionChanged,
                    onMapTypeChange = viewModel::onMapTypeChange,
                    onMarkerActionEvent = viewModel::onMarkerActionEvent
                )
            }
        }
    }
}