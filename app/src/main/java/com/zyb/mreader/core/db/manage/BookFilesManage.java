package com.zyb.mreader.core.db.manage;


import com.zyb.base.base.db.BaseDBManager;
import com.zyb.mreader.base.bean.Book;
import com.zyb.mreader.base.bean.BookFiles;

import org.greenrobot.greendao.AbstractDao;

/**
 * Describe：DB操作类
 */

public class BookFilesManage extends BaseDBManager<BookFiles, String> {
    public BookFilesManage(AbstractDao dao) {
        super(dao);
    }

}
