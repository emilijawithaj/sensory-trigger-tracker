package com.example.soverloadtracker

import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.chip.Chip
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditTriggersActivityTest {

    //activity rule
    @get:Rule
    val activityRule = ActivityScenarioRule(EditTriggersActivity::class.java)
    private lateinit var db: SqLiteDatabase

    @Before
    fun setup() {
        //clear database before tests
        db = SqLiteDatabase.getInstance(ApplicationProvider.getApplicationContext())
        val triggers = db.getTriggers()
        for (trigger in triggers) {
            db.deleteTrigger(trigger)
        }
    }

    @Test
    fun testAddTrigger() {
        val testTrigger = "Test Trigger"

        //trigger add button click and entry of test trigger
        onView(withId(R.id.add_trigger_button)).perform(click())
        onView(withId(R.id.trigger_input)).perform(typeText(testTrigger), closeSoftKeyboard())
        onView(withText(R.string.dialog_ok_text)).perform(click())

        //test chipGroup for the trigger
        onView(withText(testTrigger)).check(matches(isDisplayed()))
    }

    @Test
    fun testAddTriggerEmptyInputShowsError() {
        //trigger add button and continue without entering text
        onView(withId(R.id.add_trigger_button)).perform(click())
        onView(withText(R.string.dialog_ok_text)).perform(click())

        //check for error snackbar
        onView(withText("Trigger cannot be empty")).check(matches(isDisplayed()))
    }

    @Test
    fun testRemoveTriggerRemovesChip() {
        val testTrigger = "Test Trigger"

        //Load in test trigger
        onView(withId(R.id.add_trigger_button)).perform(click())
        onView(withId(R.id.trigger_input)).perform(typeText(testTrigger), closeSoftKeyboard())
        onView(withText(R.string.dialog_ok_text)).perform(click())
        onView(withText(testTrigger)).check(matches(isDisplayed()))

        //trigger close button on the chip
        onView(withText(testTrigger)).perform(closeChip())

        //check the chip is gone
        onView(withText(testTrigger)).check(ViewAssertions.doesNotExist())
        onView(withText(R.string.no_triggers_text)).check(matches(isDisplayed()))
    }

    /**
     * Assistant method to trigger the close icon on a trigger chip
     */
    fun closeChip(): ViewAction = actionWithAssertions(
        object : ViewAction {
            override fun getConstraints() = isAssignableFrom(Chip::class.java)
            override fun getDescription() = "click close"
            override fun perform(uiController: UiController?, view: View?) {
                val chip = view as Chip
                chip.performCloseIconClick()
            }
        }
    )
}