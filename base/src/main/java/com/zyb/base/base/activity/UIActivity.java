package com.zyb.base.base.activity;

import android.support.annotation.ColorRes;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.gyf.barlibrary.ImmersionBar;
import com.zyb.base.R;

/**
 * Activity基类，封装沉浸式和侧滑返回（默认开启沉浸式状态栏和侧滑功能）
 */
public abstract class UIActivity extends BaseActivity
        implements ViewTreeObserver.OnGlobalLayoutListener {

    private ImmersionBar mImmersionBar;//状态栏沉浸


    @Override
    protected void initLayout() {
        super.initLayout();
        initImmersion();
    }

    /**
     * 初始化沉浸式
     */
    protected void initImmersion() {
        //初始化沉浸式状态栏
        if (isStatusBarEnabled()) {
            statusBarConfig().init();
            //设置标题栏
            if (getTitleBarId() > 0) {
                ImmersionBar.setTitleBar(this, findViewById(getTitleBarId()));
            }
        }
    }

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
     * 初始化沉浸式状态栏
     */
    private ImmersionBar statusBarConfig() {
        //在BaseActivity里初始化
        mImmersionBar = ImmersionBar.with(this)
                .statusBarDarkFont(statusBarDarkFont())    //默认状态栏字体颜色为黑色
                .navigationBarColor(navigationBarColor())  //默认虚拟导航栏背景颜色为窗口颜色
                .navigationBarDarkIcon(true)
                .keyboardEnable(false, WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);  //解决软键盘与底部输入框冲突问题，默认为false，还有一个重载方法，可以指定软键盘mode
        //必须设置View树布局变化监听，否则软键盘无法顶上去，还有模式必须是SOFT_INPUT_ADJUST_PAN
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(this);
        return mImmersionBar;
    }

    /**
     * {@link ViewTreeObserver.OnGlobalLayoutListener}
     */
    @Override
    public void onGlobalLayout() {
    }//不用写任何方法

    /**
     * 获取状态栏字体颜色
     */
    public boolean statusBarDarkFont() {
        //返回false表示白色字体
        return true;
    }

    public @ColorRes int navigationBarColor() {
        return R.color.windowBackground;
    }

    protected void gone(final View... views) {
        if (views != null && views.length > 0) {
            for (View view : views) {
                if (view != null) {
                    view.setVisibility(View.GONE);
                }
            }
        }
    }

    protected void visible(final View... views) {
        if (views != null && views.length > 0) {
            for (View view : views) {
                if (view != null) {
                    view.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImmersionBar != null) mImmersionBar.destroy();
        getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }
}