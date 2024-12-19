package org.billthefarmer.editor.fileHandler;

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

    private FileHandler(){
        //Get SharedVariables Singelton
        sharedVariables = SharedVariables.getInstance();
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

}
