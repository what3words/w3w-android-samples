package com.what3words.samples.multiple.test.steps

import android.content.Intent
import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.what3words.components.maps.extensions.generateUniqueId
import com.what3words.javawrapper.response.Coordinates
import com.what3words.samples.multiple.MultiComponentsActivity
import com.what3words.samples.multiple.test.ActivityScenarioHolder
import com.what3words.samples.multiple.test.ComposeRuleHolder
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

class OCRSteps(
    private val composeRuleHolder: ComposeRuleHolder,
    private val scenarioHolder: ActivityScenarioHolder
) :
    SemanticsNodeInteractionsProvider by composeRuleHolder.composeRule {
    private val TIMEOUT = 60000L

    @Given("The main screen is visible")
    fun theMainScreenIsVisible() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        scenarioHolder.launch(Intent(appContext, MultiComponentsActivity::class.java))
    }

    @When("tapped the OCR scan button")
    fun tappedTheOCRScanButton() {
        onNodeWithTag("ocrButton").performClick()
    }

    @Then("accept the permission")
    fun acceptThePermission() {
        with(composeRuleHolder.composeRule) {
            waitForIdle()
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
    }

    @OptIn(ExperimentalTestApi::class)
    @Then("suggestions should show (\\S+)$")
    fun suggestionsShouldShow(textSuggestion: String) {
        with(composeRuleHolder.composeRule) {
            waitUntilAtLeastOneExists(hasTestTag("itemOCR $textSuggestion"), TIMEOUT).apply { isDisplayed() }
        }
    }

    @When("tapped suggestion (\\S+)$")
    fun tappedSuggestionSuggestion(textSuggestion: String) {
        with(composeRuleHolder.composeRule) {
            onAllNodesWithTag("itemOCR $textSuggestion")
                .filter(hasClickAction())
                .apply {
                    fetchSemanticsNodes().forEachIndexed { i, _ ->
                        get(i).performSemanticsAction(SemanticsActions.OnClick)
                    }
                }
        }

    }

    @OptIn(ExperimentalTestApi::class)
    @Then("the autosuggest text is (\\S+)$")
    fun theAutosuggestTextIsSuggestion(textSuggestion: String) {
        with(composeRuleHolder.composeRule) {
            waitUntilAtLeastOneExists(hasText(textSuggestion), TIMEOUT).apply {
                isDisplayed()
            }

        }
    }

    @And("map shows marker at {string}")
    fun mapShowsMarkerAtMarkerLocation(coordinatesStr: String) {
        val coordinateElements = coordinatesStr.split(", ")
        val coordinates =
            Coordinates(coordinateElements[0].toDouble(), coordinateElements[1].toDouble())
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        with(composeRuleHolder.composeRule) {
            waitForIdle()
            onView(withContentDescription("Zoom out")).perform(click())
            onView(withContentDescription("Zoom out")).perform(click())
            onView(withContentDescription("Zoom out")).perform(click())

            val zoomOutMarker = uiDevice.findObject(By.desc(coordinates.generateUniqueId().toString()))
            assert(zoomOutMarker != null)
        }
    }
}
