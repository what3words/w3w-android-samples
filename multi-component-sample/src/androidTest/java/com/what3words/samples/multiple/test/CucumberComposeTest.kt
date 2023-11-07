package com.what3words.samples.multiple.test

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
import com.what3words.samples.multiple.MultiComponentsActivity
import com.what3words.samples.multiple.test.utils.hasItemCountGreaterThanZero
import com.what3words.samples.multiple.test.utils.waitUntilVisible
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

    @When("I choose option")
    fun iChooseOption() {
        onView(
            withId(R.id.w3wAutoSuggestDefaultPicker)
        )
            .perform(waitUntilVisible(hasItemCountGreaterThanZero()))
            .check(matches(isDisplayed()))
    }

    @And("Map navigate")
    fun iMapNavigate() {
        onView(withText("filled.count.soap"))
            .perform(
                click(),
            )

        onView(withHint("e.g. ///lock.spout.radar"))
            .perform(waitUntilVisible(withText("///filled.count.soap")))

    }
}
