
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotClickable
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.billthefarmer.editor.Editor
import org.billthefarmer.editor.R
import org.hamcrest.core.IsNot.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KotlinIntegrationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(Editor::class.java)

    @Test
    fun testCreateAndSaveFile_WithContent(){

        // Verify pre-conditions edit visible, view invisible
        onView(withId(R.id.view)).check(matches((isNotClickable())))
        onView(withId(R.id.edit)).check(matches((isClickable())))


        // Click edit icon to disable read-only mode
        onView(withId(R.id.edit)).perform(click())

        onView((withId(R.id.edit))).check(matches(withText("")))

        // Edit button should now be invisible
        onView(withId(R.id.edit)).check(matches(isNotClickable()))
        onView(withId(R.id.view)).check(matches(isClickable()))
    }


    @Test
    fun exampleIntegration(){
        val filePath = ""


        val textView = withId(R.id.text)
        System.out.println(textView.toString())
        onView(textView).perform(click())
    }
}