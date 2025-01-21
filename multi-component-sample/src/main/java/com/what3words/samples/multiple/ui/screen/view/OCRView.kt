package com.what3words.samples.multiple.ui.screen.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import com.what3words.core.datasource.image.W3WImageDataSource
import com.what3words.core.datasource.text.W3WTextDataSource
import com.what3words.core.types.domain.W3WSuggestion
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.options.W3WAutosuggestOptions
import com.what3words.design.library.ui.components.What3wordsAddressListItemDefaults
import com.what3words.design.library.ui.theme.w3wTypography
import com.what3words.ocr.components.R
import com.what3words.ocr.components.ui.W3WOcrScanner
import com.what3words.ocr.components.ui.W3WOcrScannerDefaults
import com.what3words.ocr.components.ui.rememberOcrScanManager


@Composable
fun OcrView(
    w3WImageDataSource: W3WImageDataSource,
    w3WTextDataSource: W3WTextDataSource,
    scanScreenVisible: Boolean, onScanScreenVisibleChange: (Boolean) -> Unit,
    onSuggestionScanned: (W3WSuggestion) -> (Unit)
) {

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
            ocrScanManager = rememberOcrScanManager(
                w3wImageDataSource = w3WImageDataSource,
                w3wTextDataSource = w3WTextDataSource,
                options = W3WAutosuggestOptions.Builder()
                    .focus(W3WCoordinates(51.520847, -0.195521))
                    .includeCoordinates(true)
                    .build(),
            ),
            onDismiss = {
                onScanScreenVisibleChange(false)
            },
            onSuggestionSelected = {
                onSuggestionScanned(it)
                onScanScreenVisibleChange(false)
            },
            onError = {
                onScanScreenVisibleChange(false)
            },
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
        )
    }
}