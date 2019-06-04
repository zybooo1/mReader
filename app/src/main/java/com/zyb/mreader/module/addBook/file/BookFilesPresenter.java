package com.zyb.mreader.module.addBook.file;


import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.http.CommonSubscriber;
import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.base.utils.EventBusUtil;
import com.zyb.base.utils.RxUtil;
import com.zyb.common.db.bean.Book;
import com.zyb.common.db.bean.BookFiles;
import com.zyb.mreader.core.AppDataManager;
import com.zyb.mreader.utils.FileSizeComparator;
import com.zyb.mreader.utils.FileUtils;

import org.reactivestreams.Publisher;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.functions.Function;


/**
 *
 */

public class BookFilesPresenter extends AbstractPresenter<BookFilesContract.View, AppDataManager> implements BookFilesContract.Presenter {

    @Inject
    BookFilesPresenter(AppDataManager dataManager) {
        super(dataManager);
    }


    @Override
    public void updateBookFiles(List<BookFiles> books) {
        mDataManager.updateBookFiles(books);
    }

    @Override
    public void addBook(Book book) {
        mDataManager.addBook(book);
        EventBusUtil.sendStickyEvent(new BaseEvent(EventConstants.EVENT_MAIN_REFRESH_BOOK_SHELF));
    }

    @Override
    public void scanFiles() {

        addSubscribe(FileUtils.scanTxtFile()
                .flatMap(new Function<List<File>, Publisher<List<BookFiles>>>() {
                    @Override
                    public Publisher<List<BookFiles>> apply(List<File> files) throws Exception {
                        Collections.sort(files, new FileSizeComparator());
                        List<BookFiles> books = new ArrayList<>();
                        for (File file : files) {
                            BookFiles book = new BookFiles();
                            book.setId(file.getAbsolutePath());
                            book.setPath(file.getAbsolutePath());
                            book.setSize(FileUtils.getFileSize(file.length()));
                            book.setTitle(FileUtils.getSimpleName(file));
                            books.add(book);
                        }
                        return RxUtil.createFlowableData(books);
                    }
                })
                .compose(RxUtil.rxSchedulerHelper())
                .subscribeWith(new CommonSubscriber<List<BookFiles>>(mView) {
                    @Override
                    protected void onStartWithViewAlive() {
                        mView.showPageLoading();
                    }

                    @Override
                    protected void onCompleteWithViewAlive() {

                    }

                    @Override
                    protected void onNextWithViewAlive(List<BookFiles> bookFiles) {
                        if (mView == null) return;
                        if (bookFiles.size() <= 0) {
                            mView.showPageEmpty();
                            return;
                        }
                        mView.showPageContent();
                        mView.onBookFilesLoaded(bookFiles);
                        updateBookFiles(bookFiles);
                    }

                    @Override
                    protected void onErrorWithViewAlive(Throwable e) {
                        mView.showPageRetry();
                    }
                }));
    }

    @Override
    public boolean isBookAdded(BookFiles book) {
        return mDataManager.isBookAdded(book);
    }

    @Override
    public boolean isBookFilesCached() {
        return mDataManager.isBookFilesCached();
    }

    @Override
    public List<BookFiles> getAllBookFiles() {
        return mDataManager.getAllBookFiles();
    }
}
