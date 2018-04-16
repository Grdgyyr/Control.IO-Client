package com.grdgyyr.controlio.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Settings {
    private static final int DEFAULT_MIN_MILLIS_TOPDATE = 10;
    private static final int DEFAULT_MIN_MOVEMENT = 1;
    private static final int DEFAULT_MIN_WHEEL_PIXELS = 10;
    private static final int DEFAULT_MOTION_FACTOR = 50;
    private static final int DEFAULT_PORT = 6000;
    private static final boolean FIRST_USE = true;
    private static final String KEY_CALIBRATION_DELTAX = "CALIBRATION_DELTAX";
    private static final String KEY_CALIBRATION_DELTAY = "CALIBRATION_DELTAY";
    private static final String KEY_FIRST_USE = "FIRST_USE";
    private static final String KEY_MIN_MILLIS_TOPDATE = "MIN_MILLIS_TOPDATE";
    private static final String KEY_MIN_MOVEMENT = "MIN_MOVEMENT";
    private static final String KEY_MIN_WHEEL_PIXELS = "MIN_WHEEL_PIXELS";
    private static final String KEY_MOTION_FACTOR = "MOTION_FACTOR";
    private static final String KEY_PORT = "PORT";
    private static final String KEY_SWITCH_BUTTONS = "SWITCH_BUTTONS";
    private static final String KEY_VOLUME_BUTTONS_RAISE_CLICK = "VOLUME_BUTTONS_RAISE_CLICK";
    private static final String SHARED_PREFS_FILE = "HMPrefs";
    private static final boolean SWITCH_BUTTONS = false;
    private static final boolean VOLUME_BUTTONS_RAISE_CLICK = false;
    private static Context mContext;

    public static void LoadSettings(Context context) {
        mContext = context;
    }

    public static SharedPreferences getSettings() {
        return mContext.getSharedPreferences(SHARED_PREFS_FILE, 0);
    }

    public static void PushFloat(String key, float f) {
        Editor editor = getSettings().edit();
        editor.putFloat(key, f);
        editor.commit();
    }

    public static void PushBool(String key, boolean b) {
        Editor editor = getSettings().edit();
        editor.putBoolean(key, b);
        editor.commit();
    }

    public static void PushInt(String key, int i) {
        Editor editor = getSettings().edit();
        editor.putInt(key, i);
        editor.commit();
    }

    public static int getMIN_WHEEL_PIXELS() {
        return getSettings().getInt(KEY_MIN_WHEEL_PIXELS, 10);
    }

    public static void setMIN_WHEEL_PIXELS(int value) {
        PushInt(KEY_MIN_WHEEL_PIXELS, value);
    }

    public static int getMIN_MILLIS_TOPDATE() {
        return getSettings().getInt(KEY_MIN_MILLIS_TOPDATE, 10);
    }

    public static void setMIN_MILLIS_TOPDATE(int value) {
        PushInt(KEY_MIN_MILLIS_TOPDATE, value);
    }

    public static int getPORT() {
        return getSettings().getInt(KEY_PORT, DEFAULT_PORT);
    }

    public static void setPORT(int value) {
        PushInt(KEY_PORT, value);
    }

    public static int getMOTION_FACTOR() {
        return getSettings().getInt(KEY_MOTION_FACTOR, DEFAULT_MOTION_FACTOR);
    }

    public static void setMOTION_FACTOR(int value) {
        PushInt(KEY_MOTION_FACTOR, value);
    }

    public static int getMIN_MOVEMENT() {
        return getSettings().getInt(KEY_MIN_MOVEMENT, 1);
    }

    public static void setMIN_MOVEMENT(int value) {
        PushInt(KEY_MIN_MOVEMENT, value);
    }

    public static float getCALIBRATION_DELTAX() {
        return getSettings().getFloat(KEY_CALIBRATION_DELTAX, 0.0f);
    }

    public static void setCALIBRATION_DELTAX(float value) {
        PushFloat(KEY_CALIBRATION_DELTAX, value);
    }

    public static float getCALIBRATION_DELTAY() {
        return getSettings().getFloat(KEY_CALIBRATION_DELTAY, 0.0f);
    }

    public static void setCALIBRATION_DELTAY(float value) {
        PushFloat(KEY_CALIBRATION_DELTAY, value);
    }

    public static boolean getSWITCH_BUTTONS() {
        return getSettings().getBoolean(KEY_SWITCH_BUTTONS, false);
    }

    public static void setSWITCH_BUTTONS(boolean value) {
        PushBool(KEY_SWITCH_BUTTONS, value);
    }

    public static boolean getSWITCH_VOLUME_BUTTONS_RAISE_CLICK() {
        return getSettings().getBoolean(KEY_VOLUME_BUTTONS_RAISE_CLICK, false);
    }

    public static void setSWITCH_VOLUME_BUTTONS_RAISE_CLICK(boolean value) {
        PushBool(KEY_VOLUME_BUTTONS_RAISE_CLICK, value);
    }

    public static boolean getFISRT_USE() {
        return getSettings().getBoolean(KEY_FIRST_USE, FIRST_USE);
    }

    public static void setFISRT_USE(boolean value) {
        PushBool(KEY_FIRST_USE, value);
    }
}
