package com.zyb.reader.utils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理书籍的工具类
 */

public class BookManager {
    private static final String TAG = "BookManager";
    private String chapterName;
    private String bookId;
    private long chapterLen;
    private long position;
    private Map<String, Cache> cacheMap = new HashMap<>();
    private static volatile BookManager sInstance;

    public static BookManager getInstance() {
        if (sInstance == null) {
            synchronized (BookManager.class) {
                if (sInstance == null) {
                    sInstance = new BookManager();
                }
            }
        }
        return sInstance;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public long getPosition() {
        return position;
    }

    public void clear() {
        cacheMap.clear();
        position = 0;
        chapterLen = 0;
    }

    public class Cache {
        private long size;
        private WeakReference<char[]> data;

        public WeakReference<char[]> getData() {
            return data;
        }


        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }
    }
}
