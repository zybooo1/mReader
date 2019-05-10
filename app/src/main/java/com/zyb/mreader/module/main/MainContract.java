package com.zyb.mreader.module.main;


import com.zyb.base.mvp.BasePresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.mreader.base.bean.Book;

import java.util.List;


public interface MainContract {
    interface View extends BaseView {
        void onBooksLoaded(List<Book> books);
        void toAddBook();
    }

    interface Presenter extends BasePresenter<View> {
        void drawerAction(DRAWER_ACTION action);
        void getBooks();
        void removeBook(Book book);
    }
     enum DRAWER_ACTION{
        TO_ADD_BOOK,
    }
}
