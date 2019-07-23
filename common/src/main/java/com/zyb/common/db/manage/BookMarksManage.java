package com.zyb.common.db.manage;


import com.zyb.base.base.db.BaseDBManager;
import com.zyb.common.db.bean.Book;
import com.zyb.common.db.bean.BookMarks;

import org.greenrobot.greendao.AbstractDao;

/**
 * Describe：DB操作类
 */
public class BookMarksManage extends BaseDBManager<BookMarks, String> {
    public BookMarksManage(AbstractDao dao) {
        super(dao);
    }

}
