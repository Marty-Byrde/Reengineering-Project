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

    public void checkHighlight(Map editorPreferences, File file,EditText textView,ScrollView scrollView, Runnable updateHighlight)
    {
        // No syntax
        sharedVariables.syntax = NO_SYNTAX;

        // Check extension
        if ((boolean) editorPreferences.get(Preferences.isHighlightEnabled) && file != null)
        {
            String ext = FileUtils.getExtension(file.getName());
            if (ext != null)
            {
                String type = FileUtils.getMimeType(file);

                if (ext.matches(CC_EXT))
                    sharedVariables.syntax = CC_SYNTAX;

                else if (ext.matches(HTML_EXT))
                    sharedVariables.syntax = HTML_SYNTAX;

                else if (ext.matches(CSS_EXT))
                    sharedVariables.syntax = CSS_SYNTAX;

                else if (ext.matches(ORG_EXT))
                    sharedVariables.syntax = ORG_SYNTAX;

                else if (ext.matches(MD_EXT))
                    sharedVariables.syntax = MD_SYNTAX;

                else if (ext.matches(SH_EXT))
                    sharedVariables.syntax = SH_SYNTAX;

                else if (!SharedConstants.getInstance().TEXT_PLAIN.equals(type))
                    sharedVariables.syntax = DEF_SYNTAX;

                else
                    sharedVariables.syntax = NO_SYNTAX;

                // Add callback
                if (textView != null && sharedVariables.syntax != NO_SYNTAX)
                {
                    if (updateHighlight == null) {
                        updateHighlight = () -> highlightText(scrollView,textView);
                    }

                    textView.removeCallbacks(updateHighlight);
                    textView.postDelayed(updateHighlight, SharedConstants.getInstance().UPDATE_DELAY);

                    return;
                }
            }
        }

        // Remove highlighting
        if (updateHighlight != null)
        {
            textView.removeCallbacks(updateHighlight);
            textView.postDelayed(updateHighlight, SharedConstants.getInstance().UPDATE_DELAY);

            updateHighlight = null;
        }
    }

    public void highlightText(ScrollView scrollView, EditText textView)
    {
        // Get visible extent
        int top = scrollView.getScrollY();
        int height = scrollView.getHeight();

        int line = textView.getLayout().getLineForVertical(top);
        int start = textView.getLayout().getLineStart(line);
        int first = textView.getLayout().getLineStart(line + 1);

        line = textView.getLayout().getLineForVertical(top + height);
        int end = textView.getLayout().getLineEnd(line);
        int last = (line == 0)? end:
                textView.getLayout().getLineStart(line - 1);

        // Move selection if outside range
        if (textView.getSelectionStart() < start)
            textView.setSelection(first);

        if (textView.getSelectionStart() > end)
            textView.setSelection(last);

        // Get editable
        Editable editable = textView.getEditableText();

        // Get current spans
        ForegroundColorSpan spans[] =
                editable.getSpans(start, end, ForegroundColorSpan.class);
        // Remove spans
        for (ForegroundColorSpan span: spans)
            editable.removeSpan(span);

        Matcher matcher;

        switch (sharedVariables.syntax)
        {
            case NO_SYNTAX:
                // Get current spans
                spans = editable.getSpans(0, editable.length(),
                        ForegroundColorSpan.class);
                // Remove spans
                for (ForegroundColorSpan span: spans)
                    editable.removeSpan(span);
                break;

            case CC_SYNTAX:
                matcher = KEYWORDS.matcher(editable);
                matcher.region(start, end);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.CYAN);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(TYPES);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.MAGENTA);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(CLASS);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.BLUE);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(NUMBER);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.YELLOW);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(ANNOTATION);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.CYAN);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(CONSTANT);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.LTGRAY);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(OPERATOR);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.CYAN);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(CC_COMMENT);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.RED);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                break;

            case HTML_SYNTAX:
                matcher = HTML_TAGS.matcher(editable);
                matcher.region(start, end);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.CYAN);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(HTML_ATTRS);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.MAGENTA);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(QUOTED);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.RED);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(HTML_COMMENT);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.RED);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                break;

            case CSS_SYNTAX:
                matcher = CSS_STYLES.matcher(editable);
                matcher.region(start, end);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.CYAN);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(CSS_HEX);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.MAGENTA);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(CC_COMMENT);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.RED);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                break;

            case ORG_SYNTAX:
                matcher = ORG_HEADER.matcher(editable);
                matcher.region(start, end);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.BLUE);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }


                matcher.region(start, end).usePattern(ORG_EMPH);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.MAGENTA);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(ORG_LINK);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.CYAN);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(ORG_COMMENT);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.RED);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                break;

            case MD_SYNTAX:
                matcher = MD_HEADER.matcher(editable);
                matcher.region(start, end);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.BLUE);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(MD_LINK);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.CYAN);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(MD_EMPH);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.MAGENTA);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(MD_CODE);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.CYAN);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                break;

            case SH_SYNTAX:
                matcher = KEYWORDS.matcher(editable);
                matcher.region(start, end);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.CYAN);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(NUMBER);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.YELLOW);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(CONSTANT);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.LTGRAY);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(SH_VAR);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.MAGENTA);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(OPERATOR);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.CYAN);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(QUOTED);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.RED);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(SH_COMMENT);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.RED);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                break;

            case DEF_SYNTAX:
                matcher = KEYWORDS.matcher(editable);
                matcher.region(start, end);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.CYAN);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(TYPES);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.MAGENTA);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(CLASS);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.BLUE);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(NUMBER);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.YELLOW);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(CONSTANT);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.LTGRAY);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                matcher.region(start, end).usePattern(QUOTED);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new
                            ForegroundColorSpan(Color.RED);

                    // Highlight it
                    editable.setSpan(span, matcher.start(), matcher.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                break;
        }
    }
}
