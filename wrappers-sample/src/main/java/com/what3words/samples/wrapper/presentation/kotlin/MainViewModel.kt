package com.what3words.samples.wrapper.presentation.kotlin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.what3words.core.datasource.voice.audiostream.W3WAudioStream
import com.what3words.core.datasource.voice.audiostream.W3WAudioStreamState
import com.what3words.core.datasource.voice.audiostream.W3WMicrophone
import com.what3words.core.types.common.W3WError
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.domain.W3WSuggestion
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.language.W3WLanguage
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.samples.wrapper.data.W3WSuggestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainViewModel(
    private val w3WSuggestionRepository: W3WSuggestionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val microphone = W3WMicrophone()

    init {
        microphone.setEventsListener(object : W3WAudioStream.EventsListener {
            override fun onVolumeChange(volume: Float) {
                _state.update {
                    it.copy(volume = (volume.times(100).roundToInt()))
                }
            }

            override fun onError(error: W3WError) {
                _state.update {
                    it.copy(error = error)
                }
            }

            override fun onAudioStreamStateChange(state: W3WAudioStreamState) {
                when (state) {
                    W3WAudioStreamState.LISTENING -> {
                        _state.update {
                            it.copy(isRecording = true)
                        }
                    }

                    W3WAudioStreamState.STOPPED -> {
                        _state.update {
                            it.copy(isRecording = false, volume = 0)
                        }
                    }
                }
            }
        })
    }

    suspend fun convertTo3wa(
        coordinates: W3WCoordinates,
        language: W3WLanguage
    ) = w3WSuggestionRepository.convertTo3wa(coordinates, language)

    suspend fun convertToCoordinates(words: String) =
        w3WSuggestionRepository.convertToCoordinates(words)

    suspend fun autosuggest(input: String) =
        w3WSuggestionRepository.autosuggest(input, null)

    suspend fun isValid3wa(input: String) =
        w3WSuggestionRepository.isValid3wa(input)

    fun autosuggestWithVoice(language: W3WRFC5646Language) {
        viewModelScope.launch {
            when (val result =
                w3WSuggestionRepository.voiceAutosuggest(microphone, language, null, null)) {
                is W3WResult.Failure -> {
                    _state.update {
                        it.copy(error = result.error, suggestions = emptyList())
                    }
                }

                is W3WResult.Success -> {
                    _state.update {
                        it.copy(suggestions = result.value, error = null)
                    }
                }
            }
        }
    }

    fun terminateVoiceAutosuggest() {
        w3WSuggestionRepository.terminateVoiceAutosuggest()
    }

    companion object {
        fun provideFactory(
            w3WSuggestionRepository: W3WSuggestionRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(w3WSuggestionRepository) as T
            }
        }
    }

    data class State(
        val isRecording: Boolean = false,
        val volume: Int = 0,
        val suggestions: List<W3WSuggestion> = emptyList(),
        val error: W3WError? = null
    )
}