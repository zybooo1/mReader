package com.zyb.reader.core.db;


import com.zyb.common.db.DBFactory;
import com.zyb.common.db.bean.CollBookBean;

import javax.inject.Inject;

/**
 *
 */

public class GreenDaoHelper implements DbHelper {

    private DBFactory dbFactory;

    @Inject
    GreenDaoHelper() {
        dbFactory = DBFactory.getInstance();
    }


    @Override
    public void saveBook(CollBookBean book) {
        dbFactory.getCollBooksManage().insertOrUpdate(book);
    }
}
