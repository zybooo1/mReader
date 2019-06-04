package com.zyb.common.db.manage;


import com.zyb.base.base.db.BaseDBManager;
import com.zyb.common.db.bean.CollBookBean;

import org.greenrobot.greendao.AbstractDao;

/**
 * Describe：DB操作类
 */

public class CollBooksManage extends BaseDBManager<CollBookBean, String> {
    public CollBooksManage(AbstractDao dao) {
        super(dao);
    }

}
