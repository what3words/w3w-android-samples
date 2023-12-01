package com.what3words.samples.multiple.test

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.core.view.allViews
import androidx.core.view.children
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.layers.getLayer
import com.what3words.components.maps.extensions.generateUniqueId
import com.what3words.javawrapper.response.Coordinates
import com.what3words.samples.multiple.MultiComponentsActivity
import com.what3words.samples.multiple.test.utils.hasItemCountGreaterThanZero
import com.what3words.samples.multiple.test.utils.waitUntilVisible
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

const val SELECTED_ZOOMED_LAYER_ID_PREFIX = "SELECTED_ZOOMED_LAYER_%s"

class CucumberComposeTest(
    private val composeRuleHolder: ComposeRuleHolder,
    private val scenarioHolder: ActivityScenarioHolder
) :
    SemanticsNodeInteractionsProvider by composeRuleHolder.composeRule {

    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private lateinit var activity: Activity

    @Given("The main screen is visible")
    fun initializeApp() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        scenarioHolder.launch(Intent(appContext, MultiComponentsActivity::class.java))
        scenarioHolder.scenario?.onActivity {
            activity = it
        }
    }


    @Then("I change using MapBox")
    fun i_change_using_map_box() {
        uiDevice.findObject(By.desc("Map Type")).click()
        Espresso.onView(withContentDescription("MapBoxView"))
            .perform(waitUntilVisible(isDisplayed()))
    }

    @When("I type {string} into auto suggest text field")
    fun i_type_into_auto_suggest_text_field(incompleteW3W: String) {
        Espresso.onView(withId(com.what3words.components.R.id.w3wAutoSuggestionTextField))
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
        Espresso.onView(withId(com.what3words.components.R.id.w3wAutoSuggestionTextField))
            .perform(waitUntilVisible(withText(text)))
    }

    @Then("Map show maker at {string}")
    fun map_show_maker_at(coordinatesStr: String) {
        Thread.sleep(5000)
        val coordinateElements = coordinatesStr.split(", ")
        val coordinates =
            Coordinates(coordinateElements[0].toDouble(), coordinateElements[1].toDouble())

        val mapView = getMapView(activity.window.decorView)
        val selectedZoomMarker = mapView?.getMapboxMap()?.getStyle()?.getLayer(
            String.format(
                SELECTED_ZOOMED_LAYER_ID_PREFIX,
                coordinates.generateUniqueId()
            )
        )

        assert(selectedZoomMarker != null)
    }

    private fun getMapView(view: View): MapView? {
        return getAllViews(view).firstOrNull {
            it is MapView
        } as? MapView
    }

    private fun getAllViews(view: View): List<View> {
        if (view !is ViewGroup || view.childCount == 0) return listOf(view)

        return view.children
            .toList()
            .flatMap {
                it.allViews
            }
            .plus(view as View)
    }
}

