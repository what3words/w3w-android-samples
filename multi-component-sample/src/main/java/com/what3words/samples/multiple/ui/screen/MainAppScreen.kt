package com.what3words.samples.multiple.ui.screen

import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.what3words.androidwrapper.What3WordsAndroidWrapper
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.design.library.ui.theme.W3WTheme
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import com.what3words.samples.multiple.ui.screen.view.AutoTextField
import com.what3words.samples.multiple.ui.screen.view.MapWrapperView
import com.what3words.samples.multiple.ui.screen.view.OcrView
import com.what3words.samples.multiple.ui.theme.W3WMultiComponentTheme
import com.what3words.ocr.components.models.W3WOcrWrapper
import com.what3words.samples.multiple.R

@Composable
fun MainAppScreen(
    wrapper: What3WordsV3,
    ocrWrapper: W3WOcrWrapper,
    dataProvider: What3WordsAndroidWrapper,
    selectedSuggestion: SuggestionWithCoordinates?,
    onSuggestionChanged: (SuggestionWithCoordinates?) -> (Unit)
) {
    var scanScreenVisible by rememberSaveable { mutableStateOf(false) }
    var isGGMap by rememberSaveable {
        mutableStateOf(true)
    }

    var addMarker: Location? by rememberSaveable { mutableStateOf(null) }

    W3WMultiComponentTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {

            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .fillMaxSize()
            ) {
                val (w3wTextFieldRef, mapRef, ocrRef, mapTypeRef, addMarkerRef) = createRefs()

                OcrView(
                    ocrWrapper,
                    dataProvider,
                    scanScreenVisible = scanScreenVisible,
                    onScanScreenVisibleChange = { scanScreenVisible = it },
                    onSuggestionScanned = onSuggestionChanged
                )

                MapWrapperView(
                    wrapper,
                    modifier = Modifier.constrainAs(ref = mapRef) {
                        linkTo(start = parent.start, end = parent.end)
                        top.linkTo(anchor = parent.top)
                        bottom.linkTo(anchor = parent.bottom)
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                    },
                    isGGMap = isGGMap,
                    addMarker = addMarker,
                    suggestion = selectedSuggestion,
                    onMapClicked =
                    onSuggestionChanged,
                    onAddMarkerSucceeded = {addMarker = null}
                )

                AutoTextField(modifier = Modifier
                    .constrainAs(ref = w3wTextFieldRef) {
                        linkTo(start = parent.start, end = parent.end)
                        top.linkTo(anchor = parent.top)
                        width = Dimension.fillToConstraints
                        height = Dimension.wrapContent
                    }, selectedSuggestion, onItemSelected = onSuggestionChanged)

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
                        selectedSuggestion?.let {
                            val location = Location("")
                            location.latitude = it.coordinates.lat
                            location.longitude = it.coordinates.lng
                            addMarker = location

                        }
                    },
                    backgroundColor = W3WTheme.colors.background,
                    contentColor = W3WTheme.colors.primary
                ) {
                    Icon(painterResource(R.drawable.icon_pin_drop), "Add Marker")
                }

                FloatingActionButton(
                    modifier = Modifier
                        .constrainAs(ref = mapTypeRef) {
                            start.linkTo(parent.start)
                            bottom.linkTo(anchor = ocrRef.top)
                            width = Dimension.wrapContent
                            height = Dimension.wrapContent
                        }
                        .padding(bottom = 12.dp, start = 24.dp),
                    onClick = {
                        isGGMap = !isGGMap
                    },
                    backgroundColor = W3WTheme.colors.background,
                    contentColor = W3WTheme.colors.primary
                ) {
                    Icon(painterResource(R.drawable.icon_map), "Map Type")
                }

                FloatingActionButton(
                    modifier = Modifier
                        .constrainAs(ref = ocrRef) {
                            start.linkTo(parent.start)
                            bottom.linkTo(anchor = parent.bottom)
                            width = Dimension.wrapContent
                            height = Dimension.wrapContent
                        }
                        .padding(bottom = 32.dp, start = 24.dp),
                    onClick = {
                        scanScreenVisible = true
                    },
                    backgroundColor = W3WTheme.colors.background,
                    contentColor = W3WTheme.colors.primary
                ) {
                    Icon(painterResource(R.drawable.icon_camera), "OCR Button")
                }
            }
        }
    }

}