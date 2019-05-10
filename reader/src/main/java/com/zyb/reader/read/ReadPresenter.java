package com.zyb.reader.read;


import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.reader.core.ReadDataManager;

import javax.inject.Inject;

/**
 */
public class ReadPresenter extends AbstractPresenter<ReadContract.View, ReadDataManager> implements ReadContract.Presenter {

    @Inject
    ReadPresenter(ReadDataManager dataManager) {
        super(dataManager);
    }

}
