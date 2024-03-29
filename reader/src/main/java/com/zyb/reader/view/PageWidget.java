package com.zyb.reader.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.zyb.base.utils.LogUtil;
import com.zyb.base.utils.RxUtil;
import com.zyb.reader.Config;
import com.zyb.reader.util.PageFactory;
import com.zyb.reader.view.animation.AnimationProvider;
import com.zyb.reader.view.animation.CoverAnimation;
import com.zyb.reader.view.animation.NoneAnimation;
import com.zyb.reader.view.animation.SimulationAnimation;
import com.zyb.reader.view.animation.SlideAnimation;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


/**
 * Created by Administrator on 2016/8/29 0029.
 */
public class PageWidget extends View {
    private final static String TAG = "BookPageWidget";
    private int mScreenWidth = 0; // 屏幕宽
    private int mScreenHeight = 0; // 屏幕高
    private Context mContext;

    //是否移动了
    private Boolean isMove = false;
    //是否翻到下一页
    private Boolean isNext = false;
    //是否取消翻页
    private Boolean cancelPage = false;
    //是否没下一页或者上一页
    private Boolean noNext = false;
    private int downX = 0;
    private int downY = 0;

    private int moveX = 0;
    private int moveY = 0;
    //翻页动画是否在执行
    private Boolean isAnimating = false;

    //长按判断时间
    private static final long LONG_PRESS_TIME = 500;

    Bitmap mCurPageBitmap = null; // 当前页
    Bitmap mNextPageBitmap = null;// 下一页
    private AnimationProvider mAnimationProvider;

    Scroller mScroller;
    private int mBgColor = 0xFFCEC29C;
    private TouchListener mTouchListener;

    public PageWidget(Context context) {
        this(context, null);
    }

    public PageWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mScreenWidth = w;
        mScreenHeight = h;
        initPage();
    }

    private void initPage() {
        LogUtil.e("initPage:mScreenWidth=" + mScreenWidth + "mScreenHeight=" + mScreenHeight);
        mCurPageBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.RGB_565);      //android:LargeHeap=true  use in  manifest application
        mNextPageBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.RGB_565);

        mScroller = new Scroller(getContext(), new LinearInterpolator());
        mAnimationProvider = new SimulationAnimation(mCurPageBitmap, mNextPageBitmap, mScreenWidth, mScreenHeight);

        //解决视图更新(onSizeChanged)后页面黑色(没绘制当前页面)
        if(PageFactory.getInstance()!=null)PageFactory.getInstance().updateCurrentPage();
    }

    public void setPageMode(int pageMode) {
        switch (pageMode) {
            case Config.PAGE_MODE_SIMULATION:
                mAnimationProvider = new SimulationAnimation(mCurPageBitmap, mNextPageBitmap, mScreenWidth, mScreenHeight);
                break;
            case Config.PAGE_MODE_COVER:
                mAnimationProvider = new CoverAnimation(mCurPageBitmap, mNextPageBitmap, mScreenWidth, mScreenHeight);
                break;
            case Config.PAGE_MODE_SLIDE:
                mAnimationProvider = new SlideAnimation(mCurPageBitmap, mNextPageBitmap, mScreenWidth, mScreenHeight);
                break;
            case Config.PAGE_MODE_NONE:
                mAnimationProvider = new NoneAnimation(mCurPageBitmap, mNextPageBitmap, mScreenWidth, mScreenHeight);
                break;
        }
    }

    public Bitmap getCurPage() {
        return mCurPageBitmap;
    }

    public Bitmap getNextPage() {
        return mNextPageBitmap;
    }

    public void setBgColor(int color) {
        mBgColor = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(mBgColor);
        Log.e("onDraw", "isNext:" + isNext + "          isRuning:" + isAnimating);
        if (isAnimating) {
            mAnimationProvider.drawMove(canvas);
        } else {
            mAnimationProvider.drawStatic(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (PageFactory.getStatus() == PageFactory.Status.OPENING) {
            return true;
        }

        int x = (int) event.getX();
        int y = (int) event.getY();

        mAnimationProvider.setTouchPoint(x, y);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            doLongClick();

            downX = (int) event.getX();
            downY = (int) event.getY();
            moveX = 0;
            moveY = 0;
            isMove = false;
//            cancelPage = false;
            noNext = false;
            isNext = false;
            isAnimating = false;
            mAnimationProvider.setStartPoint(downX, downY);
            abortAnimation();
            Log.e(TAG, "ACTION_DOWN");
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            clearSubscribe();
            if (hasPerformedLongPress) return true;

            final int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            //判断是否移动了
            if (!isMove) {
                isMove = Math.abs(downX - x) > slop || Math.abs(downY - y) > slop;
            }

            if (isMove) {
                isMove = true;
                if (moveX == 0 && moveY == 0) {
                    Log.e(TAG, "isMove");
                    //判断翻得是上一页还是下一页
                    if (x - downX > 0) {
                        isNext = false;
                    } else {
                        isNext = true;
                    }
                    cancelPage = false;
                    if (isNext) {
                        Boolean isNext = mTouchListener.nextPage();
//                        calcCornerXY(downX,mScreenHeight);
                        mAnimationProvider.setDirection(AnimationProvider.Direction.next);

                        if (!isNext) {
                            noNext = true;
                            return true;
                        }
                    } else {
                        Boolean isPre = mTouchListener.prePage();
                        mAnimationProvider.setDirection(AnimationProvider.Direction.pre);

                        if (!isPre) {
                            noNext = true;
                            return true;
                        }
                    }
                    Log.e(TAG, "isNext:" + isNext);
                } else {
                    //判断是否取消翻页
                    if (isNext) {
                        if (x - moveX > 0) {
                            cancelPage = true;
                            mAnimationProvider.setCancel(true);
                        } else {
                            cancelPage = false;
                            mAnimationProvider.setCancel(false);
                        }
                    } else {
                        if (x - moveX < 0) {
                            mAnimationProvider.setCancel(true);
                            cancelPage = true;
                        } else {
                            mAnimationProvider.setCancel(false);
                            cancelPage = false;
                        }
                    }
                    Log.e(TAG, "cancelPage:" + cancelPage);
                }

                moveX = x;
                moveY = y;
                isAnimating = true;
                this.postInvalidate();
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            clearSubscribe();
            if (hasPerformedLongPress) return true;

            Log.e(TAG, "ACTION_UP");
            if (!isMove) {
                cancelPage = false;
                //是否点击了中间
                if (downX > mScreenWidth / 5 && downX < mScreenWidth * 4 / 5 && downY > mScreenHeight / 3 && downY < mScreenHeight * 2 / 3) {
                    if (mTouchListener != null) {
                        mTouchListener.center();
                    }
                    Log.e(TAG, "center");
//                    mCornerX = 1; // 拖拽点对应的页脚
//                    mCornerY = 1;
//                    mTouch.x = 0.1f;
//                    mTouch.y = 0.1f;
                    return true;
                } else if (x < mScreenWidth / 2) {
                    isNext = false;
                } else {
                    isNext = true;
                }

                if (isNext) {
                    Boolean isNext = mTouchListener.nextPage();
                    mAnimationProvider.setDirection(AnimationProvider.Direction.next);
                    if (!isNext) {
                        return true;
                    }
                } else {
                    Boolean isPre = mTouchListener.prePage();
                    mAnimationProvider.setDirection(AnimationProvider.Direction.pre);
                    if (!isPre) {
                        return true;
                    }
                }
            }

            if (cancelPage && mTouchListener != null) {
                mTouchListener.cancel();
            }

            Log.e(TAG, "isNext:" + isNext);
            if (!noNext) {
                isAnimating = true;
                mAnimationProvider.startAnimation(mScroller);
                this.postInvalidate();
            }
        }

        return true;
    }

    private CompositeDisposable compositeDisposable;
    private boolean hasPerformedLongPress = false;

    private void addSubscribe(Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    private void clearSubscribe() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
    }

    private void doLongClick() {
        hasPerformedLongPress = false;
        addSubscribe(Observable.timer(LONG_PRESS_TIME, TimeUnit.MILLISECONDS)
                .compose(RxUtil.rxObservableSchedulerHelper())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (mTouchListener != null) {
                            hasPerformedLongPress = true;
                            mTouchListener.longClick();
                        }
                    }
                }, throwable -> {
                }));
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            float x = mScroller.getCurrX();
            float y = mScroller.getCurrY();
            mAnimationProvider.setTouchPoint(x, y);
            if (mScroller.getFinalX() == x && mScroller.getFinalY() == y) {
                isAnimating = false;
            }
            postInvalidate();
        }
        super.computeScroll();
    }

    public void abortAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            mAnimationProvider.setTouchPoint(mScroller.getFinalX(), mScroller.getFinalY());
            postInvalidate();
        }
    }

    public boolean isRunning() {
        return isAnimating;
    }

    public void setTouchListener(TouchListener mTouchListener) {
        this.mTouchListener = mTouchListener;
    }

    public interface TouchListener {
        void center();

        void longClick();

        Boolean prePage();

        Boolean nextPage();

        void cancel();
    }

}
