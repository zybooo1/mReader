package com.zyb.mreader.module.addBook.file.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zyb.common.db.bean.BookFiles;
import com.zyb.mreader.R;
import com.zyb.mreader.module.addBook.file.BookFilesContract;

import java.util.List;

/**
 * 列表适配器
 */
public class FilesAdapter extends BaseQuickAdapter<BookFiles, BaseViewHolder> {
    private BookFilesContract.Presenter mPresenter;


    public FilesAdapter(@Nullable List<BookFiles> datas, BookFilesContract.Presenter mPresenter) {
        super(R.layout.item_file_book, datas);
        this.mPresenter = mPresenter;
    }

    @Override
    protected void convert(BaseViewHolder helper, BookFiles bookFiles) {
        helper.setText(R.id.tv_title, bookFiles.getTitle())
                .setText(R.id.tv_size, bookFiles.getSize())
                .setGone(R.id.ivAdd, !mPresenter.isBookAdded(bookFiles))
                .setGone(R.id.tv_exist_already, mPresenter.isBookAdded(bookFiles))
                .addOnClickListener(R.id.ivAdd);
    }

}
