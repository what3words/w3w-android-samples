package com.what3words.samples.wrapper.presentation.kotlin

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.what3words.androidwrapper.datasource.voice.error.W3WApiVoiceError
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.samples.wrapper.MyApplication
import com.what3words.samples.wrapper.R
import com.what3words.samples.wrapper.databinding.ActivityMainBinding
import com.what3words.samples.wrapper.databinding.ActivityMainBinding.inflate
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = MainViewModel.provideFactory(MyApplication.appContainer.w3WSuggestionRepository)
            .create(MainViewModel::class.java)
        binding = inflate(layoutInflater)
        setContentView(binding.root)

        setupButtonListeners()
        observeViewModel()
    }

    @SuppressLint("SetTextI18n")
    private fun setupButtonListeners() {
        // convert-to-3wa sample
        binding.buttonConvertTo3wa.setOnClickListener {
            val latLong =
                binding.textInputConvertTo3wa.text?.replace("\\s".toRegex(), "")?.split(",")
                    ?.filter { it.isNotEmpty() }
            val lat = latLong?.getOrNull(0)?.toDoubleOrNull()
            val long = latLong?.getOrNull(1)?.toDoubleOrNull()

            if (lat != null && long != null) {
                val coordinates = W3WCoordinates(lat, long)

                lifecycleScope.launch {
                    when (val result =
                        viewModel.convertTo3wa(coordinates, W3WRFC5646Language.EN_GB)) {
                        is W3WResult.Failure -> {
                            binding.resultConvertTo3wa.text = result.message
                        }

                        is W3WResult.Success -> {
                            binding.resultConvertTo3wa.text =
                                "3 word address: ${result.value.address}"
                        }
                    }
                }
            } else {
                binding.resultConvertTo3wa.text = "invalid lat,long"
            }
        }

        // convert-to-coordinates sample
        binding.buttonConvertToCoordinates.setOnClickListener {
            lifecycleScope.launch {
                when (val result =
                    viewModel.convertToCoordinates(binding.textInputConvertToCoordinates.text.toString())) {
                    is W3WResult.Failure -> {
                        binding.resultConvertToCoordinates.text = result.message
                    }

                    is W3WResult.Success -> {
                        binding.resultConvertToCoordinates.text =
                            "Coordinates: ${result.value.lat}, ${result.value.lng}"
                    }
                }
            }
        }

        // text autosuggest sample
        binding.buttonAutoSuggest.setOnClickListener {
            lifecycleScope.launch {
                val result = viewModel.autosuggest(binding.textInputAutoSuggest.text.toString())
                when (result) {
                    is W3WResult.Failure -> {
                        binding.resultAutoSuggest.text = result.message
                    }

                    is W3WResult.Success -> {
                        binding.resultAutoSuggest.text = if (result.value.isNotEmpty())
                            "Suggestions: ${result.value.joinToString { it.w3wAddress.address }}"
                        else "No suggestions available"
                    }
                }
            }
        }

        // voice autosuggest sample
        binding.buttonAutoSuggestVoice.setOnClickListener {
            if (viewModel.state.value.isRecording) {
                viewModel.terminateVoiceAutosuggest()
            } else {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestRecordPermission.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    viewModel.autosuggestWithVoice(W3WRFC5646Language.EN_GB)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.buttonAutoSuggestVoice.setIconResource(if (state.isRecording) R.drawable.ic_stop else R.drawable.ic_record)
                    binding.volumeAutoSuggestVoice.text =
                        "volume: ${state.volume}"
                    binding.resultAutoSuggestVoice.text =
                        "Suggestions: ${state.suggestions.joinToString { it.w3wAddress.address }}"
                    state.error?.let {
                        if (it is W3WApiVoiceError) {
                            binding.resultAutoSuggestVoice.text = "${it.message}"
                        } else {
                            binding.resultAutoSuggestVoice.text = it.message
                        }
                    }
                }
            }
        }
    }

    private val requestRecordPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.autosuggestWithVoice(W3WRFC5646Language.EN_GB)
            }
        }
}
