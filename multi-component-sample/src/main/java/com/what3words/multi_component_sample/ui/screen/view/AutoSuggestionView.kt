package com.what3words.multi_component_sample.ui.screen.view

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import com.what3words.components.compose.wrapper.AutoSuggestConfiguration
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextField
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextFieldDefaults
import com.what3words.components.compose.wrapper.rememberW3WAutoSuggestTextFieldState
import com.what3words.components.models.VoiceScreenType
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import com.what3words.multi_component_sample.BuildConfig
import com.what3words.multi_component_sample.R

@Composable
fun AutoTextField(
    modifier: Modifier,
    suggestion: SuggestionWithCoordinates?,
    onSelectedItem: (SuggestionWithCoordinates?) -> Unit
) {
    val TAG = "Auto-suggest TextField View"

    ConstraintLayout(modifier = modifier) {
        val (ref) = createRefs()
        suggestion?.let {
            Log.d(TAG, "suggestion " + suggestion.words)
        }

        //  what3words autosuggest text component for compose
        val w3wTextFieldState =
            rememberW3WAutoSuggestTextFieldState().apply {
                nResults(n = 4)
                voiceLanguage("en")
                returnCoordinates(true)
                voiceEnabled(enabled = true, type = VoiceScreenType.AnimatedPopup)
                suggestion?.let { display(it) }
            }

        W3WAutoSuggestTextField(
            modifier = modifier,
            ref = ref,
            state = w3wTextFieldState,
            configuration = AutoSuggestConfiguration.Api(apiKey = BuildConfig.W3W_API_KEY),
            suggestionPicker = null,
            correctionPicker = null,
            invalidAddressMessageView = null,
            errorView = null,
            onSuggestionWithCoordinates = {
                if (it != null) {
                    Log.d(TAG, "W3WAutoSuggestTextField ${it.words}")
                    onSelectedItem(it)
                } else {
                    onSelectedItem(null)
                }
            },
            themes = W3WAutoSuggestTextFieldDefaults.themes(
                autoSuggestEditTextTheme = R.style.W3WAutoSuggestEditTextDayNightTheme,
                autoSuggestPickerTheme = R.style.W3WAutoSuggestPickerDayNight,
                autoSuggestErrorMessageTheme = R.style.W3WAutoSuggestErrorMessageDayNight,
                autoSuggestInvalidAddressMessageTheme = R.style.W3WAutoSuggestErrorMessageDayNight
            )
        )
    }
}