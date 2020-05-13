package com.zyb.mreader.module.webdav.bookSelect;


import com.thegrizzlylabs.sardineandroid.DavResource;
import com.zyb.base.mvp.BasePresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.common.db.bean.Book;

import java.util.List;

public interface BookSelectContract {
    interface View extends BaseView {
        void onBooksLoaded(List<DavResource> books);
    }

    interface Presenter extends BasePresenter<View> {
        List<Book> getBooks();
        void getWebDavBooks();
    }
}
