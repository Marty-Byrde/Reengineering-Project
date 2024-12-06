
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.billthefarmer.editor.Editor
import org.billthefarmer.editor.R
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KotlinIntegrationTest {

//    @get:Rule
//    val activityRule = ActivityScenarioRule(Editor::class.java)


    @Test
    fun exampleIntegration(){
        val filePath = ""


        val textView = withId(R.id.text)
        System.out.println(textView.toString())
        onView(textView).perform(click())
    }
}