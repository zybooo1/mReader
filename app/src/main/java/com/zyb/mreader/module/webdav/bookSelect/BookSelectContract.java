package com.zyb.mreader.module.webdav.bookSelect;


import com.zyb.base.mvp.BasePresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.common.db.bean.Book;

import java.util.List;

public interface BookSelectContract {
    interface View extends BaseView {

    }

    interface Presenter extends BasePresenter<View> {
        List<Book> getBooks();
    }
}
