package com.zyb.base.base.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

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
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2018/10/18
 * desc   : 项目中 Fragment 懒加载基类
 */
public abstract class MyLazyFragment extends UILazyFragment implements BaseView {

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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isUnbindButterKnife()) mButterKnife.unbind();
        hideDialog();
        hideDialogLoading();
    }

    /**
     * 默认解绑Butterknife，ViewPager+Fragment出现Nullpointer
     * 可返回false不解绑
     */
    protected boolean isUnbindButterKnife() {
        return true;
    }

    @Nullable
    public TitleBar getTitleBar() {
        if (getTitleBarId() > 0 && findViewById(getTitleBarId()) instanceof TitleBar) {
            return findViewById(getTitleBarId());
        }
        return null;
    }

    @Override
    public String getStringById(int id) {
        return getString(id);
    }


    /*----------显示吐司 Begin------------*/
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
    /*----------显示吐司 End------------*/


    /*----------Loading弹窗 Begin------------*/
    BaseDialog loadingDialog;

    @Override
    public void showDialogLoading(String msg) {
        if (loadingDialog == null) {
            loadingDialog = new WaitDialog.Builder(getFragmentActivity())
                    .setMessage(msg)// 消息文本可以不用填写
                    .create();
        }
        loadingDialog.show();
    }

    @Override
    public void showDialogLoading() {
        if (loadingDialog == null) {
            loadingDialog = new WaitDialog.Builder(getFragmentActivity())
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
        messageDialog = new MessageDialog.Builder(getFragmentActivity())
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

    private void initPageStatusManager(View view) {
        layoutStatusRoot = view.findViewById(R.id.layout_status_root);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        layoutLoading = view.findViewById(R.id.layout_loading);
        layoutRetry = view.findViewById(R.id.layout_retry);

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
        LogUtil.e("onFileDownloaded"+absoluteFilePath);
        hideDialogLoading();
    }

    @Override
    public void onFileDownloadError() {
        hideDialogLoading();
        showError(R.string.app_name);
    }
}