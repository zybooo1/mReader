package com.zyb.common.db.manage;


import com.zyb.base.base.db.BaseDBManager;
import com.zyb.common.db.bean.BookRecordBean;

import org.greenrobot.greendao.AbstractDao;

/**
 * Describe：DB操作类
 */

public class BookRecordManage extends BaseDBManager<BookRecordBean, String> {
    public BookRecordManage(AbstractDao dao) {
        super(dao);
    }

}
