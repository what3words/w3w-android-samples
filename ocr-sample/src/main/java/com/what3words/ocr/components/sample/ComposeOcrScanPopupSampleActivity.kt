package com.what3words.ocr.components.sample

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import com.what3words.androidwrapper.What3WordsAndroidWrapper
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.design.library.ui.components.NavigationBarScaffold
import com.what3words.design.library.ui.components.SuggestionWhat3wordsDefaults
import com.what3words.design.library.ui.theme.W3WTheme
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import com.what3words.ocr.components.R
import com.what3words.ocr.components.extensions.serializable
import com.what3words.ocr.components.models.W3WOcrMLKitWrapper
import com.what3words.ocr.components.models.W3WOcrWrapper
import com.what3words.ocr.components.ui.BaseOcrScanActivity
import com.what3words.ocr.components.ui.MLKitOcrScanActivity
import com.what3words.ocr.components.ui.W3WOcrScanner
import com.what3words.ocr.components.ui.W3WOcrScannerDefaults

class ComposeOcrScanPopupSampleActivity : ComponentActivity() {
    private val viewModel: ComposeOcrScanSamplePopupViewModel by viewModels()
    private lateinit var ocrWrapper: W3WOcrWrapper
    private val dataProvider: What3WordsAndroidWrapper by lazy {
        What3WordsV3(
            BuildConfig.W3W_API_KEY,
            this@ComposeOcrScanPopupSampleActivity
        )
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when {
                //registerForActivityResult success with result
                result.resultCode == Activity.RESULT_OK && result.data?.hasExtra(BaseOcrScanActivity.SUCCESS_RESULT_ID) == true -> {
                    val suggestion =
                        result.data!!.serializable<SuggestionWithCoordinates>(BaseOcrScanActivity.SUCCESS_RESULT_ID)
                    if (suggestion != null) viewModel.results =
                        ("${suggestion.words}, ${suggestion.nearestPlace}, ${suggestion.country}\n${suggestion.coordinates?.lat}, ${suggestion.coordinates?.lng}")
                }
                //registerForActivityResult canceled with error
                result.resultCode == Activity.RESULT_CANCELED && result.data?.hasExtra(
                    BaseOcrScanActivity.ERROR_RESULT_ID
                ) == true -> {
                    val error =
                        result.data!!.getStringExtra(BaseOcrScanActivity.ERROR_RESULT_ID)
                    viewModel.results = error
                }
                //registerForActivityResult canceled by user.
                else -> {
                }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        if (::ocrWrapper.isInitialized) ocrWrapper.stop()
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            W3WTheme {
                // A surface container using the 'background' color from the theme
                NavigationBarScaffold(
                    title = "OCR SDK Sample app"
                ) {
                    var scanScreenVisible by remember { mutableStateOf(false) }
                    val options = remember {
                        AutosuggestOptions().apply {
                            focus = Coordinates(51.2, 1.2)
                        }
                    }

                    LaunchedEffect(key1 = viewModel.selectedMLKitLibrary, block = {
                        ocrWrapper = getOcrWrapper()
                    })

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
                                bottomDrawerBackground = W3WTheme.colors.background
                            ),
                            //optional if you want to override any text styles.
                            scannerTextStyles = W3WOcrScannerDefaults.defaultTextStyles(
                                stateTextStyle = W3WTheme.typography.headline
                            ),
                            //optional if you want to override any colors of the scanned list item composable.
                            suggestionColors = SuggestionWhat3wordsDefaults.defaultColors(
                                background = W3WTheme.colors.background
                            ),
                            //optional if you want to override any text styles of the scanned list item composable.
                            suggestionTextStyles = SuggestionWhat3wordsDefaults.defaultTextStyles(
                                wordsTextStyle = W3WTheme.typography.headline
                            ),
                            onError = {
                                scanScreenVisible = false
                                viewModel.results =
                                    ("${it.key}, ${it.message}")
                            },
                            onDismiss = {
                                scanScreenVisible = false
                            },
                            onSuggestionSelected = {
                                viewModel.results =
                                    ("${it.words}, ${it.nearestPlace}, ${it.country}\n${it.coordinates?.lat}, ${it.coordinates?.lng}")
                                scanScreenVisible = false
                            })
                    }
                    MLKitLibrariesDropdownMenuBox()
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        scanScreenVisible = true
                    }) {
                        Text(text = "Launch OCR scanner in screen")
                    }

                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        val intent = MLKitOcrScanActivity.newInstanceWithApi(
                            this,
                            viewModel.selectedMLKitLibrary,
                            BuildConfig.W3W_API_KEY,
                            options,
                            true,
                            scanStateFoundTitle = getString(R.string.scan_state_found)
                        )
                        try {
                            resultLauncher.launch(intent)
                        } catch (e: ExceptionInInitializerError) {
                            viewModel.results = e.message
                        }
                    }) {
                        Text(text = "Launch OCR scanner as a pop up")
                    }

                    if (viewModel.results != null) Text(
                        modifier = Modifier.fillMaxWidth(), text = viewModel.results!!
                    )
                }
            }
        }
    }

    private fun getOcrWrapper(): W3WOcrWrapper {
        return W3WOcrMLKitWrapper(
            this,
            viewModel.selectedMLKitLibrary
        )
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun MLKitLibrariesDropdownMenuBox() {
        var expanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = {
                expanded = !expanded
            }) {
                CompositionLocalProvider(
                    LocalTextInputService provides null
                ) {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("MLKit Language library")
                        },
                        value = viewModel.getLibName(viewModel.selectedMLKitLibrary),
                        onValueChange = {
                        },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    )
                }

                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    viewModel.availableMLKitLanguages.forEach { item ->
                        DropdownMenuItem(onClick = {
                            viewModel.selectedMLKitLibrary = item
                            expanded = false
                        }) {
                            Text(text = viewModel.getLibName(item))
                        }
                    }
                }
            }
        }
    }
}