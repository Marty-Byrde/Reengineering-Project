
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isFocusable
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withResourceName
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.billthefarmer.editor.Editor
import org.billthefarmer.editor.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class KotlinIntegrationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(Editor::class.java)

    @Test
    fun testCreateAndSaveFile_WithContent(){
        val TEXT_INPUT = "Hello from this test-case!"
        val FILE_PATH = "Documents/Example-File.txt"

        onView(withId(R.id.newFile)).check(matches((isClickable())))
        onView(withId(R.id.newFile)).perform(click())

        onView(withId(R.id.text)).perform(click(), replaceText(TEXT_INPUT))
        onView(withId(R.id.text)).check(matches(withText(TEXT_INPUT)))

        onView(withId(R.id.save)).check(matches(isClickable()))
        onView(withId(R.id.save)).perform(click())

        // Define Save Location
        onView(withId(R.id.pathText)).perform(replaceText(FILE_PATH))
        onView(
           allOf(
               withText("Save"),
               withResourceName("button1") // identifier of the save-button of file-save-dialog.
           )
        )
            .check(matches(isClickable()))
            .check(matches(isFocusable()))
            .perform(click())
    }


    @Test
    fun exampleIntegration(){
        val filePath = ""


        val textView = withId(R.id.text)
        System.out.println(textView.toString())
        onView(textView).perform(click())
    }
}