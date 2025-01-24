package com.what3words.samples.multiple.component

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.what3words.androidwrapper.voice.VoiceProvider
import com.what3words.components.compose.wrapper.AutoSuggestConfiguration
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextField
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextFieldDefaults
import com.what3words.components.compose.wrapper.rememberW3WAutoSuggestTextFieldState
import com.what3words.components.models.VoiceScreenType
import com.what3words.components.text.W3WAutoSuggestEditText
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import com.what3words.samples.multiple.BuildConfig
import com.what3words.samples.multiple.R

data class AutoTextFieldUIState(
    val voiceProvider: VoiceProvider,
    var suggestion: SuggestionWithCoordinates?,
    var isClearFocus: Boolean = false)

@Composable
fun AutoTextField(
    modifier: Modifier,
    onItemSelected: (SuggestionWithCoordinates?) -> Unit,
    uiState: AutoTextFieldUIState
) {
    val TAG = "AutoTextFieldView"
    var editText: W3WAutoSuggestEditText? by remember {
        mutableStateOf(null)
    }

    ConstraintLayout(modifier = modifier.padding(24.dp)) {
        val (ref) = createRefs()

        //  what3words autosuggest text component for compose
        val w3wTextFieldState =
            rememberW3WAutoSuggestTextFieldState().apply {
                returnCoordinates(true)
                voiceEnabled(enabled = true, type = VoiceScreenType.AnimatedPopup)
                uiState.suggestion?.let { display(it) }
                focus(Coordinates(51.520847, -0.195521))
            }

        W3WAutoSuggestTextField(
            modifier = modifier.padding(bottom = 12.dp),
            ref = ref,
            state = w3wTextFieldState,
            configuration = AutoSuggestConfiguration.Api(apiKey = BuildConfig.W3W_API_KEY, voiceProvider = uiState.voiceProvider),
            suggestionPicker = null,
            correctionPicker = null,
            invalidAddressMessageView = null,
            errorView = null,
            onSuggestionWithCoordinates = {
                if (it != null) {
                    Log.d(TAG, "W3WAutoSuggestTextField ${it.words}")
                    onItemSelected(it)
                } else {
                    onItemSelected(null)
                }
            },
            onW3WAutoSuggestEditTextReady = {
                editText = it
            },
            themes = W3WAutoSuggestTextFieldDefaults.themes(
                autoSuggestEditTextTheme = R.style.W3WAutoSuggestEditTextDayNightTheme,
                autoSuggestPickerTheme = R.style.W3WAutoSuggestPickerDayNight,
                autoSuggestErrorMessageTheme = R.style.W3WAutoSuggestErrorMessageDayNight,
                autoSuggestInvalidAddressMessageTheme = R.style.W3WAutoSuggestErrorMessageDayNight
            )
        )
    }

    if(uiState.isClearFocus) {
        if(uiState.suggestion == null) {
            editText?.text?.clear()
        }
        editText?.clearFocus()
        uiState.isClearFocus = false
    }
}