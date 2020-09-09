package org.gnucash.android.ui.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class TextUtil {
    private TextUtil() {
    }

    public static int getTextPrimary(@NonNull Context themedContext) {
        return getColorAttribute(themedContext, android.R.attr.textColorPrimary);
    }

    public static int getColorAttribute(@NonNull Context themedContext, @AttrRes int attr) {
        TypedValue typedValue = new TypedValue();
        themedContext.getTheme().resolveAttribute(attr, typedValue, true);
        TypedArray arr = themedContext.obtainStyledAttributes(typedValue.data, new int[]{attr});
        @ColorInt int attrValue = arr.getColor(0, -1);
        arr.recycle();
        return attrValue;
    }
}
