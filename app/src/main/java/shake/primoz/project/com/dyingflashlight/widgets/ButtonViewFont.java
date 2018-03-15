package shake.primoz.project.com.dyingflashlight.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageButton;

import shake.primoz.project.com.dyingflashlight.utils.FontManager;


public class ButtonViewFont extends android.support.v7.widget.AppCompatButton {

    public ButtonViewFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ButtonViewFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ButtonViewFont(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        Typeface iconFont = FontManager.getTypeface(context, FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(this, iconFont);
    }
}