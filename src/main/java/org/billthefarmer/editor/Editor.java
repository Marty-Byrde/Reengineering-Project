///////////////////////////////////////////////////////////////////////////////
//
//  Editor - Text editor for Android
//
//  Copyright © 2017  Bill Farmer
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
//  Bill Farmer  william j farmer [at] yahoo [dot] co [dot] uk.
//
////////////////////////////////////////////////////////////////////////////////

package org.billthefarmer.editor;

import static org.billthefarmer.editor.helpers.SyntaxPatternParameters.*;
import static org.billthefarmer.editor.preferences.EditorPreferenceParameters.*;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.widget.TextViewCompat;

import org.billthefarmer.editor.editorSubClasses.QueryTextListener;
import org.billthefarmer.editor.editorSubClasses.ScaleListener;
import org.billthefarmer.editor.fileHandler.FileHandler;
import org.billthefarmer.editor.fileHandler.IFileHandler;
import org.billthefarmer.editor.editorTextUtils.EditorTextUtils;
import org.billthefarmer.editor.helpers.FileAdapter;
import org.billthefarmer.editor.helpers.ThemeHandler;
import org.billthefarmer.editor.helpers.FileUtils;
import org.billthefarmer.editor.values.SharedConstants;
import org.billthefarmer.editor.values.SharedVariables;
import org.billthefarmer.editor.preferences.EditorPreferenceHandler;
import org.billthefarmer.editor.preferences.Preferences;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Editor extends Activity
{
    private EditText textView;
    private TextView customView;
    private MenuItem searchItem;
    public SearchView searchView;
    private ScrollView scrollView;
    private Runnable updateWordCount;

    private ScaleGestureDetector scaleDetector;
    private QueryTextListener queryTextListener;

    public Map<String, Integer> pathMap;
    private List<String> removeList;

    private boolean edit = false;

    private int theme = LIGHT;
    private int type = MONO;


    private Map<Preferences, Object> editorPreferences;

    private static final IFileHandler fileHandler = FileHandler.getInstance();
    private static final SharedConstants sharedConstants= SharedConstants.getInstance();
    private static final SharedVariables sharedVariables = SharedVariables.getInstance();
    private static final EditorTextUtils editorTextUtils = EditorTextUtils.getInstance();

    // onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editorPreferences = EditorPreferenceHandler.fetchPreferences(getResources(), sharedPreferences);

        sharedVariables.appContext = this;

        Set<String> pathSet = (Set<String>) editorPreferences.get(Preferences.pathSet);
        pathMap = new HashMap<>();

        if (pathSet != null)
            for (String pathEntry : pathSet)
                pathMap.put(pathEntry, sharedPreferences.getInt(pathEntry, 0));

        removeList = new ArrayList<>();

        ThemeHandler.setTheme(theme,this);


        if ((boolean) editorPreferences.get(Preferences.isContentWrapped))
            setContentView(R.layout.wrap);

        else
            setContentView(R.layout.edit);

        textView = findViewById(R.id.text);
        scrollView = findViewById(R.id.vscroll);

        getActionBar().setSubtitle(sharedVariables.match);
        getActionBar().setCustomView(R.layout.custom);
        getActionBar().setDisplayShowCustomEnabled(true);
        customView = (TextView) getActionBar().getCustomView();

        updateWordCount = () -> editorTextUtils.wordCountText(textView,customView);

        if (savedInstanceState != null)
            edit = savedInstanceState.getBoolean(sharedConstants.EDIT);

        if (!edit)
        {
            textView.setRawInputType(InputType.TYPE_NULL);
            textView.setTextIsSelectable(true);
        }

        else if (!(boolean) editorPreferences.get(Preferences.isSuggestEnabled))
            textView.setInputType(InputType.TYPE_CLASS_TEXT |
                                  InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                                  InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        setSizeAndTypeface(sharedVariables.size, type);

        Intent intent = getIntent();
        Uri intentUri = intent.getData();

        switch (intent.getAction())
        {
        case Intent.ACTION_VIEW,Intent.ACTION_EDIT:
            if ((savedInstanceState == null) && (intentUri != null))
                readFile(intentUri);

            getActionBar().setDisplayHomeAsUpEnabled(true);
            break;

        case Intent.ACTION_SEND:
            if (savedInstanceState == null)
            {
                // Get uri
                intentUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                // Get text
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (intentUri != null)
                    readFile(intentUri);

                else if (text != null)
                {
                    defaultFile(text);
                    sharedVariables.changed = true;
                }

                else
                    defaultFile(null);
            }
            break;

        case "org.billthefarmer.editor.OPEN_NEW":
            if (savedInstanceState == null)
            {
                defaultFile(null);
                textView.postDelayed(() -> editClicked(null), sharedConstants.UPDATE_DELAY);
            }
            break;

        case Intent.ACTION_MAIN:
            if (savedInstanceState == null)
            {
                if ((boolean) editorPreferences.get(Preferences.isLast))
                    lastFile();

                else
                    defaultFile(null);
            }
            break;
            default:
                break;
        }

        setListeners();
    }

    // setListeners
    private void setListeners()
    {
        scaleDetector = new ScaleGestureDetector(this, new ScaleListener(textView,this));
        queryTextListener = new QueryTextListener(textView,scrollView);

        if (textView != null)
        {
            textView.addTextChangedListener(new TextWatcher()
            {
                // afterTextChanged
                @Override
                public void afterTextChanged(Editable s)
                {
                    if (!sharedVariables.changed)
                    {
                        sharedVariables.changed = true;
                        invalidateOptionsMenu();
                    }

                    if (sharedVariables.updateHighlight != null)
                    {
                        textView.removeCallbacks(sharedVariables.updateHighlight);
                        textView.postDelayed(sharedVariables.updateHighlight, sharedConstants.UPDATE_DELAY);
                    }

                    if (updateWordCount != null)
                    {
                        textView.removeCallbacks(updateWordCount);
                        textView.postDelayed(updateWordCount, sharedConstants.UPDATE_DELAY);
                    }
                }

                // beforeTextChanged
                @Override
                public void beforeTextChanged(CharSequence s, int start,int count,int after)
                {
                    if (searchItem != null &&
                        searchItem.isActionViewExpanded())
                    {
                        final CharSequence query = searchView.getQuery();

                        textView.postDelayed(() ->
                        {
                            if (searchItem != null &&
                                searchItem.isActionViewExpanded() && query != null)
                                    searchView.setQuery(query, false);

                        }, sharedConstants.UPDATE_DELAY);
                    }
                }

                // onTextChanged
                @Override
                public void onTextChanged(CharSequence s,int start,int before,int count) { /* Needs to be overridden because of android*/ }
            });

            // onFocusChange
            textView.setOnFocusChangeListener((v, hasFocus) ->
            {
                // Hide keyboard
                InputMethodManager manager = (InputMethodManager)
                    getSystemService(INPUT_METHOD_SERVICE);
                if (!hasFocus)
                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                if (sharedVariables.updateHighlight != null)
                {
                    textView.removeCallbacks(sharedVariables.updateHighlight);
                    textView.postDelayed(sharedVariables.updateHighlight, sharedConstants.UPDATE_DELAY);
                }
            });

            // onLongClick
            textView.setOnLongClickListener(v ->
            {
                // Do nothing if already editable
                if (edit)
                    return false;

                // Get scroll position
                int y = scrollView.getScrollY();
                // Get height
                int height = scrollView.getHeight();
                // Get width
                int width = scrollView.getWidth();

                // Get offset
                int line = textView.getLayout()
                    .getLineForVertical(y + height / 2);
                int offset = textView.getLayout()
                    .getOffsetForHorizontal(line, (float) width / 2);
                // Set cursor
                textView.setSelection(offset);

                // Set editable with or without suggestions
                if ((boolean) editorPreferences.get(Preferences.isSuggestEnabled))
                    textView
                    .setInputType(InputType.TYPE_CLASS_TEXT |
                                  InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                else
                    textView
                    .setInputType(InputType.TYPE_CLASS_TEXT |
                                  InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                                  InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                // Update boolean
                edit = true;

                // Restart
                recreate(this);

                return false;
            });

            textView.getViewTreeObserver().addOnGlobalLayoutListener(() ->
            {
                if (sharedVariables.updateHighlight != null)
                {
                    textView.removeCallbacks(sharedVariables.updateHighlight);
                    textView.postDelayed(sharedVariables.updateHighlight, sharedConstants.UPDATE_DELAY);
                }
            });
        }

        if (scrollView != null)
        {
            // onScrollChange
            scrollView.getViewTreeObserver().addOnScrollChangedListener(() ->
            {
                if (sharedVariables.updateHighlight != null)
                {
                    textView.removeCallbacks(sharedVariables.updateHighlight);
                    textView.postDelayed(sharedVariables.updateHighlight, sharedConstants.UPDATE_DELAY);
                }
            });
        }
    }

    // onRestoreInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        sharedVariables.fileWrapper.path = savedInstanceState.getString(sharedConstants.PATH);
        edit = savedInstanceState.getBoolean(sharedConstants.EDIT);
        sharedVariables.changed = savedInstanceState.getBoolean(sharedConstants.CHANGED);
        sharedVariables.match = savedInstanceState.getString(sharedConstants.MATCH);
        sharedVariables.modified = savedInstanceState.getLong(sharedConstants.MODIFIED);
        sharedVariables.fileWrapper.content = savedInstanceState.getParcelable(sharedConstants.CONTENT);
        invalidateOptionsMenu();

        sharedVariables.fileWrapper.file = new File(sharedVariables.fileWrapper.path);
        sharedVariables.fileWrapper.uri = Uri.fromFile(sharedVariables.fileWrapper.file);

        if (sharedVariables.fileWrapper.content != null)
            setTitle(FileUtils.getDisplayName(this, sharedVariables.fileWrapper.content, null, null));

        else
            setTitle(sharedVariables.fileWrapper.uri.getLastPathSegment());

        if (sharedVariables.match == null)
            sharedVariables.match = sharedConstants.UTF_8;
        getActionBar().setSubtitle(sharedVariables.match);

        editorTextUtils.checkHighlight(editorPreferences,sharedVariables.fileWrapper.file,textView,scrollView);

        if (sharedVariables.fileWrapper.file.lastModified() > sharedVariables.modified)
            alertDialog(this, R.string.appName, R.string.changedReload,
                        R.string.reload, R.string.cancel, (dialog, id) ->
        {
            if(id == DialogInterface.BUTTON_POSITIVE)
            {
                readFile(sharedVariables.fileWrapper.uri);
            }
        });
    }

    // onPause
    @Override
    public void onPause() {
        super.onPause();

        savePath(sharedVariables.fileWrapper.path);
        removeTextViewCallbacks();

        savePreferences();

        // Save file if needed
        if (sharedVariables.changed && getPreferenceBoolean(Preferences.autoSaveFeature)) {
            saveFileHandler();
        }
    }

    private void savePreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        saveOnPausePreferences(editor);
        saveRecentPaths(editor);

        editor.apply();
    }

    private void saveOnPausePreferences(SharedPreferences.Editor editor) {
        editor.putBoolean(PREF_SAVE, getPreferenceBoolean(Preferences.autoSaveFeature));
        editor.putBoolean(PREF_VIEW, getPreferenceBoolean(Preferences.isReadOnly));
        editor.putBoolean(PREF_LAST, getPreferenceBoolean(Preferences.isLast));
        editor.putBoolean(PREF_WRAP, getPreferenceBoolean(Preferences.isContentWrapped));
        editor.putBoolean(PREF_SUGGEST, getPreferenceBoolean(Preferences.isSuggestEnabled));
        editor.putBoolean(PREF_HIGH, getPreferenceBoolean(Preferences.isHighlightEnabled));
        editor.putInt(PREF_THEME, getPreferenceInt(Preferences.Theme));
        editor.putInt(PREF_SIZE, getPreferenceInt(Preferences.FontSize));
        editor.putInt(PREF_TYPE, getPreferenceInt(Preferences.FontType));
        editor.putString(PREF_FILE, sharedVariables.fileWrapper.path);
    }
    private void saveRecentPaths(SharedPreferences.Editor editor) {
        editor.putStringSet(PREF_PATHS, pathMap.keySet());

        for (String pathKey : pathMap.keySet()) {
            editor.putInt(pathKey, pathMap.get(pathKey));
        }

        for (String pathKey : removeList) {
            editor.remove(pathKey);
        }
    }

    private boolean getPreferenceBoolean(Preferences preference) {
        return (boolean) editorPreferences.get(preference);
    }

    private int getPreferenceInt(Preferences preference) {
        return (int) editorPreferences.get(preference);
    }


    // onSaveInstanceState
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);


        outState.putParcelable(sharedConstants.CONTENT, sharedVariables.fileWrapper.content);
        outState.putLong(sharedConstants.MODIFIED, sharedVariables.modified);
        outState.putBoolean(sharedConstants.CHANGED, sharedVariables.changed);
        outState.putString(sharedConstants.MATCH, sharedVariables.match);
        outState.putBoolean(sharedConstants.EDIT, edit);
        outState.putString(sharedConstants.PATH, sharedVariables.fileWrapper.path);
    }

    // onCreateOptionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    // onPrepareOptionsMenu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        // Set up search view
        searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchItem.getActionView();

        // Set up search view options and listener
        if (searchView != null)
        {
            searchView.setSubmitButtonEnabled(true);
            searchView.setImeOptions(EditorInfo.IME_ACTION_GO);
            searchView.setOnQueryTextListener(queryTextListener);
        }

        // Show find all item
        menu.findItem(R.id.findAll).setVisible(menu.findItem(R.id.search).isActionViewExpanded());

        menu.findItem(R.id.edit).setVisible(!edit);
        menu.findItem(R.id.view).setVisible(edit);
        menu.findItem(R.id.save).setVisible(sharedVariables.changed);

        menu.findItem(R.id.viewFile).setChecked((boolean) editorPreferences.get(Preferences.isReadOnly));
        menu.findItem(R.id.openLast).setChecked((boolean) editorPreferences.get(Preferences.isLast));
        menu.findItem(R.id.autoSave).setChecked((boolean) editorPreferences.get(Preferences.autoSaveFeature));
        menu.findItem(R.id.wrap).setChecked((boolean) editorPreferences.get(Preferences.isContentWrapped));
        menu.findItem(R.id.suggest).setChecked((boolean) editorPreferences.get(Preferences.isSuggestEnabled));
        menu.findItem(R.id.highlight).setChecked((boolean) editorPreferences.get(Preferences.isHighlightEnabled));

        switch (theme)
        {
        case LIGHT:
            menu.findItem(R.id.light).setChecked(true);
            break;

        case DARK:
            menu.findItem(R.id.dark).setChecked(true);
            break;

        case SYSTEM:
            menu.findItem(R.id.system).setChecked(true);
            break;

        case WHITE:
            menu.findItem(R.id.white).setChecked(true);
            break;

        case BLACK:
            menu.findItem(R.id.black).setChecked(true);
            break;

        case RETRO:
            menu.findItem(R.id.retro).setChecked(true);
            break;
        default:
            break;
        }

        switch ((int) editorPreferences.get(Preferences.FontSize))
        {
        case SMALL:
            menu.findItem(R.id.small).setChecked(true);
            break;

        case MEDIUM:
            menu.findItem(R.id.medium).setChecked(true);
            break;

        case LARGE:
            menu.findItem(R.id.large).setChecked(true);
            break;
            default:
                break;
        }

        // Get the charsets
        Set<String> keySet = Charset.availableCharsets().keySet();
        // Get the submenu
        MenuItem item = menu.findItem(R.id.charset);
        item.setTitle(sharedVariables.match);
        SubMenu sub = item.getSubMenu();
        sub.clear();
        // Add charsets contained in both sets
        sub.add(Menu.NONE, R.id.charsetItem, Menu.NONE, R.string.detect);
        for (String key: keySet)
            sub.add(Menu.NONE, R.id.charsetItem, Menu.NONE, key);

        // Get the typefaces
        String[] typefaces = getResources().getStringArray(R.array.typefaces);
        item = menu.findItem(R.id.typeface);
        sub = item.getSubMenu();
        sub.clear();
        // Add typefaces
        for (String typeface: typefaces)
            sub.add(Menu.NONE, R.id.typefaceItem, Menu.NONE, typeface);
        sub.getItem(type).setCheckable(true);
        sub.getItem(type).setChecked(true);

        // Get a list of recent files
        List<Long> list = new ArrayList<>();
        Map<Long, String> map = new HashMap<>();

        // Get the last modified dates
        for (String tempPath: pathMap.keySet())
        {
            File temp = new File(tempPath);
            // Check it exists
            if (!temp.exists())
                continue;

            long last = temp.lastModified();
            list.add(last);
            map.put(last, tempPath);
        }

        // Sort in reverse order
        Collections.sort(list);
        Collections.reverse(list);

        // Get the submenu
        item = menu.findItem(R.id.openRecent);
        sub = item.getSubMenu();
        sub.clear();

        // Add the recent files
        for (long date : list)
        {
            String filePath = map.get(date);

            // Remove path prefix
            CharSequence name =
                filePath.replaceFirst(Environment
                                  .getExternalStorageDirectory()
                                  .getPath() + File.separator, "");
            // Create item
            sub.add(Menu.NONE, R.id.fileItem, Menu.NONE, TextUtils.ellipsize
                    (name, new TextPaint(), sharedConstants.MENU_SIZE,
                     TextUtils.TruncateAt.MIDDLE))
                // Use condensed title to save path as API doesn't
                // work as documented
                .setTitleCondensed(name);
        }

        // Add clear list item
        sub.add(Menu.NONE, R.id.clearList, Menu.NONE, R.string.clearList);

        return true;
    }

    // onOptionsItemSelected
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case android.R.id.home:
            onBackPressed();
            break;
        case R.id.newFile:
            newFile();
            break;
        case R.id.edit:
            editClicked(item);
            break;
        case R.id.view:
            viewClicked(item);
            break;
        case R.id.open:
            openFile();
            break;
        case R.id.save:
            saveFileHandler();
            break;
        case R.id.saveAs:
            saveAs();
            break;
        case R.id.clearList:
            clearList();
            break;
        case R.id.findAll:
            findAll();
            break;
        case R.id.goTo:
            goTo();
            break;
        case R.id.print:
            print();
            break;
        case R.id.viewMarkdown:
            viewMarkdown();
            break;
        case R.id.viewFile:
            viewFileClicked(item);
            break;
        case R.id.openLast:
            openLastClicked(item);
            break;
        case R.id.autoSave:
            autoSaveClicked(item);
            break;
        case R.id.wrap:
            wrapClicked(item);
            break;
        case R.id.suggest:
            suggestClicked(item);
            break;
        case R.id.highlight:
            highlightClicked(item);
            break;
        case R.id.light:
            themeClicked(item, LIGHT);
            break;
        case R.id.dark:
            themeClicked(item,DARK);
            break;
        case R.id.system:
            themeClicked(item,SYSTEM);
            break;
        case R.id.white:
            themeClicked(item,WHITE);
            break;
        case R.id.black:
            themeClicked(item,BLACK);
            break;
        case R.id.retro:
            themeClicked(item,RETRO);
            break;
        case R.id.small:
            textSizeClicked(item,SMALL);
            break;
        case R.id.medium:
            textSizeClicked(item,MEDIUM);
            break;
        case R.id.large:
            textSizeClicked(item,LARGE);
            break;
        case R.id.about:
            aboutClicked();
            break;
        case R.id.fileItem:
            openRecent(item);
            break;
        case R.id.charsetItem:
            setCharset(item);
            break;
        case R.id.typefaceItem:
            setTypeface(item);
            break;
        default:
           break;
        }

        // Close text search
        if (searchItem != null && searchItem.isActionViewExpanded() &&
                item.getItemId() != R.id.findAll)
            searchItem.collapseActionView();

        return true;
    }

    // onBackPressed
    @Override
    public void onBackPressed()
    {
        // Close text search
        if (searchItem != null && searchItem.isActionViewExpanded())
        {
            searchItem.collapseActionView();
            return;
        }

        if (sharedVariables.changed)
            alertDialog(this, R.string.appName, R.string.modified,
                        R.string.save, R.string.discard, (dialog, id) ->
        {
            if (id == DialogInterface.BUTTON_POSITIVE) {
                saveFileHandler();
                finish();
            } else if (id == DialogInterface.BUTTON_NEGATIVE) {
                sharedVariables.changed = false;
                finish();
            }
        });

        else
            finish();
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
            sharedVariables.fileWrapper.content = data.getData();
            readFile(sharedVariables.fileWrapper.content);
            break;

        case 2:
            sharedVariables.fileWrapper.content = data.getData();
            setTitle(FileUtils.getDisplayName(this, sharedVariables.fileWrapper.content, null, null));
            saveFileHandler();
            break;
        default:
            break;
        }
    }

    // dispatchTouchEvent
    @Override
    public boolean dispatchTouchEvent(MotionEvent event)
    {
        scaleDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    // onKeyDown
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // Check Ctrl key
        if (event.isCtrlPressed())
        {
            switch (keyCode)
            {
                // Edit, View
            case KeyEvent.KEYCODE_E:
                if (event.isShiftPressed())
                    viewClicked(null);
                else
                    editClicked(null);
                break;
                // Search
            case KeyEvent.KEYCODE_F:
                if (event.isShiftPressed())
                    searchItem.collapseActionView();
                else
                    searchItem.expandActionView();
                // Find next
                if (event.isAltPressed() &&
                    searchItem.isActionViewExpanded())
                    queryTextListener.onQueryTextSubmit
                        (searchView.getQuery().toString());
                break;
                // Goto
            case KeyEvent.KEYCODE_G:
                goTo();
                break;
                // Menu
            case KeyEvent.KEYCODE_M:
                openOptionsMenu();
                break;
                // New
            case KeyEvent.KEYCODE_N:
                newFile();
                break;
                // Open
            case KeyEvent.KEYCODE_O:
                openFile();
                break;
                // Print
            case KeyEvent.KEYCODE_P:
                print();
                break;
                // Save, Save as
            case KeyEvent.KEYCODE_S:
                if (event.isShiftPressed())
                    saveAs();
                else
                    saveFileHandler();
                break;
                // Increase text size
            case KeyEvent.KEYCODE_EQUALS,KeyEvent.KEYCODE_PLUS:
                sharedVariables.size += 2;
                sharedVariables.size = Math.max(TINY, Math.min(sharedVariables.size, HUGE));
                textView.setTextSize(sharedVariables.size);
                break;
                // Decrease text size
            case KeyEvent.KEYCODE_MINUS:
                sharedVariables.size -= 2;
                sharedVariables.size = Math.max(TINY, Math.min(sharedVariables.size, HUGE));
                textView.setTextSize(sharedVariables.size);
                break;

            default:
                return super.onKeyDown(keyCode, event);
            }

            return true;
        }

        else
        {
            switch (keyCode)
            {
                // Find next
            case KeyEvent.KEYCODE_F3:
                if (searchItem.isActionViewExpanded())
                    queryTextListener.onQueryTextSubmit
                        (searchView.getQuery().toString());
                break;
                // Menu
            case KeyEvent.KEYCODE_F10:
                openOptionsMenu();
                break;

            default:
                return super.onKeyDown(keyCode, event);
            }

            return true;
        }
    }

    // editClicked
    private void editClicked(MenuItem item)
    {
        // Get scroll position
        int y = scrollView.getScrollY();
        // Get height
        int height = scrollView.getHeight();
        // Get width
        int width = scrollView.getWidth();

        // Get offset
        int line = textView.getLayout()
            .getLineForVertical(y + height / 2);
        int offset = textView.getLayout()
            .getOffsetForHorizontal(line, (float) width / 2);
        // Set cursor
        textView.setSelection(offset);

        // Set editable with or without suggestions
        if ((boolean) editorPreferences.get(Preferences.isSuggestEnabled))
            textView.setInputType(InputType.TYPE_CLASS_TEXT |
                                  InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        else
            textView.setInputType(InputType.TYPE_CLASS_TEXT |
                                  InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                                  InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        // Update boolean
        edit = true;

        // Recreate
        recreate(this);
    }

    // viewClicked
    private void viewClicked(MenuItem item)
    {
        // Set read only
        textView.setRawInputType(InputType.TYPE_NULL);
        textView.setTextIsSelectable(true);
        textView.clearFocus();

        // Update boolean
        edit = false;

        // Update menu
        invalidateOptionsMenu();
    }

    // newFile
    private void newFile()
    {
        // Check if file changed
        if (sharedVariables.changed) {
            alertDialog(this, R.string.newFile, R.string.modified,
                    R.string.save, R.string.discard, (dialog, id) ->
                    {
                        if(id == DialogInterface.BUTTON_POSITIVE){
                            saveFileHandler();
                        }
                    });
        }
        defaultFile(null);
        invalidateOptionsMenu();
    }

    // defaultFile
    private void defaultFile(String text)
    {
        textView.setText("");
        sharedVariables.changed = false;

        sharedVariables.fileWrapper.file = fileHandler.getNewFile();
        sharedVariables.fileWrapper.uri = Uri.fromFile(sharedVariables.fileWrapper.file);
        sharedVariables.fileWrapper.path = sharedVariables.fileWrapper.uri.getPath();
        sharedVariables.fileWrapper.content = null;

        if (text != null){
            textView.append(text);
        }
        else
        {
            setTitle(sharedVariables.fileWrapper.uri.getLastPathSegment());
            sharedVariables.match = sharedConstants.UTF_8;
            getActionBar().setSubtitle(sharedVariables.match);
        }
    }

    // lastFile
    private void lastFile()
    {
        String lastFilePath = (String) editorPreferences.get(Preferences.File);

        if (lastFilePath.isEmpty())
        {
            defaultFile(null);
            return;
        }

        sharedVariables.fileWrapper.file = new File(lastFilePath);
        sharedVariables.fileWrapper.uri = Uri.fromFile(sharedVariables.fileWrapper.file);
        lastFilePath = sharedVariables.fileWrapper.uri.getPath();

        if (sharedVariables.fileWrapper.file.exists())
            readFile(sharedVariables.fileWrapper.uri);

        else
        {
            setTitle(sharedVariables.fileWrapper.uri.getLastPathSegment());
            sharedVariables.match = sharedConstants.UTF_8;
            getActionBar().setSubtitle(sharedVariables.match);
        }
    }

    // setCharset
    private void setCharset(MenuItem item)
    {
        sharedVariables.match = item.getTitle().toString();
        getActionBar().setSubtitle(sharedVariables.match);
    }

    // setTypeface
    private void setTypeface(MenuItem item)
    {
        String name = item.getTitle().toString();
        Typeface typeface = Typeface.create(name, Typeface.NORMAL);
        textView.setTypeface(typeface);
        item.setChecked(true);
        String[] typefaces = getResources().getStringArray(R.array.typefaces);
        List<String> list = Arrays.asList(typefaces);
        type = list.indexOf(name);
    }

    // alertDialog
    private static AlertDialog.Builder buildNewAlertDialog(Context context,int title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title);
        try{
            //Method was called from alertDialog with pos/neg button
            int messageInt = Integer.parseInt(message);
            builder.setMessage(messageInt);
        }catch (Exception e){
            //Method was called from alertDialog with neutral button
            builder.setMessage(message);
        }
        return builder;
    }

    private static void alertDialog(Context context, int title, int message,
                                    int positiveButton, int negativeButton,
                                    DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder builder = buildNewAlertDialog(context,title,message+"");

        // Add the buttons
        builder.setPositiveButton(positiveButton, listener);
        builder.setNegativeButton(negativeButton, listener);

        // Create the AlertDialog
        builder.show();
    }

    // alertDialog
    public static void alertDialog(Context context, int title,
                                    String message, int neutralButton)
    {
        AlertDialog.Builder builder = buildNewAlertDialog(context,title,message);

        // Add the buttons
        builder.setNeutralButton(neutralButton, null);

        // Create the AlertDialog
        builder.show();
    }

    // savePath
    private void savePath(String path)
    {
        if (path == null)
            return;

        // Save the current position
        pathMap.put(path, scrollView.getScrollY());

        // Get a list of files
        List<Long> list = new ArrayList<>();
        Map<Long, String> map = new HashMap<>();
        for (String name: pathMap.keySet())
        {
            File temp = new File(name);
            // Add to remove list if non existant
            if (!temp.exists())
            {
                removeList.add(name);
                continue;
            }

            list.add(temp.lastModified());
            map.put(temp.lastModified(), name);
        }

        // Remove non existant entries
        for (String name: removeList)
            pathMap.remove(name);

        // Sort in reverse order
        Collections.sort(list);
        Collections.reverse(list);

        int count = 0;
        for (long date : list)
        {
            String name = map.get(date);

            // Remove old files
            if (count >= sharedConstants.MAX_PATHS)
            {
                pathMap.remove(name);
                removeList.add(name);
            }

            count++;
        }
    }

    // openRecent
    private void openRecent(MenuItem item)
    {
        // Get path from condensed title
        String name = item.getTitleCondensed().toString();
        File fileToOpen = new File(name);

        // Check absolute file
        if (!fileToOpen.isAbsolute())
            fileToOpen = new File(Environment.getExternalStorageDirectory(), name);
        // Check it exists
        if (fileToOpen.exists())
        {
            Uri uriToOpen = Uri.fromFile(fileToOpen);

            if (sharedVariables.changed)
                alertDialog(this, R.string.openRecent, R.string.modified,
                            R.string.save, R.string.discard, (dialog, id) ->
            {
                if (id == DialogInterface.BUTTON_POSITIVE) {
                    saveFileHandler();
                    startActivity(new Intent(Intent.ACTION_EDIT, uriToOpen,
                            this, Editor.class));
                } else if (id == DialogInterface.BUTTON_NEGATIVE) {
                    startActivity(new Intent(Intent.ACTION_EDIT, uriToOpen,
                            this, Editor.class));
                }
            });

            else
                // New instance
                startActivity(new Intent(Intent.ACTION_EDIT, uriToOpen,
                                         this, Editor.class));
        }
    }

    // saveAs
    private void saveAs()
    {
        // Remove path prefix
        String name =
                sharedVariables.fileWrapper.path.replaceFirst(Environment
                              .getExternalStorageDirectory()
                              .getPath() + File.separator, "");
        // Open dialog
        saveAsDialog(this, name, (dialog, id) ->
        {
            switch (id)
            {
            case DialogInterface.BUTTON_POSITIVE:
                EditText text = ((Dialog) dialog).findViewById(R.id.pathText);
                String string = text.getText().toString();

                // Ignore empty string
                if (string.isEmpty())
                    return;

                sharedVariables.fileWrapper.file = new File(string);

                // Check absolute file
                if (!sharedVariables.fileWrapper.file.isAbsolute())
                    sharedVariables.fileWrapper.file = new
                        File(Environment.getExternalStorageDirectory(), string);

                // Check uri
                sharedVariables.fileWrapper.uri = Uri.fromFile(sharedVariables.fileWrapper.file);
                Uri newUri = Uri.fromFile(fileHandler.getNewFile());
                if (newUri.getPath().equals(sharedVariables.fileWrapper.uri.getPath()))
                {
                    saveAs();
                    return;
                }

                // Check exists
                if (sharedVariables.fileWrapper.file.exists())
                    alertDialog(this, R.string.appName,
                                R.string.changedOverwrite,
                                R.string.overwrite, R.string.cancel, (d, b) ->
                    {
                        if (b == DialogInterface.BUTTON_POSITIVE) {// Set interface title
                            setTitle(sharedVariables.fileWrapper.uri.getLastPathSegment());
                            sharedVariables.fileWrapper.path = sharedVariables.fileWrapper.file.getPath();
                            saveFileHandler();
                        }
                    });

                else
                {
                    // Set interface title
                    setTitle(sharedVariables.fileWrapper.uri.getLastPathSegment());
                    sharedVariables.fileWrapper.path = sharedVariables.fileWrapper.file.getPath();
                    sharedVariables.fileWrapper.content = null;
                    saveFileHandler();
                }
                break;

            case DialogInterface.BUTTON_NEUTRAL:
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.setType(sharedConstants.TEXT_WILD);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_TITLE, sharedVariables.fileWrapper.uri.getLastPathSegment());
                startActivityForResult(intent, sharedConstants.CREATE_DOCUMENT);
                break;
            default:
                break;
            }
        });
    }

    // saveAsDialog
    private static void saveAsDialog(Context context, String path,
                                     DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.save);
        builder.setMessage(R.string.choose);

        // Add the buttons
        builder.setPositiveButton(R.string.save, listener);
        builder.setNegativeButton(R.string.cancel, listener);
        builder.setNeutralButton(R.string.storage, listener);

        // Create edit text
        LayoutInflater inflater = (LayoutInflater) builder.getContext()
            .getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.save_path, null);
        builder.setView(view);

        // Create the AlertDialog
        AlertDialog dialog = builder.show();
        TextView text = dialog.findViewById(R.id.pathText);
        text.setText(path);
    }

    // clearList
    private void clearList()
    {
        for (String temp : pathMap.keySet())
            removeList.add(temp);

        pathMap.clear();
    }

    // findAll
    public void findAll()
    {
        // Get search string
        String search = searchView.getQuery().toString();

        FindTask findTask = new FindTask(this,search);
        findTask.execute();
    }

    // goTo
    public void goTo()
    {
        gotoDialog((seekBar, progress) ->
        {
            int height = textView.getHeight();
            int pos = progress * height / seekBar.getMax();

            // Scroll to it
            scrollView.smoothScrollTo(0, pos);
        });
    }

    // OnSeekBarChangeListener
    public interface OnSeekBarChangeListener
    {
        void onProgressChanged(SeekBar seekBar, int progress);
    }

    // GotoDialog
    private void gotoDialog(OnSeekBarChangeListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.goTo);

        // Add the buttons
        builder.setNegativeButton(R.string.cancel, null);

        // Create seek bar
        LayoutInflater inflater = (LayoutInflater) builder.getContext()
            .getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.seek_bar, null);
        builder.setView(view);

        // Create the AlertDialog
        AlertDialog dialog = builder.show();
        SeekBar seekBar = dialog.findViewById(R.id.seekBar);
        int height = textView.getHeight();
        int progress = scrollView.getScrollY() * seekBar.getMax() / height;
        seekBar.setProgress(progress);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser)
            {
                if (fromUser)
                    listener.onProgressChanged(seekBar, progress);
            }

            @Override
            public void onStartTrackingTouch (SeekBar seekBar) { /* Needs to be overridden because of android */ }

            @Override
            public void onStopTrackingTouch (SeekBar seekBar)
            {
                dialog.dismiss();
            }
        });
    }

    // print
    private void print()
    {
        WebView webView = new WebView(this);

        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                // Get a PrintManager instance
                PrintManager printManager = (PrintManager)
                    getSystemService(PRINT_SERVICE);

                String jobName = getString(R.string.appName) + " Document";

                // Get a print adapter instance
                PrintDocumentAdapter printAdapter =
                    view.createPrintDocumentAdapter(jobName);

                // Create a print job with name and adapter instance
                printManager
                    .print(jobName, printAdapter,
                           new PrintAttributes.Builder()
                           .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                           .build());
            }
        });

        String htmlDocument = sharedConstants.HTML_HEAD + Html.toHtml(textView.getText()) + sharedConstants.HTML_TAIL;
        webView.loadData(htmlDocument, sharedConstants.TEXT_HTML, sharedConstants.UTF_8);
    }

    // viewMarkdown
    private void viewMarkdown()
    {
        String text = textView.getText().toString();

        // Use commonmark
        Parser parser = Parser.builder().build();
        Node document = parser.parse(text);
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        String html = renderer.render(document);

        File markdownFile = new File(getCacheDir(), sharedConstants.HTML_FILE);
        markdownFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(markdownFile))
        {
            // Add HTML header and footer to make a valid page.
            writer.write(sharedConstants.HTML_HEAD);
            writer.write(html);
            writer.write(sharedConstants.HTML_TAIL);
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            // Get file provider uri
            Uri fileProviderUri = FileProvider.getUriForFile
                (this, sharedConstants.FILE_PROVIDER, markdownFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileProviderUri, sharedConstants.TEXT_HTML);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // viewFileClicked
    private void viewFileClicked(MenuItem item)
    {
        editorPreferences.put(Preferences.isReadOnly, !((boolean) editorPreferences.get(Preferences.isReadOnly)));
        item.setChecked(((boolean) editorPreferences.get(Preferences.isReadOnly)));
    }

    // openLastClicked
    private void openLastClicked(MenuItem item)
    {
        editorPreferences.put(Preferences.isLast, !((boolean) editorPreferences.get(Preferences.isLast)));
        item.setChecked(((boolean) editorPreferences.get(Preferences.isLast)));
    }

    // autoSaveClicked
    private void autoSaveClicked(MenuItem item)
    {
        editorPreferences.put(Preferences.autoSaveFeature, !((boolean) editorPreferences.get(Preferences.autoSaveFeature)));
        item.setChecked(((boolean) editorPreferences.get(Preferences.autoSaveFeature)));
    }

    // wrapClicked
    private void wrapClicked(MenuItem item)
    {
        editorPreferences.put(Preferences.isContentWrapped, !((boolean) editorPreferences.get(Preferences.isContentWrapped)));
        item.setChecked(((boolean) editorPreferences.get(Preferences.isContentWrapped)));
        recreate(this);
    }

    // suggestClicked
    private void suggestClicked(MenuItem item)
    {
        editorPreferences.put(Preferences.isSuggestEnabled, !((boolean) editorPreferences.get(Preferences.isSuggestEnabled)));
        item.setChecked(((boolean) editorPreferences.get(Preferences.isSuggestEnabled)));

        if ((boolean) editorPreferences.get(Preferences.isSuggestEnabled))
            textView.setRawInputType(InputType.TYPE_CLASS_TEXT |
                                     InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        else
            textView.setRawInputType(InputType.TYPE_CLASS_TEXT |
                                     InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                                     InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        recreate(this);
    }

    // highlightClicked
    private void highlightClicked(MenuItem item)
    {
        editorPreferences.put(Preferences.isHighlightEnabled, !((boolean) editorPreferences.get(Preferences.isHighlightEnabled)));
        item.setChecked(((boolean) editorPreferences.get(Preferences.isHighlightEnabled)));

        editorTextUtils.checkHighlight(editorPreferences,sharedVariables.fileWrapper.file,textView,scrollView);
    }

    private void themeClicked(MenuItem item,int selectedTheme)
    {
        theme = selectedTheme;
        item.setChecked(true);
        recreate(this);
    }

    private void textSizeClicked(MenuItem item, int selectedSize)
    {
        sharedVariables.size = selectedSize;
        item.setChecked(true);

        textView.setTextSize(sharedVariables.size);
    }

    // setSizeAndTypeface
    private void setSizeAndTypeface(int size, int type)
    {
        // Set size
        textView.setTextSize(size);

        // Set type
        String[] names = getResources().getStringArray(R.array.typefaces);
        Typeface typeface = Typeface.create(names[type], Typeface.NORMAL);
        textView.setTypeface(typeface);
    }

    // aboutClicked
    private void aboutClicked()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.appName);

        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        SpannableStringBuilder spannable =
            new SpannableStringBuilder(getText(R.string.version));
        Pattern pattern = Pattern.compile("%s");
        Matcher matcher = pattern.matcher(spannable);
        if (matcher.find())
            spannable.replace(matcher.start(), matcher.end(),
                              BuildConfig.VERSION_NAME);
        matcher.reset(spannable);
        if (matcher.find())
            spannable.replace(matcher.start(), matcher.end(),
                              dateFormat.format(BuildConfig.BUILT));
        builder.setMessage(spannable);

        // Add the button
        builder.setPositiveButton(R.string.ok, null);

        // Create the AlertDialog
        Dialog dialog = builder.show();

        // Set movement method
        TextView text = dialog.findViewById(android.R.id.message);
        if (text != null)
        {
            TextViewCompat.setTextAppearance(textView, android.R.style.TextAppearance_Small);
            text.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    // recreate
    private void recreate(Context context)
    {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M)
            recreate();
    }

    // openFile
    private void openFile() {
        // Check if file changed
        if (sharedVariables.changed) {
        alertDialog(this, R.string.open, R.string.modified,R.string.save, R.string.discard, (dialog, id) ->
                {
                    if (id == DialogInterface.BUTTON_POSITIVE) {
                        saveFileHandler();
                    } else if (id == DialogInterface.BUTTON_NEGATIVE) {
                        sharedVariables.changed = false;
                    }
                });
        }
        getFile();
    }

    // getFile
    private void getFile()
    {
        if(!checkPermissions(sharedConstants.REQUEST_OPEN)){
            return;
        }

        // Open parent folder
        File dir = sharedVariables.fileWrapper.file.getParentFile();
        getFile(dir);
    }

    // getFile
    private void getFile(File dir)
    {
        // Get list of files
        List<File> fileList = getList(dir);
        if (fileList == null)
            return;

        // Get list of folders
        List<String> dirList = new ArrayList<>();
        dirList.add(File.separator);
        dirList.addAll(Uri.fromFile(dir).getPathSegments());

        // Pop up dialog
        openDialog(this, dirList, fileList, (dialog, which) ->
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
                readFile(Uri.fromFile(selection));
        });
    }

    // getList
    public static List<File> getList(File dir)
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

    // openDialog
    public static void openDialog(Context context, List<String> dirList,
                                  List<File> fileList,
                                  DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(sharedConstants.FOLDER);

        // Add the adapter
        FileAdapter adapter = new FileAdapter(builder.getContext(), fileList);
        builder.setAdapter(adapter, listener);

        // Add storage button
        builder.setNeutralButton(R.string.storage, listener);
        // Add cancel button
        builder.setNegativeButton(R.string.cancel, null);

        // Create the Dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Find the title view
        ViewGroup title = dialog.findViewById
            (context.getResources().getIdentifier("title_template",
                                                  "id", "android"));
        // Replace content with scroll view
        title.removeAllViews();
        HorizontalScrollView scroll = new
            HorizontalScrollView(dialog.getContext());
        title.addView(scroll);
        // Add a row of folder buttons
        LinearLayout layout = new LinearLayout(dialog.getContext());
        scroll.addView(layout);
        for (String dir: dirList)
        {
            Button button = new Button(dialog.getContext(), null,
                                       android.R.attr.buttonStyleSmall);
            button.setId(dirList.indexOf(dir) + sharedConstants.FOLDER_OFFSET);
            button.setText(dir);
            button.setOnClickListener(v ->
            {
                listener.onClick(dialog, v.getId());
                dialog.dismiss();
            });
            layout.addView(button);
        }

        // Scroll to the end
        scroll.postDelayed(() ->
                scroll.fullScroll(View.FOCUS_RIGHT), sharedConstants.POSITION_DELAY);
    }

    // onRequestPermissionsResult
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults)
    {
        switch (requestCode)
        {
        case 2:
            for (int i = 0; i < grantResults.length; i++)
                if (permissions[i].equals(Manifest.permission
                                          .WRITE_EXTERNAL_STORAGE) &&
                    grantResults[i] == PackageManager.PERMISSION_GRANTED)
                    // Granted, save file
                    saveFileHandler();
            break;

        case 1:
            for (int i = 0; i < grantResults.length; i++)
                if (permissions[i].equals(Manifest.permission
                                          .READ_EXTERNAL_STORAGE) &&
                    grantResults[i] == PackageManager.PERMISSION_GRANTED)
                    // Granted, read file
                    readFile(sharedVariables.fileWrapper.uri);
            break;

        case 3:
            for (int i = 0; i < grantResults.length; i++)
                if (permissions[i].equals(Manifest.permission
                                          .READ_EXTERNAL_STORAGE) &&
                    grantResults[i] == PackageManager.PERMISSION_GRANTED)
                    // Granted, open file
                    getFile();
            break;
            default:
                break;
        }
    }

    public void readFile(Uri uri) {
        if (uri == null) return;

        if (!checkPermissions(sharedConstants.REQUEST_READ)) {
            this.sharedVariables.fileWrapper.uri = uri;
            return;
        }

        if(!checkFileSize(uri)){
            return;
        }


        removeTextViewCallbacks();

        if (BuildConfig.DEBUG) Log.d(sharedConstants.TAG, "Uri: " + uri);

        sharedVariables.fileWrapper.content = resolveOrSetContentUri(uri);

        if (sharedConstants.CONTENT.equalsIgnoreCase(uri.getScheme())) {
            prepareNewFileForReading();
        } else {
            prepareExistingFile(uri);
        }

        textView.setText(R.string.loading);

        try {
            CharSequence fileContent = fileHandler.readFileFromUri(uri);
            displayText(fileContent);
        } catch (Exception e) {
            Log.e(sharedConstants.TAG, "Error reading file: " + e.getMessage(), e);
            alertDialog(this, R.string.appName, e.getMessage(), R.string.ok);
        }

        sharedVariables.changed = false;
        sharedVariables.modified = sharedVariables.fileWrapper.file.lastModified();
        savePath(sharedVariables.fileWrapper.path);
        invalidateOptionsMenu();
    }

    private boolean checkFileSize(Uri uri) {
        long size = sharedConstants.CONTENT.equalsIgnoreCase(uri.getScheme())
                ? FileUtils.getSize(this, uri, null, null)
                : new File(uri.getPath()).length();

        if (BuildConfig.DEBUG) Log.d(sharedConstants.TAG, "Size: " + size);

        if (size > sharedConstants.TOO_LARGE) {
            showFileTooLargeAlert(size);
            return false;
        }
        return true;
    }

    private void showFileTooLargeAlert(long size) {
        String large = getString(R.string.tooLarge);
        alertDialog(this, R.string.appName,
                String.format(large, FileUtils.getReadableFileSize(size)),
                R.string.ok);
    }

    private void removeTextViewCallbacks() {
        textView.removeCallbacks(sharedVariables.updateHighlight);
        textView.removeCallbacks(updateWordCount);
    }

    private Uri resolveOrSetContentUri(Uri uri) {
        if (sharedConstants.CONTENT.equalsIgnoreCase(uri.getScheme())) {
            sharedVariables.fileWrapper.content = uri;
            return resolveContent(uri);
        } else {
            sharedVariables.fileWrapper.content = null;
            return uri;
        }
    }

    private void prepareNewFileForReading() {
        sharedVariables.fileWrapper.file = fileHandler.getNewFile();
        sharedVariables.fileWrapper.path = Uri.fromFile(sharedVariables.fileWrapper.file).getPath();
        setTitle(FileUtils.getDisplayName(this, sharedVariables.fileWrapper.content, null, null));
    }

    private void prepareExistingFile(Uri uri) {
        sharedVariables.fileWrapper.uri = uri;
        sharedVariables.fileWrapper.path = uri.getPath();
        sharedVariables.fileWrapper.file = new File(sharedVariables.fileWrapper.path);
        setTitle(uri.getLastPathSegment());
    }

    private Uri resolveContent(Uri uri) {
        String path = FileUtils.getPath(this, uri);
        if (path != null) {
            File temp = new File(path);
            if (temp.canRead()) uri = Uri.fromFile(temp);
        }
        return uri;
    }
    public void displayText(CharSequence text)
    {
        if (textView != null)
            textView.setText(text);

        sharedVariables.changed = false;

        // Check for saved position
        if (pathMap.containsKey(sharedVariables.fileWrapper.path))
            textView.postDelayed(() ->
                            scrollView.smoothScrollTo
                                    (0, pathMap.get(sharedVariables.fileWrapper.path)),
                    sharedConstants.POSITION_DELAY);
        else
            textView.postDelayed(() ->
                            scrollView.smoothScrollTo(0, 0),
                    sharedConstants.POSITION_DELAY);
        // Check mode
        checkMode(text);

        // Check highlighting
        editorTextUtils.checkHighlight(editorPreferences,sharedVariables.fileWrapper.file,textView,scrollView);

        // Set read only
        if ((boolean) editorPreferences.get(Preferences.isReadOnly))
        {
            textView.setRawInputType(InputType.TYPE_NULL);
            textView.setTextIsSelectable(true);

            // Update boolean
            edit = false;
        }

        else
        {
            // Set editable with or without suggestions
            if ((boolean) editorPreferences.get(Preferences.isSuggestEnabled))
                textView.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            else
                textView.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                        InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            // Update boolean
            edit = true;
        }

        // Dismiss keyboard
        textView.clearFocus();

        // Update menu
        invalidateOptionsMenu();
    }



    private void saveFileHandler() {
        if (!checkPermissions(sharedConstants.REQUEST_SAVE)) {
            return;
        }

        Uri currentUri = Uri.fromFile(sharedVariables.fileWrapper.file);
        Uri newFileUri = Uri.fromFile(fileHandler.getNewFile());

        if (sharedVariables.fileWrapper.content == null && newFileUri.getPath().equals(currentUri.getPath())) {
            saveAs();
        } else {
                removeTextViewCallbacks();

            if (sharedVariables.fileWrapper.file.lastModified() > sharedVariables.modified) {
                promptOverwrite();
                sharedVariables.changed = false;
            } else {
                saveFile(sharedVariables.fileWrapper.content != null ? sharedVariables.fileWrapper.content : sharedVariables.fileWrapper.file);
            }
        }
    }
    private void promptOverwrite() {
        alertDialog(this, R.string.appName, R.string.changedOverwrite, R.string.overwrite, R.string.cancel, (dialog, id) -> {
            if (id == DialogInterface.BUTTON_POSITIVE) {
                saveFile(sharedVariables.fileWrapper.file);
            }
        });
    }

    private void saveFile(Object input) {
        CharSequence textContent = textView.getText();
        try {
            String charset = resolveCharset();

            if (input instanceof Uri) {
                writeToUri((Uri) input, textContent, charset);
            } else if (input instanceof File) {
                writeToFile((File) input, textContent, charset);
            } else {
                throw new IllegalArgumentException("Unsupported input type. Expected Uri or File.");
            }
        } catch (Exception e) {
            handleSaveError(e);
        }

        invalidateOptionsMenu();
    }

    private String resolveCharset() {
        if (sharedVariables.match != null && !sharedVariables.match.equals(getString(R.string.detect))) {
            return sharedVariables.match;
        }
        return sharedConstants.UTF_8;
    }

    private void writeToUri(Uri uri, CharSequence textContent, String charset) throws IOException {
            fileHandler.writeToUri(textContent, charset,uri);

    }

    private void writeToFile(File file, CharSequence textContent, String charset) throws IOException {
        fileHandler.writeToFile(textContent, file, charset);
        savePath(file.getPath());
    }

    private void handleSaveError(Exception e) {
        alertDialog(this, R.string.appName, e.getMessage(), R.string.ok);
        e.printStackTrace();
    }

    // onActionModeStarted
    @Override
    public void onActionModeStarted(ActionMode mode)
    {
        super.onActionModeStarted(mode);

        // If there's a file
        if (sharedVariables.fileWrapper.file != null)
        {
            // Get the mime type
            String mimeType = FileUtils.getMimeType(sharedVariables.fileWrapper.file);
            // If the type is not text/plain
            if (!sharedConstants.TEXT_PLAIN.equals(mimeType))
            {
                // Get the start and end of the selection
                int start = textView.getSelectionStart();
                int end = textView.getSelectionEnd();
                // And the text
                CharSequence text = textView.getText();

                // Get a pattern and a matcher for delimiter
                // characters
                Matcher matcher = PATTERN_CHARS.matcher(text);

                // Find the first match after the end of the selection
                if (matcher.find(end))
                {
                    // Update the selection end
                    end = matcher.start();

                    // Get the matched char
                    char c = text.charAt(end);

                    // Check for opening brackets
                    if (sharedConstants.BRACKET_CHARS.indexOf(c) == -1)
                    {
                        switch (c)
                        {
                            // Check for close brackets and look for
                            // the open brackets
                        case ')':
                            c = '(';
                            break;

                        case ']':
                            c = '[';
                            break;

                        case '}':
                            c = '{';
                            break;

                        case '>':
                            c = '<';
                            break;
                        default:
                            break;
                        }

                        String string = text.toString();
                        // Do reverse search
                        start = string.lastIndexOf(c, start) + 1;

                        // Check for included newline
                        if (start > string.lastIndexOf('\n', end))
                            // Update selection
                            textView.setSelection(start, end);
                    }
                }
            }
        }
    }

    // checkMode
    private void checkMode(CharSequence text) {
        boolean change = false;

        CharSequence first = text.subSequence(0, Math.min(text.length(), sharedConstants.FIRST_SIZE));
        CharSequence last = text.subSequence(Math.max(0, text.length() - sharedConstants.LAST_SIZE), text.length());

        for (CharSequence sequence : new CharSequence[]{first, last}) {
            Matcher matcher = MODE_PATTERN.matcher(sequence);
            if (matcher.find()) {
                matcher.region(matcher.start(1), matcher.end(1));
                matcher.usePattern(OPTION_PATTERN);
                while (matcher.find()) {
                    boolean no = "no".equals(matcher.group(2));
                    String option = matcher.group(3);
                    String value = matcher.group(4);

                    switch (option) {
                        case "vw":
                            change |= updatePreference(Preferences.isReadOnly.name(), no);
                            break;

                        case "ww":
                            change |= updatePreference(Preferences.isContentWrapped.name(), no);
                            break;

                        case "sg":
                            change |= updatePreference(Preferences.isSuggestEnabled.name(), no);
                            break;

                        case "hs":
                            change |= updatePreference(Preferences.isHighlightEnabled.name(), no);
                            if (change) {
                                editorTextUtils.checkHighlight(editorPreferences, sharedVariables.fileWrapper.file, textView, scrollView);
                            }
                            break;

                        case "th":
                            change |= updateTheme(value);
                            break;

                        case "ts":
                            change |= updateFontSize(value);
                            break;

                        case "tf":
                            change |= updateTypeface(value);
                            break;

                        case "cs":
                            if (":u".equals(value)) {
                                sharedVariables.match = sharedConstants.UTF_8;
                                getActionBar().setSubtitle(sharedVariables.match);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        if (change) {
            recreate(this);
        }
    }

    private boolean updatePreference(String preferenceKey, boolean no) {
        boolean current = (boolean) editorPreferences.get(preferenceKey);
        if (current == no) {
            editorPreferences.put(Preferences.valueOf(preferenceKey), !no);
            return true;
        }
        return false;
    }

    private boolean updateTheme(String value) {
        int newTheme;
        switch (value) {
            case ":l":
                newTheme = LIGHT;
                break;
            case ":d":
                newTheme = DARK;
                break;
            case ":s":
                newTheme = SYSTEM;
                break;
            case ":w":
                newTheme = WHITE;
                break;
            case ":b":
                newTheme = BLACK;
                break;
            case ":r":
                newTheme = RETRO;
                break;
            default:
                return false;
        }
        if (theme != newTheme) {
            theme = newTheme;
            return true;
        }
        return false;
    }

    private boolean updateFontSize(String value) {
        int newSize;
        switch (value) {
            case ":l":
                newSize = LARGE;
                break;
            case ":m":
                newSize = MEDIUM;
                break;
            case ":s":
                newSize = SMALL;
                break;
            default:
                return false;
        }
        if (sharedVariables.size != newSize) {
            sharedVariables.size = newSize;
            textView.setTextSize(newSize);
            return true;
        }
        return false;
    }

    private boolean updateTypeface(String value) {
        Typeface newTypeface;
        int newType;
        switch (value) {
            case ":m":
                newTypeface = Typeface.MONOSPACE;
                newType = MONO;
                break;
            case ":p":
                newTypeface = Typeface.DEFAULT;
                newType = NORMAL;
                break;
            case ":s":
                newTypeface = Typeface.SERIF;
                newType = SERIF;
                break;
            default:
                return false;
        }
        if (type != newType) {
            type = newType;
            textView.setTypeface(newTypeface);
            return true;
        }
        return false;
    }



    // FindTask
    private static class FindTask {
        private final WeakReference<Editor> editorWeakReference;
        private final String search;
        private final ExecutorService executorService;
        private final Handler mainHandler;

        // Constructor
        public FindTask(Editor editor, String search) {
            this.editorWeakReference = new WeakReference<>(editor);
            this.search = search;
            this.executorService = Executors.newSingleThreadExecutor();
            this.mainHandler = new Handler(Looper.getMainLooper());
        }

        // Execute method
        public void execute() {
            executorService.execute(this::doInBackground);
        }

        // Background processing
        private void doInBackground() {
            Pattern pattern;
            List<File> matchList = new ArrayList<>();
            final Editor editor = editorWeakReference.get();
            if (editor == null) return;

            // Compile the search pattern
            try {
                pattern = Pattern.compile(search, Pattern.MULTILINE);
            } catch (Exception e) {
                return;
            }

            // Get entry list
            List<File> entries = new ArrayList<>();
            for (String path : editor.pathMap.keySet()) {
                File entry = new File(path);
                entries.add(entry);
            }

            // Check the entries
            for (File file : entries) {
                CharSequence content = editor.fileHandler.readFileFromFile(file);
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    matchList.add(file);
                }
            }

            // Pass the result to the UI thread
            onPostExecute(matchList);
        }

        // UI updates
        private void onPostExecute(List<File> matchList) {
            mainHandler.post(() -> {
                final Editor editor = editorWeakReference.get();
                if (editor == null) return;

                // Build dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(editor);
                builder.setTitle(R.string.findAll);

                // If found, populate dialog
                if (!matchList.isEmpty()) {
                    List<String> choiceList = new ArrayList<>();
                    for (File file : matchList) {
                        String path = file.getPath();
                        String name = path.replaceFirst(Environment
                                .getExternalStorageDirectory()
                                .getPath() + File.separator, "");
                        choiceList.add(name);
                    }

                    String[] choices = choiceList.toArray(new String[0]);
                    builder.setItems(choices, (dialog, which) -> {
                        File file = matchList.get(which);
                        Uri uri = Uri.fromFile(file);
                        // Open the entry chosen
                        editor.readFile(uri);

                        // Restore the search text after a delay
                        editor.searchView.postDelayed(() ->
                                editor.searchView.setQuery(search, false), sharedConstants.FIND_DELAY);
                    });
                }

                builder.setNegativeButton(android.R.string.cancel, null);
                builder.show();
            });
        }
    }


    private boolean checkPermissions(int requestCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
                return false;
        }
        return true; //already granted or older versions of android that dont need this
    }
}
