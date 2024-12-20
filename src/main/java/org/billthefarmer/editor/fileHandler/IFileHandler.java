package org.billthefarmer.editor.fileHandler;

import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IFileHandler {
    CharSequence readFileFromFile(File file);
    CharSequence readFileFromUri(Uri uri);
    File getNewFile();
    void saveFile(Object input,CharSequence textContent) throws IOException;
    List<File> getList(File dir);
}
