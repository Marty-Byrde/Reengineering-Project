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
        // Arrange: Create a temporary file with sample content
        File tempFile = File.createTempFile("test", ".txt");
        String fileContent = "This is a test file.";
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(fileContent);
        }

        // Act: Launch the activity and call the readFile method
        try (ActivityScenario<Editor> scenario = ActivityScenario.launch(Editor.class)) {
            scenario.onActivity(activity -> {
                try {
                    CharSequence content = activity.readFile(tempFile); // Zugriff auf die Methode
                    // Assert: Verify that the file content matches
                    assertEquals("File content should match", fileContent + System.lineSeparator(), content.toString());
                } catch (Exception e) {
                    fail("Exception occurred while testing readFile: " + e.getMessage());
                }
            });
        }

        // Cleanup
        tempFile.delete();
    }

    @Test
    public void testSaveFile() throws IOException {
        // Arrange: Create a temporary file and some content
        File tempFile = File.createTempFile("test", ".txt");
        String contentToSave = "Save this content to file.";

        // Act: Launch the activity and call the saveFile method
        try (ActivityScenario<Editor> scenario = ActivityScenario.launch(Editor.class)) {
            scenario.onActivity(activity -> {
                try {
                    activity.write(contentToSave, tempFile); // Zugriff auf die Methode
                } catch (Exception e) {
                    fail("Exception occurred while testing saveFile: " + e.getMessage());
                }
            });
        }

        // Assert: Verify that the content was saved correctly
        String savedContent = new String(Files.readAllBytes(tempFile.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        assertEquals("Content in the file should match", contentToSave, savedContent);

        // Cleanup
        tempFile.delete();
    }

    @Test
    public void testNewFile() {
        // Act: Launch the activity and call the getNewFile method
        try (ActivityScenario<Editor> scenario = ActivityScenario.launch(Editor.class)) {
            scenario.onActivity(activity -> {
                File newFile = activity.getNewFile(); // Zugriff auf die Methode

                // Assert: Verify the file path and name
                assertNotNull("New file should not be null", newFile);
                assertTrue("File name should end with 'Untitled.txt'", newFile.getName().endsWith("Untitled.txt"));
            });
        }
    }
}
