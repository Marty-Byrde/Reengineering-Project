package org.billthefarmer.editor.editorSubClasses;

import android.graphics.Color;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryTextListener implements SearchView.OnQueryTextListener
{
    private BackgroundColorSpan span = new BackgroundColorSpan(Color.YELLOW);
    private Editable editable;
    private Matcher matcher;
    private Pattern pattern;
    private int index;
    private int height;
    private ScrollView scrollView;
    private TextView textView;

    public QueryTextListener(TextView textView,ScrollView scrollView){
        this.textView = textView;
        this.scrollView = scrollView;
    }

    // onQueryTextChange
    @Override
    @SuppressWarnings("deprecation")
    public boolean onQueryTextChange(String newText)
    {
        // Use regex search and spannable for highlighting
        height = scrollView.getHeight();
        editable = textView.getEditableText();

        // Reset the index and clear highlighting
        if (newText.length() == 0)
        {
            index = 0;
            editable.removeSpan(span);
            return false;
        }

        // Check pattern
        try
        {
            pattern = Pattern.compile(newText, Pattern.MULTILINE);
            matcher = pattern.matcher(editable);
        }

        catch (Exception e)
        {
            return false;
        }

        // Find text
        if (matcher.find(index))
        {
            // Get index
            index = matcher.start();

            // Check layout
            if (textView.getLayout() == null)
                return false;

            // Get text position
            int line = textView.getLayout().getLineForOffset(index);
            int pos = textView.getLayout().getLineBaseline(line);

            // Scroll to it
            scrollView.smoothScrollTo(0, pos - height / 2);

            // Highlight it
            editable.setSpan(span, matcher.start(), matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        else
            index = 0;

        return true;
    }

    // onQueryTextSubmit
    @Override
    public boolean onQueryTextSubmit(String query)
    {
        // Check matcher
        if (matcher == null)
            return false;

        // Find next text
        if (matcher.find())
        {
            // Get index
            index = matcher.start();

            // Get text position
            int line = textView.getLayout().getLineForOffset(index);
            int pos = textView.getLayout().getLineBaseline(line);

            // Scroll to it
            scrollView.smoothScrollTo(0, pos - height / 2);

            // Highlight it
            editable.setSpan(span, matcher.start(), matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        else
        {
            matcher.reset();
            index = 0;
        }

        return true;
    }
}
