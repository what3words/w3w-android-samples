package com.what3words.samples.multiple.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.what3words.androidwrapper.What3WordsAndroidWrapper
import com.what3words.components.compose.maps.W3WMapComponent
import com.what3words.components.compose.maps.W3WMapDefaults
import com.what3words.components.compose.maps.models.W3WLocationSource
import com.what3words.components.compose.maps.models.W3WMarkerColor
import com.what3words.components.compose.maps.models.W3WMarkerWithList
import com.what3words.components.compose.maps.rememberW3WMapManager
import com.what3words.core.datasource.image.W3WImageDataSource
import com.what3words.core.datasource.text.W3WTextDataSource
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import com.what3words.samples.multiple.UiState
import com.what3words.samples.multiple.component.AddMarkerDialog
import com.what3words.samples.multiple.component.AutoTextField
import com.what3words.samples.multiple.component.AutoTextFieldUIState
import com.what3words.samples.multiple.component.OcrView
import com.what3words.samples.multiple.component.RemoveAllMarkersDialog
import com.what3words.samples.multiple.component.RemoveSpecificMarkersDialog
import com.what3words.samples.multiple.util.DummyData
import com.what3words.samples.multiple.util.toSuggestionWithCoordinates
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    textDataSource: W3WTextDataSource,
    imageDataSource: W3WImageDataSource,
    locationSource: W3WLocationSource,
    dataProvider: What3WordsAndroidWrapper,
    uiState: UiState,
    onSuggestionChanged: (SuggestionWithCoordinates?) -> Unit,
    onMapTypeChange: () -> Unit,
    onMarkerActionEvent: (UiState.MarkerActionEvent) -> Unit,
) {
    var scanScreenVisible by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val isDarkTheme = isSystemInDarkTheme()
    val mapManager = rememberW3WMapManager(
        mapProvider = uiState.mapProvider
    )

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            skipHiddenState = false,
        )
    )

    val autoTextFieldUIState by remember(uiState.selectedSuggestion) {
        mutableStateOf(
            AutoTextFieldUIState(dataProvider.voiceProvider, uiState.selectedSuggestion, false)
        )
    }

    LaunchedEffect(uiState.markerActionEvent) {
        coroutineScope.launch {
            if (uiState.markerActionEvent == UiState.MarkerActionEvent.NONE) {
                bottomSheetScaffoldState.bottomSheetState.hide()
            } else {
                bottomSheetScaffoldState.bottomSheetState.expand()
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { bottomSheetScaffoldState.bottomSheetState.currentValue }.collect {
            if (it == SheetValue.PartiallyExpanded) {
                onMarkerActionEvent(UiState.MarkerActionEvent.NONE)
            }
        }
    }

    // Update the map when the selected suggestion changes
    LaunchedEffect(uiState.selectedSuggestion) {
        uiState.selectedSuggestion?.let {
            coroutineScope.launch {
                if (mapManager.getSelectedAddress()?.words != it.words) {
                    mapManager.setSelectedAt(it.words)

                    mapManager.moveToPosition(
                        coordinates = W3WCoordinates(
                            it.coordinates.lat,
                            it.coordinates.lng
                        )
                    )
                }
            }
        }
    }

    // Update the map provider accordingly when the UI state changes
    LaunchedEffect(uiState.mapProvider) {
        mapManager.setMapProvider(uiState.mapProvider)
    }

    // Update map dark theme by system dark theme
    LaunchedEffect(isDarkTheme) {
        mapManager.enableDarkMode(isDarkTheme)
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            when (uiState.markerActionEvent) {
                UiState.MarkerActionEvent.ADD_MARKER -> {
                    AddMarkerDialog(
                        onConfirmSingle = { listName, backgroundColor, slashColor ->
                            coroutineScope.launch {
                                val selectedAddress = mapManager.getSelectedAddress()
                                if (selectedAddress == null) {
                                    Toast.makeText(
                                        context,
                                        "Select a square to add marker at",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@launch
                                }

                                if (listName != null) {
                                    mapManager.addMarkerAt(
                                        words = selectedAddress.words,
                                        markerColor = W3WMarkerColor(
                                            backgroundColor,
                                            slashColor
                                        ),
                                        listName = listName
                                    )
                                } else {
                                    mapManager.addMarkerAt(
                                        words = selectedAddress.words,
                                        markerColor = W3WMarkerColor(
                                            backgroundColor,
                                            slashColor
                                        ),
                                    )
                                }
                            }
                        },
                        onConfirmBatch = { size, listName, backgroundColor, slashColor ->
                            val markers = DummyData.addresses.shuffled().take(size)
                            coroutineScope.launch {
                                mapManager.addMarkersAt(
                                    markers,
                                    listName,
                                    W3WMarkerColor(
                                        backgroundColor,
                                        slashColor
                                    )
                                )
                            }

                            Toast.makeText(context, "Added $size markers", Toast.LENGTH_SHORT)
                                .show()
                        },
                        onDismiss = {
                            onMarkerActionEvent(UiState.MarkerActionEvent.NONE)
                        }
                    )
                }

                UiState.MarkerActionEvent.REMOVE_MARKER -> {
                    val selectedAddress = uiState.selectedSuggestion
                    val markersAtSelectedAddress: List<W3WMarkerWithList> =
                        if (selectedAddress == null) {
                            emptyList()
                        } else {
                            mapManager.getMarkersAt(
                                W3WCoordinates(
                                    selectedAddress.coordinates.lat,
                                    selectedAddress.coordinates.lng
                                )
                            )
                        }


                    if (markersAtSelectedAddress.isEmpty()) {
                        RemoveAllMarkersDialog(
                            onDismiss = {
                                coroutineScope.launch {
                                    bottomSheetScaffoldState.bottomSheetState.hide()
                                }
                            },
                            onConfirm = {
                                coroutineScope.launch {
                                    mapManager.removeAllMarkers()
                                    bottomSheetScaffoldState.bottomSheetState.hide()
                                }
                            }
                        )
                    } else {
                        RemoveSpecificMarkersDialog(
                            markers = markersAtSelectedAddress,
                            onDismiss = {
                                coroutineScope.launch {
                                    bottomSheetScaffoldState.bottomSheetState.hide()
                                }
                            },
                            onConfirm = { markersToRemove ->
                                coroutineScope.launch {
                                    markersToRemove.forEach { markerWithList ->
                                        mapManager.removeMarkerAt(
                                            markerWithList.marker.words,
                                            markerWithList.listName
                                        )
                                    }
                                    bottomSheetScaffoldState.bottomSheetState.hide()
                                }
                            }
                        )
                    }
                }

                else -> {}
            }
        },
        sheetPeekHeight = 0.dp
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .fillMaxSize()
        ) {
            val (w3wTextFieldRef, mapRef, ocrRef, mapTypeRef, addMarkerRef) = createRefs()

            OcrView(
                w3WTextDataSource = textDataSource,
                w3WImageDataSource = imageDataSource,
                scanScreenVisible = scanScreenVisible,
                onScanScreenVisibleChange = { scanScreenVisible = it },
                onSuggestionScanned = { w3wSuggestion ->
                    onSuggestionChanged(w3wSuggestion.toSuggestionWithCoordinates())
                }
            )

            W3WMapComponent(
                textDataSource = textDataSource,
                modifier = Modifier.constrainAs(ref = mapRef) {
                    linkTo(start = parent.start, end = parent.end)
                    top.linkTo(anchor = parent.top)
                    bottom.linkTo(anchor = parent.bottom)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                },
                layoutConfig = W3WMapDefaults.defaultLayoutConfig(
                    contentPadding = PaddingValues(
                        top = 70.dp,
                        start = 16.dp,
                        end = 4.dp,
                        bottom = 4.dp
                    )
                ),
                locationSource = locationSource,
                mapConfig = W3WMapDefaults.defaultMapConfig(
                    buttonConfig = W3WMapDefaults.ButtonConfig(
                        isRecallFeatureEnabled = true,
                        isMapSwitchFeatureEnabled = true,
                        isMyLocationFeatureEnabled = true
                    )
                ),
                mapManager = mapManager,
                onSelectedSquareChanged = {
                    onSuggestionChanged(
                        it.toSuggestionWithCoordinates()
                    )
                }
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

            MarkerFunctionExpandingFabMenu(
                modifier = Modifier
                    .constrainAs(ref = addMarkerRef) {
                        start.linkTo(parent.start)
                        bottom.linkTo(anchor = mapTypeRef.top)
                        width = Dimension.wrapContent
                        height = Dimension.wrapContent
                    }
                    .padding(bottom = 12.dp, start = 24.dp),
                onAddMarker = {
                    onMarkerActionEvent(UiState.MarkerActionEvent.ADD_MARKER)
                },
                onRemoveMarker = {
                    onMarkerActionEvent(UiState.MarkerActionEvent.REMOVE_MARKER)
                },
            )

            FloatingActionButton(
                modifier = Modifier
                    .testTag("mapTypeButton")
                    .constrainAs(ref = mapTypeRef) {
                        start.linkTo(parent.start)
                        bottom.linkTo(ocrRef.top)
                        width = Dimension.wrapContent
                        height = Dimension.wrapContent
                    }
                    .padding(bottom = 12.dp, start = 24.dp),
                onClick = onMapTypeChange
            ) {
                Icon(imageVector = Icons.Default.Map, "Map Type")
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
                    scanScreenVisible = true
                }
            ) {
                Icon(Icons.Filled.CameraAlt, "OCR Button")
            }
        }
    }
}

@Composable
private fun MarkerFunctionExpandingFabMenu(
    modifier: Modifier = Modifier,
    onAddMarker: () -> Unit,
    onRemoveMarker: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = { isExpanded = !isExpanded }
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Menu,
                    contentDescription = if (isExpanded) "Close Menu" else "Open Menu"
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandHorizontally(
                    expandFrom = Alignment.Start,
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = shrinkHorizontally(
                    shrinkTowards = Alignment.Start,
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallFloatingActionButton(onClick = onAddMarker) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add 1 marker at selected address"
                        )
                    }
                    SmallFloatingActionButton(onClick = onRemoveMarker) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Remove markers at selected address"
                        )
                    }
                }
            }
        }
    }
}