package org.billthefarmer.editor.editorSubClasses;

import android.app.Activity;
import android.view.ScaleGestureDetector;
import android.widget.TextView;

import org.billthefarmer.editor.values.SharedVariables;

import static androidx.core.app.ActivityCompat.invalidateOptionsMenu;
import static org.billthefarmer.editor.preferences.EditorPreferenceParameters.HUGE;
import static org.billthefarmer.editor.preferences.EditorPreferenceParameters.TINY;

public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
{
    private SharedVariables sharedVariables;
    private TextView textView;
    private Activity activity;
    public ScaleListener(TextView textView,Activity activity){
        sharedVariables = SharedVariables.getInstance();
        this.textView = textView;
        this.activity = activity;

    }
    // onScale
    @Override
    public boolean onScale(ScaleGestureDetector detector)
    {
        sharedVariables.size *= Math.cbrt(detector.getScaleFactor());
        sharedVariables.size = Math.max(TINY, Math.min(sharedVariables.size, HUGE));
        textView.setTextSize(sharedVariables.size);
        invalidateOptionsMenu(activity);

        return true;
    }
}