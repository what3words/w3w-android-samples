package com.what3words.sample_voice

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import autosuggestsample.util.addOnTextChangedListener
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import com.what3words.sample_voice.databinding.ActivityVoiceBinding
import com.what3words.sample_voice.databinding.ActivityVoiceBinding.inflate

class VoiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVoiceBinding

    private fun showSuggestion(suggestions: List<SuggestionWithCoordinates>?) {
        binding.selectedInfo.text = ""
        if (suggestions != null && suggestions.isNotEmpty()) {
            suggestions.forEach { selected ->
                binding.selectedInfo.text =
                    binding.selectedInfo.text.toString() + "\n\nwords: ${selected.words}\ncountry: ${selected.country}\nnear: ${selected.nearestPlace}\ndistance: ${if (selected.distanceToFocusKm == null) "N/A" else selected.distanceToFocusKm.toString() + "km"}\nlatitude: ${selected.coordinates?.lat}\nlongitude: ${selected.coordinates?.lng}"
            }
        } else {
            binding.selectedInfo.text = ""
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        binding.w3wVoice.apiKey(BuildConfig.W3W_API_KEY)
            .microphone(
                16000,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioFormat.CHANNEL_IN_MONO,
                MediaRecorder.AudioSource.MIC
            )
            .onResults { suggestions ->
                showSuggestion(suggestions)
            }.onError {
                Log.e("MainActivity", "${it.key} - ${it.message}")
                Snackbar.make(binding.main, "${it.key} - ${it.message}", LENGTH_INDEFINITE).apply {
                    setAction("OK") { dismiss() }
                }.show()
            }.onListeningStateChanged {
                Log.i("MainActivity", "${it.name}")
            }

        binding.checkboxCoordinates.setOnCheckedChangeListener { _, b ->
            binding.w3wVoice.returnCoordinates(b)
        }

        binding.textVoiceLanguage.setText("en")
        binding.textVoiceLanguage.addOnTextChangedListener {
            binding.w3wVoice.voiceLanguage(it)
        }

        binding.textClipToCountry.addOnTextChangedListener { input ->
            val test = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            binding.w3wVoice.clipToCountry(test)
        }

        binding.textFocus.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val lat = latLong.getOrNull(0)?.toDoubleOrNull()
            val long = latLong.getOrNull(1)?.toDoubleOrNull()
            if (lat != null && long != null) {
                binding.w3wVoice.focus(Coordinates(lat, long))
            } else {
                binding.w3wVoice.focus(null)
            }
        }

        binding.textClipToCircle.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val lat = latLong.getOrNull(0)?.toDoubleOrNull()
            val long = latLong.getOrNull(1)?.toDoubleOrNull()
            val km = latLong.getOrNull(2)?.toDoubleOrNull()
            if (lat != null && long != null) {
                binding.w3wVoice.clipToCircle(Coordinates(lat, long), km ?: 0.0)
            } else {
                binding.w3wVoice.clipToCircle(null, null)
            }
        }

        binding.textClipToBoundingBox.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val swLat = latLong.getOrNull(0)?.toDoubleOrNull()
            val swLong = latLong.getOrNull(1)?.toDoubleOrNull()
            val neLat = latLong.getOrNull(2)?.toDoubleOrNull()
            val neLong = latLong.getOrNull(3)?.toDoubleOrNull()
            if (swLat != null && swLong != null && neLat != null && neLong != null) {
                binding.w3wVoice.clipToBoundingBox(
                    BoundingBox(
                        Coordinates(swLat, swLong),
                        Coordinates(neLat, neLong)
                    )
                )
            } else {
                binding.w3wVoice.clipToBoundingBox(null)
            }
        }

        binding.textClipToPolygon.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val listCoordinates = mutableListOf<Coordinates>()
            if (latLong.count() % 2 == 0) {
                for (x in 0 until latLong.count() step 2) {
                    if (latLong[x].toDoubleOrNull() != null &&
                        latLong[x + 1].toDoubleOrNull() != null
                    ) {
                        listCoordinates.add(
                            Coordinates(
                                latLong[x].toDouble(),
                                latLong[x + 1].toDouble()
                            )
                        )
                    }
                }
            }
            binding.w3wVoice.clipToPolygon(
                listCoordinates
            )
        }
        setContentView(binding.root)
    }
}
