# <img src="https://what3words.com/assets/images/w3w_square_red.png" width="64" height="64" alt="what3words">&nbsp; Auto Suggest Voice Sample 

Autosuggest Voice Sample is a sample demonstration voice only by using the W3WAutoSuggestVoice component.

 For more detailing information about the library please refer the [what3words android components library](https://github.com/what3words/w3w-android-components) repository. 

<img src="readme/AutoSuggestVoiceSample.png" width="320">

## Configuration

build.gradle
```gradle
implementation "com.what3words:w3w-android-components:$what3words_android_components_version"
implementation 'androidx.constraintlayout:constraintlayout-compose:<latest-compose-constraintlayout-version>'
implementation 'androidx.compose.ui:ui:<latest-compose-version>'
```

AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.INTERNET" />
<!-- if using voice -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
    ...
```

# Features

## W3WAutoSuggestVoice component

activity_voice.xml
```xml
    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <TextView
            android:id="@+id/textHeader"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="@dimen/main_s"
            android:layout_marginTop="@dimen/main_l"
            android:text="@string/welcome_to_autosuggest"
            android:visibility="visible"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

        <com.what3words.components.voice.W3WAutoSuggestVoice
            android:id="@+id/w3wVoice"
            android:layout_width="250dp"
            android:layout_height="250dp"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@+id/textHeader" />
    ...        

</androidx.constraintlayout.widget.ConstraintLayout>
```
VoiceActivity.kt
```Kotlin
class VoiceActivity : AppCompatActivity() {

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
    ...
}
```
