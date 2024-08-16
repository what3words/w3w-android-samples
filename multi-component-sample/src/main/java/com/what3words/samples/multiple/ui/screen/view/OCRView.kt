package com.what3words.samples.multiple.ui.screen.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import com.what3words.androidwrapper.What3WordsAndroidWrapper
import com.what3words.design.library.ui.components.What3wordsAddressListItemDefaults
import com.what3words.design.library.ui.theme.w3wTypography
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import com.what3words.ocr.components.R
import com.what3words.ocr.components.models.W3WOcrWrapper
import com.what3words.ocr.components.ui.W3WOcrScanner
import com.what3words.ocr.components.ui.W3WOcrScannerDefaults


@Composable
fun OcrView(
    ocrWrapper: W3WOcrWrapper,
    dataProvider: What3WordsAndroidWrapper,
    scanScreenVisible: Boolean, onScanScreenVisibleChange: (Boolean) -> Unit,
    onSuggestionScanned: (SuggestionWithCoordinates) -> (Unit)
) {
    val options = remember {
        AutosuggestOptions().apply {
            focus = Coordinates(51.520847, -0.195521)
        }
    }

    AnimatedVisibility(
        visible = scanScreenVisible,
        modifier = Modifier.zIndex(Float.MAX_VALUE),
        enter = expandVertically(
            animationSpec = tween(
                750
            ),
        ),
        exit = shrinkVertically(
            animationSpec = tween(
                750
            )
        )
    ) {
        W3WOcrScanner(
            ocrWrapper,
            dataProvider = dataProvider,
            options = options,
            returnCoordinates = true,
            //optional if you want to override any string of the scanner composable, to allow localisation and accessibility.
            scannerStrings = W3WOcrScannerDefaults.defaultStrings(
                scanStateFoundTitle = stringResource(id = R.string.scan_state_found),
            ),
            //optional if you want to override any colors of the scanner composable.
            scannerColors = W3WOcrScannerDefaults.defaultColors(
                bottomDrawerBackground = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            //optional if you want to override any text styles.
            scannerTextStyles = W3WOcrScannerDefaults.defaultTextStyles(
                stateTextStyle = MaterialTheme.typography.titleMedium,
            ),
            //optional if you want to override any colors of the scanned list item composable.
            suggestionColors = What3wordsAddressListItemDefaults.defaultColors(
                background = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            //optional if you want to override any text styles of the scanned list item composable.
            suggestionTextStyles = What3wordsAddressListItemDefaults.defaultTextStyles(
                wordsTextStyle = MaterialTheme.w3wTypography.titleMediumSemibold
            ),
            onError = {
                onScanScreenVisibleChange(false)
            },
            onDismiss = {
                onScanScreenVisibleChange(false)
            },
            onSuggestionSelected = {
                onSuggestionScanned(it)
                onScanScreenVisibleChange(false)
            })
    }
}