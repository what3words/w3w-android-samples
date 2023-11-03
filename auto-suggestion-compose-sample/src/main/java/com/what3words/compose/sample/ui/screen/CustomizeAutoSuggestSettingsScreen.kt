package com.what3words.compose.sample.ui.screen

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextFieldState
import com.what3words.compose.sample.R
import com.what3words.compose.sample.ui.components.LabelCheckBox
import com.what3words.compose.sample.ui.components.MultiLabelTextField
import com.what3words.compose.sample.ui.components.RadioGroup
import com.what3words.compose.sample.ui.components.RadioGroupState
import com.what3words.compose.sample.ui.model.VoiceOption
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates


class CustomizeAutoSuggestSettingsScreenState(context: Context) {
    var useCustomSuggestionPicker: Boolean by mutableStateOf(value = false)

    var useCustomCorrectionPicker: Boolean by mutableStateOf(value = false)

    var useCustomErrorMessageView: Boolean by mutableStateOf(value = false)

    var clipToCountry: String by mutableStateOf(value = "")

    var clipToCircle: String by mutableStateOf(value = "")

    var clipToBox: String by mutableStateOf(value = "")

    var clipToPolygon: String by mutableStateOf(value = "")

    var focus: String by mutableStateOf(value = "")

    var voiceLanguage: String by mutableStateOf(value = "en")

    var language: String by mutableStateOf(value = "")

    var voicePlaceHolder: String by mutableStateOf(value = "Say a 3 word address...")

    var placeHolder: String by mutableStateOf(value = context.getString(R.string.txt_label_3word_hint))
}

// a group of ui components that is used to configure the w3w autosuggestion settings
@Composable
fun CustomizeAutoSuggestSettingsScreen(
    autoSuggestTextFieldState: W3WAutoSuggestTextFieldState,
    state: CustomizeAutoSuggestSettingsScreenState,
    modifier: Modifier = Modifier
) {


    LaunchedEffect(
        state.placeHolder,
        state.voicePlaceHolder,
        state.voiceLanguage,
        state.language,
        block = {
            with(autoSuggestTextFieldState) {
                voicePlaceholder(placeholder = state.voicePlaceHolder)
                hint(state.placeHolder)
                voiceLanguage(state.voiceLanguage)
                language(language = state.language)
            }
        }
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // customize the autosuggest component label
        Text(
            text = stringResource(id = R.string.txt_label_customize_the_autosuggest_component),
            style = MaterialTheme.typography.body2.copy(color = LocalContentColor.current.copy(alpha = 0.5f))
        )

        // return coordinates checkbox
        LabelCheckBox(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            checked = autoSuggestTextFieldState.returnCoordinates,
            onCheckedChange = {
                autoSuggestTextFieldState.returnCoordinates(enabled = it)
            },
            text = stringResource(id = R.string.txt_label_return_coordinates)
        )

        // prefer land checkbox
        var preferLand by remember { mutableStateOf(value = false) }
        LabelCheckBox(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            checked = preferLand,
            onCheckedChange = {
                preferLand = it
                autoSuggestTextFieldState.preferLand(isPreferred = preferLand)
            },
            text = stringResource(id = R.string.txt_label_prefer_land)
        )

        // voice options radio group state
        val voiceOptionGroupState = remember {
            RadioGroupState(
                items = VoiceOption.values(),
                selectedItemIndex = 0,
                label = {
                    it.label
                }
            )
        }

        /**
         * observe the selected radio item [RadioGroupState.selected] so that you can update the [W3WAutoSuggestTextFieldState] accordingly
         *  **/
        LaunchedEffect(key1 = voiceOptionGroupState.selected, block = {
            if (voiceOptionGroupState.selected != null) {
                if (voiceOptionGroupState.selected == VoiceOption.DisableVoice) {
                    autoSuggestTextFieldState.voiceEnabled(enabled = false)
                } else {
                    autoSuggestTextFieldState.voiceEnabled(
                        enabled = true,
                        type = voiceOptionGroupState.selected!!.type!!
                    )
                }
            }
        })

        // voice option radio groupd
        RadioGroup(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            state = voiceOptionGroupState
        )

        // use custom picker
        LabelCheckBox(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            checked = state.useCustomSuggestionPicker,
            onCheckedChange = {
                state.useCustomSuggestionPicker = it
            },
            text = stringResource(id = R.string.txt_label_use_custom_picker)
        )

        // use custom error message view check box
        LabelCheckBox(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            checked = state.useCustomErrorMessageView,
            onCheckedChange = {
                state.useCustomErrorMessageView = it
            },
            text = stringResource(id = R.string.txt_label_use_custom_error_message_view)
        )

        // use custom correction picker
        LabelCheckBox(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            checked = state.useCustomCorrectionPicker,
            onCheckedChange = {
                state.useCustomCorrectionPicker = it
            },
            text = stringResource(id = R.string.txt_label_use_custom_correction_picker)
        )

        // allow invalid 3wa
        LabelCheckBox(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            checked = autoSuggestTextFieldState.allowInvalid3wa,
            onCheckedChange = {
                autoSuggestTextFieldState.allowInvalid3wa(it)
            },
            text = stringResource(id = R.string.txt_label_allow_invalid_3wa)
        )

        // allow flexible delimiters
        LabelCheckBox(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            checked = autoSuggestTextFieldState.allowFlexibleDelimiters,
            onCheckedChange = {
                autoSuggestTextFieldState.allowFlexibleDelimiters(it)
            },
            text = stringResource(id = R.string.txt_label_allow_flexible_delimiters)
        )

        // placeholder
        MultiLabelTextField(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            text = state.placeHolder,
            onTextChanged = {
                state.placeHolder = it
            },
            primaryLabel = stringResource(id = R.string.txt_label_placeholder)
        )

        // language
        MultiLabelTextField(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            text = state.language,
            onTextChanged = {
                state.language = it
            },
            primaryLabel = stringResource(id = R.string.txt_label_language),
            secondaryLabel = stringResource(id = R.string.txt_label_language_info)
        )

        // voice placeholder
        MultiLabelTextField(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            text = state.voicePlaceHolder,
            onTextChanged = {
                state.voicePlaceHolder = it
            },
            primaryLabel = stringResource(id = R.string.txt_label_voice_placeholder)
        )

        // voice language
        MultiLabelTextField(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            text = state.voiceLanguage,
            onTextChanged = {
                state.voiceLanguage = it
            },
            primaryLabel = stringResource(id = R.string.txt_label_voice_language),
            secondaryLabel = stringResource(id = R.string.txt_label_voice_language_info)
        )

        // focus
        MultiLabelTextField(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            text = state.focus,
            onTextChanged = {
                state.focus = it
                val latLong =
                    state.focus.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
                val lat = latLong.getOrNull(0)?.toDoubleOrNull()
                val lng = latLong.getOrNull(1)?.toDoubleOrNull()
                if (lat != null && lng != null) {
                    autoSuggestTextFieldState.focus(coordinates = Coordinates(lat, lng))
                } else {
                    autoSuggestTextFieldState.focus(coordinates = null)
                }
            },
            primaryLabel = stringResource(id = R.string.txt_label_focus),
            secondaryLabel = stringResource(id = R.string.txt_label_focus_info)
        )

        // clip to country 
        MultiLabelTextField(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            text = state.clipToCountry,
            onTextChanged = {
                state.clipToCountry = it
                autoSuggestTextFieldState.clipToCountry(countryCodes = state.clipToCountry.replace(
                    "\\s".toRegex(),
                    ""
                ).split(",")
                    .filter { it.isNotEmpty() })
            },
            primaryLabel = stringResource(id = R.string.txt_label_clip_to_country),
            secondaryLabel = stringResource(id = R.string.txt_label_clip_to_country_info)
        )

        // clip to circle
        MultiLabelTextField(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            text = state.clipToCircle,
            onTextChanged = {
                state.clipToCircle = it
                val latLong = state.clipToCircle.replace("\\s".toRegex(), "").split(",")
                    .filter { it.isNotEmpty() }
                val lat = latLong.getOrNull(0)?.toDoubleOrNull()
                val long = latLong.getOrNull(1)?.toDoubleOrNull()
                val km = latLong.getOrNull(2)?.toDoubleOrNull()
                if (lat != null && long != null) {
                    autoSuggestTextFieldState.clipToCircle(
                        centre = Coordinates(lat, long),
                        radius = km ?: 0.0
                    )
                } else {
                    autoSuggestTextFieldState.clipToCircle(
                        centre = null,
                        radius = null
                    )
                }
            },
            primaryLabel = stringResource(id = R.string.txt_label_clip_to_circle),
            secondaryLabel = stringResource(id = R.string.txt_label_clip_to_circle_info)
        )
        // clip to box
        MultiLabelTextField(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            text = state.clipToBox,
            onTextChanged = {
                state.clipToBox = it
                val latLong = state.clipToBox.replace("\\s".toRegex(), "").split(",")
                    .filter { it.isNotEmpty() }
                val swLat = latLong.getOrNull(0)?.toDoubleOrNull()
                val swLong = latLong.getOrNull(1)?.toDoubleOrNull()
                val neLat = latLong.getOrNull(2)?.toDoubleOrNull()
                val neLong = latLong.getOrNull(3)?.toDoubleOrNull()
                if (swLat != null && swLong != null && neLat != null && neLong != null) {
                    autoSuggestTextFieldState.clipToBoundingBox(
                        boundingBox = BoundingBox(
                            Coordinates(swLat, swLong),
                            Coordinates(neLat, neLong)
                        )
                    )
                } else {
                    autoSuggestTextFieldState.clipToBoundingBox(boundingBox = null)
                }
            },
            primaryLabel = stringResource(id = R.string.txt_label_clip_to_box),
            secondaryLabel = stringResource(id = R.string.txt_label_clip_to_box_info)
        )
        // clip to polygon
        MultiLabelTextField(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            text = state.clipToCountry,
            onTextChanged = {
                state.clipToPolygon = it
                val latLong = state.clipToPolygon.replace("\\s".toRegex(), "").split(",")
                    .filter { it.isNotEmpty() }
                val listCoordinates = mutableListOf<Coordinates>()
                if (latLong.count() % 2 == 0) {
                    for (x in 0 until latLong.count() step 2) {
                        if (latLong[x].toDoubleOrNull() != null &&
                            latLong[x + 1].toDoubleOrNull() != null
                        ) {
                            listCoordinates.add(
                                Coordinates(
                                    latLong[x].toDouble(),
                                    latLong[x + 1].toDouble()
                                )
                            )
                        }
                    }
                }
                autoSuggestTextFieldState.clipToPolygon(polygon = listCoordinates)
            },
            primaryLabel = stringResource(id = R.string.txt_label_clip_to_polygon),
            secondaryLabel = stringResource(id = R.string.txt_label_clip_to_polygon_info)
        )
    }
}