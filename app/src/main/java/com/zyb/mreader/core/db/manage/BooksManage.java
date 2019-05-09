package com.zyb.mreader.core.db.manage;


import com.zyb.base.base.db.BaseDBManager;
import com.zyb.mreader.base.bean.Book;

import org.greenrobot.greendao.AbstractDao;

/**
 * Describe：DB操作类
 */

public class BooksManage extends BaseDBManager<Book, String> {
    public BooksManage(AbstractDao dao) {
        super(dao);
    }

}
