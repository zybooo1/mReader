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
        return null;
    }

    @Override
    public Drawable getBackIcon() {
        return null;
    }

    @Override
    public int getLeftColor() {
        return 0;
    }

    @Override
    public int getTitleColor() {
        return 0;
    }

    @Override
    public int getRightColor() {
        return 0;
    }

    @Override
    public boolean isLineVisible() {
        return false;
    }

    @Override
    public Drawable getLineDrawable() {
        return null;
    }

    @Override
    public Drawable getLeftBackground() {
        return null;
    }

    @Override
    public Drawable getRightBackground() {
        return null;
    }
}