# <img src="https://what3words.com/assets/images/w3w_square_red.png" width="64" height="64" alt="what3words">&nbsp;w3w-android-components in Jetpack compose

An Android library to use
the [what3words v3 API autosuggest](https://developer.what3words.com/public-api/docs#autosuggest) in
Jetpack compose.

We created a port that allows you use W3WAutoSuggestEditText directly in Jetpack compose. The W3WAutoSuggestTextField composable is a port of
the [W3WAutoSuggestEditText](https://github.com/what3words/w3w-android-components) component in
Jetpack compose. This composable extends all the core functionalities of the W3WAutoSuggestEditText to
Jetpack compose, and it also provides you direct access to an instance of W3WAutoSuggestEditText used
internally. At the moment, the W3WAutoSuggestTextField composable can only be used inside a ConstraintLayout in Jetpack compose.


<img src="https://github.com/what3words/w3w-android-components/blob/dev/assets/components-1-new.gif" width=40% height=40%>

To obtain an API key, please
visit [https://what3words.com/select-plan](https://what3words.com/select-plan) and sign up for an
account.

## Installation

The artifact is available
through [![Maven Central](https://img.shields.io/maven-central/v/com.what3words/w3w-android-components.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.what3words%22%20AND%20a:%22w3w-android-components%22)

### Android minimum SDK support

[![Generic badge](https://img.shields.io/badge/minSdk-23-green.svg)](https://developer.android.com/about/versions/marshmallow/android-6.0/)

### Gradle

```groovy
implementation 'com.what3words:w3w-android-components:3.0.2'
implementation 'androidx.constraintlayout:constraintlayout-compose:<latest-compose-constraintlayout-version>'
implementation 'androidx.compose.ui:ui:<latest-compose-version>'
```

## Documentation

See the what3words public API [documentation](https://docs.what3words.com/api/v3/)

## Usage

AndroidManifest.xml

```xml

<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.yourpackage.yourapp">

    <uses-permission android:name="android.permission.INTERNET" />
    ...
</manifest>
```

add this to build.gradle (app level)

```groovy
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}
```

add this the following proguard rules

```
-keep class com.what3words.javawrapper.request.* { *; }
-keep class com.what3words.javawrapper.response.* { *; }
```

Using W3WAutoSuggestTextField

```Kotlin
class MainActivity : Component() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize() // W3WAutoSuggestTextField direct parent should fill max height 
            ) {
                val (w3wTextFieldRef) = createRefs()

                // what3words autosuggest textfield component 
                W3WAutoSuggestTextField(
                    modifier = Modifier.constrainAs(ref = w3wTextFieldRef) {
                        linkTo(start = parent.start, end = parent.end)
                        top.linkTo(anchor = parent.top)
                    },
                    ref = w3wTextFieldRef,
                    configuration = AutoSuggestConfiguration.Api(apiKey = BuildConfig.W3W_API_KEY),
                    onSuggestionWithCoordinates = {
                        Toast.makeText(
                            this@MainActivity,
                            it.words,
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }
}
```

Configuring W3WAutoSuggestTextFieldState The W3WAutoSuggestTextFieldState allows you to access and
customize the properties and behaviour of the W3WAutoSuggestTextField. By using the
W3WAutoSuggestTextFieldState, you can easily access clipping methods, focus and other public
functions that was provided by the W3WAutoSuggestEditText in the view system.

```Kotlin

val autoSuggestTextFieldState = rememberW3WAutoSuggestTextFieldState().apply {
    returnCoordinates(enabled = true)
    preferLand(isPreferred = false)
}

W3WAutoSuggestTextField(
    modifier = Modifier.constrainAs(ref = w3wTextFieldRef) {
        linkTo(start = parent.start, end = parent.end)
        top.linkTo(anchor = parent.top)
    },
    ref = w3wTextFieldRef,
    configuration = AutoSuggestConfiguration.Api(apiKey = BuildConfig.W3W_API_KEY),
    state = autoSuggestTextFieldState,
    onSuggestionWithCoordinates = {
        Log.i("SelectedWords", it.words)
    }
)
```

If you run our Enterprise Suite API Server yourself, you may specify the URL to your own server like
so:

```Kotlin
 // ..... other sections of code 
W3WAutoSuggestTextField(
    modifier = Modifier.constrainAs(ref = w3wTextFieldRef) {
        linkTo(start = parent.start, end = parent.end)
        top.linkTo(anchor = parent.top)
    },
    ref = w3wTextFieldRef,
    configuration = AutoSuggestConfiguration.ApiWithEnterpriseEndpoint(
        apiKey = BuildConfig.W3W_API_KEY,
        endpoint = BuildConfig.YOUR_ENTERPRISE_ENDPOINT
    ),
    onSuggestionWithCoordinates = {}
)
// ..... other sections of code
```

## General functions in W3WAutoSuggestTextFieldState:

| Name | Summary | Example |
|---|---|----|
|returnCoordinates|Calls the what3words API to obtain the coordinates for the selected 3 word address (to then use on a map or pass through to a logistic company etc)|```returnCoordinates(true)```|
|onSuggestionSelected|Will provide the user selected 3 word address, if user selects an invalid 3 word address SuggestionWithCoordinates will be null.|```onSuggestionSelected { suggestion -> }```<br>or for custom picker view<br>```onSuggestionSelected(W3WAutoSuggestPicker) { suggestion -> }```|
|onDisplaySuggestions|Callback to update view when suggestion picker is being displayed or not, example, show tips when false hide tips when true|```onDisplaySuggestions { isShowing -> }```|
|onError|Will provide any errors APIResponse.What3WordsError that might happen during the API call.|```onError { error -> }```<br>or for custom error view<br>```onError(W3WAutoSuggestErrorMessage) { error -> }```|
|focus|This is a location, specified as a latitude/longitude (often where the user making the query is). If specified, the results will be weighted to give preference to those near the focus. |```focus(Coordinates(49.180803, -8.001330))```|
|clipToBoundingBox|Clip results to a bounding box specified using co-ordinates.|```clipToBoundingBox(BoundingBox(Coordinates(49.180803, -8.001330),Coordinates(58.470001, 2.158991)))```|
|clipToCircle|Restrict autosuggest results to a circle, specified by Coordinates representing the centre of the circle, plus the radius in kilometres.|```clipToCircle(Coordinates(49.180803, -8.001330), 100.0)```|
|clipToCountry|Restricts autosuggest to only return results inside the countries specified by comma-separated list of uppercase ISO 3166-1 alpha-2 country codes.|```clipToCountry(listOf("GB", "BE"))```|
|clipToPolygon|Restrict autosuggest results to a polygon, specified by a collection of Coordinates.|```clipToPolygon(listOf(Coordinates(49.180803, -8.001330), ..., Coordinates(49.180803, -8.001330)))```|
|correctionMessage|Set end-user correction picker title, default: "Did you mean?"|```correctionMessage("new title")```|
|customCorrectionPicker|Add custom correction view.|```customCorrectionPicker(W3WAutoSuggestCorrectionPicker)```|
|displayUnit|Set end-user display unit, DisplayUnits.SYSTEM (default), DisplayUnits.METRIC, DisplayUnits.IMPERIAL|```displayUnit(DisplayUnits.METRIC)```|
|errorMessage|Set end-user error message for API related issues, default: An error occurred.|```errorMessage("new message")```|
|invalidSelectionMessage|Set end-user invalid address message for when user selects invalid three word address, default: "No valid what3words address found" ||
|onHomeClick|If DrawableStart is set and it's pressed callback will be called, usage example is to have a back button as drawableStart.|```onHomeClick { }```|
|allowFlexibleDelimiters|Allow EditText to accept different delimiters than the what3words standard full stop "index.home.raft", i.e  "index home raft" or "index,home,raft".| ```allowFlexibleDelimiters(true)``` |
|allowInvalid3wa|By default the EditText field will clear an inputted value if a valid 3 word address is not entered. Setting allowInvalid3wa to true stops this behaviour and the value is persisted in the EditText.|```allowInvalid3wa(true)```|
|preferLand|Makes AutoSuggest prefer results on land to those in the sea. This setting is on by default. Use false to disable this setting and receive more suggestions in the sea.|```preferLand(false)```|

## Enable voice autosuggest:

<img src="https://github.com/what3words/w3w-autosuggest-edittext-android/blob/dev/assets/components-2-new.gif" width=35% height=35%>

The component also allows for voice input using the what3words Voice API. This feature allows the
user to say 3 words and using speech recognition technology displays 3 word address suggestions to
the user.

Before enabling Voice AutoSuggest you will need to add a Voice API plan
in [your account](https://accounts.what3words.com/billing).

By default the voice language is set to English but this can be changed using the voiceLanguage
function in the W3WAutoSuggestTextFieldState (for list of available languages please check the
properties table below). Voice input respects the clipping and focus options applied within the
general properties. We recommend applying clipping and focus where possible to display as accurate
suggestions as possible. To enable voice you can do with programmatically or directly in the XML.

AndroidManifest.xml

```xml

<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.yourpackage.yourapp">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    ...
</manifest>
```

add following proguard rules

```
-keep class com.what3words.androidwrapper.voice.* { *; } //only needed if using voice functionality.
```

Kotlin (Compose)

```kotlin
// enable voice while initializing W3WAutoSuggestTextFieldState
val autoSuggestTextFieldState = rememberW3WAutoSuggestTextFieldState(
    voiceEnabledByDefault = true,
    voiceScreenType = VoiceScreenType.AnimatedPopup
)

// OR 

// enable voice at a later time, maybe from a callback
autoSuggestTextFieldState.voiceEnabled(enabled = true, type = VoiceScreenType.FullScreen)
autoSuggestTextFieldState.voiceLanguage(language = "ar") // set voice language as Arabic

```

## Voice properties in W3WAutoSuggestTextFieldState:

| Name | Summary | Example |
|---|---|----|
|voiceEnabled|Enable voice for autosuggest component.|```voiceEnabled(true)```<br>or for other animations<br>```voiceEnabled(true, VoiceScreenType.AnimatedPopup)```<br>or for custom microphone icon<br>```voiceEnabled(true, VoiceScreenType.Inline, getDrawable(R.drawable.new_mic))```
|voiceLanguage|Available voice languages: `ar` for Arabic, `cmn` for Mandarin Chinese, `de` for German, `en` Global English (default), `es` for Spanish, `hi` for Hindi, `ja` for Japanese and `ko` for Korean | ```voiceLanguage("de")```
|voicePlaceholder|Voice placeholder for fullscreen popup for autosuggest component, default: "Say a 3 word address…"|```voicePlaceholder("new placeholder")```
|toggleVoice|Will trigger the voice programmatically, in cases where the developer wants to start listening without user touching the screen.|```toggleVoice()```

## Voice only:

If you want to use voice-only (no text input) please look at our **voice-sample** app in this repo
for examples of how to use our **W3WAutoSuggestVoice component**.

## Advanced usage:

If you want to check different ways to use our component please look at our **advanced-sample** app
in this repo for examples of how to use and customize our **W3WAutoSuggestText component**.

![alt text](https://github.com/what3words/w3w-android-components/blob/master/assets/screen_10.png?raw=true "Screenshot 10")

## Styles:

<img src="https://github.com/what3words/w3w-android-components/blob/dev/assets/components-3-new.gif" width=35% height=35%>

You can provide/reuse your custom xml styles for each component used in the W3WAutoSuggestTextField
by passing the resource id to the style of the desired component via the themes parameter in the
W3WAutoSuggestTextField.

Applying a custom style to the each components should be fairly straightforward. You can reference
the [compose code sample](https://github.com/what3words/w3w-android-components/blob/dev/compose-sample/src/main/java/com/what3words/compose/sample/ui/screen/W3WTextFieldInConstraintLayoutScreen.kt#:~:text=themes%20%3D%20W3WAutoSuggestTextFieldDefaults,)
and for an example of that.

However to apply a custom style to the internal W3WAutoSuggestEditText that's used in
W3WAutoSuggestTextField, your custom style must provide a resource value for the
W3WAutoSuggestEditText default style attribute which is called customW3WAutoSuggestEditTextStyle. If
you don’t do this then the internal W3WAutoSuggestEditText used by the W3WAutoSuggestTextField won’t
be inflated correctly in Jetpack compose.

For instance the code sample below depicts how to go about specifying a dark theme for the
W3WAutoSuggestEditText used internally in the W3WAutoSuggestTextField. You can also check the
compose-sample in this project for more examples.

```XML

<style name="W3WAutoSuggestEditTextDayNightTheme" parent="@android:style/Theme">
    <item name="customW3WAutoSuggestEditTextStyle">
        @style/Widget.AppCompat.W3WAutoSuggestEditTextDayNight
    </item>
</style>


<style name="Widget.AppCompat.W3WAutoSuggestEditTextDayNight" parent="Widget.AppCompat.EditText">
<item name="isDayNightEnabled">true</item>
<item name="android:textColor">@color/textColor</item>
<item name="android:layout_width">match_parent</item>
<item name="android:layout_height">wrap_content</item>
<item name="android:background">@drawable/bg_with_border</item>
<item name="android:textColorHint">@color/hintColor</item>
<item name="android:hint">@string/input_hint</item>
<item name="android:minHeight">@dimen/input_height</item>
<item name="android:paddingRight">@dimen/xlarge_margin</item>
<item name="android:paddingLeft">@dimen/xlarge_margin</item>
<item name="android:paddingTop">@dimen/large_margin</item>
<item name="android:paddingBottom">@dimen/large_margin</item>
<item name="android:inputType">textNoSuggestions</item>
<item name="android:includeFontPadding">false</item>
<item name="android:maxLines">1</item>
<item name="android:imeOptions">flagNoExtractUi|flagNoFullscreen|actionDone</item>
<item name="android:textCursorDrawable">@drawable/cursor</item>
<item name="android:textAppearance">@style/W3WAutoSuggestEditTextTextAppearance</item>
</style>
```

```kotlin
W3WAutoSuggestTextField(
    modifier = Modifier.constrainAs(ref = w3wTextFieldRef) {
        linkTo(start = parent.start, end = parent.end)
        top.linkTo(anchor = headerTxtRef.bottom)
    },
    ref = w3wTextFieldRef,
    configuration = AutoSuggestConfiguration.Api(apiKey = BuildConfig.W3W_API_KEY),
    onSuggestionWithCoordinates = { },
    themes = W3WAutoSuggestTextFieldDefaults.themes(
        autoSuggestEditTextTheme = R.style.W3WAutoSuggestEditTextDayNightTheme // APPLY THE CUSTOM THEME DEFINED IN XML TO THE INTERNAL w3wAutoSuggestEditText 
    )
)
```

![alt text](https://github.com/what3words/w3w-android-components/blob/master/assets/screen_4.png?raw=true "Screenshot 4")![alt text](https://github.com/what3words/w3w-android-components/blob/master/assets/screen_5.png?raw=true "Screenshot 5")![alt text](https://github.com/what3words/w3w-android-components/blob/master/assets/screen_6.png?raw=true "Screenshot 6")
