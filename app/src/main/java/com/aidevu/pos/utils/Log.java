package com.aidevu.pos.utils;

public class Log {

    public static final String TAG = "kts";

    public static void d(String s) {
        android.util.Log.d(TAG, s);
    }

    public static void e(String s) {
        android.util.Log.e(TAG, s);
    }

    public static void eLarge(String s) {
        if (s.length() > 3000) {
            android.util.Log.e(TAG, s.substring(0, 3000));
            Log.eLarge(s.substring(3000));
        } else {
            android.util.Log.e(TAG, s);
        }
    }
}