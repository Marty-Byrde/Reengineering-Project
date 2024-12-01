package org.billthefarmer.editor

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KotlinIntTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(Editor::class.java)


    @Test
    fun exampleIntegration(){
        val filePath = ""

        onView(withId(R.id.open)).perform(click())
    }
}