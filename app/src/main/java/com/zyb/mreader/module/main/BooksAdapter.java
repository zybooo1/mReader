package com.zyb.mreader.module.main;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zyb.mreader.R;
import com.zyb.mreader.base.bean.Book;

import java.util.List;

/**
 * 列表适配器
 */
public class BooksAdapter extends BaseQuickAdapter<Book, BaseViewHolder> {

    public BooksAdapter(@Nullable List<Book> datas) {
        super(R.layout.item_book, datas);
    }


    @Override
    protected void convert(BaseViewHolder helper, Book item) {
        int position = helper.getLayoutPosition();
        helper.setText(R.id.item_title, item.getTitle())
                .setGone(R.id.ivAdd, position == mData.size()-1);
    }
}
