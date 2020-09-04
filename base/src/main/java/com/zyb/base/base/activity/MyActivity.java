package com.zyb.base.base.activity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;
import com.hjq.toast.ToastUtils;
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialog.v3.MessageDialog;
import com.kongzue.dialog.v3.TipDialog;
import com.kongzue.dialog.v3.WaitDialog;
import com.zyb.base.R;
import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.base.router.RouterConstants;
import com.zyb.base.router.RouterUtils;
import com.zyb.base.umeng.UmengClient;
import com.zyb.base.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 项目中的 Activity 基类,若不需要MVP结构，直接继承此类
 */
public abstract class MyActivity extends BaseActivity
        implements OnTitleBarListener, BaseView, ViewTreeObserver.OnGlobalLayoutListener {
    protected static final int IMG_SELECT_REQUEST_CODE = 0x678;
    private Unbinder mButterKnife;//View注解

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initLayout() {
        super.initLayout();
        initImmersion();
        // 初始化标题栏的监听
        if (getTitleBar() != null) {
            getTitleBar().setOnTitleBarListener(this);
        }

        mButterKnife = ButterKnife.bind(this);
        initPageStatusManager();
    }

    /**
     * 默认什么都不做
     */
    @Override
    protected void initData() {
    }

    /**
     * {@link android.view.ViewTreeObserver.OnGlobalLayoutListener}
     */
    @Override
    public void onGlobalLayout() {
    }//不用写任何方法



    //===================== TitleBar Start ==================================================
    /**
     * 默认没有titlebar
     */
    @Override
    protected int getTitleBarId() {
        return 0;
    }

    /**
     * 设置标题栏的标题
     */
    @Override
    public void setTitle(int titleId) {
        setTitle(getText(titleId));
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
        //如果没设置，自动寻找
        return findTitleBar(findViewById(Window.ID_ANDROID_CONTENT));
    }

    /**
     * 递归获取 ViewGroup 中的 TitleBar 对象
     */
    private TitleBar findTitleBar(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View view = group.getChildAt(i);
            if ((view instanceof TitleBar)) {
                return (TitleBar) view;
            } else if (view instanceof ViewGroup) {
                TitleBar titleBar = findTitleBar((ViewGroup) view);
                if (titleBar != null) {
                    return titleBar;
                }
            }
        }
        return null;
    }

    /**
     * TitleBar点击回调
     *
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
    //===================== TitleBar End ==================================================


    //===================== 沉浸式 Start ==================================================
    private ImmersionBar mImmersionBar;//状态栏沉浸
    /**
     * 是否使用沉浸式状态栏
     */
    public boolean isStatusBarEnabled() {
        return true;
    }

    /**
     * 获取状态栏沉浸的配置对象
     */
    public ImmersionBar getStatusBarConfig() {
        return mImmersionBar;
    }

    /**
     * 初始化沉浸式
     */
    protected void initImmersion() {
        //初始化沉浸式状态栏
        if (isStatusBarEnabled()) {
            statusBarConfig().init();

            // 设置标题栏沉浸
            if (getTitleBar() != null) {
                ImmersionBar.setTitleBar(this, getTitleBar());
            }
        }
    }
    /**
     * 沉浸式状态栏
     */
    private ImmersionBar statusBarConfig() {
        //在BaseActivity里初始化
        mImmersionBar = ImmersionBar.with(this)
                .statusBarDarkFont(statusBarDarkFont())    //默认状态栏字体颜色为黑色
                .navigationBarColor(navigationBarColor())
                .keyboardEnable(false, WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);  //解决软键盘与底部输入框冲突问题，默认为false，还有一个重载方法，可以指定软键盘mode
        //必须设置View树布局变化监听，否则软键盘无法顶上去，还有模式必须是SOFT_INPUT_ADJUST_PAN
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(this);
        return mImmersionBar;
    }

    public boolean statusBarDarkFont() {
        //返回true表示状态栏黑色字体
        return true;
    }

    public @ColorRes
    int navigationBarColor() {
        return R.color.white;
    }

    public void setNavigationBarColor(int color) {
        if (getStatusBarConfig() != null)
            getStatusBarConfig().navigationBarColor(color).init();
    }

    //===================== 沉浸式 End ==================================================



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mButterKnife != null) mButterKnife.unbind();
        ImmersionBar.with(this).destroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }

    @Override
    public String getStringById(int id) {
        return getString(id);
    }


    /**
     * 判断某个界面是否在前台,返回true，为显示,否则不是
     */
    private boolean isForeGround=false;

    boolean isForeground() {
        return isForeGround;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeGround=true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isForeGround =false;
    }

    //===================== 提示 Start ==================================================
    @Override
    public void toast(CharSequence s) {
        if (isForeground()) ToastUtils.show(s);
    }

    @Override
    public void toast(int id) {
        if (isForeground()) ToastUtils.show(id);
    }
    //===================== 提示 End ==================================================


    /*----------------------Loading弹窗 Begin------------------------------------------*/
    @Override
    public void showDialogLoading(boolean canCancel) {
        WaitDialog.show(this,"").setCancelable(canCancel);
    }

    @Override
    public void showDialogLoading(String msg) {
        WaitDialog.show(this, msg);
    }

    @Override
    public void showDialogLoading() {
        WaitDialog.show(this,R.string.msg_loading).setCancelable(true);
    }

    /**
     * 不用在onDestroy执行
     */
    @Override
    public void hideDialogLoading() {
        WaitDialog.dismiss();
    }
    /*-------------------Loading弹窗 End------------------------------------------------------*/


    /*-------------------提示弹窗 Begin------------------------------------------------------*/
    /**
     * @deprecated  下版本取消这个方法，直接MessageDialog.build
     */
    @Override
    public void showDialog(boolean canCancel, String title, String confirmText,
                           String cancelText, OnDialogButtonClickListener cancelListener,
                           OnDialogButtonClickListener confirmListener) {
        MessageDialog.build(this)
                .setCancelable(canCancel)
                .setTitle(title)
                .setMessage("")
                .setCancelButton(cancelText, cancelListener)
                .setOkButton(confirmText, confirmListener)
                .show();
    }


    @Override
    public void showDialog(boolean canCancel, String title, String msg, String confirmText,
                           String cancelText, OnDialogButtonClickListener cancelListener,
                           OnDialogButtonClickListener confirmListener) {
        MessageDialog.build(this)
                .setCancelable(canCancel)
                .setTitle(title)
                .setMessage(msg)
                .setCancelButton(cancelText, cancelListener)
                .setOkButton(confirmText, confirmListener)
                .show();
    }
    /*------------------提示弹窗 End------------------------------------------*/


    /*------------------页面状态管理Begin--------------------------------------*/
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

        View.OnClickListener retryListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPageRetry(v);
            }
        };
        if (layoutRetry != null && layoutRetry.findViewById(R.id.btnRetry) != null) {
            layoutRetry.findViewById(R.id.btnRetry).setOnClickListener(retryListener);
        }
        if (layoutRetry != null && layoutRetry.findViewById(R.id.ivRetry) != null) {
            layoutRetry.findViewById(R.id.ivRetry).setOnClickListener(retryListener);
        }
        if (layoutEmpty != null && layoutEmpty.findViewById(R.id.btnRetry) != null) {
            layoutEmpty.findViewById(R.id.btnRetry).setOnClickListener(retryListener);
        }
        if (layoutEmpty != null && layoutEmpty.findViewById(R.id.ivEmpty) != null) {
            layoutEmpty.findViewById(R.id.ivEmpty).setOnClickListener(retryListener);
        }
        if (usePageStatusManager()) showPageLoading();
    }

    protected void hideAllStatusPage() {
        if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
        if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);
        if (layoutRetry != null) layoutRetry.setVisibility(View.GONE);
    }

    @Override
    public void showPageLoading() {
        hideAllStatusPage();
        if (layoutLoading != null) layoutLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void showPageEmpty(String message) {
        hideAllStatusPage();
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.VISIBLE);
            ((TextView) layoutEmpty.findViewById(R.id.tv_page_empty)).setText(message);
        }
    }

    @Override
    public void showPageEmpty() {
        showPageEmpty(getString(R.string.base_data_empty_tip));
    }

    @Override
    public void showPageRetry() {
        hideAllStatusPage();
        if (layoutRetry != null) layoutRetry.setVisibility(View.VISIBLE);
    }

    /**
     * 只要显示一次界面内容，就会隐藏界面状态根布局，就不会再显示各种页面状态了可以交给Toast
     * 或  refreshLayout(如果不这么做，每次刷新都会显示pageLoading，而覆盖了smartRefresh）
     */
    @Override
    public void showPageContent() {
        if (layoutStatusRoot != null) layoutStatusRoot.setVisibility(View.GONE);
        hideAllStatusPage();
    }

    /**
     * 页面重试
     */
    protected void onPageRetry(View v) {

    }
    /*------------------页面状态管理End---------------------------------------------*/
}