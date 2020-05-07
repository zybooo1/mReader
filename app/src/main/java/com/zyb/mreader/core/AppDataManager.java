package com.zyb.mreader.core;


import com.zyb.base.mvp.BaseDataManager;
import com.zyb.common.db.bean.Book;
import com.zyb.common.db.bean.BookFiles;
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
    public void removeBook(List<Book> book) {
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

    @Override
    public void sortBook(Book book, int newPosition) {
        mGreenDaoHelper.sortBook(book, newPosition);
    }

    @Override
    public void setIsFilterENfiles(boolean isFilterENfiles) {
        mPreferenceHelper.setIsFilterENfiles(isFilterENfiles);
    }

    @Override
    public void setFilterSize(long filterSize) {
        mPreferenceHelper.setFilterSize(filterSize);
    }

    @Override
    public boolean getIsFilterENfiles() {
        return mPreferenceHelper.getIsFilterENfiles();
    }

    @Override
    public long getFilterSize() {
        return mPreferenceHelper.getFilterSize();
    }

    @Override
    public boolean isShowedContract() {
        return mPreferenceHelper.isShowedContract();
    }

    @Override
    public void setIsShowedContract(boolean isShowedContract) {
        mPreferenceHelper.setIsShowedContract(isShowedContract);
    }

    @Override
    public boolean isFirst() {
        return mPreferenceHelper.isFirst();
    }

    @Override
    public void setIsFirst(boolean isFirst) {
        mPreferenceHelper.setIsFirst(isFirst);
    }

    @Override
    public String getWebDavUserName() {
        return mPreferenceHelper.getWebDavUserName();
    }

    @Override
    public void setWebDavUserName(String s) {
        mPreferenceHelper.setWebDavUserName(s);
    }

    @Override
    public String getWebDavPassword() {
        return mPreferenceHelper.getWebDavPassword();
    }

    @Override
    public void setWebDavPassword(String s) {
        mPreferenceHelper.setWebDavPassword(s);
    }

    @Override
    public String getWebDavHost() {
        return mPreferenceHelper.getWebDavHost();
    }

    @Override
    public void setWebDavHost(String s) {
        mPreferenceHelper.setWebDavHost(s);
    }
}
