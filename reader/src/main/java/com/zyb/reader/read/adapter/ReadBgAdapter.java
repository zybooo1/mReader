package com.zyb.reader.read.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zyb.reader.R;
import com.zyb.reader.core.bean.ReadBgBean;

import java.util.List;

/**
 * 选择阅读背景
 */
public class ReadBgAdapter extends BaseQuickAdapter<ReadBgBean, BaseViewHolder> {

    public ReadBgAdapter(@Nullable List<ReadBgBean> data) {
        super(R.layout.reader_item_read_bg, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ReadBgBean item) {
        helper.setBackgroundRes(R.id.read_bg_view, item.getBgColor());
        helper.setVisible(R.id.read_bg_iv_checked, item.isSelect());
    }
}
