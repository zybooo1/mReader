package com.zyb.mreader.module.webdav.bookSelect;


import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;
import com.zyb.base.http.CommonSubscriber;
import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.base.utils.RxUtil;
import com.zyb.base.utils.constant.Constants;
import com.zyb.common.db.bean.Book;
import com.zyb.mreader.core.AppDataManager;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.functions.Function;

/**
 *
 */

public class BookSelectPresenter extends AbstractPresenter<BookSelectContract.View, AppDataManager> implements BookSelectContract.Presenter {


    @Inject
    BookSelectPresenter(AppDataManager dataManager) {
        super(dataManager);
    }

    @Override
    public List<Book> getBooks() {
        return mDataManager.getAllBooks();
    }
    @Override
    public void getWebDavBooks() {
        addSubscribe(RxUtil.createFlowableData(getSardine())
                .map(new Function<Sardine, List<DavResource>>() {
                    @Override
                    public List<DavResource> apply(Sardine sardine) throws Exception {
                        String serverHostUrl = mDataManager.getWebDavHost() + Constants.WEBDAV_BACKUP_PATH + "/";
                        List<DavResource> list = sardine.list(serverHostUrl);
                        Iterator<DavResource> it = list.iterator();
                        while (it.hasNext()) {
                            DavResource davResource = it.next();
                            if (davResource.isDirectory()) {
                                it.remove();
                            }
                        }
                        return list;
                    }
                })
                .compose(RxUtil.rxSchedulerHelper())
                .subscribeWith(new CommonSubscriber<List<DavResource>>(mView) {
                    @Override
                    protected void onStartWithViewAlive() {
                        mView.showDialogLoading();
                    }

                    @Override
                    protected void onCompleteWithViewAlive() {
                        mView.hideDialogLoading();
                    }

                    @Override
                    protected void onNextWithViewAlive(List<DavResource> davResources) {
                        mView.onBooksLoaded(davResources);
                    }

                    @Override
                    protected void onErrorWithViewAlive(Throwable e) {
                        mView.showToast("抱歉，获取书籍失败了");
                        mView.hideDialogLoading();
                    }
                }));
    }

    @Override
    public void detachAllBooks() {
        mDataManager.detachAll();
    }

    Sardine sardine;

    private Sardine getSardine() {
        if (sardine == null) {
            sardine = new OkHttpSardine();
            String userName = mDataManager.getWebDavUserName();
            String password = mDataManager.getWebDavPassword();
            sardine.setCredentials(userName, password);
        }

        return sardine;
    }
}
