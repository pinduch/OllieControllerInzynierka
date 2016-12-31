package com.android.projects.mateusz.olliecontroller.common;

import android.view.View;

import java.util.regex.Pattern;

/**
 * Created by Mateusz on 24.12.2016.
 */

public class Constant {
    public static int UI_OPTIONS =
             View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            |View.SYSTEM_UI_FLAG_FULLSCREEN
            |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    public static String SETTINGS_ALERT_TITLE = "Saving settings!";
    public static String SETTING_ALERT_MESSAGE = "Are you sure you want to save your changes?";
    public static String SETTINGS_SHARED_PREFERENCES = "com.android.projects.mateusz.olliecontroller.settings";
    public static String HOST_ADDRESS_KEY = "host address";
    public static String USERNAME_AVAILABLE = "Available";
    public static String USERNAME_NOT_AVAILABLE = "Not Available";
    public static String SERVER_CONNECTED = "Connected";
    public static String SERVER_NO_CONNECTION = "No connection";
}

