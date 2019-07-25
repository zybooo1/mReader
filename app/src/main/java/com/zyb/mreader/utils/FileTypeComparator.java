package com.zyb.mreader.utils;

import java.io.File;
import java.util.Comparator;

/**
 * 按文件夹or文件排序
 */
public class FileTypeComparator implements Comparator<File> {
    @Override
    public int compare(File o1, File o2) {
        if (o1.isDirectory() && o2.isFile()) {
            return 1;
        }
        if (o2.isDirectory() && o1.isFile()) {
            return -1;
        }
        if (o1.isFile() && o2.isFile()) {
            return (int) (o2.length() - o1.length());
        }
        return o1.getName().compareToIgnoreCase(o2.getName());
    }
}