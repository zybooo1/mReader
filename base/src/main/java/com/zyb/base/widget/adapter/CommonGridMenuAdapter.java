package com.zyb.base.widget.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zyb.base.R;

import java.util.List;

/**
 * 通用网格类菜单（带红点数字tip）
 */
public class CommonGridMenuAdapter extends BaseQuickAdapter<MenuItem, BaseViewHolder> {


    public CommonGridMenuAdapter(@Nullable List<MenuItem> data) {
        super(R.layout.common_item_menu_grid, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, MenuItem item) {
        helper.setText(R.id.mine_menu_name, item.getName())
                .setImageResource(R.id.mine_menu_icon, item.getImgId())
                .setVisible(R.id.tvTip, item.getName().equals("证书") || item.getName().equals("学习分析"));
    }
}