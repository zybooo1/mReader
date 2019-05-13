package com.zyb.reader.core.db;


import com.zyb.reader.core.bean.CollBookBean;
import com.zyb.reader.core.db.manage.ReaderDBFactory;

import javax.inject.Inject;

/**
 *
 */

public class GreenDaoHelper implements DbHelper {

    private ReaderDBFactory dbFactory;

    @Inject
    GreenDaoHelper() {
        dbFactory = ReaderDBFactory.getInstance();
    }


    @Override
    public void saveBook(CollBookBean book) {
        dbFactory.getBooksManage().insertOrUpdate(book);
    }
}
