package com.zyb.mreader.module.backup.login;


import com.zyb.base.mvp.BasePresenter;
import com.zyb.base.mvp.BaseView;


public interface LoginContract {
    interface View extends BaseView {
        void loginSuccess();
    }

    interface Presenter extends BasePresenter<View> {
        String getWebDavHost();
        String getWebDavUserName();
        String getWebDavPassword();

        void login(String userName, String password, String host);
    }
}
