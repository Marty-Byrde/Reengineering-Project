package org.billthefarmer.editor.fileHandler;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public interface IFileHandler {
    CharSequence readFileFromFile(File file);
    void writeToFile(CharSequence text, File file,String charset) throws IOException;
    void writeToUri(CharSequence text, String charset,Uri uri) throws IOException;
    File getNewFile();
    CharSequence readFileFromUri(Uri uri);
}
