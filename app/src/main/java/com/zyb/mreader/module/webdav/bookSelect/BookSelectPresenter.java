package com.zyb.mreader.module.webdav.bookSelect;


import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.common.db.bean.Book;
import com.zyb.mreader.core.AppDataManager;

import java.util.List;

import javax.inject.Inject;

/**
 *
 */

public class BookSelectPresenter extends AbstractPresenter<BookSelectContract.View, AppDataManager> implements BookSelectContract.Presenter {


    @Inject
    BookSelectPresenter(AppDataManager dataManager) {
        super(dataManager);
    }

    @Override
    public List<Book> getBooks() {
        return mDataManager.getAllBooks();
    }
}
