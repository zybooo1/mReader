package com.zyb.mreader.module.webdav;


import com.thegrizzlylabs.sardineandroid.DavResource;
import com.zyb.base.mvp.BasePresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.common.db.bean.Book;

import java.util.List;


public interface WebdavContract {
    interface View extends BaseView {
        void onBooksLoaded(List<DavResource> books);

        void onBooksLoadComplete();

        void onBookDownloaded(int position);

        void onBookDeleted();

    }

    interface Presenter extends BasePresenter<View> {

        /**
         * 是否已登录Webdav
         */
        boolean haveWebdavAccount();

        void getWebDavBooks();

        void upload(List<Book> books);

        void download(DavResource davResource, int position);

        void delete(List<DavResource> davResource);
    }
}
