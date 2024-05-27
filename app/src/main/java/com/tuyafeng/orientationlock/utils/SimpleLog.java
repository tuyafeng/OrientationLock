package com.tuyafeng.orientationlock.utils;

import android.util.Log;
import com.tuyafeng.orientationlock.BuildConfig;

public class SimpleLog {

    public static void d(String tag, String content) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        Log.d(tag, content);
    }

    private SimpleLog() {
        throw new IllegalStateException();
    }
}
