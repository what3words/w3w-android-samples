package com.what3words.samples.multiple.test.utils

import android.media.AudioFormat
import com.what3words.androidwrapper.voice.VoiceApi
import com.what3words.androidwrapper.voice.VoiceApiListener
import com.what3words.androidwrapper.voice.VoiceApiListenerWithCoordinates
import com.what3words.javawrapper.request.AutosuggestOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.io.InputStream

class MockVoiceApi(
    private var mockSampleRate: Int = 44100,
    private var mockBitPerRate: Int = AudioFormat.ENCODING_PCM_FLOAT,
    private var mockDataSound: InputStream? = null,
    apiKey: String,
    baseUrl: String = BASE_URL,
    client: OkHttpClient = OkHttpClient()
) : VoiceApi(apiKey, baseUrl, client) {

    private var isSendData = false

    override fun initialize(
        sampleRate: Int,
        samplesPerChannel: Int,
        encoding: Int,
        voiceLanguage: String,
        autosuggestOptions: AutosuggestOptions,
        listener: VoiceApiListener
    ) {
        super.initialize(
            mockSampleRate,
            samplesPerChannel,
            mockBitPerRate,
            voiceLanguage,
            autosuggestOptions,
            listener
        )
    }

    override fun initialize(
        sampleRate: Int,
        samplesPerChannel: Int,
        encoding: Int,
        voiceLanguage: String,
        autosuggestOptions: AutosuggestOptions,
        listener: VoiceApiListenerWithCoordinates
    ) {
        super.initialize(
            mockSampleRate,
            samplesPerChannel,
            mockBitPerRate,
            voiceLanguage,
            autosuggestOptions,
            listener
        )
    }

    override fun sendData(readCount: Int, buffer: ShortArray) {
        if(!isSendData) {
            isSendData = true
            CoroutineScope(Dispatchers.IO).launch {
                val data = mockDataSound?.readBytes()
                data?.let {
                    var offsetSample = 0
                    val samplePerRate = 5000
                    while(offsetSample<it.size-samplePerRate) {
                        super.sendData(it.toByteString(offsetSample,samplePerRate))
                        offsetSample += samplePerRate
                    }
                }
            }
        }
    }
}