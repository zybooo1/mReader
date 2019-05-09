package com.zyb.reader.read;


import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.base.utils.LogUtil;
import com.zyb.reader.base.bean.ChapterContentBean;
import com.zyb.reader.core.AppDataManager;
import com.zyb.reader.utils.BookManager;
import com.zyb.reader.utils.BookSaveUtils;
import com.zyb.reader.widget.page.TxtChapter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 *
 */

public class ReadPresenter extends AbstractPresenter<ReadContract.View, AppDataManager> implements ReadContract.Presenter {

    @Inject
    ReadPresenter(AppDataManager dataManager) {
        super(dataManager);
    }


    @Override
    public void loadContent(String bookId, List<TxtChapter> bookChapterList) {
        int size = bookChapterList.size();

        List<Observable<ChapterContentBean>> chapterContentBeans = new ArrayList<>(bookChapterList.size());
        ArrayDeque<String> titles = new ArrayDeque<>(bookChapterList.size());
        //首先判断是否Chapter已经存在
        for (int i = 0; i < size; i++) {
            TxtChapter bookChapter = bookChapterList.get(i);
            if (!(BookManager.isChapterCached(bookId, bookChapter.getTitle()))) {
                titles.add(bookChapter.getTitle());
            }
            //如果已经存在，再判断是不是我们需要的下一个章节，如果是才返回加载成功
            else if (i == 0) {
                if (mView != null) {
                    mView.finishChapters();
                }
            }
        }
        final String[] title = {titles.poll()};
        addSubscribe(Observable.concat(chapterContentBeans)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<ChapterContentBean>() {
                            @Override
                            public void accept(ChapterContentBean bean) throws Exception {
                                BookSaveUtils.getInstance().saveChapterInfo(bookId, title[0], bean.getChapter().getCpContent());
                                mView.finishChapters();
                                title[0] = titles.poll();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                if (bookChapterList.get(0).getTitle().equals(title[0])) {
                                    mView.errorChapters();
                                }
                                LogUtil.e(throwable.getMessage());
                            }
                        }, new Action() {
                            @Override
                            public void run() throws Exception {

                            }
                        }, new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {
                            }
                        }));
        ;
    }
}
