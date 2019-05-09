package com.zyb.base.base.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;


import com.zyb.base.di.component.AppComponent;
import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.base.utils.Utils;

import javax.inject.Inject;

/**
 * Mvp模式的Fragmnent基础类
 *
 * @author zyb
 * @param <T>
 */
public abstract class MVPFragment<T extends AbstractPresenter> extends MyLazyFragment implements BaseView {
    protected AppComponent mAppComponent;
    @Inject
    protected T mPresenter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mAppComponent = Utils.getAppComponent();
        //依赖注入
        setupFragmentComponent(mAppComponent);
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        if (mPresenter != null) {
            mPresenter.detachView();
        }
        super.onDestroyView();
    }

    /**
     * 提供AppComponent(提供所有的单例对象)给子类，进行Component依赖
     * @param appComponent {@link AppComponent}
     */
    protected abstract void setupFragmentComponent(AppComponent appComponent);

}
