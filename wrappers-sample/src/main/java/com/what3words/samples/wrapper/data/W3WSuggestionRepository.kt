package com.what3words.samples.wrapper.data

import com.what3words.core.datasource.text.W3WTextDataSource
import com.what3words.core.datasource.voice.W3WVoiceDataSource
import com.what3words.core.datasource.voice.audiostream.W3WAudioStream
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.domain.W3WAddress
import com.what3words.core.types.domain.W3WSuggestion
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WGridSection
import com.what3words.core.types.geometry.W3WRectangle
import com.what3words.core.types.language.W3WLanguage
import com.what3words.core.types.language.W3WProprietaryLanguage
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.core.types.options.W3WAutosuggestOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class W3WSuggestionRepository(
    private val w3wTextDataSource: W3WTextDataSource,
    private val w3wVoiceDataSource: W3WVoiceDataSource,
    private val dispatcher: CoroutineDispatcher
) {

    suspend fun convertTo3wa(
        coordinates: W3WCoordinates,
        language: W3WLanguage
    ): W3WResult<W3WAddress> = withContext(dispatcher) {
        w3wTextDataSource.convertTo3wa(coordinates, language)
    }

    suspend fun convertToCoordinates(words: String): W3WResult<W3WAddress> =
        withContext(dispatcher) {
            w3wTextDataSource.convertToCoordinates(words)
        }

    suspend fun autosuggest(
        input: String,
        options: W3WAutosuggestOptions?
    ): W3WResult<List<W3WSuggestion>> = withContext(dispatcher) {
        w3wTextDataSource.autosuggest(input, options)
    }

    suspend fun isValid3wa(
        input: String
    ): W3WResult<Boolean> = withContext(dispatcher) {
        w3wTextDataSource.isValid3wa(input)
    }


    suspend fun gridSection(boundingBox: W3WRectangle): W3WResult<W3WGridSection> =
        withContext(dispatcher) {
            w3wTextDataSource.gridSection(boundingBox)
        }

    suspend fun availableLanguages(): W3WResult<Set<W3WProprietaryLanguage>> =
        withContext(dispatcher) {
            w3wTextDataSource.availableLanguages()
        }

    suspend fun voiceAutosuggest(
        input: W3WAudioStream,
        voiceLanguage: W3WRFC5646Language,
        options: W3WAutosuggestOptions?,
        onRawResult: ((String) -> Unit)?,
    ): W3WResult<List<W3WSuggestion>> = suspendCancellableCoroutine { continuation ->
        w3wVoiceDataSource.autosuggest(
            input,
            voiceLanguage,
            options,
            onRawResult
        ) { result ->
            continuation.resume(result)
        }
    }

    fun terminateVoiceAutosuggest() {
        w3wVoiceDataSource.terminate()
    }
}