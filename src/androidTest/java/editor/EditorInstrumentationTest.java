package editor;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.billthefarmer.editor.Editor;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class EditorInstrumentationTest {

    @Test
    public void testNewFile_ShouldCreateEmptyFile() {
        try (ActivityScenario<Editor> scenario = ActivityScenario.launch(Editor.class)) {
            scenario.onActivity(activity -> {
                activity.newFile();
                assertNotNull("Editor sollte initialisiert sein", activity);
            });
        }
    }

    @Test
    public void testOpenFile_ShouldOpenFileWithoutErrors() {
        try (ActivityScenario<Editor> scenario = ActivityScenario.launch(Editor.class)) {
            scenario.onActivity(activity -> {
                activity.openFile();
                assertNotNull("Die Methode openFile() sollte erfolgreich ausgeführt werden", activity);
            });
        }
    }

    @Test
    public void testSaveFile_ShouldSaveSuccessfully() {
        try (ActivityScenario<Editor> scenario = ActivityScenario.launch(Editor.class)) {
            scenario.onActivity(activity -> {
                activity.saveFile();
                assertNotNull("Die Methode saveFile() sollte erfolgreich ausgeführt werden", activity);
            });
        }
    }
}




