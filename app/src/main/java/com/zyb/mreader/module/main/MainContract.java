package com.zyb.mreader.module.main;


import com.zyb.base.mvp.BasePresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.common.db.bean.Book;

import java.util.List;


public interface MainContract {
    interface View extends BaseView {
        void onBooksLoaded(List<Book> books);

        void toAddBook();

        void toFeedBack();

        void toAbout();

        void toShare();

        void toLogin();
    }

    interface Presenter extends BasePresenter<View> {
        void drawerAction(DRAWER_ACTION action);

        void getBooks();
        void removeBooks(List<Book> book);

        void removeBook(Book book);
        void sortBook(Book book, int newPosition);

        void preloadBooks();
    }

    enum DRAWER_ACTION {
        TO_ADD_BOOK,
        TO_FEED_BACK,
        TO_ABOUT,
        TO_SHARE,
        TO_LOGIN,
    }
}
