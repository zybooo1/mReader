package com.zyb.mreader.module.addBook.path;


import com.zyb.base.mvp.BasePresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.common.db.bean.Book;
import com.zyb.common.db.bean.BookFiles;

public interface BookPathContract {
    interface View extends BaseView {
    }

    interface Presenter extends BasePresenter<View> {
        void addBook(Book book);
        boolean isBookAdded(BookFiles book);
    }
}
