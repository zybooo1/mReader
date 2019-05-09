package com.zyb.mreader.module.addBook.file;


import com.zyb.base.mvp.BasePresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.mreader.base.bean.Book;
import com.zyb.mreader.base.bean.BookFiles;

import java.util.List;

public interface BookFilesContract {
    interface View extends BaseView {
        void onBookFilesLoaded(List<BookFiles> books);
    }

    interface Presenter extends BasePresenter<View> {
        void updateBookFiles(List<BookFiles> books);
        void scanFiles();
        boolean isBookAdded(BookFiles book);
        boolean isBookFilesCached();
        List<BookFiles> getAllBookFiles();
    }
}
