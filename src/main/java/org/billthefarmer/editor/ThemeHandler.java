package org.billthefarmer.editor;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.Log;

public class ThemeHandler
{
    public final static int LIGHT  = 1;
    public final static int DARK   = 2;
    public final static int SYSTEM = 3;
    public final static int WHITE  = 4;
    public final static int BLACK  = 5;
    public final static int RETRO  = 6;


    public static void setTheme (int theme, Activity activity) {
        Configuration config = activity.getResources().getConfiguration();

        switch (theme)
        {
            case LIGHT:
                activity.setTheme(R.style.AppTheme);
                break;

            case DARK:
                activity.setTheme(R.style.AppDarkTheme);
                break;

            case SYSTEM:
                int night = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;

                switch (night) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        activity.setTheme(R.style.AppTheme);
                        break;

                    case Configuration.UI_MODE_NIGHT_YES:
                        activity.setTheme(R.style.AppDarkTheme);
                        break;
                }
                break;

            case WHITE:
                activity.setTheme(R.style.AppWhiteTheme);
                break;

            case BLACK:
                activity.setTheme(R.style.AppBlackTheme);
                break;

            case RETRO:
                activity.setTheme(R.style.AppRetroTheme);
                break;
        }
        Log.d("Editor", "The Theme has been set...");
    }
}
