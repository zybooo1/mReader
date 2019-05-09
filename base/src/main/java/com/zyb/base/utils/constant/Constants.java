package com.zyb.base.utils.constant;

import android.os.Environment;

/**
 * 静态常量
 */
public class Constants {
    public static String BUGLY_ID = "000";
    public static String DB_NAME = "base_db";

    //刷新完成后延迟多久隐藏刷新状态
    public static int REFRESH_COMPLETE_DELAY = 1500;

    //图片（压缩）缓存目录
    public static String IMG_CACHE_PATH = Environment.getExternalStorageDirectory() + "imageCache/";
    //文件缓存目录
    public static String FILE_CACHE_PATH = Environment.getExternalStorageDirectory() + "fileCache/";
    //SP名称
    public static final String BASE_SHARED_PREFERENCE = "base_shared_preference";
    //SP用户信息
    public static final String SP_USER_FLAG = "sp_user_data";
}
