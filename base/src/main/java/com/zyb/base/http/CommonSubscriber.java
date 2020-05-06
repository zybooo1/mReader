package com.zyb.base.http;

import android.content.Context;
import android.text.TextUtils;

import com.hjq.toast.ToastUtils;
import com.zyb.base.R;
import com.zyb.base.mvp.BaseView;

import java.net.SocketTimeoutException;

import io.reactivex.subscribers.ResourceSubscriber;
import retrofit2.HttpException;


/**
 * 统一封装网络请求结果订阅
 *
 * @param <T> view
 */
public abstract class CommonSubscriber<T> extends ResourceSubscriber<T> {

    private Context mContext;
    private BaseView mView;
    private String mErrorMsg;
    private boolean isshowToastState = true;

    protected CommonSubscriber(Context context) {
        this.mContext = context;
    }

    protected CommonSubscriber(BaseView view) {
        this.mView = view;
    }

    protected CommonSubscriber(BaseView view, String errorMsg) {
        this.mView = view;
        this.mErrorMsg = errorMsg;
    }

    protected CommonSubscriber(BaseView view, boolean isshowToastState) {
        this.mView = view;
        this.isshowToastState = isshowToastState;
    }

    protected CommonSubscriber(BaseView view, String errorMsg, boolean isshowToastState) {
        this.mView = view;
        this.mErrorMsg = errorMsg;
        this.isshowToastState = isshowToastState;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mView != null) onStartWithViewAlive();
    }


    @Override
    public void onComplete() {
        if (mView != null) onCompleteWithViewAlive();
    }


    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        //view为null不执行
        if (mView != null || mContext != null) {
            showToast(e);
        }
        if (mView != null) onErrorWithViewAlive(e);

    }

    @Override
    public void onNext(T t) {
        if (mView != null) onNextWithViewAlive(t);
    }

    /**
     * view不为null时执行以下方法
     */
    protected abstract void onStartWithViewAlive();

    protected abstract void onCompleteWithViewAlive();

    protected abstract void onNextWithViewAlive(T t);

    protected abstract void onErrorWithViewAlive(Throwable e);

    private void showToast(Throwable e) {
        if (mView != null) {
            if (mErrorMsg != null && !TextUtils.isEmpty(mErrorMsg)) {
                mView.showToast(mErrorMsg);
            } else if (e instanceof ApiException) {
                mView.showToast(e.toString());
            } else if (e instanceof HttpException) {
                mView.showToast(handleHttpExceptionTips(e));
            } else if (e instanceof SocketTimeoutException) {
                mView.showToast(R.string.msg_error_time_out);
            } else {
                mView.showToast(R.string.msg_error_unknown);
            }
        } else if (mContext != null) {
            if (mErrorMsg != null && !TextUtils.isEmpty(mErrorMsg)) {
                ToastUtils.show(mErrorMsg);
            } else if (e instanceof ApiException) {
                ToastUtils.show(e.toString());
            } else if (e instanceof HttpException) {
                int resString = handleHttpExceptionTips(e);
                ToastUtils.show(mContext.getString(resString));
            }
        }
    }

    private int handleHttpExceptionTips(Throwable e) {
        int code = ((HttpException) e).code();
        if (code == 429) {
            return R.string.msg_error_too_fast;
        } else if (code == 404) {
            return R.string.msg_error_404;
        } else {
            return R.string.msg_error_server;
        }
    }

}
