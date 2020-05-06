package com.zyb.mreader.module.backup;


import com.zyb.base.mvp.BasePresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.common.db.bean.Book;

import java.util.List;


public interface BackupContract {
    interface View extends BaseView {
        void loginSuccess();
    }

    interface Presenter extends BasePresenter<View> {
        String getWebDevHost();
        String getWebDevUserName();
        String getWebDevPassword();

        void login(String userName,String password,String host);
        void backup();
       void recover();
    }
}
