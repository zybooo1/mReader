package com.zyb.mreader.module.main;


import com.zyb.base.mvp.BasePresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.mreader.base.bean.Book;

import java.util.List;


public interface MainContract {
    interface View extends BaseView {
        void onBooksLoaded(List<Book> books);
    }

    interface Presenter extends BasePresenter<View> {
        void getBooks();
        void removeBook(Book book);
    }
}
