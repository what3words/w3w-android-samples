package com.what3words.samples.multiple.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.what3words.androidwrapper.What3WordsAndroidWrapper
import com.what3words.components.maps.models.W3WMarkerColor
import com.what3words.components.maps.wrappers.W3WMapWrapper
import com.what3words.core.datasource.image.W3WImageDataSource
import com.what3words.core.datasource.text.W3WTextDataSource
import com.what3words.core.types.geometry.km
import com.what3words.design.library.ui.theme.W3WTheme
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import com.what3words.samples.multiple.ui.screen.view.AutoTextField
import com.what3words.samples.multiple.ui.screen.view.AutoTextFieldUIState
import com.what3words.samples.multiple.ui.screen.view.MapWrapperView
import com.what3words.samples.multiple.ui.screen.view.OcrView

@Composable
fun MainAppScreen(
    w3WTextDataSource: W3WTextDataSource,
    w3WImageDataSource: W3WImageDataSource,
    dataProvider: What3WordsAndroidWrapper,
    isGoogleMapType: Boolean,
    selectedSuggestion: SuggestionWithCoordinates?,
    onSuggestionChanged: (SuggestionWithCoordinates?) -> (Unit)
) {
    var scanScreenVisible by rememberSaveable { mutableStateOf(false) }
    var isGGMap by rememberSaveable {
        mutableStateOf(isGoogleMapType)
    }
    var w3wMapsWrapper: W3WMapWrapper? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(key1 = w3wMapsWrapper) {
        w3wMapsWrapper?.selectAtWords("filled.count.soap", onSuccess = {
            onSuggestionChanged(it)
        })
    }

    var autoTextFieldUIState by remember(selectedSuggestion) {
        mutableStateOf(
            AutoTextFieldUIState(dataProvider.voiceProvider, selectedSuggestion, false)
        )
    }

    W3WTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {

            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .fillMaxSize()
            ) {
                val (w3wTextFieldRef, mapRef, ocrRef, mapTypeRef, addMarkerRef) = createRefs()

                OcrView(
                    w3WTextDataSource = w3WTextDataSource,
                    w3WImageDataSource = w3WImageDataSource,
                    scanScreenVisible = scanScreenVisible,
                    onScanScreenVisibleChange = { scanScreenVisible = it },
                    onSuggestionScanned = { w3wSuggestion ->
                        val suggestion = SuggestionWithCoordinates(
                            Suggestion(
                                w3wSuggestion.w3wAddress.words,
                                w3wSuggestion.w3wAddress.nearestPlace,
                                w3wSuggestion.w3wAddress.country.twoLetterCode,
                                w3wSuggestion.distanceToFocus?.km()?.toInt(),
                                w3wSuggestion.rank,
                                w3wSuggestion.w3wAddress.language.w3wCode
                            ),
                            w3wSuggestion.w3wAddress.center!!.lat,
                            w3wSuggestion.w3wAddress.center!!.lng,
                            w3wSuggestion.w3wAddress.square!!.northeast.lat,
                            w3wSuggestion.w3wAddress.square!!.northeast.lng,
                            w3wSuggestion.w3wAddress.square!!.southwest.lat,
                            w3wSuggestion.w3wAddress.square!!.southwest.lng,
                        )
                        onSuggestionChanged(suggestion)
                    }
                )

                MapWrapperView(
                    dataProvider,
                    modifier = Modifier.constrainAs(ref = mapRef) {
                        linkTo(start = parent.start, end = parent.end)
                        top.linkTo(anchor = parent.top)
                        bottom.linkTo(anchor = parent.bottom)
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                    },
                    isGGMap = isGGMap,
                    suggestion = selectedSuggestion,
                    onMapClicked = { onSuggestionChanged(w3wMapsWrapper?.getSelectedMarker()) },
                    onWrapperInitialized = { w3wMapsWrapper = it }
                )

                AutoTextField(
                    modifier = Modifier
                        .constrainAs(ref = w3wTextFieldRef) {
                            linkTo(start = parent.start, end = parent.end)
                            top.linkTo(anchor = parent.top)
                            width = Dimension.fillToConstraints
                            height = Dimension.wrapContent
                        },
                    onItemSelected = onSuggestionChanged,
                    uiState = autoTextFieldUIState
                )

                FloatingActionButton(
                    modifier = Modifier
                        .constrainAs(ref = addMarkerRef) {
                            start.linkTo(parent.start)
                            bottom.linkTo(anchor = mapTypeRef.top)
                            width = Dimension.wrapContent
                            height = Dimension.wrapContent
                        }
                        .padding(bottom = 12.dp, start = 24.dp),
                    onClick = {
                        w3wMapsWrapper?.getSelectedMarker()?.let { location ->
                            w3wMapsWrapper?.findMarkerByCoordinates(
                                location.coordinates.lat,
                                location.coordinates.lng,
                            )?.let {
                                w3wMapsWrapper?.removeMarkerAtCoordinates(
                                    location.coordinates.lat,
                                    location.coordinates.lng
                                )
                            } ?: run {
                                w3wMapsWrapper?.addMarkerAtCoordinates(
                                    location.coordinates.lat,
                                    location.coordinates.lng,
                                    W3WMarkerColor.RED
                                )
                            }
                        }
                    }
                ) {
                    Icon(Icons.Filled.PinDrop, "Add Marker")
                }

                FloatingActionButton(
                    modifier = Modifier
                        .testTag("mapTypeButton")
                        .constrainAs(ref = mapTypeRef) {
                            start.linkTo(parent.start)
                            bottom.linkTo(anchor = ocrRef.top)
                            width = Dimension.wrapContent
                            height = Dimension.wrapContent
                        }
                        .padding(bottom = 12.dp, start = 24.dp),
                    onClick = {
                        isGGMap = !isGGMap
                    }
                ) {
                    Icon(Icons.Filled.Map, "Map Type")
                }

                FloatingActionButton(
                    modifier = Modifier
                        .testTag("ocrButton")
                        .constrainAs(ref = ocrRef) {
                            start.linkTo(parent.start)
                            bottom.linkTo(anchor = parent.bottom)
                            width = Dimension.wrapContent
                            height = Dimension.wrapContent
                        }
                        .padding(bottom = 32.dp, start = 24.dp),
                    onClick = {
                        autoTextFieldUIState =
                            autoTextFieldUIState.copy().apply { isClearFocus = true }
                        scanScreenVisible = true
                    }
                ) {
                    Icon(Icons.Filled.CameraAlt, "OCR Button")
                }
            }
        }
    }
}