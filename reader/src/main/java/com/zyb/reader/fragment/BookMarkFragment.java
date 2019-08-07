package com.zyb.reader.fragment;

import android.app.Dialog;
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
import com.zyb.base.widget.dialog.MessageDialog;
import com.zyb.common.db.DBFactory;
import com.zyb.common.db.bean.BookMarks;
import com.zyb.common.db.bean.BookMarksDao;
import com.zyb.reader.R;
import com.zyb.reader.R2;
import com.zyb.reader.adapter.MarkAdapter;
import com.zyb.reader.util.PageFactory;

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
    private List<BookMarks> bookMarksList;
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
            showDialog(true, "是否删除书签？", "删除", "取消",
                    new MessageDialog.OnListener() {
                        @Override
                        public void onConfirm(Dialog dialog) {
                            DBFactory.getInstance().getBookMarksManage().delete(bookMarksList.get(position));
                            bookMarksList.clear();
                            bookMarksList.addAll(queryAllMarks());
                            markAdapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancel(Dialog dialog) { }
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
        bookMarksList = queryAllMarks();

        markAdapter = new MarkAdapter(bookMarksList);
        rvBookmark.setAdapter(markAdapter);
        rvBookmark.setLayoutManager(new LinearLayoutManager(mActivity));
        VerticalItemLineDecoration decoration = new VerticalItemLineDecoration.Builder(mActivity)
                .colorRes(R.color.reader_list_item_divider)
                .build();
        rvBookmark.addItemDecoration(decoration);
        markAdapter.setOnItemClickListener(onItemClickListener);
        markAdapter.setOnItemLongClickListener(onItemLongClickListener);

    }

    private List<BookMarks> queryAllMarks() {
        return DBFactory.getInstance().getBookMarksManage()
                .getQueryBuilder()
                .where(BookMarksDao.Properties.Bookpath.eq(bookpath))
                .list();
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

}
