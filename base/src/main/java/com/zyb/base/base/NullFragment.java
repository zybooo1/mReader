package com.zyb.base.base;


import com.zyb.base.R;
import com.zyb.base.base.fragment.BaseLazyFragment;

/**
 * @author Rabtman
 */
public class NullFragment extends BaseLazyFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_null;
    }

    @Override
    protected int getTitleBarId() {
        return 0;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }

}
