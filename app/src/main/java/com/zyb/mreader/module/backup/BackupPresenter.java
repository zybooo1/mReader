package com.zyb.mreader.module.backup;


import com.google.gson.Gson;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;
import com.zyb.base.http.CommonSubscriber;
import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.base.utils.RxUtil;
import com.zyb.base.utils.constant.Constants;
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

public class BackupPresenter extends AbstractPresenter<BackupContract.View, AppDataManager> implements BackupContract.Presenter {

    PreferenceHelperImpl preferenceHelper;

    @Inject
    BackupPresenter(AppDataManager dataManager) {
        super(dataManager);
        preferenceHelper = new PreferenceHelperImpl();
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
                                    mDataManager.setWebDevUserName(userName);
                                    mDataManager.setWebDevPassword(password);
                                    mDataManager.setWebDevHost(host);
                                    mView.showMsg("登录成功");
                                    mView.loginSuccess();
                                } else {
                                    mView.showMsg("登录失败");
                                }
                            }

                            @Override
                            protected void onErrorWithViewAlive(Throwable e) {
                                mView.hideDialogLoading();
                                mView.showMsg("登录失败");
                            }
                        }));
    }

    @Override
    public void backup() {
        addSubscribe(
                Flowable.create(new FlowableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(FlowableEmitter<Boolean> emitter) throws Exception {
                        try {
                            Sardine sardine = getSardine();
                            Gson gson = new Gson();
                            String jsons = "{\n" +
                                    "\t\"name\": \"hello\"\n" +
                                    "}";
                            //把要上传的数据转成byte数组
                            byte[] data = jsons.getBytes();
                            //首先判断目标存储路径文件夹存不存在
                            String serverHostUrl = mDataManager.getWebDevHost();
                            if (!sardine.exists(serverHostUrl + Constants.WEBDEV_BACKUP_PATH + "/")) {
                                //若不存在需要创建目录
                                sardine.createDirectory(serverHostUrl + Constants.WEBDEV_BACKUP_PATH + "/");
                            }
                            //存入数据
                            sardine.put(serverHostUrl + Constants.WEBDEV_BACKUP_PATH + "/backup.txt", data);
                            emitter.onNext(true);
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
                                mView.showMsg("成功");
                            }

                            @Override
                            protected void onErrorWithViewAlive(Throwable e) {
                                mView.hideDialogLoading();
                                mView.showMsg("登录失败");
                            }
                        }));
    }

    @Override
    public void recover() {

    }

    private Sardine getSardine() {
        Sardine sardine = new OkHttpSardine();
        String userName = mDataManager.getWebDevUserName();
        String password = mDataManager.getWebDevPassword();
        sardine.setCredentials(userName, password);
        return sardine;
    }
}
