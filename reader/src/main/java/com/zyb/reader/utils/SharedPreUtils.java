package com.zyb.reader.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.zyb.base.base.app.BaseApplication;

import static com.zyb.base.utils.constant.Constants.BASE_SHARED_PREFERENCE;


public class SharedPreUtils {
    private static SharedPreUtils sInstance;
    private static SharedPreferences sharedReadable;
    private static SharedPreferences.Editor sharedWritable;

    private SharedPreUtils() {
        sharedReadable = BaseApplication.getInstance()
                .getSharedPreferences(BASE_SHARED_PREFERENCE, Context.MODE_MULTI_PROCESS);
        sharedWritable = sharedReadable.edit();
    }

    public static SharedPreUtils getInstance() {
        if (sInstance == null) {
            synchronized (SharedPreUtils.class) {
                if (sInstance == null) {
                    sInstance = new SharedPreUtils();
                }
            }
        }
        return sInstance;
    }

    /**
     * 清除本地数据
     */
    public void sharedPreClear() {
        sharedWritable.clear().apply();
    }

    /**
     * 清除本地数据指定key
     */
    public void sharedPreRemove(String key) {
        sharedWritable.remove(key).apply();
    }

    public String getString(String key, String defValue) {
        return sharedReadable.getString(key, defValue);
    }

    public void putString(String key, String value) {
        sharedWritable.putString(key, value);
        sharedWritable.apply();
    }

    public void putInt(String key, int value) {
        sharedWritable.putInt(key, value);
        sharedWritable.apply();
    }

    public void putBoolean(String key, boolean value) {
        sharedWritable.putBoolean(key, value);
        sharedWritable.apply();
    }

    public int getInt(String key, int def) {
        return sharedReadable.getInt(key, def);
    }

    public boolean getBoolean(String key, boolean def) {
        return sharedReadable.getBoolean(key, def);
    }

}
