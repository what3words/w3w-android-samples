package com.what3words.samples.multiple

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.ocr.components.models.W3WOcrMLKitWrapper
import com.what3words.ocr.components.models.W3WOcrWrapper
import com.what3words.samples.multiple.ui.screen.MainAppScreen

class MultiComponentsActivity : ComponentActivity() {
    private val viewModel: MultiComponentsViewModel by viewModels()
    private lateinit var ocrWrapper: W3WOcrWrapper
    private val dataProvider by lazy {
        What3WordsV3(
            BuildConfig.W3W_API_KEY,
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ocrWrapper = W3WOcrMLKitWrapper(context = this@MultiComponentsActivity)
        val wrapper = What3WordsV3(BuildConfig.W3W_API_KEY, this)

        setContent {
            val selectedSuggestion by viewModel.selectedSuggestion.collectAsState()

            MainAppScreen(
                wrapper,
                ocrWrapper,
                true,
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