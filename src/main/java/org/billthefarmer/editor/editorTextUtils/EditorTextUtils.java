package org.billthefarmer.editor.editorTextUtils;

import android.graphics.Color;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.billthefarmer.editor.preferences.Preferences;
import org.billthefarmer.editor.helpers.FileUtils;
import org.billthefarmer.editor.values.SharedConstants;
import org.billthefarmer.editor.values.SharedVariables;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.billthefarmer.editor.helpers.SyntaxPatternParameters.*;

public class EditorTextUtils implements IEditorTextUtils{
    private static EditorTextUtils instance;
    private SharedVariables sharedVariables;
    private EditorTextUtils(){
        sharedVariables = SharedVariables.getInstance();
    }
    public static synchronized EditorTextUtils getInstance() {
        if (instance == null) {
            instance = new EditorTextUtils();
        }
        return instance;
    }
    public void wordCountText(TextView textView,TextView customView)
    {
        int words = 0;
        Matcher matcher = WORD_PATTERN.matcher(textView.getText());
        while (matcher.find())
        {
            words++;
        }

        if (customView != null)
        {
            String string = String.format(Locale.getDefault(), "%d\n%d", words, textView.length());
            customView.setText(string);
        }
    }

    public void checkHighlight(Map editorPreferences, File file, EditText textView, ScrollView scrollView) {
        // Set default syntax
        sharedVariables.syntax = NO_SYNTAX;

        // Validate highlighting preferences and file existence
        if (Boolean.TRUE.equals(editorPreferences.get(Preferences.isHighlightEnabled)) && file != null) {
            String extension = FileUtils.getExtension(file.getName());

            if (extension != null) {
                String mimeType = FileUtils.getMimeType(file);
                sharedVariables.syntax = determineSyntax(extension, mimeType);

                // Setup highlighting callback if syntax is detected
                if (textView != null && sharedVariables.syntax != NO_SYNTAX) {
                    setupHighlightingCallback(textView, scrollView);
                    return;
                }
            }
        }

        // Remove highlighting if applicable
        removeHighlightingCallback(textView);
    }

    private int determineSyntax(String extension, String mimeType) {
        if (extension.matches(CC_EXT)) {
            return CC_SYNTAX;
        } else if (extension.matches(HTML_EXT)) {
            return HTML_SYNTAX;
        } else if (extension.matches(CSS_EXT)) {
            return CSS_SYNTAX;
        } else if (extension.matches(ORG_EXT)) {
            return ORG_SYNTAX;
        } else if (extension.matches(MD_EXT)) {
            return MD_SYNTAX;
        } else if (extension.matches(SH_EXT)) {
            return SH_SYNTAX;
        } else if (!SharedConstants.getInstance().TEXT_PLAIN.equals(mimeType)) {
            return DEF_SYNTAX;
        } else {
            return NO_SYNTAX;
        }
    }

    private void setupHighlightingCallback(EditText textView, ScrollView scrollView) {
        if (sharedVariables.updateHighlight == null) {
            sharedVariables.updateHighlight = () -> highlightText(scrollView, textView);
        }

        textView.removeCallbacks(sharedVariables.updateHighlight);
        textView.postDelayed(sharedVariables.updateHighlight, SharedConstants.getInstance().UPDATE_DELAY);
    }

    private void removeHighlightingCallback(EditText textView) {
        if (sharedVariables.updateHighlight != null) {
            textView.removeCallbacks(sharedVariables.updateHighlight);
            textView.postDelayed(sharedVariables.updateHighlight, SharedConstants.getInstance().UPDATE_DELAY);
            sharedVariables.updateHighlight = null;
        }
    }


    public void highlightText(ScrollView scrollView, EditText textView) {
        // Get visible extent
        int top = scrollView.getScrollY();
        int height = scrollView.getHeight();

        int line = textView.getLayout().getLineForVertical(top);
        int start = textView.getLayout().getLineStart(line);
        int first = textView.getLayout().getLineStart(line + 1);

        line = textView.getLayout().getLineForVertical(top + height);
        int end = textView.getLayout().getLineEnd(line);
        int last = (line == 0) ? end : textView.getLayout().getLineStart(line - 1);

        // Adjust selection if out of range
        if (textView.getSelectionStart() < start) {
            textView.setSelection(first);
        } else if (textView.getSelectionStart() > end) {
            textView.setSelection(last);
        }

        // Get editable content
        Editable editable = textView.getEditableText();

        // Clear existing spans
        removeExistingSpans(editable, start, end, ForegroundColorSpan.class);

        // Highlight text based on syntax
        switch (sharedVariables.syntax) {
            case NO_SYNTAX:
                removeExistingSpans(editable, 0, editable.length(), ForegroundColorSpan.class);
                break;
            case CC_SYNTAX:
                applySyntaxHighlighting(editable, start, end, KEYWORDS, Color.CYAN);
                applySyntaxHighlighting(editable, start, end, TYPES, Color.MAGENTA);
                applySyntaxHighlighting(editable, start, end, CLASS, Color.BLUE);
                applySyntaxHighlighting(editable, start, end, NUMBER, Color.YELLOW);
                applySyntaxHighlighting(editable, start, end, ANNOTATION, Color.CYAN);
                applySyntaxHighlighting(editable, start, end, CONSTANT, Color.LTGRAY);
                applySyntaxHighlighting(editable, start, end, OPERATOR, Color.CYAN);
                applySyntaxHighlighting(editable, start, end, CC_COMMENT, Color.RED);
                break;
            case HTML_SYNTAX:
                applySyntaxHighlighting(editable, start, end, HTML_TAGS, Color.CYAN);
                applySyntaxHighlighting(editable, start, end, HTML_ATTRS, Color.MAGENTA);
                applySyntaxHighlighting(editable, start, end, QUOTED, Color.RED);
                applySyntaxHighlighting(editable, start, end, HTML_COMMENT, Color.RED);
                break;
            case CSS_SYNTAX:
                applySyntaxHighlighting(editable, start, end, CSS_STYLES, Color.CYAN);
                applySyntaxHighlighting(editable, start, end, CSS_HEX, Color.MAGENTA);
                applySyntaxHighlighting(editable, start, end, CC_COMMENT, Color.RED);
                break;
            case ORG_SYNTAX:
                applySyntaxHighlighting(editable, start, end, ORG_HEADER, Color.BLUE);
                applySyntaxHighlighting(editable, start, end, ORG_EMPH, Color.MAGENTA);
                applySyntaxHighlighting(editable, start, end, ORG_LINK, Color.CYAN);
                applySyntaxHighlighting(editable, start, end, ORG_COMMENT, Color.RED);
                break;
            case MD_SYNTAX:
                applySyntaxHighlighting(editable, start, end, MD_HEADER, Color.BLUE);
                applySyntaxHighlighting(editable, start, end, MD_LINK, Color.CYAN);
                applySyntaxHighlighting(editable, start, end, MD_EMPH, Color.MAGENTA);
                applySyntaxHighlighting(editable, start, end, MD_CODE, Color.CYAN);
                break;
            case SH_SYNTAX:
                applySyntaxHighlighting(editable, start, end, KEYWORDS, Color.CYAN);
                applySyntaxHighlighting(editable, start, end, NUMBER, Color.YELLOW);
                applySyntaxHighlighting(editable, start, end, CONSTANT, Color.LTGRAY);
                applySyntaxHighlighting(editable, start, end, SH_VAR, Color.MAGENTA);
                applySyntaxHighlighting(editable, start, end, OPERATOR, Color.CYAN);
                applySyntaxHighlighting(editable, start, end, QUOTED, Color.RED);
                applySyntaxHighlighting(editable, start, end, SH_COMMENT, Color.RED);
                break;
            case DEF_SYNTAX:
                applySyntaxHighlighting(editable, start, end, KEYWORDS, Color.CYAN);
                applySyntaxHighlighting(editable, start, end, TYPES, Color.MAGENTA);
                applySyntaxHighlighting(editable, start, end, CLASS, Color.BLUE);
                applySyntaxHighlighting(editable, start, end, NUMBER, Color.YELLOW);
                applySyntaxHighlighting(editable, start, end, CONSTANT, Color.LTGRAY);
                applySyntaxHighlighting(editable, start, end, QUOTED, Color.RED);
                break;
        }
    }

    private void removeExistingSpans(Editable editable, int start, int end, Class<?> spanType) {
        Object[] spans = editable.getSpans(start, end, spanType);
        for (Object span : spans) {
            editable.removeSpan(span);
        }
    }

    private void applySyntaxHighlighting(Editable editable, int start, int end, Pattern pattern, int color) {
        Matcher matcher = pattern.matcher(editable);
        matcher.region(start, end);
        while (matcher.find()) {
            ForegroundColorSpan span = new ForegroundColorSpan(color);
            editable.setSpan(span, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

}
