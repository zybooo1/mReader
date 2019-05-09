package com.zyb.mreader.module.addBook.path;

import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.zyb.base.base.fragment.MVPFragment;
import com.zyb.base.di.component.AppComponent;
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

public class BookPathFragment extends MVPFragment<BookPathPresenter> implements BookPathContract.View {
    @BindView(R.id.rv_books)
    RecyclerView rvBooks;

    @BindView(R.id.loading_view)
    LinearLayout loadingView;
    @BindView(R.id.file_category_tv_path)
    TextView tvPath;
    @BindView(R.id.file_category_tv_back_last)
    TextView tvPathBack;

    public List<BookFiles> mFileBeans = new ArrayList<>();
    private FileStack mFileStack;

    private PathAdapter booksAdapter;
    private View.OnClickListener onPathBackClickListener = v -> {
        FileStack.FileSnapshot snapshot = mFileStack.pop();
        int oldScrollOffset = rvBooks.computeHorizontalScrollOffset();
        if (snapshot == null) return;
        tvPath.setText(snapshot.filePath);
        addFiles(snapshot.files);
        rvBooks.scrollBy(0, snapshot.scrollOffset - oldScrollOffset);
    };
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
            } else {
                BookFiles book = mFileBeans.get(position);
                if(!mPresenter.isBookAdded(book)){
                    book.setIsChecked(!book.getIsChecked());
                    booksAdapter.notifyItemChanged(position);
                }
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
            if (file.isFile()){
                fileBean.setSize(FileUtils.getFileSize(file.length()));
            }else {
                fileBean.setSize(FileUtils.getChildNum(file));
            }
            fileBean.setTitle(FileUtils.getSimpleName(file));
            mFileBeans.add(fileBean);
        }
        loadingView.setVisibility(View.GONE);
        booksAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_book_paths;
    }

    @Override
    protected void initView() {
        mFileStack = new FileStack();

        booksAdapter = new PathAdapter(mFileBeans,mPresenter);
        booksAdapter.setOnItemClickListener(onItemClickListener);
        rvBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBooks.addItemDecoration(new VerticalItemLineDecoration(getFragmentActivity()));
        rvBooks.setAdapter(booksAdapter);


        File root = Environment.getExternalStorageDirectory();
        toggleFileTree(root);

        tvPathBack.setOnClickListener(onPathBackClickListener);

    }

    @Override
    protected void initData() {

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

    }

}
