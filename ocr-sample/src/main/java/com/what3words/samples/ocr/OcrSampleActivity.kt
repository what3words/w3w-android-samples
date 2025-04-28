package com.what3words.samples.ocr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.core.datasource.image.W3WImageDataSource
import com.what3words.core.datasource.text.W3WTextDataSource
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.options.W3WAutosuggestOptions
import com.what3words.design.library.ui.theme.W3WTheme
import com.what3words.ocr.components.datasource.W3WMLKitImageDataSource
import com.what3words.ocr.components.ui.W3WOcrScanner
import com.what3words.ocr.components.ui.rememberOcrScanManager

class OcrSampleActivity : ComponentActivity() {

    private val viewModel: OcrSampleViewModel by viewModels()

    private lateinit var w3WTextDataSource: W3WTextDataSource
    private lateinit var w3WImageDataSource: W3WImageDataSource

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        w3WTextDataSource = W3WApiTextDataSource.create(this, BuildConfig.W3W_API_KEY)

        setContent {
            W3WTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            title = {
                                Text(
                                    text = "OCR Component sample app",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        )
                    }
                ) { contentPadding ->
                    val uiState by viewModel.uiState.collectAsState()

                    LaunchedEffect(uiState.mlKit) {
                        w3WImageDataSource = W3WMLKitImageDataSource.create(
                            context = this@OcrSampleActivity,
                            recognizerOptions = uiState.mlKit.value
                        )
                    }

                    Column(modifier = Modifier.padding(contentPadding)) {
                        MLKitLibrariesDropdownMenuBox(uiState)
                        Button(modifier = Modifier.fillMaxWidth(), onClick = {
                            viewModel.showScanner(true)
                        }) {
                            Text(text = "Launch OCR scanner in screen")
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(contentPadding),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            uiState.selectedSuggestion?.let { suggestion ->
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Selected suggestion: ${suggestion.w3wAddress.words}"
                                )
                            }
                            uiState.error?.let {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Error: ${it.message}"
                                )
                            }
                        }
                    }
                    val options = remember {
                        W3WAutosuggestOptions.Builder().focus(W3WCoordinates(51.2, 1.2)).build()
                    }

                    AnimatedVisibility(
                        visible = uiState.isScanning,
                        modifier = Modifier.zIndex(Float.MAX_VALUE),
                        enter = slideInVertically(
                            animationSpec = tween(750),
                            initialOffsetY = { fullHeight -> fullHeight } // Start from below the screen
                        ),
                        exit = slideOutVertically(
                            animationSpec = tween(750),
                            targetOffsetY = { fullHeight -> fullHeight } // Exit towards the bottom
                        )
                    ) {
                        W3WOcrScanner(
                            modifier = Modifier.padding(contentPadding),
                            ocrScanManager = rememberOcrScanManager(
                                w3wImageDataSource = w3WImageDataSource,
                                w3wTextDataSource = w3WTextDataSource,
                                options = options,
                            ),
                            onDismiss = {
                                viewModel.showScanner(false)
                            },
                            onSuggestionSelected = viewModel::selectSuggestion,
                            onError = viewModel::onError,
                            onSuggestionFound = {

                            }
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MLKitLibrariesDropdownMenuBox(
        uiState: UiState
    ) {
        var expanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            ExposedDropdownMenuBox(
                modifier = Modifier.fillMaxWidth(),
                expanded = expanded,
                onExpandedChange = {
                    expanded = !expanded
                }) {
                CompositionLocalProvider(
                    LocalTextInputService provides null
                ) {
                    TextField(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        label = {
                            Text("MLKit Language library")
                        },
                        value = uiState.mlKit.mlKitName,
                        onValueChange = {
                        },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    )
                }

                ExposedDropdownMenu(
                    modifier = Modifier.fillMaxWidth(),
                    expanded = expanded,
                    onDismissRequest = { expanded = false }) {
                    uiState.availableMlKits.forEach { item ->
                        DropdownMenuItem(
                            modifier = Modifier.fillMaxWidth(),
                            text = {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = item.mlKitName
                                )
                            },
                            onClick = {
                                viewModel.changeMlKit(item)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }
    }
}