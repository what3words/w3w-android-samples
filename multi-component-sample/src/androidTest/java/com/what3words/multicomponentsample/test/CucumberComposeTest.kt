package com.what3words.multicomponentsample.test

import android.content.Intent
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.what3words.components.R
import com.what3words.multicomponentsample.MultiComponentsActivity
import com.what3words.multicomponentsample.test.utils.hasItemCountGreaterThanZero
import com.what3words.multicomponentsample.test.utils.waitUntilVisible
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When


class CucumberComposeTest(
    private val composeRuleHolder: ComposeRuleHolder,
    private val scenarioHolder: ActivityScenarioHolder
) :
    SemanticsNodeInteractionsProvider by composeRuleHolder.composeRule {

    @Given("^I initialize App")
    fun initializeApp() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        scenarioHolder.launch(Intent(appContext, MultiComponentsActivity::class.java))
    }

    @Then("Show text input and show suggestion")
    fun showTextInputAndShowSuggestion() {
        onView(withText("///filled.count.soa")).check(matches(isDisplayed()))
    }

    @When("I enter the text on AutoTextField")
    fun iEnterTheTextOnAutoTextField() {
        onView(withHint("e.g. ///lock.spout.radar"))
            .perform(
                click(),
                replaceText("///filled.count.soa")
            )
    }

    @OptIn(ExperimentalTestApi::class)
    @When("I choose option")
    fun iChooseOption() {
//        onNode(hasText("///filled.count.soap"),true)
//            .assertIsDisplayed().performClick()

//        Espresso.onView(withText("///filled.count.soap"))
//            .perform(
//                click(),
//            )
        onView(
            withId(R.id.w3wAutoSuggestDefaultPicker)
        )
            .perform(waitUntilVisible(hasItemCountGreaterThanZero()))
            .check(matches(isDisplayed()))
    }

    @Then("Map navigate")
    fun iMapNavigate() {
        onView(withText("filled.count.soap"))
            .perform(
                click(),
            )

        onView(withHint("e.g. ///lock.spout.radar"))
            .perform(waitUntilVisible(withText("///filled.count.soap")))

    }

    @And("Map show marker")
    fun mapShowMarker() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val marker = device.findObject(UiSelector().descriptionContains("marker title"))
        marker.click()

        Thread.sleep(5000)

    }

}

