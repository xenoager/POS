package com.aidevu.pos.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;

import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class Utils {

    public static String getCurrentActivity(Context context){
        String topActivityName = "";
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> info = manager.getRunningTasks(1);
            ComponentName componentName = info.get(0).topActivity;
            topActivityName = componentName.getClassName();
        } catch (Exception e) {
            Timber.e(e);
        }

        return topActivityName;
    }

    public static String byteArrToHex(byte[] inBytArr) {
        StringBuilder strBuilder = new StringBuilder();
        int j = inBytArr.length;
        for (int i = 0; i < j; i++) {
            strBuilder.append(byte2Hex(inBytArr[i]));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    public static String byte2Hex(Byte inByte) {
        return String.format("%02x", inByte).toUpperCase();
    }

    public static void beep() {

        int toneType = ToneGenerator.TONE_SUP_RADIO_ACK;
        int durationMs = -1;
        int volume = 100;

        try {
            ToneGenerator beep = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, volume);
            beep.startTone(toneType, durationMs);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public static String charArray2HexString(char[] buffer, int bufferLength) {
        String bufferString = "";
        for (int i = 0; i < bufferLength; i++) {
            bufferString += String.format(Locale.ENGLISH, "%02X", Integer.valueOf(buffer[i]));
        }
        return bufferString;
    }
}
