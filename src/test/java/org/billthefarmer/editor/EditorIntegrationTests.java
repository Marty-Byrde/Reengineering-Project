package org.billthefarmer.editor;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static java.util.regex.Pattern.matches;

import android.net.Uri;


import androidx.test.rule.ActivityTestRule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EditorIntegrationTests {

    public ActivityTestRule<Editor> activityTestRule;

    @BeforeEach
    void setup(){
         activityTestRule = new ActivityTestRule<>(Editor.class);
    }

    @Test
    public void testOpenFile() {
        // Simulate opening a file (mock or use a test file in assets)
        // Assume we have a method in Editor to open a file for testing
        activityTestRule.getActivity().runOnUiThread(() -> {
            Uri testFileUri = Uri.parse("file:///android_asset/testfile.txt");
//            activityTestRule
            onData(withId(R.id.openFile)).perform(click());
        });

        // Check if the EditText contains the expected text
//        onView(withId(R.id.text)).check(matches(withText("Expected file content")));
    }

}
