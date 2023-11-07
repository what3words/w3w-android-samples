package com.what3words.multicomponentsample

import android.content.Intent
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.what3words.multi_component_sample.MultiComponentsActivity
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.java.PendingException

class CucumberComposeTest (
    private val composeRuleHolder: ComposeRuleHolder,
    private val scenarioHolder: ActivityScenarioHolder
):
    SemanticsNodeInteractionsProvider by composeRuleHolder.composeRule{

   @Given("^I initialize App")
   fun initializeApp(){
       val appContext = InstrumentationRegistry.getInstrumentation().targetContext
       scenarioHolder.launch(Intent(appContext, MultiComponentsActivity::class.java))
   }

    @Then("Show text input and show suggestion")
    fun showTextInputAndShowSuggestion() {
        Espresso.onView(withText("///filled.count.soa")).check(matches(isDisplayed()))
    }

    @When("I enter the text on AutoTextField")
    fun iEnterTheTextOnAutoTextField() {
        Espresso.onView(withHint("e.g. ///lock.spout.radar"))
            .perform(
                click(),
                replaceText("///filled.count.soa")
            )
    }

    @When("I choose option")
    fun iChooseOption() {
//        onNode(hasText("///filled.count.soap"),true)
//            .assertIsDisplayed().performClick()

        Espresso.onView(withText("///filled.count.soap"))
            .perform(
                click(),
            )
    }

    @Then("Map show marker")
    fun mapShowMarker() {
        Thread.sleep(10000)
    }
}

