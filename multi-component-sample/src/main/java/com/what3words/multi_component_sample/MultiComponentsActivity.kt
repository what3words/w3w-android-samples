package com.what3words.multi_component_sample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface.LATIN
import com.what3words.androidwrapper.What3WordsAndroidWrapper
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.multi_component_sample.ui.screen.MainAppScreen
import com.what3words.ocr.components.models.W3WOcrMLKitWrapper
import com.what3words.ocr.components.models.W3WOcrWrapper
import kotlinx.coroutines.launch

class MultiComponentsActivity : ComponentActivity() {
    private val viewModel: MultiComponentsViewModel by viewModels()
    private lateinit var ocrWrapper: W3WOcrWrapper
    private val dataProvider: What3WordsAndroidWrapper by lazy {
        What3WordsV3(
            BuildConfig.W3W_API_KEY,
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ocrWrapper = W3WOcrMLKitWrapper(context = this@MultiComponentsActivity, LATIN)
        val wrapper = What3WordsV3(BuildConfig.W3W_API_KEY, this)

        setContent {
            val selectedSuggestion by viewModel.selectedSuggestion.collectAsState()

            MainAppScreen(
                wrapper,
                ocrWrapper,
                dataProvider,
                selectedSuggestion = selectedSuggestion,
                onSuggestionChanged = { viewModel.selectedSuggestion.value = it }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::ocrWrapper.isInitialized) ocrWrapper.stop()
    }
}