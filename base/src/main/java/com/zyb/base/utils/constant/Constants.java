package com.zyb.base.utils.constant;

import android.os.Environment;

/**
 * 静态常量
 */
public class Constants {
    public static String BUGLY_ID = "b7a7ef8d34";

    public static String DB_NAME = "base_db";

    //图片（压缩）缓存目录
    public static String IMG_CACHE_PATH = Environment.getExternalStorageDirectory() + "imageCache/";
    //文件缓存目录
    public static String FILE_CACHE_PATH = Environment.getExternalStorageDirectory() + "fileCache/";

    //SP名称
    public static final String SP_NAME = "sp_mreader";
    //搜索书籍是否自动过滤纯英文文件
    public static final String IS_FILTER_EN_FILE = "is_filter_en_file";
    //搜索书籍过滤最小文件的大小
    public static final String FILTER_SIZE = "fileter_size";
    //是否已显示用户协议
    public static final String IS_SHOWED_CONTRACT = "showed_contract";
    //是否第一次进入APP
    public static final String IS_FIRST_IN_APP = "is_first_in";
    //WebDav SP名称
    public static final String WEBDAV_USER_NAME = "webdev_user_name";
    public static final String WEBDAV_PASSWORD = "webdev_psw";
    public static final String WEBDAV_HOST = "webdev_host";
    public static final String WEBDAV_BACKUP_PATH = "猫豆阅读";

    //用户隐私html资源  如果是本地资源file:///android_asset/xxx.html
    public static final String PRIVACY_HTML = "https://zybooo1.github.io/zybooo1/mReader/mreader_privacy.html";
    public static final String PROTOCOL_HTML = "https://zybooo1.github.io/zybooo1/mReader/mreader_protocol.html";

    //坚果云
    public static final String JIANGUOYUN_HELP_URL = "http://help.jianguoyun.com/?p=2064";
    public static final String JIANGUOYUN_USERNAME = "739758058@qq.com";
    public static final String JIANGUOYUN_PASSWORD = "arku25izyvfuqvxm";   //WebDav应用授权密码
    public static final String JIANGUOYUN_HOST = "https://dav.jianguoyun.com/dav/";
    public static final String BOX_HOST = "https://dav.box.com/dav/";

    public static final String DEFAULT_FILEPROVIDER = "com.zyb.mreader.fileprovider";
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

    /**
     * 一个通用的跳转参数名称
     */
    public static final String JUMP_PARAM_FLAG_DATA = "dataFlag";  //数据
    public static final String JUMP_PARAM_FLAG_DATA2 = "dataFlag2";  //数据2
    public static final String JUMP_PARAM_FLAG_DATA3 = "dataFlag3";  //数据3
    public static final String JUMP_PARAM_FLAG_STRING = "stringFlag";   //字符串
    public static final String JUMP_PARAM_FLAG_STRING2 = "stringFlag2";   //字符串2
    public static final String JUMP_PARAM_FLAG_STRING3 = "stringFlag3";   //字符串3
    public static final String JUMP_PARAM_FLAG_STRING4= "stringFlag4";   //字符串4

}
