package autosuggestsample.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import autosuggestsample.BuildConfig
import autosuggestsample.R
import autosuggestsample.databinding.ActivityMainBinding
import autosuggestsample.databinding.ActivityMainBinding.inflate
import autosuggestsample.util.addOnTextChangedListener
import com.what3words.components.models.VoiceScreenType
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)

        binding.suggestionEditText.apiKey(BuildConfig.W3W_API_KEY)
            .onDisplaySuggestions {
            }
            .onSuggestionSelected {
                if (it != null) {
                    binding.selectedInfo.text =
                        "words: ${it.words}\ncountry: ${it.country}\nnear: ${it.nearestPlace}\ndistance: ${if (it.distanceToFocusKm == null) "N/A" else it.distanceToFocusKm.toString() + "km"}\nlatitude: ${it.coordinates?.lat}\nlongitude: ${it.coordinates?.lng}"
                } else {
                    binding.selectedInfo.text = ""
                }
            }
            .onError {
                Log.e("MainActivity", "${it.key} - ${it.message}")
                Snackbar.make(binding.main, "${it.key} - ${it.message}", LENGTH_INDEFINITE).apply {
                    setAction("OK") { dismiss() }
                    show()
                }
            }

        binding.checkboxCoordinates.setOnCheckedChangeListener { _, b ->
            binding.suggestionEditText.returnCoordinates(b)
        }

        binding.checkboxPreferLand.setOnCheckedChangeListener { _, b ->
            binding.suggestionEditText.preferLand(b)
        }

        binding.checkboxAllowInvalidAddress.setOnCheckedChangeListener { _, b ->
            binding.suggestionEditText.allowInvalid3wa(b)
        }

        binding.checkboxAllowFlexibleDelimiters.setOnCheckedChangeListener { _, b ->
            binding.suggestionEditText.allowFlexibleDelimiters(b)
        }

        binding.checkboxVoiceDisabled.isChecked = true

        binding.checkboxCustomPicker.setOnCheckedChangeListener { _, _ ->
            updateOnSelectedAndOnError()
        }

        binding.checkboxCustomError.setOnCheckedChangeListener { _, _ ->
            updateOnSelectedAndOnError()
        }

        binding.checkboxCustomCorrectionPicker.setOnCheckedChangeListener { _, b ->
            binding.suggestionEditText.customCorrectionPicker(if (b) binding.correctionPicker else null)
        }

        binding.textPlaceholder.setText(R.string.input_hint)
        binding.textPlaceholder.addOnTextChangedListener {
            binding.suggestionEditText.hint = it
        }

        binding.textVoicePlaceholder.setText(R.string.voice_placeholder)
        binding.textVoicePlaceholder.addOnTextChangedListener {
            binding.suggestionEditText.voicePlaceholder(it)
        }

        // how to change fallback text language
        binding.textLanguage.addOnTextChangedListener {
            binding.suggestionEditText.language(it)
        }

        // how to change voicelanguage
        binding.textVoiceLanguage.setText("en")
        binding.textVoiceLanguage.addOnTextChangedListener {
            binding.suggestionEditText.voiceLanguage(it)
        }

        // how to clipToCountry
        binding.textClipToCountry.addOnTextChangedListener { input ->
            val test = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            binding.suggestionEditText.clipToCountry(test)
        }

        binding.closeButton.setOnClickListener {
            binding.suggestionEditText.setText("")
        }

        // how to apply focus
        binding.textFocus.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val lat = latLong.getOrNull(0)?.toDoubleOrNull()
            val long = latLong.getOrNull(1)?.toDoubleOrNull()
            if (lat != null && long != null) {
                binding.suggestionEditText.focus(Coordinates(lat, long))
            } else {
                binding.suggestionEditText.focus(null)
            }
        }

        // how to clipToCircle
        binding.textClipToCircle.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val lat = latLong.getOrNull(0)?.toDoubleOrNull()
            val long = latLong.getOrNull(1)?.toDoubleOrNull()
            val km = latLong.getOrNull(2)?.toDoubleOrNull()
            if (lat != null && long != null) {
                binding.suggestionEditText.clipToCircle(Coordinates(lat, long), km ?: 0.0)
            } else {
                binding.suggestionEditText.clipToCircle(null, null)
            }
        }

        // how to clipToBoundingBox
        binding.textClipToBoundingBox.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val swLat = latLong.getOrNull(0)?.toDoubleOrNull()
            val swLong = latLong.getOrNull(1)?.toDoubleOrNull()
            val neLat = latLong.getOrNull(2)?.toDoubleOrNull()
            val neLong = latLong.getOrNull(3)?.toDoubleOrNull()
            if (swLat != null && swLong != null && neLat != null && neLong != null) {
                binding.suggestionEditText.clipToBoundingBox(
                    BoundingBox(
                        Coordinates(swLat, swLong),
                        Coordinates(neLat, neLong)
                    )
                )
            } else {
                binding.suggestionEditText.clipToBoundingBox(null)
            }
        }

        // how to clipToPolygon
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
            binding.suggestionEditText.clipToPolygon(
                listCoordinates
            )
        }

        setContentView(binding.root)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        startActivity(Intent.makeRestartActivityTask(this.intent?.component))
    }

    // example of how to use a custom suggestion picker and custom error message on your view instead of using the default provided below the W3WAutoSuggestEditText
    private fun updateOnSelectedAndOnError() {
        binding.suggestionEditText.onSuggestionSelected(
            if (binding.checkboxCustomPicker.isChecked) binding.suggestionPicker else null,
            if (binding.checkboxCustomError.isChecked) binding.suggestionError else null
        ) {
            if (it != null) {
                binding.selectedInfo.text =
                    "words: ${it.words}\ncountry: ${it.country}\nnear: ${it.nearestPlace}\ndistance: ${if (it.distanceToFocusKm == null) "N/A" else it.distanceToFocusKm.toString() + "km"}\nlatitude: ${it.coordinates?.lat}\nlongitude: ${it.coordinates?.lng}"
            } else {
                binding.selectedInfo.text = ""
            }
        }.onError(if (binding.checkboxCustomError.isChecked) binding.suggestionError else null) {
            Log.e("MainActivity", "${it.key} - ${it.message}")
            Snackbar.make(binding.main, "${it.key} - ${it.message}", LENGTH_INDEFINITE).apply {
                setAction("OK") { dismiss() }
                show()
            }
        }
    }

    fun onRadioButtonClicked(view: View) {
        // Is the button now checked?

        if (view !is RadioButton) {
            return
        }

        binding.suggestionEditText.voiceEnabled(false, VoiceScreenType.Inline)
        // Check which radio button was clicked
        when (view.id) {
            R.id.checkboxVoiceEnabled ->
                binding.suggestionEditText.voiceEnabled(true, VoiceScreenType.Inline)
            R.id.checkboxVoicePopup ->
                binding.suggestionEditText.voiceEnabled(true, VoiceScreenType.AnimatedPopup)
            R.id.checkboxVoiceFullscreen ->
                binding.suggestionEditText.voiceEnabled(true, VoiceScreenType.Fullscreen)
        }
    }
}
