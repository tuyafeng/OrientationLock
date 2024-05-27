package com.tuyafeng.orientationlock.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.TypedValue;

public class ContextUtils {

    public static int getColor(Context context, int colorRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(colorRes);
        }
        return context.getResources().getColor(colorRes);
    }

    public static int getColorFromAttr(Context context, int attrColorRes) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { attrColorRes });
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    private ContextUtils() {
        throw new IllegalStateException();
    }
}
