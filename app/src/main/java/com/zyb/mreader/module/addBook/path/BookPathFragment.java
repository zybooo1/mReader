package com.zyb.mreader.module.addBook.path;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.zyb.base.base.fragment.MVPFragment;
import com.zyb.base.di.component.AppComponent;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.utils.EventBusUtil;
import com.zyb.base.utils.LogUtil;
import com.zyb.base.widget.decoration.VerticalItemLineDecoration;
import com.zyb.mreader.R;
import com.zyb.mreader.base.bean.BookFiles;
import com.zyb.mreader.di.component.DaggerFragmentComponent;
import com.zyb.mreader.di.module.ApiModule;
import com.zyb.mreader.di.module.FragmentModule;
import com.zyb.mreader.module.addBook.path.adapter.PathAdapter;
import com.zyb.mreader.utils.FileStack;
import com.zyb.mreader.utils.FileTypeComparator;
import com.zyb.mreader.utils.FileUtils;
import com.zyb.mreader.utils.SimpleTxtFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class BookPathFragment extends MVPFragment<BookPathPresenter> implements BookPathContract.View {
    @BindView(R.id.rv_books)
    RecyclerView rvBooks;

    @BindView(R.id.tvEmpty)
    TextView tvEmpty;

    @BindView(R.id.file_category_tv_path)
    TextView tvPath;

    public List<BookFiles> mFileBeans = new ArrayList<>();
    private FileStack mFileStack;

    private PathAdapter booksAdapter;
    private BaseQuickAdapter.OnItemClickListener onItemClickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            File file = new File(mFileBeans.get(position).getPath());
            if (file.isDirectory()) {
                //保存当前信息。
                FileStack.FileSnapshot snapshot = new FileStack.FileSnapshot();
                snapshot.filePath = tvPath.getText().toString();
                snapshot.files = new ArrayList<File>(booksAdapter.getAllFiles());
                snapshot.scrollOffset = rvBooks.computeVerticalScrollOffset();
                mFileStack.push(snapshot);
                //切换下一个文件
                toggleFileTree(file);
            }
        }
    };
    private BaseQuickAdapter.OnItemChildClickListener onItemChildClickListener = new BaseQuickAdapter.OnItemChildClickListener() {
        @Override
        public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
            if (view.getId() == R.id.ivAdd) {
                BookFiles book = mFileBeans.get(position);
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
            LogUtil.e("onScrolled dy:" + dy);
            if (dy <= 0) {
                EventBusUtil.sendEvent(new BaseEvent(EventConstants.EVENT_SHOW_STATUS_BAR));
            } else {
                EventBusUtil.sendEvent(new BaseEvent(EventConstants.EVENT_HIDE_STATUS_BAR));
            }
        }

    };

    public static BookPathFragment newInstance() {
        BookPathFragment fragment = new BookPathFragment();
        return fragment;
    }

    private void toggleFileTree(File file) {
        //路径名
        tvPath.setText(getString(R.string.file_path, file.getPath()));
        //获取数据
        File[] files = file.listFiles(new SimpleTxtFileFilter());
        //转换成List
        List<File> rootFiles = Arrays.asList(files);
        //排序
        Collections.sort(rootFiles, new FileTypeComparator());
        //加入
        addFiles(rootFiles);
    }

    /**
     * 添加文件数据
     *
     * @param files
     */
    private void addFiles(List<File> files) {
        mFileBeans.clear();
        for (File file : files) {
            BookFiles fileBean = new BookFiles();
            fileBean.setId(file.getAbsolutePath());
            fileBean.setIsFile(file.isFile());
            fileBean.setPath(file.getAbsolutePath());
            if (file.isFile()) {
                fileBean.setSize(FileUtils.getFileSize(file.length()));
            } else {
                fileBean.setSize(FileUtils.getChildNum(file));
            }
            fileBean.setTitle(FileUtils.getSimpleName(file));
            mFileBeans.add(fileBean);
        }
        if (mFileBeans.size() <= 0) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
        booksAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_book_paths;
    }

    @Override
    protected void initView() {
        mFileStack = new FileStack();

        booksAdapter = new PathAdapter(mFileBeans, mPresenter);
        booksAdapter.setOnItemClickListener(onItemClickListener);
        booksAdapter.setOnItemChildClickListener(onItemChildClickListener);
        rvBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBooks.addItemDecoration(new VerticalItemLineDecoration(getFragmentActivity()));
        rvBooks.setAdapter(booksAdapter);
        rvBooks.addOnScrollListener(onFlingListener);


        File root = Environment.getExternalStorageDirectory();
        toggleFileTree(root);
    }

    @Override
    protected void initData() {

    }

    @OnClick(R.id.file_category_tv_back_last)
    public void pathBack() {
        FileStack.FileSnapshot snapshot = mFileStack.pop();
        int oldScrollOffset = rvBooks.computeHorizontalScrollOffset();
        if (snapshot == null) return;
        tvPath.setText(snapshot.filePath);
        addFiles(snapshot.files);
        rvBooks.scrollBy(0, snapshot.scrollOffset - oldScrollOffset);
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

}
