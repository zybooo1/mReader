package com.zyb.mreader.module.addBook.file;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.zyb.base.base.fragment.MVPFragment;
import com.zyb.base.di.component.AppComponent;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.utils.EventBusUtil;
import com.zyb.base.widget.decoration.VerticalItemLineDecoration;
import com.zyb.common.db.bean.BookFiles;
import com.zyb.mreader.R;
import com.zyb.mreader.di.component.DaggerFragmentComponent;
import com.zyb.mreader.di.module.ApiModule;
import com.zyb.mreader.di.module.FragmentModule;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class BookFilesFragment extends MVPFragment<BookFilesPresenter> implements BookFilesContract.View {

    private long currentFilterSize;
    private boolean isFilterENfile;

    @BindView(R.id.rv_books)
    RecyclerView rvBooks;
    public List<BookFiles> bookList = new ArrayList<>();
    private FilesAdapter booksAdapter;
    private BaseQuickAdapter.OnItemChildClickListener onItemChildClickListener = new BaseQuickAdapter.OnItemChildClickListener() {
        @Override
        public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
            if (view.getId() == R.id.tvAdd) {
                BookFiles book = bookList.get(position);
                if (!mPresenter.isBookAdded(book)) {
                    mPresenter.addBook(book.toBook());
                    booksAdapter.notifyItemChanged(position);
                }
            }

        }
    };
    private RecyclerView.OnScrollListener onFlingListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
//            LogUtil.e("onScrolled dy:" + dy);
            if (dy <= 0) {
                EventBusUtil.sendEvent(new BaseEvent(EventConstants.EVENT_SHOW_STATUS_BAR));
            } else {
                EventBusUtil.sendEvent(new BaseEvent(EventConstants.EVENT_HIDE_STATUS_BAR));
            }
        }

    };

    public static BookFilesFragment newInstance() {
        BookFilesFragment fragment = new BookFilesFragment();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_book_files;
    }

    @Override
    protected void initView() {
        booksAdapter = new FilesAdapter(bookList, mPresenter);
        booksAdapter.setOnItemChildClickListener(onItemChildClickListener);
        rvBooks.setLayoutManager(new LinearLayoutManager(getFragmentActivity()));
        rvBooks.addItemDecoration(new VerticalItemLineDecoration(getFragmentActivity()));
        rvBooks.setAdapter(booksAdapter);
        rvBooks.addOnScrollListener(onFlingListener);

        if (mPresenter.isBookFilesCached()) {
            onBookFilesLoaded(mPresenter.getAllBookFiles());
        } else {
            loadDatas();
        }
    }

    @Override
    protected void initData() {

    }

    private void loadDatas() {
        currentFilterSize = mPresenter.getFilterSize();
        isFilterENfile = mPresenter.getIsFilterENfiles();
        mPresenter.scanFiles(currentFilterSize, isFilterENfile);
    }

    @Override
    protected void setupFragmentComponent(AppComponent appComponent) {
        DaggerFragmentComponent.builder()
                .appComponent(appComponent)
                .fragmentModule(new FragmentModule(this))
                .apiModule(new ApiModule())
                .build()
                .inject(this);
    }

    @Override
    public void onBookFilesLoaded(List<BookFiles> books) {
        bookList.clear();
        bookList.addAll(books);
        booksAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.btnRefresh)
    public void click(View v) {
        loadDatas();
    }

    @Override
    protected void onPageRetry(View v) {
        super.onPageRetry(v);
        loadDatas();
    }

    @Override
    protected boolean isRegisterEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventReceived(BaseEvent<Object> event) {
        if (event == null) return;
        switch (event.getCode()) {
            case EventConstants.RESEARCH_BOOK:
                loadDatas();
                break;
        }
    }
}
