package com.zyb.reader.core.db.manage;


import com.zyb.base.base.app.BaseApplication;
import com.zyb.reader.core.bean.DaoSession;

/**
 */

public class ReaderDBFactory {

    private static ReaderDBFactory mInstance = null;
    private BooksManage booksManage;
    private BookRecordManage bookRecordManage;

    public static ReaderDBFactory getInstance() {
        if (mInstance == null) {
            synchronized (ReaderDBFactory.class) {
                if (mInstance == null) {
                    mInstance = new ReaderDBFactory();
                }
            }
        }
        return mInstance;
    }

    /**
     * Mange
     */
    public BooksManage getBooksManage() {
        if (booksManage == null) {
            booksManage = new BooksManage(getDaoSession().getCollBookBeanDao());
        }
        return booksManage;
    }

    /**
     * Mange
     */
    public BookRecordManage getBookRecordManage() {
        if (bookRecordManage == null) {
            bookRecordManage = new BookRecordManage(getDaoSession().getBookRecordBeanDao());
        }
        return bookRecordManage;
    }

    /**
     * Mange
     */
    public DaoSession getDaoSession() {
        return ReaderDBManage.getInstance(BaseApplication.getInstance()).getDaoSession();
    }
}
