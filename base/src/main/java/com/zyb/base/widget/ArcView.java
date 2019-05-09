package com.zyb.base.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.zyb.base.R;

/**
 * 弧形View
 * @author zyb
 * Create at 2019/4/25
 */
public class ArcView extends View {
    /**
     * 背景颜色
     */
    private int mBottomColor;
    private Paint mPaint;
    private Context mContext;

    public ArcView(Context context) {
        this(context, null);
    }

    public ArcView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ArcView);
        mBottomColor = typedArray.getColor(R.styleable.ArcView_bgColor, Color.WHITE);

        mContext = context;
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int canvasId = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);

        mPaint.setColor(Color.WHITE);
        canvas.drawRect(new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight()), mPaint);

        mPaint.setColor(Color.BLUE);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        Path path = new Path();
        path.moveTo(0, 0);
        path.quadTo(getMeasuredWidth() / 2, getMeasuredHeight() * 2, getMeasuredWidth(), 0);
        canvas.drawPath(path, mPaint);

        mPaint.setXfermode(null);
        canvas.restoreToCount(canvasId);
    }

}
