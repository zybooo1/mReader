package com.zyb.reader.core.db.manage;


import com.zyb.base.base.db.BaseDBManager;
import com.zyb.reader.core.bean.BookRecordBean;

import org.greenrobot.greendao.AbstractDao;

/**
 * Describe：DB操作类
 */

public class BookRecordManage extends BaseDBManager<BookRecordBean, String> {
    public BookRecordManage(AbstractDao dao) {
        super(dao);
    }

}
