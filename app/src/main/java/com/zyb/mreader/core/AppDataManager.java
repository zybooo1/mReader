package com.zyb.mreader.core;


import com.zyb.base.mvp.BaseDataManager;
import com.zyb.mreader.base.bean.Book;
import com.zyb.mreader.base.bean.BookFiles;
import com.zyb.mreader.core.db.DbHelper;
import com.zyb.mreader.core.db.GreenDaoHelper;
import com.zyb.mreader.core.http.HttpHelper;
import com.zyb.mreader.core.http.RetrofitHelper;
import com.zyb.mreader.core.prefs.PreferenceHelper;
import com.zyb.mreader.core.prefs.PreferenceHelperImpl;

import java.util.List;

import javax.inject.Inject;

/**
 * 数据处理中心
 */

public class AppDataManager implements HttpHelper, PreferenceHelper, DbHelper, BaseDataManager {
    private HttpHelper mHttpHelper;
    private PreferenceHelper mPreferenceHelper;
    private GreenDaoHelper mGreenDaoHelper;

    @Inject
    public AppDataManager(RetrofitHelper httpHelper, PreferenceHelperImpl preferencesHelper, GreenDaoHelper greenDaoHelper) {
        mHttpHelper = httpHelper;
        mPreferenceHelper = preferencesHelper;
        mGreenDaoHelper = greenDaoHelper;
    }

    @Override
    public List<Book> getAllBooks() {
        return mGreenDaoHelper.getAllBooks();
    }

    @Override
    public void addBook(Book book) {
        mGreenDaoHelper.addBook(book);
    }

    @Override
    public void addBooks(List<Book> books) {
        mGreenDaoHelper.addBooks(books);
    }

    @Override
    public boolean isBookAdded(BookFiles book) {
        return mGreenDaoHelper.isBookAdded(book);
    }

    @Override
    public void removeBook(Book book) {
        mGreenDaoHelper.removeBook(book);
    }

    @Override
    public boolean isBookFilesCached() {
        return mGreenDaoHelper.isBookFilesCached();
    }

    @Override
    public List<BookFiles> getAllBookFiles() {
        return mGreenDaoHelper.getAllBookFiles();
    }

    @Override
    public void updateBookFiles(List<BookFiles> books) {
        mGreenDaoHelper.updateBookFiles(books);
    }
}
