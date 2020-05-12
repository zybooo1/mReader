package com.zyb.mreader.module.webdav;

import android.support.annotation.Nullable;
import android.util.SparseBooleanArray;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.zyb.mreader.R;

import java.util.List;

/**
 * 列表适配器
 */
public class WebdavBookAdapter extends BaseQuickAdapter<DavResource, BaseViewHolder> {
    public SparseBooleanArray getmSelectedPositions() {
        return mSelectedPositions;
    }

    public void setmSelectedPositions(SparseBooleanArray mSelectedPositions) {
        this.mSelectedPositions = mSelectedPositions;
    }

    SparseBooleanArray mSelectedPositions = new SparseBooleanArray();

    public WebdavBookAdapter(@Nullable List<DavResource> books) {
        super(R.layout.item_webdav_book, books);
        for (int i = 0; i < books.size(); i++) {
            setItemSelected(i, false);
        }
    }

    public void setItemSelected(int position, boolean isSelected) {
        mSelectedPositions.put(position, isSelected);
    }

    //根据位置判断条目是否选中
    public boolean isItemSelected(int position) {
        return mSelectedPositions.get(position);
    }

    private boolean canSelect;

    public boolean isCanSelect() {
        return canSelect;
    }

    /**
     * 进入或退出选择模式
     */
    public void setCanSelect(boolean canSelect) {
        this.canSelect = canSelect;

        for (int i = 0; i < mData.size(); i++) {
            setItemSelected(i, false);
        }

        notifyDataSetChanged();
    }

    /**
     * 若当前未全选则全选，反之取消全选
     */
    public void selectOrUnselectAll() {
        int unselectedCount = 0;
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            if (!isItemSelected(i)) unselectedCount++;
        }
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            setItemSelected(i, unselectedCount != 0);
        }
        notifyDataSetChanged();
    }

    public boolean isAllSelected() {
        int unselectedCount = 0;
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            if (!isItemSelected(i)) unselectedCount++;
        }
        return unselectedCount == 0;
    }

    @Override
    protected void convert(BaseViewHolder helper, DavResource book) {
        int position = helper.getLayoutPosition();
        helper.setText(R.id.tvTitle, book.getDisplayName())
                .setText(R.id.item_title, book.getDisplayName())
                .setText(R.id.tvInfo, book.getPath())
                .setChecked(R.id.cbSelect, isItemSelected(position))
                .setGone(R.id.cbSelect, isCanSelect())
                .setGone(R.id.tvDownloaded, !isCanSelect()&&false)
                .setGone(R.id.btnDownload, !isCanSelect()&&true)
                .addOnClickListener(R.id.tvDownloaded);
    }

}
