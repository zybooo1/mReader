package com.zyb.mreader.module.addBook.file;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.zyb.base.base.fragment.MVPFragment;
import com.zyb.base.di.component.AppComponent;
import com.zyb.base.widget.decoration.VerticalItemLineDecoration;
import com.zyb.mreader.R;
import com.zyb.mreader.base.bean.BookFiles;
import com.zyb.mreader.di.component.DaggerFragmentComponent;
import com.zyb.mreader.di.module.ApiModule;
import com.zyb.mreader.di.module.FragmentModule;
import com.zyb.mreader.module.addBook.file.adapter.FilesAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class BookFilesFragment extends MVPFragment<BookFilesPresenter> implements BookFilesContract.View {
    @BindView(R.id.rv_books)
    RecyclerView rvBooks;
    public List<BookFiles> bookList = new ArrayList<>();
    private FilesAdapter booksAdapter;
    private BaseQuickAdapter.OnItemClickListener onItemClickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            BookFiles book = bookList.get(position);
            if(!mPresenter.isBookAdded(book)){
                book.setIsChecked(!book.getIsChecked());
                booksAdapter.notifyItemChanged(position);
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
        booksAdapter.setOnItemClickListener(onItemClickListener);
        rvBooks.setLayoutManager(new LinearLayoutManager(getFragmentActivity()));
        rvBooks.addItemDecoration(new VerticalItemLineDecoration(getFragmentActivity()));
        rvBooks.setAdapter(booksAdapter);

        if (mPresenter.isBookFilesCached()) {
            onBookFilesLoaded(mPresenter.getAllBookFiles());
        } else {
            mPresenter.scanFiles();
        }
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onPageRetry(View v) {
        super.onPageRetry(v);
        mPresenter.scanFiles();
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
        mPresenter.scanFiles();
    }
}
