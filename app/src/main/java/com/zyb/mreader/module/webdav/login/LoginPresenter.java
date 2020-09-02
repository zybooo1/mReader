package com.zyb.mreader.module.webdav.login;


import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;
import com.zyb.base.http.CommonSubscriber;
import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.base.utils.RxUtil;
import com.zyb.mreader.core.AppDataManager;
import com.zyb.mreader.core.prefs.PreferenceHelperImpl;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

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
                                    mView.toast("登录成功");
                                    mView.loginSuccess();
                                } else {
                                    mView.toast("登录失败");
                                }
                            }

                            @Override
                            protected void onErrorWithViewAlive(Throwable e) {
                                mView.hideDialogLoading();
                                mView.toast("登录失败");
                            }
                        }));
    }
}
