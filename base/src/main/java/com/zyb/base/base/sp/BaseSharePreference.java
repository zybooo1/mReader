package com.zyb.base.base.sp;

import android.content.Context;
import android.content.SharedPreferences;

import com.zyb.base.base.app.BaseApplication;
import com.zyb.base.base.app.BaseApplicationLike;

import javax.inject.Inject;

import static com.zyb.base.utils.constant.Constants.SP_NAME;


/**
 * Base SharePreference
 */
public class BaseSharePreference {

    protected final SharedPreferences mPreferences;
    private static SharedPreferences.Editor sharedWritable;

    @Inject
    public BaseSharePreference() {
        mPreferences = BaseApplicationLike.getInstance().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sharedWritable=mPreferences.edit();
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
        return mPreferences.getString(key, defValue);
    }

    public void putString(String key, String value) {
        sharedWritable.putString(key, value);
        sharedWritable.apply();
    }

    public void putInt(String key, int value) {
        sharedWritable.putInt(key, value);
        sharedWritable.apply();
    }

    public int getInt(String key, int def) {
        return mPreferences.getInt(key, def);
    }

    public void putBoolean(String key, boolean value) {
        sharedWritable.putBoolean(key, value);
        sharedWritable.apply();
    }

    public boolean getBoolean(String key, boolean def) {
        return mPreferences.getBoolean(key, def);
    }
}
