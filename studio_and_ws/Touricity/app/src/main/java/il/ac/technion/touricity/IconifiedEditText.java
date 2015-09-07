package il.ac.technion.touricity;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.EditText;

public class IconifiedEditText extends EditText {

    public IconifiedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void displayIconWithoutErrorMsg(Drawable icon) {
        if (icon != null) {
            icon.setBounds(new Rect(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight()));
            setCompoundDrawables(null, null, icon, null);
        }
    }

    public void clearIcon() {
        setCompoundDrawables(null, null, null, null);
    }

}
