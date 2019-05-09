package com.zyb.reader.core.db;


import com.zyb.reader.core.db.manage.AppDBFactory;

import javax.inject.Inject;

/**
 *
 */

public class GreenDaoHelper implements DbHelper {

    private AppDBFactory dbFactory;

    @Inject
    GreenDaoHelper() {
        dbFactory = AppDBFactory.getInstance();
    }


}
