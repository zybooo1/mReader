package com.zyb.mreader.module.addBook.file;


import com.zyb.base.mvp.BasePresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.common.db.bean.Book;
import com.zyb.common.db.bean.BookFiles;

import java.util.List;

public interface BookFilesContract {
    interface View extends BaseView {
        void onBookFilesLoaded(List<BookFiles> books);
    }

    interface Presenter extends BasePresenter<View> {
        void updateBookFiles(List<BookFiles> books);
        void addBook(Book book);
        void scanFiles(long filterSize,boolean isFilterENfile);
        boolean isBookAdded(BookFiles book);
        boolean isBookFilesCached();
        List<BookFiles> getAllBookFiles();


        boolean getIsFilterENfiles( );
        long getFilterSize( );
    }
}
