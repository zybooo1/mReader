package com.zyb.mreader.module.addBook.path;


import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.base.utils.EventBusUtil;
import com.zyb.mreader.base.bean.Book;
import com.zyb.mreader.base.bean.BookFiles;
import com.zyb.mreader.core.AppDataManager;

import java.util.List;

import javax.inject.Inject;


/**
 *
 */

public class BookPathPresenter extends AbstractPresenter<BookPathContract.View, AppDataManager> implements BookPathContract.Presenter {

    @Inject
    BookPathPresenter(AppDataManager dataManager) {
        super(dataManager);
    }


    @Override
    public void addBook(Book book) {
        mDataManager.addBook(book);
        EventBusUtil.sendStickyEvent(new BaseEvent(EventConstants.EVENT_MAIN_REFRESH_BOOK_SHELF));
    }

    @Override
    public boolean isBookAdded(BookFiles book) {
        return mDataManager.isBookAdded(book);
    }
}
