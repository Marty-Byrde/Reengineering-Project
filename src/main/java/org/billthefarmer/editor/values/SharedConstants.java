package org.billthefarmer.editor.values;

public class SharedConstants {

    public final String TAG = "Editor";

    public final String PATH = "path";
    public final String EDIT = "edit";
    public final String MATCH = "match";
    public final String CHANGED = "changed";
    public final String CONTENT = "content";
    public final String MODIFIED = "modified";
    public final String MONOSPACE = "monospace";

    public final String DOCUMENTS = "Documents";
    public final String FOLDER = "Folder";
    public final String UTF_8 = "UTF-8";

    public final String NEW_FILE = "Untitled.txt";
    public final String EDIT_FILE = "Editor.txt";
    public final String HTML_FILE = "Editor.html";

    public final String TEXT_HTML = "text/html";
    public final String TEXT_PLAIN = "text/plain";
    public final String TEXT_WILD = "text/*";

    public final String BRACKET_CHARS = "([{<";

    public final String HTML_HEAD =
            "<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"utf-8\">\n" +
                    "<meta name=\"viewport\" content=\"width=device-width, " +
                    "initial-scale=1.0\">\n</head>\n<body>\n";
    public final String HTML_TAIL = "\n</body>\n</html>\n";
    public final String FILE_PROVIDER =
            "org.billthefarmer.editor.fileprovider";
    public final String OPEN_NEW =
            "org.billthefarmer.editor.OPEN_NEW";


    public final int LAST_SIZE = 256;
    public final int MENU_SIZE = 192;
    public final int FIRST_SIZE = 256;
    public final int TOO_LARGE = 524288;
    public final int FOLDER_OFFSET = 0x7d000000;
    public final int POSITION_DELAY = 128;
    public final int UPDATE_DELAY = 128;
    public final int FIND_DELAY = 128;
    public final int MAX_PATHS = 10;

    public final int GET_TEXT = 0;

    public final int REQUEST_READ = 1;
    public final int REQUEST_SAVE = 2;
    public final int REQUEST_OPEN = 3;

    public final int OPEN_DOCUMENT   = 1;
    public final int CREATE_DOCUMENT = 2;




    private static SharedConstants instance;
    private SharedConstants(){

    }

    public static synchronized SharedConstants getInstance() {
        if (instance == null) {
            instance = new SharedConstants();
        }
        return instance;
    }

}
