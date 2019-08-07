package com.zyb.reader.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.zyb.base.base.fragment.MyLazyFragment;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.utils.EventBusUtil;
import com.zyb.base.widget.decoration.VerticalItemLineDecoration;
import com.zyb.common.db.bean.BookCatalogue;
import com.zyb.reader.R;
import com.zyb.reader.R2;
import com.zyb.reader.adapter.CatalogueAdapter;
import com.zyb.reader.util.PageFactory;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;


/**
 * 目录
 */
public class CatalogFragment extends MyLazyFragment {
    public static final String ARGUMENT = "argument";

    private PageFactory pageFactory;
    ArrayList<BookCatalogue> catalogueList = new ArrayList<>();

    @BindView(R2.id.rvCatalogue)
    RecyclerView rvCatalogue;
    private BaseQuickAdapter.OnItemClickListener onItemClickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            pageFactory.changeChapter(catalogueList.get(position).getBookCatalogueStartPos());
            EventBusUtil.sendEvent(new BaseEvent(EventConstants.EVENT_CLOSE_READ_DRAWER));
        }
    };
    private CatalogueAdapter catalogueAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.reader_fragment_catalog;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        pageFactory = PageFactory.getInstance();
        catalogueAdapter = new CatalogueAdapter(catalogueList);
        rvCatalogue.setAdapter(catalogueAdapter);
        rvCatalogue.setLayoutManager(new LinearLayoutManager(mActivity));
        VerticalItemLineDecoration decoration= new VerticalItemLineDecoration.Builder(mActivity)
                .colorRes(R.color.reader_list_item_divider)
                .build();
        rvCatalogue.addItemDecoration(decoration);
        catalogueAdapter.setOnItemClickListener(onItemClickListener);
    }

    public static CatalogFragment newInstance(String bookpath) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT, bookpath);
        CatalogFragment catalogFragment = new CatalogFragment();
        catalogFragment.setArguments(bundle);
        return catalogFragment;
    }


    @Override
    protected boolean isRegisterEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventReceived(BaseEvent<Object> event) {
        if (event == null) return;
        switch (event.getCode()) {
            case EventConstants.EVENT_ON_CATALOGS_LOADED:
                catalogueList.addAll(pageFactory.getDirectoryList());
                catalogueAdapter.notifyDataSetChanged();
                catalogueAdapter.setCurrentPosition(pageFactory.getCurrentCharter());
                break;
        }
    }
}
