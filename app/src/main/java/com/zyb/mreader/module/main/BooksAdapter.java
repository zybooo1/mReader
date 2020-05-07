package com.zyb.mreader.module.main;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zyb.common.db.bean.Book;
import com.zyb.mreader.R;

import java.io.File;
import java.util.List;

/**
 * 列表适配器
 */
public class BooksAdapter extends BaseQuickAdapter<Book, BaseViewHolder> {

    private boolean canSelect;

    public BooksAdapter(@Nullable List<Book> datas) {
        super(R.layout.item_book, datas);
    }

    public boolean isCanSelect() {
        return canSelect;
    }

    /**
     * 进入或退出选择模式
     */
    public void setCanSelect(boolean canSelect) {
        this.canSelect = canSelect;

        for (Book mDatum : mData) {
            if (!canSelect) mDatum.setSelected(false);
        }
        notifyDataSetChanged();
    }

    /**
     * 若当前未全选则全选，反之取消全选
     */
    public void selectOrUnselectAll() {
        int unselectedCount = 0;
        for (Book mDatum : mData) {
            if (!mDatum.isSelected()) unselectedCount++;
        }
        for (Book mDatum : mData) {
            //未全选就全选
            mDatum.setSelected(unselectedCount != 0);
        }
        notifyDataSetChanged();
    }


    @Override
    protected void convert(BaseViewHolder helper, Book book) {
        helper.setText(R.id.item_title, book.getTitle())
                .setText(R.id.tvReadProgress, book.getProgress())
                .setTextColor(R.id.item_title, new File(book.getPath()).exists() ?
                        ContextCompat.getColor(mContext,R.color.gray60) : ContextCompat.getColor(mContext,R.color.gray20))
                .setGone(R.id.btnSelected, book.isSelected() && canSelect)
                .setGone(R.id.btnUnselected, canSelect)
                .setGone(R.id.tvReadProgress, !book.getProgress().isEmpty() && !canSelect)
                .setGone(R.id.progressLeft, !book.getProgress().isEmpty() && !canSelect)
                .setGone(R.id.progressRight, !book.getProgress().isEmpty() && !canSelect);
    }
}
