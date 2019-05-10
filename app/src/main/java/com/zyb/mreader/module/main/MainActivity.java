package com.zyb.mreader.module.main;

import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.zyb.base.base.BaseDialog;
import com.zyb.base.base.activity.MVPActivity;
import com.zyb.base.di.component.AppComponent;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.utils.CommonUtils;
import com.zyb.base.utils.LogUtil;
import com.zyb.base.widget.decoration.GridItemSpaceDecoration;
import com.zyb.base.widget.dialog.MenuDialog;
import com.zyb.mreader.R;
import com.zyb.mreader.base.bean.Book;
import com.zyb.mreader.di.component.DaggerActivityComponent;
import com.zyb.mreader.di.module.ActivityModule;
import com.zyb.mreader.di.module.ApiModule;
import com.zyb.mreader.module.addBook.AddBookActivity;
import com.zyb.reader.db.entity.CollBookBean;
import com.zyb.reader.db.helper.CollBookHelper;
import com.zyb.reader.read.ReadActivity;
import com.zyb.reader.utils.Constant;
import com.zyb.reader.utils.StringUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends MVPActivity<MainPresenter> implements MainContract.View {

    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.rv_books)
    RecyclerView rvBooks;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.smartRefresh)
    SmartRefreshLayout smartRefresh;

    private BooksAdapter booksAdapter;
    List<Book> books = new ArrayList<>();

    private RecyclerView.OnScrollListener onFlingListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            LogUtil.e("onScrolled dy:" + dy);
            if (dy <= 0) {
                MainActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏
            } else {
                MainActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏
            }
        }
    };
    private BaseQuickAdapter.OnItemClickListener onItemClickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            if (position < books.size()-1) {
                File file = new File(books.get(position).getPath());
                CollBookBean collBook = convertCollBook(file);
                CollBookHelper.getsInstance().saveBook(collBook);
                Intent intent = new Intent(MainActivity.this, ReadActivity.class);
                intent.putExtra(ReadActivity.EXTRA_COLL_BOOK, collBook);
                intent.putExtra(ReadActivity.EXTRA_IS_COLLECTED, false);
                startActivity(intent);
            }else {
                toAddBook();
            }
        }
    };

    /**
     * 将文件转换成CollBook
     */
    private CollBookBean convertCollBook(File file) {
        //判断文件是否存在
        if (!file.exists()) return null;

        CollBookBean collBook = new CollBookBean();
        collBook.setLocal(true);
        collBook.set_id(file.getAbsolutePath());
        collBook.setTitle(file.getName().replace(".txt", ""));
        collBook.setLastChapter("开始阅读");
        collBook.setLastRead(StringUtils.
                dateConvert(System.currentTimeMillis(), Constant.FORMAT_BOOK_DATE));
        return collBook;
    }

    private BaseQuickAdapter.OnItemLongClickListener onItemLongClickListener = new BaseQuickAdapter.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
            if (position < books.size()-1) {
                longClickBook = books.get(position);
                showRemoveDialog();
            }
            return true;
        }
    };
    private OnRefreshListener onRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh(@NonNull RefreshLayout refreshLayout) {
            refreshBooks();
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getTitleBarId() {
        return R.id.titleBar;
    }

    @Override
    protected boolean isRegisterEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventReceived(BaseEvent<Object> event) {
        if (event == null) return;
        switch (event.getCode()) {
            case EventConstants.EVENT_MAIN_REFRESH_BOOK_SHELF:
                refreshBooks();
                break;
        }
    }

    @Override
    protected void initView() {
        booksAdapter = new BooksAdapter(books);
        booksAdapter.setOnItemClickListener(onItemClickListener);
        booksAdapter.setOnItemLongClickListener(onItemLongClickListener);
        rvBooks.setLayoutManager(new GridLayoutManager(this, 3));
        int space = CommonUtils.dp2px(20);
        rvBooks.addItemDecoration(new GridItemSpaceDecoration(3, space, true, 0));
        rvBooks.setAdapter(booksAdapter);

        rvBooks.addOnScrollListener(onFlingListener);

        smartRefresh.setOnRefreshListener(onRefreshListener);
    }

    @Override
    protected void initData() {
        smartRefresh.autoRefresh();
    }

    private void refreshBooks() {
        mPresenter.getBooks();
    }

    @Override
    public void onLeftClick(View v) {
        drawerLayout.openDrawer(Gravity.START);
    }

    @Override
    protected void setupActivityComponent(AppComponent appComponent) {
        DaggerActivityComponent.builder()
                .appComponent(appComponent)
                .apiModule(new ApiModule())
                .activityModule(new ActivityModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onBooksLoaded(List<Book> books) {
        this.books.clear();
        this.books.addAll(books);
        booksAdapter.notifyDataSetChanged();

        smartRefresh.finishRefresh();
    }

    @OnClick(R.id.addBook)
    public void addBookClick(View view) {
        drawerLayout.closeDrawers();
        switch (view.getId()) {
            case R.id.addBook:
                mPresenter.drawerAction(MainContract.DRAWER_ACTION.TO_ADD_BOOK);
                break;
        }
    }

    @Override
    public void toAddBook() {
        startActivity(AddBookActivity.class);
    }

    BaseDialog removeDialog;
    Book longClickBook;
    MenuDialog.OnListener removeListener = new MenuDialog.OnListener() {

        @Override
        public void onSelected(Dialog dialog, int position, String text) {
            mPresenter.removeBook(longClickBook);
            refreshBooks();
        }

        @Override
        public void onCancel(Dialog dialog) {

        }
    };

    private void showRemoveDialog() {
        if (removeDialog == null) {
            List<String> strings = new ArrayList<>();
            strings.add("移除");
            removeDialog = new MenuDialog.Builder(this)
                    .setCancel("取消") // 设置 null 表示不显示取消按钮
                    //.setAutoDismiss(false) // 设置点击按钮后不关闭对话框
                    .setList(strings)
                    .setListener(removeListener)
                    .setGravity(Gravity.BOTTOM)
                    .setAnimStyle(BaseDialog.AnimStyle.BOTTOM)
                    .create();

        }
        removeDialog.show();
    }

    private void hideRemoveLawDialog() {
        if (removeDialog != null) removeDialog.dismiss();
    }

}
