package org.sefaria.sefaria.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import org.sefaria.sefaria.MyApp;
import org.sefaria.sefaria.Util;

/**
 * Created by nss on 2/17/16.
 */
public class SefariaTextView extends TextView {

    public SefariaTextView(Context context) {
        super(context);
    }

    public SefariaTextView (final Context context,final AttributeSet attrs,final int defStyle) {
        super(context,attrs,defStyle);
    }

    public SefariaTextView(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
    }



    public void setFont(Util.Lang lang, boolean isSerif) {
        setFont(lang, isSerif, -1);
    }


    /**
     *
     * @param lang choose font based on language of text
     * @param isSerif
     * @param textSize -1 means keep the textSize the same
     */
    public void setFont(Util.Lang lang, boolean isSerif, float textSize) {
        MyApp.Font font;
        if (lang == Util.Lang.HE) {
            if (isSerif) font = MyApp.Font.TAAMEY_FRANK;
            else font = MyApp.Font.OPEN_SANS_HE;

            if (textSize != -1) {
                setTextSize(textSize);
            }
        } else {
            if (isSerif) font = MyApp.Font.QUATTROCENTO;
            else font = MyApp.Font.MONTSERRAT;

            if (textSize != -1) {
                setTextSize((float) Math.round(textSize * 0.8));
                //Log.d("seftv", "getTextSize() = " + getTextSize() + " rounded = " + Math.round(getTextSize()*0.9));
            }
            setLineSpacing(0, 1.3f);
        }
        setTypeface(MyApp.getFont(font));
    }

    //set text alignment correctly depending on language (only a problem when both languages appear in the text)
    public void setLangGravity(Util.Lang lang) {
        if (lang == Util.Lang.HE) setGravity(Gravity.RIGHT);
        else if (lang == Util.Lang.EN) setGravity(Gravity.LEFT);
    }


}
