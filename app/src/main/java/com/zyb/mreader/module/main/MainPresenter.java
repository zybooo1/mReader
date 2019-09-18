package com.zyb.mreader.module.main;


import com.zyb.base.http.CommonSubscriber;
import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.base.utils.RxUtil;
import com.zyb.common.db.bean.Book;
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
    public void drawerAction(MainContract.DRAWER_ACTION action) {

        addSubscribe(RxUtil.createDelayFlowable(250, new CommonSubscriber<Long>(mView) {
            @Override
            protected void onStartWithViewAlive() { }
            @Override
            protected void onCompleteWithViewAlive() { }
            @Override
            protected void onNextWithViewAlive(Long aLong) {
                switch (action){
                    case TO_ADD_BOOK:
                        mView.toAddBook();
                        break;
                    case TO_FEED_BACK:
                        mView.toFeedBack();
                        break;
                    case TO_ABOUT:
                        mView.toAbout();
                        break;
                    case TO_SHARE:
                        mView.toShare();
                        break;
                    case TO_LOGIN:
                        mView.toLogin();
                        break;
                }
            }
            @Override
            protected void onErrorWithViewAlive(Throwable e) { }
        }));
    }

    @Override
    public void getBooks() {
        List<Book> allBooks = mDataManager.getAllBooks();
        mView.onBooksLoaded(allBooks);
    }

    @Override
    public void removeBook(List<Book> book) {
        mDataManager.removeBook(book);
    }

    @Override
    public void sortBook(Book book, int newPosition) {
        mDataManager.sortBook(book,newPosition);
    }
}
