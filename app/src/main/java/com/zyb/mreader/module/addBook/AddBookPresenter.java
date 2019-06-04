package com.zyb.mreader.module.addBook;


import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.mreader.core.AppDataManager;

import java.util.List;

import javax.inject.Inject;

/**
 *
 */

public class AddBookPresenter extends AbstractPresenter<AddBookContract.View, AppDataManager> implements AddBookContract.Presenter {

    @Inject
    AddBookPresenter(AppDataManager dataManager) {
        super(dataManager);
    }

}
