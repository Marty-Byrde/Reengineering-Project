package editor;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.billthefarmer.editor.Editor;
import org.billthefarmer.editor.R;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class EditorEspressoTests {

    private CountingIdlingResource idlingResource;

    @Before
    public void setUp() {
        idlingResource = new CountingIdlingResource("UI");
        IdlingRegistry.getInstance().register(idlingResource);
    }

    @After
    public void tearDown() {
        IdlingRegistry.getInstance().unregister(idlingResource);
    }

    @Test
    public void testNewFile_ShouldClearText() {
        try (ActivityScenario<Editor> scenario = ActivityScenario.launch(Editor.class)) {
            scenario.onActivity(activity -> {
                idlingResource.increment();
                activity.newFile();
                idlingResource.decrement();
            });
            onView(withId(R.id.text)).check(matches(withText("")));
        }
    }

    @Test
    public void testOpenFile_ShouldOpenFile() {
        try (ActivityScenario<Editor> scenario = ActivityScenario.launch(Editor.class)) {
            scenario.onActivity(activity -> {
                idlingResource.increment();
                activity.openFile();
                idlingResource.decrement();
            });
            onView(withId(R.id.text)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testSaveFile_ShouldSaveFile() {
        try (ActivityScenario<Editor> scenario = ActivityScenario.launch(Editor.class)) {
            scenario.onActivity(activity -> {
                idlingResource.increment();
                activity.getWindow().getDecorView().requestFocus(); // Fokus erzwingen
                activity.saveFile();
                idlingResource.decrement();
            });
            onView(withText("Datei gespeichert")).check(matches(isDisplayed()));
        }
    }


}


