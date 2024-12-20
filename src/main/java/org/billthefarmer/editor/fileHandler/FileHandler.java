package org.billthefarmer.editor.fileHandler;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FileHandler implements IFileHandler {
    private static FileHandler instance;
    private static final SharedVariables sharedVariables = SharedVariables.getInstance();
    private static final SharedConstants sharedConstants = SharedConstants.getInstance();;

    private FileHandler(){
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

    public CharSequence readFileFromUri(Uri uri) {
        Context context = sharedVariables.appContext;
        if (context == null || uri == null) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        SharedVariables sharedVariables = SharedVariables.getInstance();
        SharedConstants sharedConstants = SharedConstants.getInstance();

        if (sharedVariables.match == null) {
            sharedVariables.match = sharedConstants.UTF_8;
        }

        try (BufferedInputStream inputStream = new BufferedInputStream(context.getContentResolver().openInputStream(uri))) {
            BufferedReader reader = createReader(context, inputStream);

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append(System.lineSeparator());
            }

        } catch (Exception e) {
            Log.e(sharedConstants.TAG, "Error reading file: " + e.getMessage(), e);
        }

        return stringBuilder;
    }

    private BufferedReader createReader(Context context, InputStream inputStream) throws IOException {
        if (sharedVariables.match.equals(context.getString(R.string.detect))) {
            // Detect charset using CharsetDetector with UTF-8 as a hint
            CharsetMatch match = new CharsetDetector()
                    .setDeclaredEncoding(sharedConstants.UTF_8)
                    .setText(inputStream)
                    .detect();

            if (match != null) {
                sharedVariables.match = match.getName();

                if (BuildConfig.DEBUG) {
                    Log.d(sharedConstants.TAG, "Detected Charset: " + sharedVariables.match);
                }
                return new BufferedReader(match.getReader());
            }
        }

        return new BufferedReader(new InputStreamReader(inputStream, sharedVariables.match));
    }


    private void writeToFile(CharSequence text, File file,String charset) throws IOException {
        file.getParentFile().mkdirs();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
        writer.append(text);
        writer.flush();

        sharedVariables.changed = false;
        sharedVariables.modified = file.lastModified();
    }

    private void writeToUri(CharSequence text, String charset,Uri uri) throws IOException {

        OutputStream outputStream = sharedVariables.appContext.getContentResolver().openOutputStream(uri, "rwt");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset));
        writer.append(text);
        writer.flush();

        sharedVariables.changed = false;
    }


    public File getNewFile()
    {
        File documents = new File(Environment.getExternalStorageDirectory(), sharedConstants.DOCUMENTS);
        return new File(documents, sharedConstants.NEW_FILE);
    }

    public void saveFile(Object input,CharSequence textContent) throws IOException {

            String charset = resolveCharset();

            if (input instanceof Uri) {
                writeToUri(textContent, charset,(Uri) input);
            } else if (input instanceof File) {
                writeToFile( textContent, (File) input,charset);
            } else {
                throw new IllegalArgumentException("Unsupported input type. Expected Uri or File.");
            }
    }
    private String resolveCharset() {
        if (sharedVariables.match != null && !sharedVariables.match.equals(sharedVariables.appContext.getString(R.string.detect))) {
            return sharedVariables.match;
        }
        return sharedConstants.UTF_8;
    }


    public List<File> getList(File dir)
    {
        List<File> list = null;
        File[] files = dir.listFiles();
        // Check files
        if (files == null)
        {
            // Create a list with just the parent folder and the
            // external storage folder
            list = new ArrayList<>();
            if (dir.getParentFile() == null)
                list.add(dir);

            else
                list.add(dir.getParentFile());

            list.add(Environment.getExternalStorageDirectory());

            return list;
        }

        // Sort the files
        Arrays.sort(files);
        // Create a list
        list = new ArrayList<>(Arrays.asList(files));

        // Add parent folder
        if (dir.getParentFile() == null)
            list.add(0, dir);

        else
            list.add(0, dir.getParentFile());

        return list;
    }

}
