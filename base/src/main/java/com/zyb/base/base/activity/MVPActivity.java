package com.zyb.base.base.activity;


import android.os.Bundle;


import com.zyb.base.di.component.AppComponent;
import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.base.utils.Utils;

import javax.inject.Inject;

/**
 * MVP模式的Base Activity
 * 注入P层
 */
public abstract class MVPActivity<T extends AbstractPresenter> extends MyActivity {
    protected AppComponent mAppComponent;
    @Inject
    protected T mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAppComponent = Utils.getAppComponent();
        setupActivityComponent(mAppComponent);//依赖注入
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        if (mPresenter != null) {
            mPresenter.detachView();
        }
        this.mAppComponent = null;
        super.onDestroy();
    }

    /**
     * 提供AppComponent(提供所有的单例对象)给子类，进行Component依赖
     */
    protected abstract void setupActivityComponent(AppComponent appComponent);

}
