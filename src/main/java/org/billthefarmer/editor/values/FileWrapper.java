package org.billthefarmer.editor.values;

import android.net.Uri;

import java.io.File;

public class FileWrapper {
    public File file;
    public String path;
    public Uri uri;
    public Uri content;

    private static FileWrapper instance;
    private FileWrapper(){

    }
    public static synchronized FileWrapper getInstance() {
        if (instance == null) {
            instance = new FileWrapper();
        }
        return instance;
    }
}
