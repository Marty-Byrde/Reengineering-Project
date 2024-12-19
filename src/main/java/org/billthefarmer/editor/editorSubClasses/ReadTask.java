package org.billthefarmer.editor.editorSubClasses;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import org.billthefarmer.editor.BuildConfig;
import org.billthefarmer.editor.Editor;
import org.billthefarmer.editor.R;
import org.billthefarmer.editor.values.SharedConstants;
import org.billthefarmer.editor.values.SharedVariables;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

public class ReadTask extends AsyncTask<Uri, Void, CharSequence>
{
    private WeakReference<Editor> editorWeakReference;
    private static SharedVariables sharedVariables;
    private static SharedConstants sharedConstants;

    public ReadTask(Editor editor)
    {
        editorWeakReference = new WeakReference<>(editor);
        sharedVariables = SharedVariables.getInstance();
        sharedConstants = SharedConstants.getInstance();
    }

    // doInBackground
    @Override
    protected CharSequence doInBackground(Uri... uris)
    {
        StringBuilder stringBuilder = new StringBuilder();
        final Editor editor = editorWeakReference.get();
        if (editor == null)
            return stringBuilder;

        // Default UTF-8
        if (sharedVariables.match == null)
        {
            sharedVariables.match = sharedConstants.UTF_8;
            editor.runOnUiThread(() ->
                    editor.getActionBar().setSubtitle(sharedVariables.match));
        }

        try (BufferedInputStream in = new BufferedInputStream
                (editor.getContentResolver().openInputStream(uris[0])))
        {
            // Create reader
            BufferedReader reader = null;
            if (sharedVariables.match.equals(editor.getString(R.string.detect)))
            {
                // Detect charset, using UTF-8 hint
                CharsetMatch match = new
                        CharsetDetector().setDeclaredEncoding(sharedConstants.UTF_8)
                        .setText(in).detect();

                if (match != null)
                {
                    sharedVariables.match = match.getName();
                    editor.runOnUiThread(() ->
                            editor.getActionBar().setSubtitle(sharedVariables.match));
                    reader = new BufferedReader(match.getReader());
                }

                else
                    reader = new BufferedReader
                            (new InputStreamReader(in));

                if (BuildConfig.DEBUG && match != null)
                    Log.d(sharedConstants.TAG, "Charset " + sharedVariables.match);
            }

            else
                reader = new BufferedReader
                        (new InputStreamReader(in, sharedVariables.match));

            String line;
            while ((line = reader.readLine()) != null)
            {
                stringBuilder.append(line);
                stringBuilder.append(System.getProperty("line.separator"));
            }
        }

        catch (Exception e)
        {
            editor.runOnUiThread(() ->
                    Editor.alertDialog(editor, R.string.appName,
                            e.getMessage(),
                            R.string.ok));
            e.printStackTrace();
        }

        return stringBuilder;
    }

    // onPostExecute
    @Override
    protected void onPostExecute(CharSequence result)
    {
        final Editor editor = editorWeakReference.get();
        if (editor == null)
            return;

        editor.loadText(result);
    }
}
