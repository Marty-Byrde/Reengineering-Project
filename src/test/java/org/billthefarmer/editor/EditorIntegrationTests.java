package org.billthefarmer.editor;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static java.util.regex.Pattern.matches;

import android.net.Uri;


import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.ActivityTestRule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EditorIntegrationTests {

    public ActivityScenarioRule<Editor> activityTestRule;

    @BeforeEach
    void setup(){
         activityTestRule = new ActivityScenarioRule<>(Editor.class);
    }

    @Test
    public void testOpenFile() {

//        activityTestRule.getScenario()
//        activityTestRule.getScenario().runOnUiThread(() -> {
//            Uri testFileUri = Uri.parse("file:///android_asset/testfile.txt");
////            onData(withId(R.id.openFile)).perform(click());
//        });

//        onView(withId(R.id.text)).check(matches(withText("Expected file content")));
    }

}
