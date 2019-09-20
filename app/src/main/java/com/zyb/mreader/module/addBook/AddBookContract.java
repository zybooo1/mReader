package com.zyb.mreader.module.addBook;


import android.widget.ArrayAdapter;

import com.zyb.base.mvp.BasePresenter;
import com.zyb.base.mvp.BaseView;

import java.util.List;


public interface AddBookContract {
    interface View extends BaseView {
    }

    interface Presenter extends BasePresenter<View> {

        void setIsFilterENfiles(boolean isFilterENfiles);
        void setFilterSize(long filterSize);
        boolean getIsFilterENfiles( );
        long getFilterSize( );
    }
}
