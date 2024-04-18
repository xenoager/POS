package com.aidevu.pos.service;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;

public class SharedPreference {

    public static final String KEY_COOKIE = "com.aidevu.pos.key.cookie";
    public static final String KEY_ARROW_POSITION_NFC = "com.aidevu.pos.key.ARROW_POSITION_NFC";

    private static SharedPreference dsp = null;

    public static SharedPreference getInstanceOf(Context c) {
        if (dsp == null) {
            dsp = new SharedPreference(c);
        }
        return dsp;
    }

    private Context mContext;
    private SharedPreferences pref;

    public SharedPreference(Context c) {
        mContext = c;
        final String PREF_NAME = c.getPackageName();
        pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);

    }

    public void putHashSet(String key, HashSet<String> set) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet(key, set);
        editor.commit();
    }

    public void removeHashSet() {
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(KEY_COOKIE);
        editor.commit();
    }

    public HashSet<String> getHashSet(String key, HashSet<String> dftValue) {
        try {
            return (HashSet<String>) pref.getStringSet(key, dftValue);
        } catch (Exception e) {
            e.printStackTrace();
            return dftValue;
        }
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key, String defaultValue) {
        return pref.getString(key, defaultValue);
    }
}
