package com.zyb.base.mvp;


import com.zyb.base.widget.dialog.MessageDialog;

import java.util.List;

/**
 * View 基类
 */
public interface BaseView {

    String getStringById(int id);


    /**
     * 显示消息提示
     */
    void showMsg(CharSequence s);

    void showMsg(int id);

    void showError(CharSequence s);

    void showError(int id);

    void showSuccess(CharSequence s);

    void showSuccess(int id);

    /**
     * 页面加载中
     */
    void showPageLoading();

    /**
     * 空白页面
     */
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
                    String cancelText, MessageDialog.OnListener listener);

    void hideDialog();

    void showDialogLoading(String msg);

    void showDialogLoading();

    void hideDialogLoading();


    void getCompressedImgs(List<String> imgs);

    /**
     * 文件下载
     *
     * @see AbstractPresenter#initSingleDownload(String)
     */
    void onFileDownloaded(String absoluteFilePath);

    void onFileDownloadError();

}
