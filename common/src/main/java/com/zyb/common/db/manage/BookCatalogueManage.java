package com.zyb.common.db.manage;


import com.zyb.base.base.db.BaseDBManager;
import com.zyb.common.db.bean.Book;
import com.zyb.common.db.bean.BookCatalogue;

import org.greenrobot.greendao.AbstractDao;

/**
 * Describe：DB操作类
 */
public class BookCatalogueManage extends BaseDBManager<BookCatalogue, String> {
    public BookCatalogueManage(AbstractDao dao) {
        super(dao);
    }

}
