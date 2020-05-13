package com.zyb.mreader.module.webdav;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.gyf.barlibrary.ImmersionBar;
import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialog.util.BaseDialog;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.zyb.base.base.activity.MVPActivity;
import com.zyb.base.di.component.AppComponent;
import com.zyb.base.utils.QMUIViewHelper;
import com.zyb.base.widget.decoration.VerticalItemLineDecoration;
import com.zyb.common.db.bean.Book;
import com.zyb.mreader.R;
import com.zyb.mreader.di.component.DaggerActivityComponent;
import com.zyb.mreader.di.module.ActivityModule;
import com.zyb.mreader.di.module.ApiModule;
import com.zyb.mreader.module.webdav.bookSelect.BookSelectActivity;
import com.zyb.mreader.module.webdav.login.LoginActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 *
 */
public class WebdavActivity extends MVPActivity<WebdavPresenter> implements
        WebdavContract.View {

    @BindView(R.id.layoutLogin)
    View layoutLogin;
    @BindView(R.id.btnSelectBook)
    FloatingActionButton btnSelectBook;
    @BindView(R.id.layoutEmpty)
    View layoutEmpty;

    @BindView(R.id.smartRefresh)
    SmartRefreshLayout smartRefresh;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private WebdavBookAdapter booksAdapter;
    List<DavResource> booksList = new ArrayList<>();
    private OnRefreshListener onRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh(@NonNull RefreshLayout refreshLayout) {
            mPresenter.getWebDavBooks();
        }
    };
    private BaseQuickAdapter.OnItemChildClickListener onChildClick = new BaseQuickAdapter.OnItemChildClickListener() {
        @Override
        public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
            mPresenter.download(booksList.get(position), position);
        }
    };
    BaseQuickAdapter.OnItemClickListener onItemClick = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            if (booksAdapter.isCanSelect()) {
                booksAdapter.setItemSelected(position, !booksAdapter.isItemSelected(position));
                booksAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_webdav;
    }

    @Override
    protected int getTitleBarId() {
        return R.id.titleBar;
    }

    @Override
    protected void initView() {
        ImmersionBar.setTitleBar(this, layoutActionTop);
        layoutActionTop.setOnTitleBarListener(onTopActionBarListener);

        booksAdapter = new WebdavBookAdapter(booksList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new VerticalItemLineDecoration(this));
        recyclerView.setAdapter(booksAdapter);
        booksAdapter.bindToRecyclerView(recyclerView);
        booksAdapter.setOnItemChildClickListener(onChildClick);
        booksAdapter.setOnItemClickListener(onItemClick);
        booksAdapter.setOnItemLongClickListener(onItemLongClick);
        smartRefresh.setOnRefreshListener(onRefreshListener);

        if (mPresenter.haveWebdavAccount()) {
            mPresenter.getWebDavBooks();
        }
        refreshView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == LoginActivity.RESULT_SUCCESS) {
            refreshView();
            mPresenter.getWebDavBooks();
        }
        if (resultCode == BookSelectActivity.RESULT_SUCCESS) {
            mPresenter.upload((List<Book>) data.getSerializableExtra(BookSelectActivity.RESULT_FLAG));
        }
    }

    private void refreshView() {
        if (mPresenter.haveWebdavAccount()) {
            layoutLogin.setVisibility(View.GONE);
        } else {
            layoutLogin.setVisibility(View.VISIBLE);
        }
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
    public void onBackPressed() {
        if (layoutActionTop.getVisibility() == View.VISIBLE) {
            exitEditMode();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onRightClick(View v) {
        super.onRightClick(v);
        tvLogin();
    }

    @OnClick(R.id.tvLogin)
    void tvLogin() {
        startActivityForResult(new Intent(this, LoginActivity.class), 117);
    }

    @OnClick({R.id.btnSelectBook,R.id.tvAdd})
    void btnSelectBook() {
        startActivityForResult(new Intent(this, BookSelectActivity.class), 118);
    }

    @Override
    public void onBooksLoaded(List<DavResource> books) {
        booksList.clear();
        booksList.addAll(books);
        booksAdapter.notifyDataSetChanged();

        smartRefresh.finishRefresh();

        layoutEmpty.setVisibility(booksList.size() <= 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBookDownloaded(int position) {
        booksAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBookDeleted() {
        mPresenter.getWebDavBooks();
    }

    //==============编辑模式Start================
    @BindView(R.id.layoutActionTop)
    TitleBar layoutActionTop;
    @BindView(R.id.layoutActionBottom)
    ConstraintLayout layoutActionBottom;
    private static final int ANIM_SHOW_DURATION = 400;
    private static final int ANIM_HIDE_DURATION = 300;
    BaseQuickAdapter.OnItemLongClickListener onItemLongClick = new BaseQuickAdapter.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
            enterEditMode();
            booksAdapter.setItemSelected(position, true);
            return false;
        }
    };

    OnTitleBarListener onTopActionBarListener = new OnTitleBarListener() {
        @Override
        public void onLeftClick(View v) {
            exitEditMode();
        }

        @Override
        public void onTitleClick(View v) {
        }

        @Override
        public void onRightClick(View v) {
            layoutActionTop.setRightTitle(layoutActionTop.getRightTitle().equals("全选") ? "取消" : "全选");
            booksAdapter.selectOrUnselectAll();
        }
    };

    /**
     * 进入编辑模式
     */
    private void enterEditMode() {
        booksAdapter.setCanSelect(true);
        toggleActionBar(true);
        getStatusBarConfig().statusBarDarkFont(false).init();

    }

    /**
     * 退出编辑模式
     */
    private void exitEditMode() {
        booksAdapter.setCanSelect(false);
        toggleActionBar(false);
        getStatusBarConfig().statusBarDarkFont(true).init();
    }

    /**
     * 切换书本操作菜单栏可视状态
     */
    private void toggleActionBar(boolean isShown) {
        if (isShown) {
            if (layoutActionTop.getVisibility() == View.GONE)
                QMUIViewHelper.slideIn(layoutActionTop, ANIM_SHOW_DURATION, QMUIViewHelper.QMUIDirection.TOP_TO_BOTTOM);
            if (layoutActionBottom.getVisibility() == View.GONE)
                QMUIViewHelper.slideIn(layoutActionBottom, ANIM_SHOW_DURATION, QMUIViewHelper.QMUIDirection.BOTTOM_TO_TOP);
            if (btnSelectBook.getVisibility() == View.VISIBLE)
                QMUIViewHelper.slideOut(btnSelectBook, ANIM_HIDE_DURATION, QMUIViewHelper.QMUIDirection.TOP_TO_BOTTOM);
            return;
        }
        if (layoutActionTop.getVisibility() == View.VISIBLE)
            QMUIViewHelper.slideOut(layoutActionTop, ANIM_HIDE_DURATION, QMUIViewHelper.QMUIDirection.BOTTOM_TO_TOP);
        if (layoutActionBottom.getVisibility() == View.VISIBLE)
            QMUIViewHelper.slideOut(layoutActionBottom, ANIM_HIDE_DURATION, QMUIViewHelper.QMUIDirection.TOP_TO_BOTTOM);
        if (btnSelectBook.getVisibility() == View.GONE)
            QMUIViewHelper.slideIn(btnSelectBook, ANIM_SHOW_DURATION, QMUIViewHelper.QMUIDirection.BOTTOM_TO_TOP);
    }

    @OnClick(R.id.btnDelete)
    public void btnDelete() {
        List<DavResource> deleteBooks = new ArrayList<>();
        for (int i = 0; i < booksAdapter.getmSelectedPositions().size(); i++) {
            if (booksAdapter.isItemSelected(i)) deleteBooks.add(booksList.get(i));
        }
        if (deleteBooks.size() <= 0) {
            showToast("请选择书本");
            return;
        }
        showDialog(true, "是否删除这些书本？", "取消", "移除",
                new OnDialogButtonClickListener() {
                    @Override
                    public boolean onClick(BaseDialog baseDialog, View v) {
                        mPresenter.delete(deleteBooks);
                        exitEditMode();
                        return false;
                    }
                }, null);
    }
    //==============编辑模式End================

}
