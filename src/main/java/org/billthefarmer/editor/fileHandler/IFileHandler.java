package org.billthefarmer.editor.fileHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public interface IFileHandler {
    CharSequence readFileFromFile(File file);
    void writeToFile(CharSequence text, File file,String charset) throws IOException;
    void writeToOutputStream(CharSequence text, OutputStream os, String charset) throws IOException;
}
