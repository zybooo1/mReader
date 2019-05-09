package com.zyb.mreader.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;

public class SimpleTxtFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        if (pathname.getName().startsWith(".")) {
            return false;
        }
        //文件夹内部数量为0
        if (pathname.isDirectory() && pathname.list().length == 0) {
            return false;
        }

        /**
         * 现在只支持TXT文件的显示
         */
        //文件内容为空,或者不以txt为开头
        if (!pathname.isDirectory() &&
                (pathname.length() == 0 || !pathname.getName().endsWith(FileUtils.SUFFIX_TXT))) {
            return false;
        }
        return true;
    }
}