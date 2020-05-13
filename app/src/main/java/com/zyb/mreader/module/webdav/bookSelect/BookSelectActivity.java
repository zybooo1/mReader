package com.zyb.mreader.module.webdav.bookSelect;


import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.zyb.base.base.activity.MVPActivity;
import com.zyb.base.di.component.AppComponent;
import com.zyb.base.widget.decoration.VerticalItemLineDecoration;
import com.zyb.common.db.bean.Book;
import com.zyb.mreader.R;
import com.zyb.mreader.di.component.DaggerActivityComponent;
import com.zyb.mreader.di.module.ActivityModule;
import com.zyb.mreader.di.module.ApiModule;
import com.zyb.mreader.utils.WevdavUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 *
 */
public class BookSelectActivity extends MVPActivity<BookSelectPresenter> implements
        BookSelectContract.View {
    public static final int RESULT_SUCCESS = 0x587;
    public static final String RESULT_FLAG = "result_flag_select";
    List<DavResource> davResources = new ArrayList<>();

    @BindView(R.id.layoutEmpty)
    View layoutEmpty;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private BookSelectAdapter booksAdapter;
    List<Book> booksList = new ArrayList<>();
    private BaseQuickAdapter.OnItemClickListener onItemClick = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            Book book = booksList.get(position);
            book.setSelected(!book.isSelected());
            booksAdapter.notifyItemChanged(position);
            int unselectedCount = 0;
            for (Book book1 : booksList) {
                if (!book.isSelected()) unselectedCount++;
            }
            getTitleBar().setRightTitle(unselectedCount == 0 ? "取消" : "全选");
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_book_select;
    }

    @Override
    protected int getTitleBarId() {
        return R.id.titleBar;
    }

    @Override
    protected void initView() {
        mPresenter.getWebDavBooks();
    }

    @Override
    public void onRightClick(View v) {
        getTitleBar().setRightTitle(getTitleBar().getRightTitle().equals("全选") ? "取消" : "全选");
        booksAdapter.selectOrUnselectAll();
    }

    @OnClick(R.id.btnCommit)
    public void btnCommit() {
        ArrayList<Book> selectedBooks = booksAdapter.getSelectedBooks();
        Iterator<Book> iterator = selectedBooks.iterator();
        while (iterator.hasNext()) {
            Book b = iterator.next();
            if (WevdavUtils.isFileUploaded(b.getTitle(), davResources))
                iterator.remove();
        }
        if (selectedBooks.size() == 0) {
            showToast("请选择书籍");
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(RESULT_FLAG, selectedBooks);
        setResult(RESULT_SUCCESS, intent);
        finish();
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
    public void onBooksLoaded(List<DavResource> davResources) {
        this.davResources.addAll(davResources);

        List<Book> books = mPresenter.getBooks();
        Iterator<Book> it = books.iterator();
        while (it.hasNext()) {
            Book book = it.next();
            if (book.getTitle().equals("欢迎使用") || !new File(book.getPath()).exists()) {
                it.remove();
            }
        }
        booksList.addAll(books);
        layoutEmpty.setVisibility(booksList.size()<=0?View.VISIBLE:View.GONE);

        booksAdapter = new BookSelectAdapter(booksList, this.davResources);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new VerticalItemLineDecoration(this));
        recyclerView.setAdapter(booksAdapter);
        booksAdapter.bindToRecyclerView(recyclerView);
        booksAdapter.setOnItemClickListener(onItemClick);
    }

    @Override
    protected void onDestroy() {
        for (Book book : booksList) {
            book.setSelected(false);
        }
        super.onDestroy();
    }
}
