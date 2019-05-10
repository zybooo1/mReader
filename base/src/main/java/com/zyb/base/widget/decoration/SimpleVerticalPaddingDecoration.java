package com.zyb.base.widget.decoration;


import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zyb.base.utils.CommonUtils;

/**
 * 竖向列表底部Padding间距
 */
public class SimpleVerticalPaddingDecoration extends RecyclerView.ItemDecoration {

    private int dividerHeight;

    public SimpleVerticalPaddingDecoration(int dp) {
        dividerHeight = CommonUtils.dp2px(dp);
    }

    public SimpleVerticalPaddingDecoration() {
        dividerHeight = CommonUtils.dp2px(10);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = dividerHeight;//类似加了一个bottom padding
    }
}