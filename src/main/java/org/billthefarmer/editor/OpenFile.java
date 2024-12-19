////////////////////////////////////////////////////////////////////////////////
//
//  Editor - Text editor for Android
//
//  Copyright © 2019  Bill Farmer
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Bill Farmer	 william j farmer [at] yahoo [dot] co [dot] uk.
//
////////////////////////////////////////////////////////////////////////////////

package org.billthefarmer.editor;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;

import org.billthefarmer.editor.helpers.FileUtils;
import org.billthefarmer.editor.values.SharedConstants;
import org.billthefarmer.editor.preferences.EditorPreferenceParameters;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

public class OpenFile extends Activity
{
    public final static String TAG = "OpenFile";

    private TextView nameView;
    private TextView pathView;

    private String path;
    private File file;
    private Uri uri;

    private static SharedConstants sharedConstants;

    // onCreate
    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get preferences
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);

        //Get SharedConstants Singelton
        sharedConstants = SharedConstants.getInstance();

        int theme = preferences.getInt(EditorPreferenceParameters.PREF_THEME, EditorPreferenceParameters.LIGHT);

        // Get day/night mode
        Configuration config = getResources().getConfiguration();
        int night = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;

        // Set theme
        switch (theme)
        {
        case EditorPreferenceParameters.LIGHT:
            setTheme(R.style.DialogTheme);
            break;

        case EditorPreferenceParameters.DARK:
            setTheme(R.style.DialogDarkTheme);
            break;

        case EditorPreferenceParameters.SYSTEM:
            switch (night)
            {
            case Configuration.UI_MODE_NIGHT_NO:
                setTheme(R.style.DialogTheme);
                break;

            case Configuration.UI_MODE_NIGHT_YES:
                setTheme(R.style.DialogDarkTheme);
                break;
            }
            break;

        case EditorPreferenceParameters.WHITE:
            setTheme(R.style.DialogWhiteTheme);
            break;

        case EditorPreferenceParameters.BLACK:
            setTheme(R.style.DialogBlackTheme);
            break;

        case EditorPreferenceParameters.RETRO:
            setTheme(R.style.DialogRetroTheme);
            break;
        }

        // Set content
        setContentView(R.layout.open_file);

        // Find views
        nameView = findViewById(R.id.name);
        pathView = findViewById(R.id.path);

        // Get last path
        boolean last = preferences.getBoolean(EditorPreferenceParameters.PREF_LAST, false);
        String lastPath = preferences.getString(EditorPreferenceParameters.PREF_FILE, "");

        // Configure buttons
        Button cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener((v) ->
        {
            setResult(RESULT_CANCELED, null);
            finish();
        });

        Button openFile = findViewById(R.id.openFile);
        openFile.setOnClickListener((v) ->
        {
            if (last && !lastPath.isEmpty())
                getFile(new File(lastPath).getParentFile());

            else
                getFile(new File(Environment.getExternalStorageDirectory(),
                        sharedConstants.DOCUMENTS));
        });

        Button create = findViewById(R.id.create);
        create.setOnClickListener((v) ->
        {
            if (pathView.length() == 0)
                return;

            // Create the shortcut intent
            Intent shortcut = new Intent(this, Editor.class);
            shortcut.setAction(Intent.ACTION_EDIT);
            shortcut.addCategory(Intent.CATEGORY_DEFAULT);

            if (uri == null)
            {
                file = new File(pathView.getText().toString());
                if (!file.isAbsolute())
                    file = new File(Environment.getExternalStorageDirectory(),
                                    file.getPath());
                uri = Uri.fromFile(file);

                if (nameView.length() == 0)
                    nameView.setText(uri.getLastPathSegment());

            }

            // Set uri
            shortcut.setData(uri);

            // Create the shortcut
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcut);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                            nameView.getText().toString());
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource.fromContext
                            (this, R.drawable.ic_launcher));

            setResult(RESULT_OK, intent);
            finish();
        });

        // Check permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                     Manifest.permission.READ_EXTERNAL_STORAGE},
                                   sharedConstants.REQUEST_OPEN);
                return;
            }
        }

        if (last && !lastPath.isEmpty())
            getFile(new File(lastPath).getParentFile());

        else
            getFile(new File(Environment.getExternalStorageDirectory(),
                    sharedConstants.DOCUMENTS));
    }

    // onRequestPermissionsResult
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults)
    {
        switch (requestCode)
        {
        case 3:
            for (int i = 0; i < grantResults.length; i++)
                if (permissions[i].equals(Manifest.permission
                                          .READ_EXTERNAL_STORAGE) &&
                    grantResults[i] == PackageManager.PERMISSION_GRANTED)
                    // Granted, get file
                    getFile(new File(Environment.getExternalStorageDirectory(),
                            sharedConstants.DOCUMENTS));
            break;
        }
    }

    // onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        if (resultCode == RESULT_CANCELED)
            return;

        switch (requestCode)
        {
        case 1:
            uri = data.getData();
            path = uri.getPath();
            pathView.setText(path);
            nameView.setText(FileUtils.getDisplayName(this, uri,
                                                      null, null));
            break;
        }
    }

    // getFile
    private void getFile(File dir)
    {
        // Get list of files
        List<File> fileList = Editor.getList(dir);
        if (fileList == null)
            return;

        // Get list of folders
        List<String> dirList = new ArrayList<String>();
        dirList.add(File.separator);
        dirList.addAll(Uri.fromFile(dir).getPathSegments());

        // Pop up dialog
        Editor.openDialog(this, dirList, fileList, (dialog, which) ->
        {
            if (DialogInterface.BUTTON_NEUTRAL == which)
            {
                // Use storage
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType(sharedConstants.TEXT_WILD);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, sharedConstants.OPEN_DOCUMENT);
                return;
            }

            if (sharedConstants.FOLDER_OFFSET <= which)
            {
                File file = new File(File.separator);
                for (int i = 0; i <= which - sharedConstants.FOLDER_OFFSET; i++)
                    file = new File(file, dirList.get(i));
                if (file.isDirectory())
                    getFile(file);
                return;
            }

            File selection = fileList.get(which);
            if (selection.isDirectory())
                getFile(selection);

            else
            {
                uri = Uri.fromFile(selection);
                path = uri.getPath();
                pathView.setText(path);
                nameView.setText(uri.getLastPathSegment());
            }
        });
    }
}
