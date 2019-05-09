package com.zyb.reader.core.db.manage;


import com.zyb.base.base.db.BaseDBManager;
import com.zyb.reader.db.entity.CollBookBean;

import org.greenrobot.greendao.AbstractDao;

/**
 * Describe：DB操作类
 */

public class BooksManage extends BaseDBManager<CollBookBean, String> {
    public BooksManage(AbstractDao dao) {
        super(dao);
    }

}
