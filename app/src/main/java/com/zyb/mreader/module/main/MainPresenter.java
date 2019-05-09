package com.zyb.mreader.module.main;


import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.mreader.base.bean.Book;
import com.zyb.mreader.core.AppDataManager;

import java.util.List;

import javax.inject.Inject;

/**
 *
 */

public class MainPresenter extends AbstractPresenter<MainContract.View, AppDataManager> implements MainContract.Presenter {

    @Inject
    MainPresenter(AppDataManager dataManager) {
        super(dataManager);
    }


    @Override
    public void getBooks() {
        List<Book> allBooks = mDataManager.getAllBooks();
        if(allBooks.size()<=0){
            mView.showPageEmpty();
        }else {
            mView.showPageContent();
        }
        mView.onBooksLoaded(allBooks);
    }

    @Override
    public void removeBook(Book book) {
        mDataManager.removeBook(book);
    }
}
