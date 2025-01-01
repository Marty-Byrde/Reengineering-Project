package example;

import org.billthefarmer.editor.Editor;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;



import static org.junit.Assert.*;

public class EditorTest {

    private Editor editor;

    @Before
    public void setUp() {
        editor = new Editor();
    }

    @Test
    public void testReadFile() throws IOException {
        // Arrange: Create a temporary file with sample content
        File tempFile = File.createTempFile("test", ".txt");
        String fileContent = "This is a test file.";
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(fileContent);
        }

        // Act: Call the readFile method (private method tested using reflection)
        StringBuilder content = new StringBuilder();
        try {
            java.lang.reflect.Method method = Editor.class.getDeclaredMethod("readFile", File.class);
            method.setAccessible(true);
            CharSequence result = (CharSequence) method.invoke(editor, tempFile);

            content.append(result);
        } catch (Exception e) {
            fail("Exception occurred while testing readFile: " + e.getMessage());
        }

        // Assert: Verify that the file content matches
        assertEquals("File content should match", fileContent + System.lineSeparator(), content.toString());

        // Cleanup
        tempFile.delete();
    }

    @Test
    public void testSaveFile() throws IOException {
        // Arrange: Create a temporary file and some content
        File tempFile = File.createTempFile("test", ".txt");
        String contentToSave = "Save this content to file.";

        // Act: Call the saveFile method using reflection
        try {
            java.lang.reflect.Method method = Editor.class.getDeclaredMethod("write", CharSequence.class, File.class);
            method.setAccessible(true);
            method.invoke(editor, contentToSave, tempFile);
        } catch (Exception e) {
            fail("Exception occurred while testing saveFile: " + e.getMessage());
        }

        // Assert: Verify that the content was saved correctly
        String savedContent = new String(Files.readAllBytes(tempFile.toPath())); // Alternative zu Files.readString()
        assertEquals("Content in the file should match", contentToSave, savedContent);

        // Cleanup
        tempFile.delete();
    }


    @Test
    public void testNewFile() {
        // Act: Create a new file
        File newFile = Editor.getNewFile();

        // Assert: Verify the file path and name
        assertNotNull("New file should not be null", newFile);
        assertTrue("File name should end with 'Untitled.txt'", newFile.getName().endsWith("Untitled.txt"));
    }
}
