package realizer.com.makemepopular.utils;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by Win on 23/12/2016.
 */
public class FontManager {

    public static final String ROOT = "fonts/",
            FONTAWESOME = ROOT + "fontawesome-webfont.ttf";

    public static final String ROOT2 = "fonts/",
            FONTAWESOME2 = ROOT2 + "hallo-sans.ttf";

    public static Typeface getTypeface(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), font);
    }

   /* public static void markAsIconContainer(View v, Typeface typeface) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                markAsIconContainer(child);
            }
        } else if (v instanceof TextView) {
            ((TextView) v).setTypeface(typeface);
        }
    }*/

}
