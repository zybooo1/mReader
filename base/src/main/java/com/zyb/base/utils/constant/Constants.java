package com.zyb.base.utils.constant;

import android.os.Environment;

/**
 * 静态常量
 */
public class Constants {
    public static String BUGLY_ID = "b7a7ef8d34";
    public static String DB_NAME = "base_db";

    //刷新完成后延迟多久隐藏刷新状态
    public static int REFRESH_COMPLETE_DELAY = 1500;

    //图片（压缩）缓存目录
    public static String IMG_CACHE_PATH = Environment.getExternalStorageDirectory() + "imageCache/";
    //文件缓存目录
    public static String FILE_CACHE_PATH = Environment.getExternalStorageDirectory() + "fileCache/";


    //SP名称
    public static final String SP_NAME = "sp_mreader";
    public static final int TEXT_SIZE_SP_DEFAULT = 20;
    public static final int READ_BG_DEFAULT = 0;
    public static final int READ_BG_1 = 1;
    public static final int READ_BG_2 = 2;
    public static final int READ_BG_3 = 3;
    public static final int READ_BG_4 = 4;
    public static final int NIGHT_MODE = 5;

    public static final String SHARED_READ_BG = "shared_read_bg";
    public static final String SHARED_READ_BRIGHTNESS = "shared_read_brightness";
    public static final String SHARED_READ_IS_BRIGHTNESS_AUTO = "shared_read_is_brightness_auto";
    public static final String SHARED_READ_TEXT_SIZE = "shared_read_text_size";
    public static final String SHARED_READ_IS_TEXT_DEFAULT = "shared_read_text_default";
    public static final String SHARED_READ_PAGE_MODE = "shared_read_mode";
    public static final String SHARED_READ_NIGHT_MODE = "shared_night_mode";
}
