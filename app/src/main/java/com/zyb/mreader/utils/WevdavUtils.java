package com.zyb.mreader.utils;

import android.os.Environment;

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.zyb.base.utils.constant.Constants;

import java.io.File;
import java.util.List;

/**
 *
 */
public class WevdavUtils {
    /**
     * 云盘文件是否已经下载了（在本地是否有）
     *
     * @param fileName 云盘文件
     */
    public static boolean isFileDownloaded(String fileName) {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.WEBDAV_BACKUP_PATH);
        if (!file.exists()) return false;
        for (File listFile : file.listFiles()) {
            if (fileName.contains(listFile.getName())) return true;
        }
        return false;
    }

    /**
     * 本地文件是否已经上传了了（在云盘是否有）
     *
     * @param fileName     本地文件
     * @param davResources 云盘文件集合
     */
    public static boolean isFileUploaded(String fileName, List<DavResource> davResources) {
        if (davResources == null) return false;
        for (DavResource davResource : davResources) {
            if (davResource.getDisplayName().contains(fileName)) return true;
        }
        return false;
    }
}
