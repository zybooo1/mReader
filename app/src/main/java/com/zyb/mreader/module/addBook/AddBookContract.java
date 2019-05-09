package com.zyb.mreader.module.addBook;


import android.widget.ArrayAdapter;

import com.zyb.base.mvp.BasePresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.mreader.base.bean.Book;

import java.util.List;


public interface AddBookContract {
    interface View extends BaseView {
        void onBooksAdded();
    }

    interface Presenter extends BasePresenter<View> {
        void addBooks(List<Book> books);
    }
}
