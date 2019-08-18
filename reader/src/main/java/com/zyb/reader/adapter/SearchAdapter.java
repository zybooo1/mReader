package com.zyb.reader.adapter;

import android.graphics.Typeface;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zyb.reader.Config;
import com.zyb.reader.R;
import com.zyb.reader.bean.SearchResultBean;
import com.zyb.reader.util.PageFactory;

import java.text.DecimalFormat;
import java.util.List;


/**
 * 搜索结果
 */
public class SearchAdapter extends BaseQuickAdapter<SearchResultBean, BaseViewHolder> {
    private Typeface typeface;

    public SearchAdapter(@Nullable List<SearchResultBean> data) {
        super(R.layout.reader_item_search_result, data);
        typeface = Config.getInstance().getTypeface();
    }

    @Override
    protected void convert(BaseViewHolder helper, SearchResultBean bean) {
        long begin = bean.getBegin();
        float fPercent = (float) (begin * 1.0 / PageFactory.getInstance().getBookLen());
        DecimalFormat df = new DecimalFormat("#0.0");
        String strPercent = df.format(fPercent * 100) + "%";

        helper.setText(R.id.text_mark, bean.getText())
                .setText(R.id.progress1, strPercent)
                .setTypeface(typeface, R.id.text_mark, R.id.progress1);
    }
}
