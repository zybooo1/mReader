package com.zyb.mreader.module.addBook.path.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zyb.common.db.bean.BookFiles;
import com.zyb.mreader.R;
import com.zyb.mreader.module.addBook.path.BookPathContract;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.zyb.common.db.bean.BookFiles.ITEM_BOOK;
import static com.zyb.common.db.bean.BookFiles.ITEM_PATH;

public class PathAdapter extends BaseMultiItemQuickAdapter<BookFiles, BaseViewHolder> {


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
                        .setGone(R.id.tvAdd, !mPresenter.isBookAdded(bean))
                        .setGone(R.id.tv_exist_already, mPresenter.isBookAdded(bean))
                .addOnClickListener(R.id.tvAdd);
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
