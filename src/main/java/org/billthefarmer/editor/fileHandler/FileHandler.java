package org.billthefarmer.editor.fileHandler;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import org.billthefarmer.editor.BuildConfig;
import org.billthefarmer.editor.R;
import org.billthefarmer.editor.values.SharedConstants;
import org.billthefarmer.editor.values.SharedVariables;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


public class FileHandler implements IFileHandler {
    private static FileHandler instance;
    private static SharedVariables sharedVariables;
    private static SharedConstants sharedConstants;

    private FileHandler(){
        sharedVariables = SharedVariables.getInstance();
        sharedConstants = SharedConstants.getInstance();
    }
    public static synchronized FileHandler getInstance() {
        if (instance == null) {
            instance = new FileHandler();
        }
        return instance;
    }
    public CharSequence readFileFromFile(File file)
    {
        StringBuilder text = new StringBuilder();
        // Open file
        try (BufferedReader reader = new BufferedReader
                (new InputStreamReader
                        (new BufferedInputStream(new FileInputStream(file)))))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                text.append(line);
                text.append(System.getProperty("line.separator"));
            }

            return text;
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }

        return text;
    }

    public CharSequence readFileFromUri(Context context, Uri uri) {
        if (context == null || uri == null) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        SharedVariables sharedVariables = SharedVariables.getInstance();
        SharedConstants sharedConstants = SharedConstants.getInstance();

        // Default UTF-8
        if (sharedVariables.match == null) {
            sharedVariables.match = sharedConstants.UTF_8;
        }

        try (BufferedInputStream in = new BufferedInputStream(context.getContentResolver().openInputStream(uri))) {
            BufferedReader reader;

            if (sharedVariables.match.equals(context.getString(R.string.detect))) {
                // Detect charset, using UTF-8 hint
                CharsetMatch match = new CharsetDetector()
                        .setDeclaredEncoding(sharedConstants.UTF_8)
                        .setText(in)
                        .detect();

                if (match != null) {
                    sharedVariables.match = match.getName();
                    reader = new BufferedReader(match.getReader());

                    if (BuildConfig.DEBUG) {
                        Log.d(sharedConstants.TAG, "Charset: " + sharedVariables.match);
                    }
                } else {
                    reader = new BufferedReader(new InputStreamReader(in));
                }
            } else {
                reader = new BufferedReader(new InputStreamReader(in, sharedVariables.match));
            }

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append(System.getProperty("line.separator"));
            }

        } catch (Exception e) {
            Log.e(sharedConstants.TAG, "Error reading file: " + e.getMessage(), e);
        }

        return stringBuilder;
    }

    public void writeToFile(CharSequence text, File file,String charset) throws IOException {
        file.getParentFile().mkdirs();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
        writer.append(text);
        writer.flush();

        sharedVariables.changed = false;
        sharedVariables.modified = file.lastModified();
    }

    public void writeToOutputStream(CharSequence text, OutputStream os,String charset) throws IOException {

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, charset));
        writer.append(text);
        writer.flush();

        sharedVariables.changed = false;
    }


    public File getNewFile()
    {
        File documents = new File(Environment.getExternalStorageDirectory(), sharedConstants.DOCUMENTS);
        return new File(documents, sharedConstants.NEW_FILE);
    }
}
