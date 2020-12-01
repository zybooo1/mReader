package com.zyb.base.utils;



import com.zyb.base.base.app.BaseApplication;

import java.io.File;

/**
 * 存储相关常量
 */
public final class MemoryUtils {

    /**
     * 应用目录名称
     */
    private static final String PATH_NAME_NET_CACHE = "NetCache";
    private static final String PATH_NAME_IMAGE_CACHE = "ImageCache";
    private static final String PATH_NAME_FILE_CACHE = "FileCache";

    //=================应用缓存（Cache）目录 开始=================================================================
    public static final String PATH_INNER_CACHE = BaseApplication.getInstance().getCacheDir().getAbsolutePath();

    private static final String PATH_INNER_NET_CACHE = PATH_INNER_CACHE + File.separator + PATH_NAME_NET_CACHE;

    private static final String PATH_INNER_IMAGE_CACHE = PATH_INNER_CACHE + File.separator + PATH_NAME_IMAGE_CACHE;

    private static final String PATH_INNER_FILE_CACHE = PATH_INNER_CACHE + File.separator + PATH_NAME_FILE_CACHE;
    private static final String PATH_INNER_GLIDE_CACHE = PATH_INNER_CACHE + File.separator + "GlideCache";

    /**
     * Glide缓存内部私有目录
     */
    public static String getInnerGlideCachePath() {
        File file = new File(PATH_INNER_GLIDE_CACHE);
        //没有就创建 创建失败直接返回缓存根目录
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            return mkdirs ? PATH_INNER_GLIDE_CACHE : PATH_INNER_CACHE;
        }
        return PATH_INNER_GLIDE_CACHE;
    }

    /**
     * 网络缓存内部私有目录
     */
    public static String getInnerNetCachePath() {
        File file = new File(PATH_INNER_NET_CACHE);
        //没有就创建 创建失败直接返回缓存根目录
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            return mkdirs ? PATH_INNER_NET_CACHE : PATH_INNER_CACHE;
        }
        return PATH_INNER_NET_CACHE;
    }

    /**
     * 图片（压缩）缓存内部私有目录
     */
    public static String getInnerImageCachePath() {
        File file = new File(PATH_INNER_IMAGE_CACHE);
        //没有就创建 创建失败直接返回缓存根目录
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            return mkdirs ? PATH_INNER_IMAGE_CACHE : PATH_INNER_CACHE;
        }
        return PATH_INNER_IMAGE_CACHE;
    }

    /**
     * 文件缓存内部私有目录
     */
    public static String getInnerFileCachePath() {
        File file = new File(PATH_INNER_FILE_CACHE);
        //没有就创建 创建失败直接返回缓存根目录
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            return mkdirs ? PATH_INNER_FILE_CACHE : PATH_INNER_CACHE;
        }
        return PATH_INNER_FILE_CACHE;
    }

    /**
     * 返回外部私有缓存目录，没有返回内部私有缓存目录
     */
    public static String getOutterCachePath() {
        File externalCacheDir = BaseApplication.getInstance().getExternalCacheDir();
        if (externalCacheDir != null && externalCacheDir.exists()) {
            return externalCacheDir.getAbsolutePath();
        } else {
            return PATH_INNER_CACHE;
        }
    }

    /**
     * 网络缓存外部私有目录
     */
    public static String getOutterNetCachePath() {
        //没有就创建 创建失败直接返回缓存根目录
        String pathname = getOutterCachePath() + File.separator + PATH_NAME_NET_CACHE;
        File file = new File(pathname);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            return mkdirs ? pathname : getOutterCachePath();
        }
        return getOutterCachePath();
    }


    /**
     * 图片（压缩）缓存外部私有目录
     */
    public static String getOutterImageCachePath() {
        //没有就创建 创建失败直接返回缓存根目录
        String pathname = getOutterCachePath() + File.separator + PATH_NAME_IMAGE_CACHE;
        File file = new File(pathname);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            return mkdirs ? pathname : getOutterCachePath();
        }
        return getOutterCachePath();
    }

    /**
     * 文件缓存外部私有目录
     */
    public static String getOutterFileCachePath() {
        //没有就创建 创建失败直接返回缓存根目录
        String pathname = getOutterCachePath() + File.separator + PATH_NAME_FILE_CACHE;
        File file = new File(pathname);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            return mkdirs ? pathname : getOutterCachePath();
        }
        return getOutterCachePath();
    }
    //=================应用缓存（Cache）目录 结束=================================================================

    //=================应用文件（File）目录 开始=================================================================
    private static final String PATH_INNER_FILE = BaseApplication.getInstance().getFilesDir().getAbsolutePath();

    /**
     * 内部私有文件目录
     */
    public static String getInnerFilePath() {
        return PATH_INNER_FILE;
    }


    /**
     * 返回外部私有文件目录，没有返回内部私有文件目录
     */
    private static String getOutterFilePath() {
        //参数null返回根目录
        File externalFileDir = BaseApplication.getInstance().getExternalFilesDir(null);
        if (externalFileDir != null && externalFileDir.exists()) {
            return externalFileDir.getAbsolutePath();
        } else {
            return PATH_INNER_FILE;
        }
    }
    //=================应用文件（File）目录 结束=================================================================


}