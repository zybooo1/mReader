package com.zyb.mreader.module.addBook;


import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.mreader.base.bean.Book;
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


    @Override
    public void addBooks(List<Book> books) {
        if(books.size()<=0){
            mView.showError("请选择书籍");
            return;
        }
        mDataManager.addBooks(books);
        mView.showSuccess("添加成功");
        mView.onBooksAdded();
    }
}
