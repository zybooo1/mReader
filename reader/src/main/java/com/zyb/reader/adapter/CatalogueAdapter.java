package com.zyb.reader.adapter;

import android.graphics.Typeface;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zyb.common.db.bean.BookCatalogue;
import com.zyb.reader.Config;
import com.zyb.reader.R;
import com.zyb.reader.util.PageFactory;

import java.text.DecimalFormat;
import java.util.List;


/**
 * 目录
 */
public class CatalogueAdapter extends BaseQuickAdapter<BookCatalogue, BaseViewHolder> {
    private Typeface typeface;
    private int currentPosition;

    public CatalogueAdapter(@Nullable List<BookCatalogue> data) {
        super(R.layout.reader_item_catalogue, data);
        typeface = Config.getInstance().getTypeface();
    }

    @Override
    protected void convert(BaseViewHolder helper, BookCatalogue bean) {
        long begin = bean.getBookCatalogueStartPos();
        float fPercent = (float) (begin * 1.0 / PageFactory.getInstance().getBookLen());
        DecimalFormat df = new DecimalFormat("#0.0");
        String strPercent = df.format(fPercent * 100) + "%";

        helper.setText(R.id.catalogue_tv, bean.getBookCatalogue())
                .setText(R.id.tv_percent, strPercent)
                .setTypeface(R.id.catalogue_tv, typeface);
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }
}
