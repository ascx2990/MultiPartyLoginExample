package com.example.multipartyloginexample;

import android.util.Log;

import com.google.gson.JsonObject;


public class LogUnit {
    public static void v(String tag, String msg) {
        if (BuildConfig.DEBUG) Log.v(tag, msg);
    }

    /**
     * log.d is for funtion
     */
    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) Log.d(tag, "=*=*=*=*=*=" + msg + "=*=*=*=*=*=");
    }

    public static void v(String tag, JsonObject msg) {
        if (BuildConfig.DEBUG) Log.v(tag, msg.toString());
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG) Log.e(tag, msg);
    }

    public static void e(String tag, JsonObject msg) {
        if (BuildConfig.DEBUG) Log.e(tag, msg.toString());
    }
}
