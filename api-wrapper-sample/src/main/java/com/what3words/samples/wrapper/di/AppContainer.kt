package com.what3words.samples.wrapper.di

import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.androidwrapper.datasource.voice.W3WApiVoiceDataSource
import com.what3words.samples.wrapper.BuildConfig
import com.what3words.samples.wrapper.data.W3WSuggestionRepository
import kotlinx.coroutines.Dispatchers

// Simple DI, container of objects shared across the whole app
class AppContainer {

    val w3WVoiceDataSource = W3WApiVoiceDataSource.create(BuildConfig.W3W_API_KEY)

    val w3wTextDataSource = W3WApiTextDataSource.create(BuildConfig.W3W_API_KEY)

    val w3WSuggestionRepository =
        W3WSuggestionRepository(w3wTextDataSource, w3WVoiceDataSource, Dispatchers.IO)
}