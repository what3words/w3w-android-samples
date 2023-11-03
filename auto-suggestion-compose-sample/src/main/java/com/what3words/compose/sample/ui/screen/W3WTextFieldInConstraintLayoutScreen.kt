package com.what3words.compose.sample.ui.screen

import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.what3words.components.compose.wrapper.AutoSuggestConfiguration
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextField
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextFieldDefaults
import com.what3words.components.compose.wrapper.rememberW3WAutoSuggestTextFieldState
import com.what3words.components.picker.W3WAutoSuggestCorrectionPicker
import com.what3words.components.picker.W3WAutoSuggestPicker
import com.what3words.components.text.W3WAutoSuggestEditText
import com.what3words.compose.sample.BuildConfig
import com.what3words.compose.sample.R
import com.what3words.compose.sample.databinding.CustomCorrectionPickerBinding
import com.what3words.compose.sample.databinding.CustomSuggestionPickerBinding

/**
 * A jetpack compose screen that's intends to be replica of the W3WAutoSuggestComponent Sample(view-specific) app.
 * Using the [W3WAutoSuggestTextField] as the jetpack compose's substitute for [W3WAutoSuggestEditText]
 * **/
@Composable
fun W3WTextFieldInConstraintLayoutScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedInfo: String? by remember { mutableStateOf(null) }
    var customPicker: W3WAutoSuggestPicker? by remember { mutableStateOf(value = null) }
    var customCorrectionPicker: W3WAutoSuggestCorrectionPicker? by remember { mutableStateOf(value = null) }
    var customErrorView: AppCompatTextView? by remember {
        mutableStateOf(value = null)
    }
    val customizeAutoSuggestSettingsScreenState: CustomizeAutoSuggestSettingsScreenState =
        remember { CustomizeAutoSuggestSettingsScreenState(context = context) }

    ConstraintLayout(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(horizontal = dimensionResource(id = R.dimen.normal_100))
    ) {
        val (headerTxtRef, w3wTextFieldRef, selectedInfoTxtRef, settingsColumnRef, customPickerRef, customCorrectionPickerRef) = createRefs()

        // welcome header
        Text(
            text = stringResource(id = R.string.txt_label_welcome_to_autosuggest_sample),
            modifier = Modifier
                .padding(
                    top = dimensionResource(id = R.dimen.normal_125),
                    bottom = dimensionResource(
                        id = R.dimen.small_25
                    )
                )
                .constrainAs(ref = headerTxtRef) {
                    linkTo(start = parent.start, end = parent.end)
                    top.linkTo(anchor = parent.top)
                    width = Dimension.fillToConstraints
                },
            style = MaterialTheme.typography.body2.copy(color = LocalContentColor.current.copy(alpha = 0.5f))
        )

        //  what3words autosuggest text component for compose
        val w3wTextFieldState =
            rememberW3WAutoSuggestTextFieldState().apply {
                nResults(n = 4)
            }

        W3WAutoSuggestTextField(
            modifier = Modifier.constrainAs(ref = w3wTextFieldRef) {
                linkTo(start = parent.start, end = parent.end)
                top.linkTo(anchor = headerTxtRef.bottom)
            },
            state = w3wTextFieldState,
            ref = w3wTextFieldRef,
            configuration = AutoSuggestConfiguration.Api(apiKey = BuildConfig.W3W_API_KEY),
            suggestionPicker = customPicker,
            correctionPicker = customCorrectionPicker,
            invalidAddressMessageView = customErrorView,
            errorView = customErrorView,
            onSuggestionWithCoordinates = {
                if (it != null) {
                    selectedInfo = context.resources.getString(
                        R.string.suggestion_info_placeholder,
                        it.words,
                        it.country,
                        it.nearestPlace,
                        if (it.distanceToFocusKm == null) "N/A" else "${it.distanceToFocusKm}km",
                        it.coordinates?.lat.toString(),
                        it.coordinates?.lng.toString()
                    )
                } else {
                    selectedInfo = ""
                }
            },
            themes = W3WAutoSuggestTextFieldDefaults.themes(
                autoSuggestEditTextTheme = R.style.W3WAutoSuggestEditTextDayNightTheme,
                autoSuggestPickerTheme = R.style.W3WAutoSuggestPickerDayNight,
                autoSuggestErrorMessageTheme = R.style.W3WAutoSuggestErrorMessageDayNight,
                autoSuggestInvalidAddressMessageTheme = R.style.W3WAutoSuggestErrorMessageDayNight
            )
        )

        // selected address info text
        Text(
            text = selectedInfo ?: "",
            modifier = Modifier
                .defaultMinSize(minHeight = dimensionResource(id = R.dimen.large_200))
                .constrainAs(ref = selectedInfoTxtRef) {
                    linkTo(start = parent.start, end = parent.end)
                    top.linkTo(w3wTextFieldRef.bottom)
                    width = Dimension.fillToConstraints
                },
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.body1.copy(color = LocalContentColor.current.copy(alpha = 0.7f))
        )


        // group of components to configure the settings of the auto suggest edit text
        CustomizeAutoSuggestSettingsScreen(
            autoSuggestTextFieldState = w3wTextFieldState,
            state = customizeAutoSuggestSettingsScreenState,
            modifier = Modifier
                .padding(top = dimensionResource(id = R.dimen.normal_200))
                .fillMaxSize()
                .constrainAs(ref = settingsColumnRef) {
                    linkTo(start = parent.start, end = parent.end)
                    linkTo(top = selectedInfoTxtRef.bottom, bottom = parent.bottom)
                }
        )

        // custom autosuggest picker
        if (customizeAutoSuggestSettingsScreenState.useCustomSuggestionPicker) {
            AndroidViewBinding(factory = CustomSuggestionPickerBinding::inflate,
                modifier = Modifier.constrainAs(ref = customPickerRef) {
                    linkTo(start = w3wTextFieldRef.start, end = w3wTextFieldRef.end)
                    top.linkTo(anchor = w3wTextFieldRef.bottom, margin = 10.dp)
                }, update = {
                    customPicker = suggestionPicker
                })
        } else {
            customPicker = null
        }

        // custom correction picker
        if (customizeAutoSuggestSettingsScreenState.useCustomCorrectionPicker) {
            AndroidViewBinding(factory = CustomCorrectionPickerBinding::inflate,
                modifier = Modifier.constrainAs(ref = customCorrectionPickerRef) {
                    linkTo(start = w3wTextFieldRef.start, end = w3wTextFieldRef.end)
                    top.linkTo(anchor = w3wTextFieldRef.bottom, margin = 10.dp)
                }, update = {
                    customCorrectionPicker = correctionPicker
                })
        } else {
            customCorrectionPicker = null
        }

        // custom error message view
        if (customizeAutoSuggestSettingsScreenState.useCustomErrorMessageView) {
            AndroidView(factory = {
                AppCompatTextView(it)
            }, update = {
                customErrorView = it
            })
        } else {
            customErrorView = null
        }
    }
}