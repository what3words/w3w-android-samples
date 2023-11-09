package com.what3words.multi_component_sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface.LATIN
import com.what3words.androidwrapper.What3WordsAndroidWrapper
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.multi_component_sample.ui.screen.MainAppScreen
import com.what3words.ocr.components.models.W3WOcrMLKitWrapper
import com.what3words.ocr.components.models.W3WOcrWrapper

class MultiComponentsActivity : ComponentActivity() {
    private val viewModel: MultiComponentsViewModel by viewModels()
    private lateinit var ocrWrapper: W3WOcrWrapper
    private val dataProvider: What3WordsAndroidWrapper by lazy {
        What3WordsV3(
            BuildConfig.W3W_API_KEY,
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ocrWrapper = W3WOcrMLKitWrapper(context = this@MultiComponentsActivity, LATIN)
        val wrapper = What3WordsV3(BuildConfig.W3W_API_KEY, this)

        setContent {
            val selectedSuggestion by viewModel.selectedSuggestion.collectAsState()

            MainAppScreen(
                wrapper,
                ocrWrapper,
                dataProvider,
                selectedSuggestion = selectedSuggestion,
                onSuggestionChanged = {
                    viewModel.selectedSuggestion.value = it
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::ocrWrapper.isInitialized) ocrWrapper.stop()
    }
}