package il.ac.technion.touricity;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.widget.TextView;


public class CustomEditTextPreference extends EditTextPreference {

    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        final Resources res = getContext().getResources();
        final Window window = getDialog().getWindow();
        final int colorTeal = res.getColor(R.color.touricity_teal);

        // Title
        final int titleId = res.getIdentifier("alertTitle", "id", "android");
        final View title = window.findViewById(titleId);
        if (title != null) {
            ((TextView)title).setTextColor(colorTeal);
        }

        // Title divider
        final int titleDividerId = res.getIdentifier("titleDivider", "id", "android");
        final View titleDivider = window.findViewById(titleDividerId);
        if (titleDivider != null) {
            titleDivider.setBackgroundColor(colorTeal);
        }
    }
}