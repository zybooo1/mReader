package com.zyb.base.widget.decoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zyb.base.utils.CommonUtils;


public class CommonDecoration extends RecyclerView.ItemDecoration {

    private int topSpace;
    private int rightSpace;
    private int bottomSpace;
    private int leftSpace;
    private int rootTopPadding;
    private int spanCount;

    public CommonDecoration(int spaceDip) {
        this(spaceDip, spaceDip, spaceDip, spaceDip);
    }

    public CommonDecoration(int spaceDip, int opPaddingDip) {
        this(spaceDip, spaceDip, spaceDip, spaceDip, opPaddingDip, 1);
    }

    public CommonDecoration(int topSpaceDip, int rightSpaceDip,
                            int bottomSpaceDip, int leftSpaceDip) {
        this(topSpaceDip, rightSpaceDip, bottomSpaceDip, leftSpaceDip, 0, 1);
    }

    public CommonDecoration(int topSpaceDip, int rightSpaceDip, int bottomSpaceDip,
                            int leftSpaceDip, int topPaddingDip, int spanCount) {
        this.topSpace = CommonUtils.dp2px(topSpaceDip);
        this.rightSpace = CommonUtils.dp2px(rightSpaceDip);
        this.bottomSpace = CommonUtils.dp2px(bottomSpaceDip);
        this.leftSpace = CommonUtils.dp2px(leftSpaceDip);
        this.rootTopPadding = CommonUtils.dp2px(topPaddingDip);
        this.spanCount = spanCount;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int childAdapterPosition = parent.getChildAdapterPosition(view);
        if (childAdapterPosition < spanCount) {
            outRect.top = topSpace + rootTopPadding;
        } else {
            outRect.top = topSpace;
        }

        outRect.right = rightSpace;
        outRect.bottom = bottomSpace;
        outRect.left = leftSpace;

    }
}