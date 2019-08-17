package com.zyb.mreader.widget;

import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ContentScaleAnimation extends Animation {
    private float mPivotX;
    private float mPivotY;
    private float marginLeft; // 控件左上角X
    private float marginTop;
    private final float scaleTimes; //缩放倍数
    private boolean mReverse; //反向

    private float viewWidth;
    private float viewHeight;

    public ContentScaleAnimation(float viewWidth, float viewHeight,
                                 float marginLeft, float marginTop,
                                 float scaleTimes, boolean mReverse) {
        this.marginLeft = marginLeft;
        this.marginTop = marginTop;
        this.scaleTimes = scaleTimes;
        this.mReverse = mReverse;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {

        Matrix matrix = t.getMatrix();//缩放方法
        if (mReverse) {
            matrix.postScale(1 + (scaleTimes - 1) * (1.0f - interpolatedTime), 1 + (scaleTimes - 1) * (1.0f - interpolatedTime), mPivotX, mPivotY);
        } else {
            matrix.postScale(1 + (scaleTimes - 1) * interpolatedTime, 1 + (scaleTimes - 1) * interpolatedTime, mPivotX, mPivotY);
        }
    }

    //缩放点坐标值
    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mPivotX = resolvePivotX(marginLeft, parentWidth, viewWidth);
        mPivotY = resolvePivoY(marginTop, parentHeight, viewHeight);
    }

    //缩放点坐标值   缩放点到自身左边距离/缩放点到父控件左边的距离=缩放点自身右侧距离/缩放点到父控件右边的距离
    private float resolvePivotX(float margingLeft, int parentWidth, float width) {
//        return (margingLeft * parentWidth) / (parentWidth - width);
        return (margingLeft * width) / (parentWidth - width);
        //缩放点在view里
        //x/(x+margingLeft)=(width-x)/(parentWidth-margingLeft-x)
        //x/(x+y)=(w-x)/(p-y-x)
        // xp-xy-xx=xw-xx+yw-xy
        // xp=xw+yw
        //xp-xw =yw
        //x(p-w)=yw
        //x=yw/(p-w)
        //= (margingLeft*width)/(parentWidth - width)

        //缩放点在view外
        // (x-width)/(margingLeft+x-parentWidth)=x/(margingLeft+x)
        // (x-w)/(y+x-p)=x/(y+x)
        // xy+xx-xp=xy+xx-yw+xw
        // -xp=-yw+xw
        //xw+xp=yw
        //x(w+p) =yw
        // x=yw/(w+p)
        // = margingLeft*width/(width+parentWidth)
    }

    //缩放点坐标值   缩放点到自身顶部距离/缩放点到父控件顶部的距离=缩放点自身底部距离/缩放点到父控件底部的距离
    private float resolvePivoY(float marginTop, int parentHeight, float height) {
//        return (marginTop * parentHeight) / (parentHeight - height);
        return (marginTop * height) / (parentHeight - height);
//        return (marginTop*height)/(height+parentHeight);
    }

    public void reverse() {
        mReverse = !mReverse;
    }

    public boolean getMReverse() {
        return mReverse;
    }
}
