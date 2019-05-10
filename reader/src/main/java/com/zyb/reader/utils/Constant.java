package com.zyb.reader.utils;

import java.io.File;

/**
 * Created by Liang_Lu on 2017/11/22.
 */

public class Constant {
    //BookCachePath (因为getCachePath引用了Context，所以必须是静态变量，不能够是静态常量)
    public static String BOOK_CACHE_PATH = FileUtils.getCachePath() + File.separator
            + "book_cache" + File.separator;
}
