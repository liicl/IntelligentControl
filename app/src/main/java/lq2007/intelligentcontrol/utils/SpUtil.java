package lq2007.intelligentcontrol.utils;

import android.content.Context;

import lq2007.intelligentcontrol.T;

/**
 * Created by lq200 on 2016/9/10.
 */
public class SpUtil {

    public static final String CONFIG = "config";

    public static void putBoolean(Context ctt, String key, boolean value){
        ctt.getSharedPreferences(CONFIG, Context.MODE_PRIVATE).edit().putBoolean(key,value).commit();
    }

    public static boolean getBoolean(Context ctt, String key, boolean defValue){
        return ctt.getSharedPreferences(CONFIG, Context.MODE_PRIVATE).getBoolean(key, defValue);
    }

    public static void putString(Context ctt, String key, String value){
        ctt.getSharedPreferences(CONFIG, Context.MODE_PRIVATE).edit().putString(key,value).commit();
    }

    public static String getString(Context ctt, String key, String defValue){
        return ctt.getSharedPreferences(CONFIG, Context.MODE_PRIVATE).getString(key, defValue);
    }

    public static void putInt(Context ctt, String key, int value){
        ctt.getSharedPreferences(CONFIG, Context.MODE_PRIVATE).edit().putInt(key,value).commit();
    }

    public static int getInt(Context ctt, String key, int defValue){
        return ctt.getSharedPreferences(CONFIG, Context.MODE_PRIVATE).getInt(key, defValue);
    }

    public static void putLong(Context ctt, String key, long value){
        ctt.getSharedPreferences(CONFIG, Context.MODE_PRIVATE).edit().putLong(key,value).commit();
    }

    public static long getLong(Context ctt, String key, long defValue){
        return ctt.getSharedPreferences(CONFIG, Context.MODE_PRIVATE).getLong(key, defValue);
    }

    public void remove(Context ctt, String key){
        ctt.getSharedPreferences(CONFIG, Context.MODE_PRIVATE).edit().remove(key).commit();
    }
}
