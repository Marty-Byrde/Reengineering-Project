package org.billthefarmer.editor.values;

import android.content.Context;

import static org.billthefarmer.editor.preferences.EditorPreferenceParameters.MEDIUM;

public class SharedVariables {

    public String match;
    public boolean changed = false;
    public long modified;
    public int size = MEDIUM;
    public Runnable updateHighlight;
    public int syntax;

    public Context appContext;


    private static SharedVariables instance;
    private SharedVariables(){

    }

    public static synchronized SharedVariables getInstance() {
        if (instance == null) {
            instance = new SharedVariables();
        }
        return instance;
    }
}
