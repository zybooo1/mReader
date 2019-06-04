package com.zyb.reader.read;


import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.common.db.bean.CollBookBean;
import com.zyb.reader.core.ReadDataManager;

import javax.inject.Inject;

/**
 */
public class ReadPresenter extends AbstractPresenter<ReadContract.View, ReadDataManager> implements ReadContract.Presenter {

    @Inject
    ReadPresenter(ReadDataManager dataManager) {
        super(dataManager);
    }

    @Override
    public void saveRecord(CollBookBean book) {
        mDataManager.saveBook(book);
    }

    @Override
    public int getBrightness() {
        return mDataManager.getBrightness();
    }

    @Override
    public boolean isBrightnessAuto() {
        return mDataManager.isBrightnessAuto();
    }

    @Override
    public boolean isNightMode() {
        return mDataManager.isNightMode();
    }
}
