package com.zyb.base.mvp;

/**
 * Presenter 基类
 */

public interface BasePresenter<T extends BaseView> {

    /**
     * 注入View
     *
     * @param view view
     */
    void attachView(T view);

    /**
     * 回收View
     */
    void detachView();

}
