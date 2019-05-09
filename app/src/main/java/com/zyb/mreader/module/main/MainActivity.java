package com.zyb.mreader.module.main;

import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.zyb.reader.db.entity.CollBookBean;
import com.zyb.reader.db.helper.CollBookHelper;
import com.zyb.reader.read.ReadActivity;
import com.zyb.reader.utils.Constant;
import com.zyb.reader.utils.StringUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.zyb.base.base.BaseDialog;
import com.zyb.base.base.activity.MVPActivity;
import com.zyb.base.di.component.AppComponent;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class MainActivity extends MVPActivity<MainPresenter> implements MainContract.View {


    @BindView(R.id.rv_books)
    RecyclerView rvBooks;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.smartRefresh)
    SmartRefreshLayout smartRefresh;

    private BooksAdapter booksAdapter;
    List<Book> books = new ArrayList<>();

    private RecyclerView.OnScrollListener onFlingListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int firstVisibleItemPosition = manager.findFirstVisibleItemPosition();
            if (firstVisibleItemPosition > 0) {
                MainActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏
                return;
            }

            //获取可视的第一个view
            View topView = manager.getChildAt(0);
            int lastOffset = 100;
            if (topView != null) {
                //获取与该view的顶部的偏移量
                lastOffset = topView.getTop();

                LogUtil.e("onScrollStateChanged " + lastOffset);
            }

            if (lastOffset <= 0) {
                MainActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏
            } else {
                MainActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏
            }
        }
    };
    private BaseQuickAdapter.OnItemClickListener onItemClickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            if (true) {
                File file = new File(books.get(position).getPath());
                CollBookBean collBook = convertCollBook(file);
                CollBookHelper.getsInstance().saveBook(collBook);
                Intent intent =new Intent(MainActivity.this, ReadActivity.class);
                intent.putExtra(ReadActivity.EXTRA_COLL_BOOK, collBook);
                intent.putExtra(ReadActivity.EXTRA_IS_COLLECTED, false);
                startActivity(intent);
                return;
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

    private BaseQuickAdapter.OnItemLongClickListener   onItemLongClickListener = new BaseQuickAdapter.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
            longClickBook=books.get(position);
            showRemoveDialog();
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
        return R.id.toolbar;
    }

    @Override
    protected void initView() {
        setSupportActionBar(toolbar);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_book:
                toAddBooks();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toAddBooks() {
        startActivityForResult(AddBookActivity.class, new ActivityCallback() {
            @Override
            public void onActivityResult(int resultCode, @Nullable Intent data) {
                booksAdapter.notifyDataSetChanged();
            }
        });
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
