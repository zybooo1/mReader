package com.zyb.reader.read;


import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.base.utils.TimeUtil;
import com.zyb.reader.core.ReadDataManager;
import com.zyb.reader.core.bean.CollBookBean;

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
}
