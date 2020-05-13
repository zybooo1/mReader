package com.zyb.mreader.module.webdav.bookSelect;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.zyb.common.db.bean.Book;
import com.zyb.mreader.R;
import com.zyb.mreader.utils.FileUtils;
import com.zyb.mreader.utils.WevdavUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 列表适配器
 */
public class BookSelectAdapter extends BaseQuickAdapter<Book, BaseViewHolder> {
    List<DavResource> davResources;

    public BookSelectAdapter(@Nullable List<Book> books, List<DavResource> davResources) {
        super(R.layout.item_book_select, books);
        this.davResources = davResources;
    }

    @Override
    protected void convert(BaseViewHolder helper, Book book) {
        String size = "-";
        File file = new File(book.getPath());
        if (file.exists()) {
            size = FileUtils.getFileSize(file.length());
        }

        boolean fileUploaded = WevdavUtils.isFileUploaded(book.getTitle(), davResources);
        helper.setText(R.id.tv_title, book.getTitle())
                .setText(R.id.tv_size, size)
                .setChecked(R.id.cbSelect, book.isSelected())
                .setGone(R.id.tvUploaded, fileUploaded)
                .setGone(R.id.cbSelect, !fileUploaded);
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

    public ArrayList<Book> getSelectedBooks() {
        ArrayList<Book> bookList = new ArrayList<>();
        for (Book mDatum : mData) {
            if (mDatum.isSelected()) bookList.add(mDatum);
        }
        return bookList;
    }
}
