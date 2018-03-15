package shake.primoz.project.com.dyingflashlight.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import shake.primoz.project.com.dyingflashlight.utils.FontManager;


public class TextViewFont extends android.support.v7.widget.AppCompatTextView {

    public TextViewFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public TextViewFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TextViewFont(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        Typeface iconFont = FontManager.getTypeface(context, FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(this, iconFont);
    }
}