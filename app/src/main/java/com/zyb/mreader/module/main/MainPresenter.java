package com.zyb.mreader.module.main;


import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.http.CommonSubscriber;
import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.base.utils.EventBusUtil;
import com.zyb.base.utils.RxUtil;
import com.zyb.base.utils.constant.Constants;
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

public class MainPresenter extends AbstractPresenter<MainContract.View, AppDataManager> implements MainContract.Presenter {

    @Inject
    MainPresenter(AppDataManager dataManager) {
        super(dataManager);
    }


    @Override
    public void drawerAction(MainContract.DRAWER_ACTION action) {

        addSubscribe(RxUtil.createDelayFlowable(250, new CommonSubscriber<Long>(mView) {
            @Override
            protected void onStartWithViewAlive() { }
            @Override
            protected void onCompleteWithViewAlive() { }
            @Override
            protected void onNextWithViewAlive(Long aLong) {
                switch (action){
                    case TO_ADD_BOOK:
                        mView.toAddBook();
                        break;
                    case TO_FEED_BACK:
                        mView.toFeedBack();
                        break;
                    case TO_ABOUT:
                        mView.toAbout();
                        break;
                    case TO_SHARE:
                        mView.toShare();
                        break;
                    case TO_LOGIN:
                        mView.toLogin();
                        break;
                    case TO_BACKUP:
                        mView.toBackup();
                        break;
                }
            }
            @Override
            protected void onErrorWithViewAlive(Throwable e) { }
        }));
    }

    @Override
    public void getBooks() {
        List<Book> allBooks = mDataManager.getAllBooks();
        mView.onBooksLoaded(allBooks);
    }

    @Override
    public void removeBooks(List<Book> book) {
        mDataManager.removeBook(book);
    }

    @Override
    public void removeBook(Book book) {
        List<Book> bookList =new ArrayList<>();
        bookList.add(book);
        mDataManager.removeBook(bookList);
    }

    @Override
    public void sortBook(Book book, int newPosition) {
        mDataManager.sortBook(book,newPosition);
    }

    @Override
    public void preloadBooks() {
        if (mDataManager.getAllBookFiles().size()>0) {
            return;
        }
        addSubscribe(FileUtils.scanTxtFile( FileUtils.MIN_TXT_FILE_SIZE, FileUtils.IS_FILTER_EN_FILES)
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
                    }

                    @Override
                    protected void onCompleteWithViewAlive() {

                    }

                    @Override
                    protected void onNextWithViewAlive(List<BookFiles> bookFiles) {
                        if (bookFiles.size() <= 0) {
                            return;
                        }
                        updateBookFiles(bookFiles);
                    }

                    @Override
                    protected void onErrorWithViewAlive(Throwable e) {
                    }
                }));
    }
    private void updateBookFiles(List<BookFiles> books) {
        mDataManager.updateBookFiles(books);
    }


}
