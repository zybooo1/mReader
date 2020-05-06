package com.zyb.base.mvp;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


/**
 * Base Presenter
 * 管理事件流订阅的生命周期
 */

public class AbstractPresenter<T extends BaseView, D extends BaseDataManager> implements BasePresenter<T> {

    protected T mView;
    private CompositeDisposable compositeDisposable;
    protected D mDataManager;

    @Inject
    protected AbstractPresenter(D dataManager) {
        mDataManager = dataManager;
    }

    protected void addSubscribe(Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    @Override
    public void attachView(T view) {
        this.mView = view;
    }

    @Override
    public void detachView() {
        this.mView = null;
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
    }


}
