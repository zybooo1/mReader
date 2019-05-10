package com.zyb.base.widget.decoration;


import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zyb.base.utils.CommonUtils;

/**
 * 横向列表左Padding间距
 */
public class SimpleHorizontalPaddingDecoration extends RecyclerView.ItemDecoration {

    private int dividerHeight;

    public SimpleHorizontalPaddingDecoration(int dp) {
        dividerHeight = CommonUtils.dp2px(dp);
    }

    public SimpleHorizontalPaddingDecoration() {
        dividerHeight = CommonUtils.dp2px(10);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.left = dividerHeight;//类似加了一个top padding
    }
}