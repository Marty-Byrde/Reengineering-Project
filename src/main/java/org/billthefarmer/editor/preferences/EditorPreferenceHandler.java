package org.billthefarmer.editor.preferences;

import static org.billthefarmer.editor.Editor.MONOSPACE;
import static org.billthefarmer.editor.preferences.EditorPreferenceParameters.*;
import static org.billthefarmer.editor.preferences.Preferences.*;

import android.content.SharedPreferences;
import android.content.res.Resources;

import org.billthefarmer.editor.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class EditorPreferenceHandler {
    private EditorPreferenceHandler (){}

    public static HashMap<Preferences, Object> fetchPreferences(Resources resources, SharedPreferences sharedPreferences){
        HashMap<Preferences, Object> preferences = new HashMap<>();

        String[] typefaces = resources.getStringArray(R.array.typefaces);
        List<String> typeList = Arrays.asList(typefaces);
        int monospace = typeList.indexOf(MONOSPACE);

        Boolean isAutoSaveEnabled = sharedPreferences.getBoolean(PREF_SAVE, false);
        Boolean isFileReadOnly = sharedPreferences.getBoolean(PREF_VIEW, true);
        Boolean last = sharedPreferences.getBoolean(PREF_LAST, false);
        Boolean wrap = sharedPreferences.getBoolean(PREF_WRAP, false);
        Boolean suggest = sharedPreferences.getBoolean(PREF_SUGGEST, true);
        Boolean highlight = sharedPreferences.getBoolean(PREF_HIGH, false);
        Set<String> pathSetPref = sharedPreferences.getStringSet(PREF_PATHS, null);

        int theme = sharedPreferences.getInt(PREF_THEME, LIGHT);
        int size = sharedPreferences.getInt(PREF_SIZE, MEDIUM);
        int type = sharedPreferences.getInt(PREF_TYPE, monospace);

        preferences.put(autoSaveFeature, isAutoSaveEnabled);
        preferences.put(isReadOnly, isFileReadOnly);
        preferences.put(isLast, last);
        preferences.put(isContentWrapped, wrap);
        preferences.put(isSuggestEnabled, suggest);
        preferences.put(isHighlightEnabled, highlight);
        preferences.put(pathSet, pathSetPref);

        preferences.put(Theme, theme);
        preferences.put(FontSize, size);
        preferences.put(FontType, type);


        return preferences;
    }
}

