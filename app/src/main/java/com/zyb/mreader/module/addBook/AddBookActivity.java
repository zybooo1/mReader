package com.zyb.mreader.module.addBook;

import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.zyb.base.base.activity.MVPActivity;
import com.zyb.base.di.component.AppComponent;
import com.zyb.mreader.R;
import com.zyb.mreader.base.bean.Book;
import com.zyb.mreader.base.bean.BookFiles;
import com.zyb.mreader.di.component.DaggerActivityComponent;
import com.zyb.mreader.di.module.ActivityModule;
import com.zyb.mreader.di.module.ApiModule;
import com.zyb.mreader.module.addBook.file.BookFilesFragment;
import com.zyb.mreader.module.addBook.path.BookPathFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class AddBookActivity extends MVPActivity<AddBookPresenter> implements AddBookContract.View {

    public static final int ADDED_RESULT = 0x123;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.tablayout)
    TabLayout tabLayout;

    private int[] tabTexts = new int[]{R.string.add_book_files, R.string.add_book_paths};

    TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            viewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }
    };

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            tabLayout.getTabAt(position).select();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };
    private BookFilesFragment bookFilesFragment;
    private BookPathFragment bookPathFragment;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_book;
    }

    @Override
    protected int getTitleBarId() {
        return R.id.toolbar;
    }

    @Override
    protected void initView() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
        for (int tabText : tabTexts) {
            TabLayout.Tab tab = tabLayout.newTab().setText(tabText);
            tabLayout.addTab(tab);
        }
        initPager();
    }

    @Override
    protected void initData() {

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

    private void initPager() {
        List<Fragment> fragments = new ArrayList<>();
        bookFilesFragment = BookFilesFragment.newInstance();
        bookPathFragment = BookPathFragment.newInstance();
        fragments.add(bookFilesFragment);
        fragments.add(bookPathFragment);
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(onPageChangeListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
addBooks();
            return true;
        }else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void addBooks() {
        List<Book> bookList = new ArrayList<>();
        bookFilesFragment.bookList.addAll(bookPathFragment.mFileBeans);
        for (BookFiles bookFiles : bookFilesFragment.bookList) {
            if (bookFiles.getIsChecked()) {
                Book book = new Book();
                book.setId(bookFiles.getId());
                book.setSize(bookFiles.getSize());
                book.setPath(bookFiles.getPath());
                book.setTitle(bookFiles.getTitle());
                bookList.add(book);
            }
        }
        mPresenter.addBooks(bookList);
    }

    @Override
    public void onBooksAdded() {
        setResult(ADDED_RESULT);
        finish();
    }
}
