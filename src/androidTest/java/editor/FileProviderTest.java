package editor;

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
import static org.mockito.Mockito.*;

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
        File mockFile = mock(File.class);
        when(mockFile.createNewFile()).thenReturn(true);

        boolean created = mockFile.createNewFile();
        assertTrue("File should be created successfully", created);

        verify(mockFile, times(1)).createNewFile();
    }

    @Test
    public void testFileProviderUriPermissions() {
        // Arrange: Create a temporary file
        File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "testfile.txt");

        try {
            if (!tempFile.exists()) {
                assertTrue("Temporary file should be created", tempFile.createNewFile());
            }
        } catch (Exception e) {
            fail("File creation failed: " + e.getMessage());
        }

        // Act: Get URI for the file through FileProvider
        Uri fileUri = FileProvider.getUriForFile(context, "com.example.fileprovider", tempFile);

        // Assert: Verify the URI is not null and well-formed
        assertNotNull("URI should not be null", fileUri);
        assertTrue("URI should start with content://", fileUri.toString().startsWith("content://"));

        // Clean up
        if (tempFile.exists()) {
            assertTrue("Temporary file should be deleted", tempFile.delete());
        }
    }
}

