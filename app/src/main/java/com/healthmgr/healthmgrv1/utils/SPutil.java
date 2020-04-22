package com.healthmgr.healthmgrv1.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.healthmgr.healthmgrv1.common.MyApplication;

/**
 * Created by Administrator on 2016/11/22 0022.
 */

public class SPutil {
    private static final String SP_NAME = "config";

    //此处MyApplication可以不要？
    public static SharedPreferences getSharedPreferences() {
        return MyApplication.mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public static void putValue(String key, Object value) {
        SharedPreferences.Editor edit = getSharedPreferences().edit();
        if (value instanceof String) {
            edit.putString(key, (String) value);
            Log.d("SpUtil", "value:" + value.toString());
        } else if (value instanceof Boolean) {
            edit.putBoolean(key, (boolean) value);
            Log.d("SpUtil", "value:" + ((boolean) value));
        } else if (value instanceof Integer) {
            edit.putInt(key, (int) value);
            Log.d("SpUtil", "value:" + ((int) value));
        }else if (value==null){
            edit.putString(key, null);
        }
        edit.apply();
    }

    public static String getString(String key) {

        return getSharedPreferences().getString(key, null);
    }

    public static int getInt(String key) {
        return getSharedPreferences().getInt(key, -1);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return getSharedPreferences().getBoolean(key, defaultValue);
    }
}
