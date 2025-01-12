package org.billthefarmer.editor;


import android.content.Context;
import android.support.v4.content.FileProvider;
import androidx.test.core.app.ApplicationProvider;
import android.os.Environment;
import android.net.Uri;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class FileProviderTest {

    private FileProvider fileProvider;
    private Context context;

    @Before
    public void setUp() {
        fileProvider = new FileProvider();
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testFileProviderInitialization() {
        assertNotNull("FileProvider instance should not be null", fileProvider);
    }

    @Test
    public void testFileCreation() throws IOException {
        File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "testfile.txt");

        if (!tempFile.exists()) {
            assertTrue("Temporary file should be created", tempFile.createNewFile());
        }

        assertTrue("Temporary file should exist", tempFile.exists());
        assertTrue("Temporary file should be deleted", tempFile.delete());
    }

    @Test
    public void testFileProviderUriPermissions() {
        File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "testfile.txt");

        try {
            if (!tempFile.exists()) {
                assertTrue("Temporary file should be created", tempFile.createNewFile());
            }
        } catch (IOException e) {
            fail("File creation failed: " + e.getMessage());
        }

        Uri fileUri = FileProvider.getUriForFile(context, "org.billthefarmer.editor.fileprovider", tempFile);

        assertNotNull("URI should not be null", fileUri);
        assertTrue("URI should start with content://", fileUri.toString().startsWith("content://"));

        if (tempFile.exists()) {
            assertTrue("Temporary file should be deleted", tempFile.delete());
        }
    }


    @Test
    public void testInvalidFileUri() {
        File invalidFile = new File(context.getFilesDir(), "invalidfile.txt");

        try {
            FileProvider.getUriForFile(context, "org.billthefarmer.editor.fileprovider", invalidFile);
            fail("Expected IllegalArgumentException for non-configured file");
        } catch (IllegalArgumentException e) {
            assertNotNull("Exception message should not be null", e.getMessage());
        }
    }
}
