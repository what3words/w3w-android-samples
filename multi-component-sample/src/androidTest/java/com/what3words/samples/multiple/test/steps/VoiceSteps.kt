package com.what3words.samples.multiple.test.steps

import android.os.Build
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.voice.VoiceProvider
import com.what3words.components.R
import com.what3words.components.maps.extensions.generateUniqueId
import com.what3words.javawrapper.response.Coordinates
import com.what3words.ocr.components.models.W3WOcrMLKitWrapper
import com.what3words.samples.multiple.BuildConfig
import com.what3words.samples.multiple.MultiComponentsViewModel
import com.what3words.samples.multiple.test.ActivityScenarioHolder
import com.what3words.samples.multiple.test.CustomComposableRuleHolder
import com.what3words.samples.multiple.test.utils.MockVoiceApi
import com.what3words.samples.multiple.test.utils.hasItemCountGreaterThanZero
import com.what3words.samples.multiple.test.utils.waitUntilVisible
import com.what3words.samples.multiple.home.HomeScreen
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

class VoiceSteps(
    private val composeRuleHolder: CustomComposableRuleHolder,
    private val scenarioHolder: ActivityScenarioHolder
) :
    SemanticsNodeInteractionsProvider by composeRuleHolder.composeRule {
    private lateinit var mockVoiceApi: VoiceProvider

    @Given("The main screen is visible voice")
    fun theMainScreenIsVisible() {
        composeRuleHolder.composeRule.setContent {
            val viewModel = MultiComponentsViewModel()
            val context = LocalContext.current
            val ocrWrapper = W3WOcrMLKitWrapper(context = context)

            val selectedSuggestion by viewModel.selectedSuggestion.collectAsState()
            val mockDataSound = context.assets.open("soundFilledCountSoap.dat")

            mockVoiceApi = MockVoiceApi(
                apiKey = BuildConfig.W3W_API_KEY,
                mockDataSound = mockDataSound
            )

            val dataProvider = What3WordsV3(
                apiKey = BuildConfig.W3W_API_KEY,
                context = context,
                voiceProvider = mockVoiceApi
            )

            HomeScreen(
                dataProvider,
                ocrWrapper,
                true,
                selectedSuggestion = selectedSuggestion,
                onSuggestionChanged = {
                    viewModel.selectedSuggestion.value = it
                }
            )
        }
    }

    @When("tapped the voice button")
    fun tappedTheVoiceButton() {
        onView(ViewMatchers.withHint("e.g. ///lock.spout.radar")).perform(click())
        onView(withId(R.id.icMic)).perform(click())
    }

    @Then("accept the voice permission")
    fun acceptThePermission() {
        val appContext = InstrumentationRegistry.getInstrumentation()
        val allowPermission = UiDevice.getInstance(appContext).findObject(
            UiSelector().text(
                when {
                    Build.VERSION.SDK_INT == 23 -> "Allow"
                    Build.VERSION.SDK_INT <= 28 -> "ALLOW"
                    Build.VERSION.SDK_INT == 29 -> "Allow only while using the app"
                    else -> "While using the app"
                }
            )
        )
        if (allowPermission.exists()) {
            allowPermission.click()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Then("suggestions by voice should show")
    fun suggestionsShouldShow() {
        with(composeRuleHolder.composeRule) {
            waitForIdle()
            Thread.sleep(5000)
            onView(ViewMatchers.withHint("e.g. ///lock.spout.radar")).perform(click())
            onView(withId(R.id.w3wAutoSuggestDefaultPicker))
                .perform(waitUntilVisible(hasItemCountGreaterThanZero()))
                .check(ViewAssertions.matches(isDisplayed()))
        }

    }

    @When("tapped voice suggestion (\\S+)$")
    fun tappedVoiceSuggestionSuggestion(textSuggestion: String) {
        onView(
            withId(
                R.id.w3wAutoSuggestDefaultPicker
            )
        ).perform(waitUntilVisible(hasItemCountGreaterThanZero()))


        onView(withText(textSuggestion)).perform(click())

    }

    @And("map shows marker at {string}")
    fun mapShowsMarkerAtMarkerLocation(coordinatesStr: String) {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val coordinateElements = coordinatesStr.split(", ")
        val coordinates =
            Coordinates(coordinateElements[0].toDouble(), coordinateElements[1].toDouble())

        onView(ViewMatchers.withContentDescription("Zoom out")).perform(click())
        onView(ViewMatchers.withContentDescription("Zoom out")).perform(click())
        onView(ViewMatchers.withContentDescription("Zoom out")).perform(click())

        Thread.sleep(2000)
        val zoomOutMarker = uiDevice.findObject(By.desc(coordinates.generateUniqueId().toString()))
        assert(zoomOutMarker != null)
    }
}