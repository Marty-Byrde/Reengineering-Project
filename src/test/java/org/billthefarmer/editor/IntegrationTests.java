package org.billthefarmer.editor;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IntegrationTests {

    ActivityScenarioRule<Editor> rule = new ActivityScenarioRule<>(Editor.class);

    @Test
    public void openFileTest(){
        onView(withId(R.id.open)).perform(click());


    }
}
