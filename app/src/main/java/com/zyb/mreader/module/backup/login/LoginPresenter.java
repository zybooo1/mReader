package com.zyb.mreader.module.backup.login;


import android.os.Environment;

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.http.CommonSubscriber;
import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.base.utils.EventBusUtil;
import com.zyb.base.utils.RxUtil;
import com.zyb.base.utils.constant.Constants;
import com.zyb.common.db.DBFactory;
import com.zyb.common.db.bean.Book;
import com.zyb.mreader.core.AppDataManager;
import com.zyb.mreader.core.prefs.PreferenceHelperImpl;
import com.zyb.mreader.utils.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 *
 */

public class LoginPresenter extends AbstractPresenter<LoginContract.View, AppDataManager> implements LoginContract.Presenter {

    PreferenceHelperImpl preferenceHelper;

    @Inject
    LoginPresenter(AppDataManager dataManager) {
        super(dataManager);
        preferenceHelper = new PreferenceHelperImpl();
    }

    @Override
    public String getWebDavHost() {
        return mDataManager.getWebDavHost();
    }

    @Override
    public String getWebDavUserName() {
        return mDataManager.getWebDavUserName();
    }

    @Override
    public String getWebDavPassword() {
        return mDataManager.getWebDavPassword();
    }

    @Override
    public void login(String userName, String password, String host) {
        addSubscribe(
                Flowable.create(new FlowableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(FlowableEmitter<Boolean> emitter) throws Exception {
                        try {
                            Sardine sardine = new OkHttpSardine();
                            sardine.setCredentials(userName, password);
                            emitter.onNext(sardine.exists(host));
                            emitter.onComplete();
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                    }
                }, BackpressureStrategy.BUFFER)
                        .compose(RxUtil.<Boolean>rxSchedulerHelper())
                        .subscribeWith(new CommonSubscriber<Boolean>(mView) {

                            @Override
                            protected void onStartWithViewAlive() {
                                mView.showDialogLoading();
                            }

                            @Override
                            protected void onCompleteWithViewAlive() {
                                mView.hideDialogLoading();
                            }

                            @Override
                            protected void onNextWithViewAlive(Boolean b) {
                                if (b) {
                                    mDataManager.setWebDavUserName(userName);
                                    mDataManager.setWebDavPassword(password);
                                    mDataManager.setWebDavHost(host);
                                    mView.showToast("登录成功");
                                    mView.loginSuccess();
                                } else {
                                    mView.showToast("登录失败");
                                }
                            }

                            @Override
                            protected void onErrorWithViewAlive(Throwable e) {
                                mView.hideDialogLoading();
                                mView.showToast("登录失败");
                            }
                        }));
    }
}
