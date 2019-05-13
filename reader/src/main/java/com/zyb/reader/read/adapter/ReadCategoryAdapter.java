package com.zyb.reader.read.adapter;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xw.repo.VectorCompatTextView;
import com.zyb.reader.R;
import com.zyb.reader.widget.page.TxtChapter;

import java.util.List;

/**
 *
 */
public class ReadCategoryAdapter extends BaseQuickAdapter<TxtChapter, BaseViewHolder> {


    public ReadCategoryAdapter(@Nullable List<TxtChapter> data) {
        super(R.layout.reader_item_category, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, TxtChapter item) {
        VectorCompatTextView textView = helper.getView(R.id.category_tv_chapter);
        textView.setText(item.getTitle().trim());
        if (item.isSelect()) {
            textView.setDrawableCompatColor(ContextCompat.getColor(mContext, R.color.green));
        } else {
            textView.setDrawableCompatColor(ContextCompat.getColor(mContext, R.color.gray40));
        }

    }
}
