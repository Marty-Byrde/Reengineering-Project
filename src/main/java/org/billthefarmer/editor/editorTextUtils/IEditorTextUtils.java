package org.billthefarmer.editor.editorTextUtils;

import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.Map;

public interface IEditorTextUtils {
    void wordCountText(TextView textView, TextView customView);
    void checkHighlight(Map editorPreferences, File file, EditText textView, ScrollView scrollView, Runnable updateHighlight);
    void highlightText(ScrollView scrollView, EditText textView);
}
