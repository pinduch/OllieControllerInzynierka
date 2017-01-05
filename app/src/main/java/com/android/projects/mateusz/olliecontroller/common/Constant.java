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
    public static final String SETTINGS_ALERT_TITLE = "Saving settings!";
    public static final String SETTING_ALERT_MESSAGE = "Are you sure you want to save your changes?";
    public static final String SETTINGS_SHARED_PREFERENCES = "com.android.projects.mateusz.olliecontroller.settings";
    public static final String HOST_ADDRESS_KEY = "host address";
    public static final String USERNAME_AVAILABLE = "Available";
    public static final String USERNAME_NOT_AVAILABLE = "Not Available";
    public static final String USERNAME_AVAILABLE_TOAST = "Username available.";
    public static final String USERNAME_NOT_AVAILABLE_TOAST = "Sorry, username is already taken.";
    public static final String SERVER_CONNECTED = "Connected";
    public static final String SERVER_NO_CONNECTION = "No connection";
    public static final String LOST_CONNECTION = "Lost connection with server.";
    public static final String USERNAME_EMPTY = "Username can not be empty.";
    public static final float JOYSTICK_SIZE_BY_SCREEN_PERCENT = 0.95f;
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 42;

}

