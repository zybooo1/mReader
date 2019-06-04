package com.zyb.common.db;


import com.zyb.base.base.app.BaseApplication;
import com.zyb.common.db.bean.DaoSession;
import com.zyb.common.db.manage.BookFilesManage;
import com.zyb.common.db.manage.BookRecordManage;
import com.zyb.common.db.manage.BooksManage;
import com.zyb.common.db.manage.CollBooksManage;

/**
 */

public class DBFactory {

    private static DBFactory mInstance = null;
    private BooksManage booksManage;
    private BookFilesManage bookFilesManage;
    private CollBooksManage collBooksManage;
    private BookRecordManage bookRecordManage;

    public static DBFactory getInstance() {
        if (mInstance == null) {
            synchronized (DBFactory.class) {
                if (mInstance == null) {
                    mInstance = new DBFactory();
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
            booksManage = new BooksManage(getDaoSession().getBookDao());
        }
        return booksManage;
    }
    /**
     * Mange
     *
     */
    public BookFilesManage getBookFilesManage() {
        if (bookFilesManage == null) {
            bookFilesManage = new BookFilesManage(getDaoSession().getBookFilesDao());
        }
        return bookFilesManage;
    }
    /**
     * Mange
     *
     */
    public CollBooksManage getCollBooksManage() {
        if (collBooksManage == null) {
            collBooksManage = new CollBooksManage(getDaoSession().getCollBookBeanDao());
        }
        return collBooksManage;
    }
    /**
     * Mange
     *
     */
    public BookRecordManage getBookRecordManage() {
        if (bookRecordManage == null) {
            bookRecordManage = new BookRecordManage(getDaoSession().getBookRecordBeanDao());
        }
        return bookRecordManage;
    }

    /**
     * Mange
     *
     */
    public DaoSession getDaoSession() {
        return DBManage.getInstance(BaseApplication.getInstance()).getDaoSession();
    }
}
