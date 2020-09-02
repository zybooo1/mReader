package com.zyb.base.base.fragment;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.hjq.bar.TitleBar;
import com.hjq.toast.ToastUtils;
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialog.v3.MessageDialog;
import com.kongzue.dialog.v3.WaitDialog;
import com.zyb.base.R;
import com.zyb.base.mvp.BaseView;
import com.zyb.base.router.RouterConstants;
import com.zyb.base.router.RouterUtils;
import com.zyb.base.umeng.UmengClient;
import com.zyb.base.utils.LogUtil;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2018/10/18
 * desc   : 项目中 Fragment 懒加载基类
 */
public abstract class MyLazyFragment extends BaseLazyFragment implements BaseView {

    private Unbinder mButterKnife; // View注解
    private View mView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = super.onCreateView(inflater, container, savedInstanceState);
        mButterKnife = ButterKnife.bind(this, mView);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPageStatusManager(view);
    }

    /**
     * 默认什么都不做
     */
    @Override
    protected void initData() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isUnbindButterKnife()) mButterKnife.unbind();
        if (mImmersionBar != null) mImmersionBar.destroy();
    }


    //===================== TitleBar Start ==================================================
    /**
     * 默认没有titlebar
     */
    @Override
    protected int getTitleBarId() {
        return 0;
    }
    @Nullable
    public TitleBar getTitleBar() {
        if (getTitleBarId() > 0 && findViewById(getTitleBarId()) instanceof TitleBar) {
            return findViewById(getTitleBarId());
        }
        //如果没设置，自动寻找
        return findTitleBar((ViewGroup) getView());
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
    //===================== TitleBar End ==================================================


    //===================== 沉浸式 Start ==================================================
    private ImmersionBar mImmersionBar; // 状态栏沉浸

    @Override
    protected void initFragment() {
        initImmersion();
        super.initFragment();
    }

    /**
     * 初始化沉浸式
     */
    protected void initImmersion() {
        // 初始化沉浸式状态栏
        if (isStatusBarEnabled()) {
            statusBarConfig().init();

            // 设置标题栏沉浸
            if (getTitleBar() != null) {
                ImmersionBar.setTitleBar(getActivity(), getTitleBar());
            }
        }
    }

    /**
     * 是否在Fragment使用沉浸式
     */
    public boolean isStatusBarEnabled() {
        return false;
    }

    /**
     * 获取状态栏沉浸的配置对象
     */
    protected ImmersionBar getStatusBarConfig() {
        return mImmersionBar;
    }

    /**
     * 初始化沉浸式
     */
    private ImmersionBar statusBarConfig() {
        //在BaseActivity里初始化
        mImmersionBar = ImmersionBar.with(this)
                .statusBarDarkFont(statusBarDarkFont())    //默认状态栏字体颜色为黑色
                .navigationBarColor(navigationBarColor())
                .keyboardEnable(true);  //解决软键盘与底部输入框冲突问题，默认为false，还有一个重载方法，可以指定软键盘mode
        return mImmersionBar;
    }

    public @ColorRes
    int navigationBarColor() {
        return R.color.windowBackground;
    }



    /**
     * 获取状态栏字体颜色
     */
    protected boolean statusBarDarkFont() {
        //返回true表示黑色字体
        return true;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isStatusBarEnabled() && isLazyLoad()) {
            // 重新初始化状态栏
            statusBarConfig().init();
        }
    }
    //===================== 沉浸式 End ==================================================


    /**
     * 默认解绑Butterknife，ViewPager+Fragment出现Nullpointer
     * 可返回false不解绑
     */
    protected boolean isUnbindButterKnife() {
        return true;
    }

    @Override
    public String getStringById(int id) {
        return getString(id);
    }


    /*---------------------显示吐司 Begin---------------------------------------------*/
    @Override
    public void toast(CharSequence s) {
        if (!isFragmentVisible()) return;
        ToastUtils.show(s);
    }

    @Override
    public void toast(int id) {
        if (!isFragmentVisible()) return;
        ToastUtils.show(id);
    }
    /*---------------------显示吐司 End---------------------------------------------*/


    /*---------------------Loading弹窗 Begin---------------------------------------------*/
    @Override
    public void showDialogLoading(boolean canCancel) {
        WaitDialog.show(((AppCompatActivity) mActivity), "").setCancelable(canCancel);
    }

    @Override
    public void showDialogLoading(String msg) {
        WaitDialog.show(((AppCompatActivity) mActivity), msg);
    }

    @Override
    public void showDialogLoading() {
        WaitDialog.show(((AppCompatActivity) mActivity), R.string.msg_loading).setCancelable(true);
    }

    /**
     * 不用在onDestroy执行
     */
    @Override
    public void hideDialogLoading() {
        WaitDialog.dismiss();
    }
    /*---------------------Loading弹窗 End---------------------------------------------*/

    /*---------------------提示弹窗 Begin---------------------------------------------*/
    /**
     * @deprecated  下版本取消这个方法，直接MessageDialog.build
     */
    @Override
    public void showDialog(boolean canCancel, String title, String confirmText,
                           String cancelText, OnDialogButtonClickListener cancelListener,
                           OnDialogButtonClickListener confirmListener) {
        MessageDialog.build(((AppCompatActivity) mActivity))
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
        MessageDialog.build(((AppCompatActivity) mActivity))
                .setCancelable(canCancel)
                .setTitle(title)
                .setMessage(msg)
                .setCancelButton(cancelText, cancelListener)
                .setOkButton(confirmText, confirmListener)
                .show();
    }
    /*-------------------提示弹窗 End-----------------------------------------------------*/


    /*--------------------页面状态管理Begin-------------------------------------------*/
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

    private void initPageStatusManager(View view) {
        layoutStatusRoot = view.findViewById(R.id.layout_status_root);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        layoutLoading = view.findViewById(R.id.layout_loading);
        layoutRetry = view.findViewById(R.id.layout_retry);

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
            TextView viewById = layoutEmpty.findViewById(R.id.tv_page_empty);
            if (viewById != null)
                viewById.setText(message);
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
     * 只要显示一次界面内容，就不会再显示各种页面状态了
     * 可以交给Toast
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
    /*-----------------页面状态管理End---------------------------------------*/

}