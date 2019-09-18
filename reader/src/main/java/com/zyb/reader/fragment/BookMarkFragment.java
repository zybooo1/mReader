package com.zyb.reader.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialog.util.BaseDialog;
import com.zyb.base.base.fragment.MyLazyFragment;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.utils.EventBusUtil;
import com.zyb.base.widget.decoration.VerticalItemLineDecoration;
import com.zyb.common.db.DBFactory;
import com.zyb.common.db.bean.BookMarks;
import com.zyb.common.db.bean.BookMarksDao;
import com.zyb.reader.R;
import com.zyb.reader.R2;
import com.zyb.reader.adapter.MarkAdapter;
import com.zyb.reader.util.PageFactory;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


/**
 * 书签
 */
public class BookMarkFragment extends MyLazyFragment {
    public static final String ARGUMENT = "argument";

    @BindView(R2.id.rvBookmark)
    RecyclerView rvBookmark;

    private String bookpath;
    private List<BookMarks> bookMarksList = new ArrayList<>();
    private MarkAdapter markAdapter;
    private PageFactory pageFactory;
    private BaseQuickAdapter.OnItemClickListener onItemClickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            pageFactory.changeChapter(bookMarksList.get(position).getBegin());
            EventBusUtil.sendEvent(new BaseEvent(EventConstants.EVENT_CLOSE_READ_DRAWER));
        }
    };
    private BaseQuickAdapter.OnItemLongClickListener onItemLongClickListener = new BaseQuickAdapter.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
            showDialog(true, "是否删除书签？", "删除", "取消", null,
                    (baseDialog, v) -> {
                        DBFactory.getInstance().getBookMarksManage().delete(bookMarksList.get(position));
                        queryAllMarks();
                        return false;
                    });
            return false;
        }
    };


    @Override
    protected int getLayoutId() {
        return R.layout.reader_fragment_bookmark;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        pageFactory = PageFactory.getInstance();
        Bundle bundle = getArguments();
        if (bundle != null) {
            bookpath = bundle.getString(ARGUMENT);
        }

        markAdapter = new MarkAdapter(bookMarksList);
        rvBookmark.setAdapter(markAdapter);
        rvBookmark.setLayoutManager(new LinearLayoutManager(mActivity));
        VerticalItemLineDecoration decoration = new VerticalItemLineDecoration.Builder(mActivity)
                .colorRes(R.color.reader_list_item_divider)
                .build();
        rvBookmark.addItemDecoration(decoration);
        markAdapter.setOnItemClickListener(onItemClickListener);
        markAdapter.setOnItemLongClickListener(onItemLongClickListener);

        queryAllMarks();
    }

    private void queryAllMarks() {
        bookMarksList.clear();
        bookMarksList.addAll(DBFactory.getInstance().getBookMarksManage()
                .getQueryBuilder()
                .where(BookMarksDao.Properties.Bookpath.eq(bookpath))
                .list());
        markAdapter.notifyDataSetChanged();
    }

    /**
     * 用于从Activity传递数据到Fragment
     *
     * @param bookpath
     * @return
     */
    public static BookMarkFragment newInstance(String bookpath) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT, bookpath);
        BookMarkFragment bookMarkFragment = new BookMarkFragment();
        bookMarkFragment.setArguments(bundle);
        return bookMarkFragment;
    }

    @Override
    protected boolean isRegisterEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventReceived(BaseEvent<Object> event) {
        if (event == null) return;
        switch (event.getCode()) {
            case EventConstants.EVENT_MARKS_REFRESH:
                queryAllMarks();
                break;
        }
    }

}
