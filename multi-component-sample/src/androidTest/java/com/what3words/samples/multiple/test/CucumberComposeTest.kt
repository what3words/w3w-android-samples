package com.what3words.samples.multiple.test

import android.content.Intent
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.what3words.components.maps.extensions.generateUniqueId
import com.what3words.javawrapper.response.Coordinates
import com.what3words.samples.multiple.MultiComponentsActivity
import com.what3words.samples.multiple.test.utils.hasItemCountGreaterThanZero
import com.what3words.samples.multiple.test.utils.waitUntilVisible
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

class CucumberComposeTest(
    private val composeRuleHolder: ComposeRuleHolder,
    private val scenarioHolder: ActivityScenarioHolder
) :
    SemanticsNodeInteractionsProvider by composeRuleHolder.composeRule {

    @Given("The main screen is visible")
    fun initializeApp() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        scenarioHolder.launch(Intent(appContext, MultiComponentsActivity::class.java))
    }

    @When("I type {string} into auto suggest text field")
    fun i_type_into_auto_suggest_text_field(incompleteW3W: String) {
        Espresso.onView(withHint("e.g. ///lock.spout.radar"))
            .perform(
                click(),
                replaceText(incompleteW3W)
            )
    }

    @Then("Suggestions should show")
    fun suggestions_should_show() {
        Espresso.onView(
            withId(com.what3words.components.R.id.w3wAutoSuggestDefaultPicker)
        )
            .perform(waitUntilVisible(hasItemCountGreaterThanZero()))
            .check(matches(isDisplayed()))
    }

    @When("I tape suggestion {string}")
    fun i_tape_suggestion(suggestion: String) {
        Espresso.onView(withText(suggestion)).perform(click())
    }

    @Then("The auto suggestion text is {string}")
    fun the_auto_suggestion_text_is(text: String) {
        Espresso.onView(withHint("e.g. ///lock.spout.radar"))
            .perform(waitUntilVisible(withText(text)))
    }

    @Then("Map show maker at {string}")
    fun map_show_maker_at(coordinatesStr: String) {
        val coordinateElements = coordinatesStr.split(", ")
        val coordinates =
            Coordinates(coordinateElements[0].toDouble(), coordinateElements[1].toDouble())
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.waitForIdle()
        Espresso.onView(withContentDescription("Zoom out")).perform(click())
        Espresso.onView(withContentDescription("Zoom out")).perform(click())
        val zoomOutMarker = uiDevice.findObject(By.desc(coordinates.generateUniqueId().toString()))

        assert(zoomOutMarker != null)
    }
}

