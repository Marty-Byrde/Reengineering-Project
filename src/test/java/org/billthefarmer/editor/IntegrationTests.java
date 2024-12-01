package org.billthefarmer.editor;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class IntegrationTests {

    @Rule
    ActivityScenarioRule<Editor> rule = new ActivityScenarioRule<>(Editor.class);

    @Test
    public void openFileTest(){
//        rule.getScenario().onActivity(activity -> {

//        });
        onView(withId(R.id.open)).perform(click());


    }
}
