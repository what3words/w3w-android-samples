package com.what3words.androidwrappersample

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.MediaRecorder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.androidwrappersample.databinding.ActivityMainBinding
import com.what3words.androidwrappersample.databinding.ActivityMainBinding.inflate
import com.what3words.javawrapper.request.Coordinates
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var builder: VoiceBuilder? = null
    private val wrapper by lazy {
        What3WordsV3(BuildConfig.W3W_API_KEY, this)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = inflate(layoutInflater)

        // convert-to-3wa sample
        binding.buttonConvertTo3wa.setOnClickListener {
            val latLong =
                binding.textInputConvertTo3wa.text?.replace("\\s".toRegex(), "")?.split(",")
                    ?.filter { it.isNotEmpty() }
            val lat = latLong?.getOrNull(0)?.toDoubleOrNull()
            val long = latLong?.getOrNull(1)?.toDoubleOrNull()
            if (lat != null && long != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    // use wrapper.convertTo3wa() with Dispatcher.IO - background thread
                    val result = wrapper.convertTo3wa(Coordinates(lat, long)).execute()
                    CoroutineScope(Dispatchers.Main).launch {
                        // use Dispatcher.Main to update your views with the results - Main thread
                        if (result.isSuccessful) {
                            binding.resultConvertTo3wa.text = "3 word address: ${result.words}"
                        } else {
                            binding.resultConvertTo3wa.text = result.error.message
                        }
                    }
                }
            } else {
                binding.resultConvertTo3wa.text = "invalid lat,long"
            }
        }

        // convert-to-coordinates sample
        binding.buttonConvertToCoordinates.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                // use wrapper.convertToCoordinates() with Dispatcher.IO - background thread
                val result =
                    wrapper.convertToCoordinates(binding.textInputConvertToCoordinates.text.toString())
                        .execute()
                CoroutineScope(Dispatchers.Main).launch {
                    // use Dispatcher.Main to update your views with the results - Main thread
                    if (result.isSuccessful) {
                        binding.resultConvertToCoordinates.text =
                            "Coordinates: ${result.coordinates.lat}, ${result.coordinates.lng}"
                    } else {
                        binding.resultConvertToCoordinates.text = result.error.message
                    }
                }
            }
        }

        // text autosuggest sample
        binding.buttonAutoSuggest.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                // use wrapper.autosuggest() with Dispatcher.IO - background thread
                val result =
                    wrapper.autosuggest(binding.textInputAutoSuggest.text.toString()).execute()
                CoroutineScope(Dispatchers.Main).launch {
                    // use Dispatcher.Main to update your views with the results - Main thread
                    if (result.isSuccessful) {
                        binding.resultAutoSuggest.text = if (result.suggestions.count() != 0)
                            "Suggestions: ${result.suggestions.joinToString { it.words }}"
                        else "No suggestions available"
                    } else {
                        binding.resultAutoSuggest.text = result.error.message
                    }
                }
            }
        }

        val microphone = Microphone(
            16000,
            AudioFormat.ENCODING_PCM_16BIT,
            AudioFormat.CHANNEL_IN_MONO,
            MediaRecorder.AudioSource.MIC
        ).onListening {
            it?.let { volume ->
                binding.volumeAutoSuggestVoice.text =
                    "volume: ${(volume.times(100).roundToInt())}"
            }
        }.onError {
            binding.buttonAutoSuggestVoice.setIconResource(R.drawable.ic_record)
            binding.resultAutoSuggestVoice.text = it
        }

        // voice autosuggest sample
        builder = wrapper.autosuggest(microphone, "en")
            .focus(Coordinates(51.457269, -0.074788))
            .onSuggestions { suggestions ->
                binding.buttonAutoSuggestVoice.setIconResource(R.drawable.ic_record)
                binding.resultAutoSuggestVoice.text =
                    "Suggestions: ${suggestions.joinToString { it.words }}"
            }.onError { error ->
                binding.buttonAutoSuggestVoice.setIconResource(R.drawable.ic_record)
                binding.resultAutoSuggestVoice.text = "${error.key}, ${error.message}"
            }

        binding.buttonAutoSuggestVoice.setOnClickListener {
            if (builder?.isListening() == true) {
                binding.buttonAutoSuggestVoice.setIconResource(R.drawable.ic_record)
                builder?.stopListening()
            } else {
                // Check if RECORD_AUDIO permission is granted
                val permission =
                    PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

                if (permission == PermissionChecker.PERMISSION_GRANTED) {
                    binding.buttonAutoSuggestVoice.setIconResource(R.drawable.ic_stop)
                    builder?.startListening()
                } else {
                    // request RECORD_AUDIO permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        1
                    )
                }
            }
        }

        setContentView(binding.root)
    }

    @SuppressLint("SetTextI18n")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.count() == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            binding.buttonAutoSuggestVoice.setIconResource(R.drawable.ic_stop)
            builder?.startListening()
        } else {
            binding.resultAutoSuggestVoice.text = "record audio permission required"
        }
    }
}
