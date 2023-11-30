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
import io.cucumber.java.PendingException

class CucumberComposeTest(
    private val composeRuleHolder: ComposeRuleHolder,
    private val scenarioHolder: ActivityScenarioHolder
) :
    SemanticsNodeInteractionsProvider by composeRuleHolder.composeRule {
    private val TIME_OUT = 60000L

    @Given("Given the main screen is visible")
    fun givenTheMainScreenIsVisible() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        scenarioHolder.launch(Intent(appContext, MultiComponentsActivity::class.java))
    }

    @OptIn(ExperimentalTestApi::class)
    @And("App show ocr button")
    fun appShowOcrButton() {
        with(composeRuleHolder.composeRule) {
            waitUntilAtLeastOneExists(hasTestTag("ocrButton"), TIME_OUT).apply { isDisplayed() }
        }
    }
}
