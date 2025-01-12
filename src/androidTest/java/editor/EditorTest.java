package editor;

import static org.junit.Assert.*;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.billthefarmer.editor.Editor;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

@RunWith(AndroidJUnit4.class)
public class EditorTest {

    @Test
    public void testReadFile() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        String fileContent = "This is a test file.";
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(fileContent);
        }

        try (ActivityScenario<Editor> scenario = ActivityScenario.launch(Editor.class)) {
            scenario.onActivity(activity -> {
                try {
                    CharSequence content = activity.readFile(tempFile);
                    assertEquals("File content should match", fileContent + System.lineSeparator(), content.toString());
                } catch (Exception e) {
                    fail("Exception occurred while testing readFile: " + e.getMessage());
                }
            });
        }

        tempFile.delete();
    }

    @Test
    public void testSaveFile() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        String contentToSave = "Save this content to file.";

        try (ActivityScenario<Editor> scenario = ActivityScenario.launch(Editor.class)) {
            scenario.onActivity(activity -> {
                try {
                    activity.write(contentToSave, tempFile);
                } catch (Exception e) {
                    fail("Exception occurred while testing saveFile: " + e.getMessage());
                }
            });
        }

        String savedContent = new String(Files.readAllBytes(tempFile.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        assertEquals("Content in the file should match", contentToSave, savedContent);

        tempFile.delete();
    }

    @Test
    public void testNewFile() {
        try (ActivityScenario<Editor> scenario = ActivityScenario.launch(Editor.class)) {
            scenario.onActivity(activity -> {
                File newFile = activity.getNewFile();

                assertNotNull("New file should not be null", newFile);
                assertTrue("File name should end with 'Untitled.txt'", newFile.getName().endsWith("Untitled.txt"));
            });
        }
    }
}
