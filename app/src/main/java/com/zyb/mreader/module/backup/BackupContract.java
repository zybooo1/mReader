package com.zyb.mreader.module.backup;


import com.zyb.base.mvp.BasePresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.common.db.bean.Book;

import java.util.List;


public interface BackupContract {
    interface View extends BaseView {
    }

    interface Presenter extends BasePresenter<View> {
        String getWebDavHost();
        String getWebDavUserName();
        String getWebDavPassword();

        void backup();
       void recover();
    }
}