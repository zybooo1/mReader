package com.zyb.reader.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.zyb.base.utils.CommonUtils;
import com.zyb.reader.R;
import com.zyb.reader.read.BaseViewModel;
import com.zyb.reader.utils.SystemUtils;
import com.zyb.reader.widget.theme.ColorView;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Liang_Lu on 2017/11/21.
 */

public class BaseActivity extends AppCompatActivity {
    protected static int NO_BINDDING = -1;//不用绑定布局

    protected Context mContext;
    private Toolbar mToolbar;
    protected BaseViewModel mModel;
    private Unbinder mUnbinder;
    private boolean isSlideBack = true;//是否设置滑动返回
    private ColorView mStatusBar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }
    }

    /**
     * Databinding设置布局绑定
     *
     * @param resId      布局layout
     * @param brVariavle BR或者不用绑定
     * @param mModel     viewmodel
     */
    public void setBinddingView(@LayoutRes int resId, int brVariavle, BaseViewModel mModel) {
        if (brVariavle == NO_BINDDING) {
            setContentView(resId);
        } else {
            ViewDataBinding dataBinding = DataBindingUtil.setContentView(this, resId);
            dataBinding.setVariable(brVariavle, mModel);
        }
        this.mModel = mModel;
        mUnbinder = ButterKnife.bind(this);
        mContext = this;
        initView();

    }


    /**
     * 设置textview图标
     *
     * @param view
     * @param iconRes
     */
    private void setIconDrawable(TextView view, @DrawableRes int iconRes) {
        view.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(iconRes),
                null, null, null);
        view.setCompoundDrawablePadding(CommonUtils.dp2px(10));
    }

    private void initStatusBar() {
        mStatusBar = findViewById(R.id.status_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mStatusBar.setVisibility(View.VISIBLE);
            mStatusBar.getLayoutParams().height = SystemUtils.getStatusHeight(this);
            mStatusBar.setLayoutParams(mStatusBar.getLayoutParams());
        } else {
            mStatusBar.setVisibility(View.GONE);
        }
    }


    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    /**
     * activity跳转（无参数）
     *
     * @param className
     */
    public void startActivity(Class<?> className) {
        Intent intent = new Intent(mContext, className);
        startActivity(intent);
    }

    /**
     * activity跳转（有参数）
     *
     * @param className
     */
    public void startActivity(Class<?> className, Bundle bundle) {
        Intent intent = new Intent(mContext, className);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * 初始化view
     */
    protected void initView() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mModel != null) {
            mModel.onDestroy();
        }
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

}