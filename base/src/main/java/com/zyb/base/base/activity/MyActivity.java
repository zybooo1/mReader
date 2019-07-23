package com.zyb.base.base.activity;

import android.content.pm.ActivityInfo;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;
import com.hjq.toast.ToastUtils;
import com.zyb.base.R;
import com.zyb.base.base.BaseDialog;
import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.base.utils.LogUtil;
import com.zyb.base.widget.dialog.MessageDialog;
import com.zyb.base.widget.dialog.WaitDialog;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 项目中的 Activity 基类,若不需要MVP结构，直接继承此类
 */
public abstract class MyActivity extends UIActivity
        implements OnTitleBarListener, BaseView {
    private Unbinder mButterKnife;//View注解

    @Override
    protected void initLayout() {
        super.initLayout();
        // 初始化标题栏的监听
        if (getTitleBarId() > 0) {
            if (findViewById(getTitleBarId()) instanceof TitleBar) {
                ((TitleBar) findViewById(getTitleBarId())).setOnTitleBarListener(this);
            }
        }
        mButterKnife = ButterKnife.bind(this);
        initOrientation();
        initPageStatusManager();
    }

    /**
     * 初始化横竖屏方向，会和 LauncherTheme 主题样式有冲突，注意不要同时使用
     */
    protected void initOrientation() {
        // 当前 Activity 不能是透明的并且没有指定屏幕方向，默认设置为竖屏
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * 设置标题栏的标题
     */
    @Override
    public void setTitle(int titleId) {
        setTitle(getText(titleId));
    }

    @Override
    protected int getTitleBarId() {
        return 0;
    }

    /**
     * 设置标题栏的标题
     */
    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        TitleBar titleBar = getTitleBar();
        if (titleBar != null) {
            titleBar.setTitle(title);
        }
    }

    @Nullable
    public TitleBar getTitleBar() {
        if (getTitleBarId() > 0 && findViewById(getTitleBarId()) instanceof TitleBar) {
            return findViewById(getTitleBarId());
        }
        return null;
    }

    @Override
    public boolean statusBarDarkFont() {
        //返回true表示黑色字体
        return false;
    }

    /**
     * {@link OnTitleBarListener}
     */
    // 标题栏左边的View被点击了
    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    // 标题栏中间的View被点击了
    @Override
    public void onTitleClick(View v) {
    }

    // 标题栏右边的View被点击了
    @Override
    public void onRightClick(View v) {
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mButterKnife != null) mButterKnife.unbind();
        hideDialogLoading();
        hideDialog();
    }

    @Override
    public String getStringById(int id) {
        return getString(id);
    }


    @Override
    public void showMsg(CharSequence s) {
        ToastUtils.show(s);
    }

    @Override
    public void showMsg(int id) {
        ToastUtils.show(id);
    }

    @Override
    public void showError(CharSequence s) {
        ToastUtils.show(s);
    }

    @Override
    public void showError(int id) {
        ToastUtils.show(id);
    }

    @Override
    public void showSuccess(CharSequence s) {
        ToastUtils.show(s);
    }

    @Override
    public void showSuccess(int id) {
        ToastUtils.show(id);
    }


    /*----------Loading弹窗 Begin------------*/
    BaseDialog loadingDialog;

    @Override
    public void showDialogLoading(String msg) {
        if (loadingDialog == null) {
            loadingDialog = new WaitDialog.Builder(this)
                    .setMessage(msg)// 消息文本可以不用填写
                    .create();
        }
        loadingDialog.show();
    }

    @Override
    public void showDialogLoading() {
        if (loadingDialog == null) {
            loadingDialog = new WaitDialog.Builder(this)
                    .setMessage(R.string.msg_loading)
                    .create();
        }
        loadingDialog.show();
    }

    @Override
    public void hideDialogLoading() {
        if (loadingDialog != null) loadingDialog.dismiss();
    }
    /*----------Loading弹窗 End------------*/


    /*----------提示弹窗 Begin------------*/
    BaseDialog messageDialog;

    @Override
    public void showDialog(boolean canCancel, String title, String confirmText,
                           String cancelText, MessageDialog.OnListener listener) {
        messageDialog = new MessageDialog.Builder(this)
                .setMessage(title)
                .setConfirm(confirmText)
                .setCancel(cancelText) // 设置 null 表示不显示取消按钮
                .setListener(listener)
                .show();
    }

    @Override
    public void hideDialog() {
        if (messageDialog != null) messageDialog.dismiss();
    }
    /*----------提示弹窗 End------------*/


    /*----------页面状态管理Begin----------*/
    FrameLayout layoutStatusRoot;
    RelativeLayout layoutEmpty;
    RelativeLayout layoutLoading;
    RelativeLayout layoutRetry;

    /**
     * 是否使用页面状态管理（空视图、重试视图、加载视图），默认不使用
     * 若使用，请在根布局引入{@link R.layout#layout_page_status}
     */
    protected boolean usePageStatusManager() {
        return false;
    }

    private void initPageStatusManager() {
        layoutStatusRoot = findViewById(R.id.layout_status_root);
        layoutEmpty = findViewById(R.id.layout_empty);
        layoutLoading = findViewById(R.id.layout_loading);
        layoutRetry = findViewById(R.id.layout_retry);

        if (layoutRetry != null && layoutRetry.findViewById(R.id.btnRetry) != null) {
            layoutRetry.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPageRetry(v);
                }
            });
        }
        if (usePageStatusManager()) showPageLoading();
    }

    protected void hideAllStatusPage(boolean isHideRoot) {
        if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
        if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);
        if (layoutRetry != null) layoutRetry.setVisibility(View.GONE);
        if (layoutStatusRoot != null) {
            layoutStatusRoot.setVisibility(isHideRoot ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void showPageLoading() {
        hideAllStatusPage(false);
        if (layoutLoading != null) layoutLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void showPageEmpty() {
        hideAllStatusPage(false);
        if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public void showPageRetry() {
        hideAllStatusPage(false);
        if (layoutRetry != null) layoutRetry.setVisibility(View.VISIBLE);
    }

    /**
     *
     */
    @Override
    public void showPageContent() {
        hideAllStatusPage(true);
    }

    /**
     * 页面重试
     */
    protected void onPageRetry(View v) {

    }
    /*------页面状态管理End------*/


    /**
     * 压缩图片 {@link AbstractPresenter#compressImgs(List)}
     */
    @Override
    public void getCompressedImgs(List<String> imgs) {
        hideDialogLoading();
    }

    @Override
    public void onFileDownloaded(String absoluteFilePath) {
        LogUtil.e("onFileDownloaded" + absoluteFilePath);
        hideDialogLoading();
    }

    @Override
    public void onFileDownloadError() {
        hideDialogLoading();
        showError(R.string.app_name);
    }
}