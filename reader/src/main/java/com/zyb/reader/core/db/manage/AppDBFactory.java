package com.zyb.reader.core.db.manage;


import com.zyb.base.base.app.BaseApplication;
import com.zyb.reader.db.entity.DaoSession;

/**
 * Describe：商城模块DB工厂  管理所有的DB操作类
 * Created by 吴天强 on 2018/11/5.
 */

public class AppDBFactory {

    private static AppDBFactory mInstance = null;
    private BooksManage booksManage;

    public static AppDBFactory getInstance() {
        if (mInstance == null) {
            synchronized (AppDBFactory.class) {
                if (mInstance == null) {
                    mInstance = new AppDBFactory();
                }
            }
        }
        return mInstance;
    }

    /**
     * Mange
     *
     */
    public BooksManage getBooksManage() {
        if (booksManage == null) {
            booksManage = new BooksManage(AppDBManage.getInstance(BaseApplication.getInstance()).getDaoSession().getCollBookBeanDao());
        }
        return booksManage;
    }

    /**
     * Mange
     *
     */
    public DaoSession getDaoSession() {
        return AppDBManage.getInstance(BaseApplication.getInstance()).getDaoSession();
    }
}
