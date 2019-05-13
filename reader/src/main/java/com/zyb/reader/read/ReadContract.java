package com.zyb.reader.read;


import com.zyb.base.mvp.BasePresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.reader.core.bean.CollBookBean;

public interface ReadContract {
    interface View extends BaseView {
    }

    interface Presenter extends BasePresenter<View> {
        void saveRecord(CollBookBean book);
    }
}
