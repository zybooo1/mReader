package com.zyb.mreader.module.addBook.path.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zyb.mreader.R;
import com.zyb.mreader.base.bean.BookFiles;
import com.zyb.mreader.module.addBook.file.BookFilesContract;
import com.zyb.mreader.module.addBook.path.BookPathContract;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PathAdapter extends BaseMultiItemQuickAdapter<BookFiles, BaseViewHolder> {
    public static final int ITEM_BOOK = 0;
    public static final int ITEM_PATH = 1;

    private BookPathContract.Presenter mPresenter;

    public PathAdapter(@Nullable List<BookFiles> data, BookPathContract.Presenter mPresenter) {
        super(data);
        addItemType(ITEM_BOOK, R.layout.item_file_book);
        addItemType(ITEM_PATH, R.layout.item_file_path);
        this.mPresenter = mPresenter;
    }

    @Override
    protected void convert(BaseViewHolder helper, BookFiles bean) {
        switch (helper.getItemViewType()) {
            case ITEM_BOOK:
                helper.setText(R.id.tv_title, bean.getTitle())
                        .setText(R.id.tv_size, bean.getSize())
                        .setChecked(R.id.cb_book, bean.getIsChecked())
                        .setGone(R.id.cb_book, !mPresenter.isBookAdded(bean))
                        .setGone(R.id.tv_exist_already, mPresenter.isBookAdded(bean));
                break;
            case ITEM_PATH:
                helper.setText(R.id.tv_title, bean.getTitle())
                        .setText(R.id.tv_size, bean.getSize());
                break;
        }

    }

    public List<File> getAllFiles() {
        List<File> files = new ArrayList<>();
        for (BookFiles mDatum : mData) {
            files.add(new File(mDatum.getPath()));
        }
        return files;
    }
}