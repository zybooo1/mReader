package com.zyb.reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;

import com.zyb.base.utils.LogUtil;

/**
 * 阅读设置
 */
public class Config {
    private final static String SP_NAME = "config";
    private final static String BOOK_BG_KEY = "bookbg";
    private final static String FONT_SIZE_KEY = "fontsize";
    private final static String NIGHT_KEY = "night";
    private final static String LIGHT_KEY = "light";
    private final static String SYSTEM_LIGHT_KEY = "systemlight";
    private final static String PAGE_MODE_KEY = "pagemode";


    public final static int BOOK_BG_DEFAULT = 0;
    public final static int BOOK_BG_1 = 1;
    public final static int BOOK_BG_2 = 2;
    public final static int BOOK_BG_3 = 3;
    public final static int BOOK_BG_4 = 4;

    public final static int PAGE_MODE_SIMULATION = 0;
    public final static int PAGE_MODE_COVER = 1;
    public final static int PAGE_MODE_SLIDE = 2;
    public final static int PAGE_MODE_NONE = 3;
    private static final String SPEAK_SPEED_KEY = "speak_speed";
    private static final String PITCH_KEY = "pitch";
    private static final String TIMING_TIME_KEY = "timing_time";
    private static final String IS_AUTO_TIMING_KEY = "is_auto_timing";
    private static final int DEFAULT_SPEED = 25;
    private static final int DEFAULT_PITCH = 10;

    private Context mContext;
    private static Config config;
    private SharedPreferences sp;
    //字体
    private Typeface typeface;
    //字体大小
    private float mFontSize = 0;
    //亮度值
    private float light = 0;
    private int bookBG;

    private Config(Context mContext) {
        this.mContext = mContext.getApplicationContext();
        sp = this.mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized Config getInstance() {
        return config;
    }

    public static synchronized Config createConfig(Context context) {
        if (config == null) {
            config = new Config(context);
        }

        return config;
    }

    public int getPageMode() {
        return sp.getInt(PAGE_MODE_KEY, PAGE_MODE_SIMULATION);
    }

    public void setPageMode(int pageMode) {
        sp.edit().putInt(PAGE_MODE_KEY, pageMode).apply();
    }

    public int getBookBgType() {
        return sp.getInt(BOOK_BG_KEY, BOOK_BG_DEFAULT);
    }

    public void setBookBg(int type) {
        sp.edit().putInt(BOOK_BG_KEY, type).apply();
    }

    public float getFontSize() {
        if (mFontSize == 0) {
            mFontSize = sp.getFloat(FONT_SIZE_KEY, mContext.getResources().getDimension(R.dimen.reader_reading_default_text_size));
        }
        return mFontSize;
    }

    public void setFontSize(float fontSize) {
        mFontSize = fontSize;
        sp.edit().putFloat(FONT_SIZE_KEY, fontSize).apply();
    }

    /**
     * 获取夜间还是白天阅读模式,true为夜晚，false为白天
     */
    public boolean getDayOrNight() {
        return sp.getBoolean(NIGHT_KEY, false);
    }

    public void setDayOrNight(boolean isNight) {
        sp.edit().putBoolean(NIGHT_KEY, isNight).apply();
    }

    public Boolean isSystemLight() {
        return sp.getBoolean(SYSTEM_LIGHT_KEY, true);
    }

    public void setSystemLight(Boolean isSystemLight) {
        sp.edit().putBoolean(SYSTEM_LIGHT_KEY, isSystemLight).apply();
    }

    public float getLight() {
        if (light == 0) {
            light = sp.getFloat(LIGHT_KEY, 0.1f);
        }
        return light;
    }

    /**
     * 记录配置文件中亮度值
     */
    public void setLight(float light) {
        this.light = light;
        sp.edit().putFloat(LIGHT_KEY, light).apply();
    }

    //=======================朗读配置记录 Begin=================================
    /**
     * 语速
     */
    public int getSpeakSpeed() {
        return sp.getInt(SPEAK_SPEED_KEY, DEFAULT_SPEED);
    }
    public float getSpeedForTTS() {
        float speed = (sp.getInt(SPEAK_SPEED_KEY, DEFAULT_SPEED)+3) / 10f;
        LogUtil.e("getSpeedForTTS---"+speed);
        return speed;
    }
    public void setSpeakSpeed(int speed) {
        sp.edit().putInt(SPEAK_SPEED_KEY, speed).apply();
    }

    /**
     * 发音人(音调)
     */
    public int getPitch() {
        return  sp.getInt(PITCH_KEY, DEFAULT_PITCH);
    }
    public float getPitchForTTS() {
        float pitch = sp.getInt(PITCH_KEY, DEFAULT_PITCH) / 10f;
        if(pitch==0) return 0.1f;
        return pitch;
    }

    public void setPitch(int pitch) {
        sp.edit().putInt(PITCH_KEY, pitch).apply();
    }

    /**
     * 上次定时时间
     */
    public int getTimingTime() {
        return sp.getInt(TIMING_TIME_KEY, 0);
    }

    public void setTimingTime(int time) {
        sp.edit().putInt(TIMING_TIME_KEY, time).apply();
    }

    /**
     * 是否自动定时
     */
    public boolean getIsAutoTiming() {
        return sp.getBoolean(IS_AUTO_TIMING_KEY, false);
    }

    public void setIsAutoTiming(boolean isAutoTiming) {
        sp.edit().putBoolean(IS_AUTO_TIMING_KEY, isAutoTiming).apply();
    }
    //=======================朗读配置记录 End=================================

}
