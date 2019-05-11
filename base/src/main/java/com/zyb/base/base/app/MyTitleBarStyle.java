package com.zyb.base.base.app;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.hjq.bar.style.BaseTitleBarStyle;
import com.zyb.base.R;

/**
 * titleBar主题样式
 */
public class MyTitleBarStyle extends BaseTitleBarStyle {

    public MyTitleBarStyle(Context context) {
        super(context);
    }

    @Override
    public Drawable getBackground() {
        return getDrawable(R.color.colorPrimary);
    }

    @Override
    public Drawable getBackIcon() {
        return getDrawable(com.hjq.bar.R.mipmap.bar_icon_back_white);
    }

    @Override
    public int getLeftColor() {
        return getColor(R.color.white);
    }

    @Override
    public int getTitleColor() {
        return getColor(R.color.white);
    }

    @Override
    public int getRightColor() {
        return getColor(R.color.white);
    }

    @Override
    public boolean isLineVisible() {
        return false;
    }

    @Override
    public Drawable getLineDrawable() {
        return getDrawable(R.color.colorLine);
    }

    @Override
    public int getLineSize() {
        return 1;
    }

    @Override
    public float getTitleSize() {
        return sp2px(20);
    }

    @Override
    public Drawable getLeftBackground() {
        return getDrawable(R.drawable.bar_selector_selectable_black);
    }

    @Override
    public Drawable getRightBackground() {
        return getDrawable(com.hjq.bar.R.drawable.bar_selector_selectable_black);
    }
}