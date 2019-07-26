package com.zyb.reader.adapter;

import android.graphics.Typeface;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zyb.common.db.bean.BookMarks;
import com.zyb.reader.Config;
import com.zyb.reader.R;
import com.zyb.reader.util.PageFactory;

import java.text.DecimalFormat;
import java.util.List;


/**
 * 书签
 */
public class MarkAdapter extends BaseQuickAdapter<BookMarks, BaseViewHolder> {
    private Typeface typeface;

    public MarkAdapter(@Nullable List<BookMarks> data) {
        super(R.layout.reader_item_bookmark, data);
        typeface = Config.getInstance().getTypeface();
    }

    @Override
    protected void convert(BaseViewHolder helper, BookMarks bean) {
        long begin = bean.getBegin();
        float fPercent = (float) (begin * 1.0 / PageFactory.getInstance().getBookLen());
        DecimalFormat df = new DecimalFormat("#0.0");
        String strPercent = df.format(fPercent * 100) + "%";

        helper.setText(R.id.text_mark, bean.getText())
                .setText(R.id.progress1, strPercent)
                .setText(R.id.mark_time, bean.getTime())
                .setTypeface(R.id.text_mark, typeface);
    }
}
