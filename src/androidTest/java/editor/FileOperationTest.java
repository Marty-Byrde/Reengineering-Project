package editor;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FileOperationTest {

    private Context context;
    private File testFile;
    private static final String FILE_NAME = "test_file.txt";

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        testFile = new File(context.getFilesDir(), FILE_NAME);
    }

    @After
    public void tearDown() {
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    @Test
    public void testCreateFile() throws IOException {
        if (!testFile.exists()) {
            boolean created = testFile.createNewFile();
            assertTrue("File should be created", created);
        }

        assertTrue("File should exist", testFile.exists());
    }

    @Test
    public void testModifyFile() throws IOException {
        if (!testFile.exists()) {
            testFile.createNewFile();
        }

        FileWriter writer = new FileWriter(testFile);
        String content = "Hello, this is a test!";
        writer.write(content);
        writer.close();

        Scanner scanner = new Scanner(testFile);
        String readContent = scanner.nextLine();
        scanner.close();

        assertEquals("Content should match", content, readContent);
    }

    @Test
    public void testDeleteFile() {
        if (!testFile.exists()) {
            try {
                testFile.createNewFile();
            } catch (IOException e) {
                fail("File creation failed during delete test");
            }
        }

        boolean deleted = testFile.delete();

        assertTrue("File should be deleted", deleted);
        assertFalse("File should no longer exist", testFile.exists());
    }

    @Test
    public void testSearchInFile() throws IOException {
        if (!testFile.exists()) {
            testFile.createNewFile();
        }

        FileWriter writer = new FileWriter(testFile);
        String content = "Search this content";
        writer.write(content);
        writer.close();

        Scanner scanner = new Scanner(testFile);
        String searchString = "this";
        boolean found = false;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains(searchString)) {
                found = true;
                break;
            }
        }
        scanner.close();

        assertTrue("Search string should be found", found);
    }
}
