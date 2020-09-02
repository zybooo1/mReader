package com.zyb.base.mvp;


import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;

import java.util.List;

/**
 * View 基类
 */
public interface BaseView {

    String getStringById(int id);


    /**
     * 显示消息提示
     */
    void toast(CharSequence s);

    void toast(int id);


    /**
     * 页面加载中
     */
    void showPageLoading();

    /**
     * 空白页面
     */
    void showPageEmpty(String message);

    void showPageEmpty();

    /**
     * 页面加载失败
     */
    void showPageRetry();

    /**
     * 展示页面内容
     */
    void showPageContent();


    void showDialog(boolean canCancel, String title, String confirmText,
                    String cancelText, OnDialogButtonClickListener cancelListener,
                    OnDialogButtonClickListener confirmListener);

    void showDialog(boolean canCancel, String title, String msg, String confirmText,
                    String cancelText, OnDialogButtonClickListener cancelListener,
                    OnDialogButtonClickListener confirmListener);


    void showDialogLoading(boolean canCancel);

    void showDialogLoading(String msg);

    void showDialogLoading();

    void hideDialogLoading();


}
