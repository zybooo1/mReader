package com.zyb.common.db;


import com.zyb.base.base.app.BaseApplication;
import com.zyb.common.db.bean.DaoSession;
import com.zyb.common.db.manage.BookCatalogueManage;
import com.zyb.common.db.manage.BookFilesManage;
import com.zyb.common.db.manage.BookMarksManage;
import com.zyb.common.db.manage.BooksManage;

/**
 *
 */

public class DBFactory {

    private static DBFactory mInstance = null;
    private BooksManage booksManage;
    private BookFilesManage bookFilesManage;
    private BookMarksManage bookMarksManage;
    private BookCatalogueManage bookCatalogueManage;

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
     */
    public BooksManage getBooksManage() {
        if (booksManage == null) {
            booksManage = new BooksManage(getDaoSession().getBookDao());
        }
        return booksManage;
    }

    /**
     * Mange
     */
    public BookFilesManage getBookFilesManage() {
        if (bookFilesManage == null) {
            bookFilesManage = new BookFilesManage(getDaoSession().getBookFilesDao());
        }
        return bookFilesManage;
    }

    /**
     * Mange
     */
    public BookMarksManage getBookMarksManage() {
        if (bookMarksManage == null) {
            bookMarksManage = new BookMarksManage(getDaoSession().getBookMarksDao());
        }
        return bookMarksManage;
    }

    /**
     * Mange
     */
    public BookCatalogueManage getBookCatalogueManage() {
        if (bookCatalogueManage == null) {
            bookCatalogueManage = new BookCatalogueManage(getDaoSession().getBookCatalogueDao());
        }
        return bookCatalogueManage;
    }

    /**
     * Mange
     */
    public DaoSession getDaoSession() {
        return DBManage.getInstance(BaseApplication.getInstance()).getDaoSession();
    }
}
