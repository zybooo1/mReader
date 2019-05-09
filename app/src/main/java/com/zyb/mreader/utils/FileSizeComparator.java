package com.zyb.mreader.utils;

import java.io.File;
import java.util.Comparator;

public class FileSizeComparator implements Comparator<File> {
    @Override
    public int compare(File lhs, File rhs) {
        return (int) (rhs.length() - lhs.length());
    }
}