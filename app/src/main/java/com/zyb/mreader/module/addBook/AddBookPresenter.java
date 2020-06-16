package com.zyb.mreader.module.addBook;


import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.mreader.core.AppDataManager;

import javax.inject.Inject;

/**
 *
 */

public class AddBookPresenter extends AbstractPresenter<AddBookContract.View, AppDataManager> implements AddBookContract.Presenter {

    @Inject
    AddBookPresenter(AppDataManager dataManager) {
        super(dataManager);
    }
    @Override
    public void setIsFilterENfiles(boolean isFilterENfiles) {
        mDataManager.setIsFilterENfiles(isFilterENfiles);
    }

    @Override
    public void setFilterSize(long filterSize) {
        mDataManager.setFilterSize(filterSize);
    }

    @Override
    public boolean getIsFilterENfiles() {
        return mDataManager.getIsFilterENfiles();
    }

    @Override
    public long getFilterSize() {
        return mDataManager.getFilterSize();
    }
}
