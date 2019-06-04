package com.zyb.mreader.module.addBook;

import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.zyb.base.base.activity.MVPActivity;
import com.zyb.base.base.fragment.BaseFragmentAdapter;
import com.zyb.base.base.fragment.BaseLazyFragment;
import com.zyb.base.di.component.AppComponent;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.utils.EventBusUtil;
import com.zyb.base.utils.LogUtil;
import com.zyb.mreader.R;
import com.zyb.mreader.di.component.DaggerActivityComponent;
import com.zyb.mreader.di.module.ActivityModule;
import com.zyb.mreader.di.module.ApiModule;
import com.zyb.mreader.module.addBook.file.BookFilesFragment;
import com.zyb.mreader.module.addBook.path.BookPathFragment;
import com.zyb.mreader.module.main.MainActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class AddBookActivity extends MVPActivity<AddBookPresenter> implements AddBookContract.View {

    public static final int ADDED_RESULT = 0x123;

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
        return R.id.titleBar;
    }

    @Override
    protected void initView() {
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
    protected boolean isRegisterEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventReceived(BaseEvent<Object> event) {
        if (event == null) return;
        switch (event.getCode()) {
            case EventConstants.EVENT_SHOW_STATUS_BAR:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
            case EventConstants.EVENT_HIDE_STATUS_BAR:
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
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

    private void initPager() {
        BaseFragmentAdapter<BaseLazyFragment> adapter = new BaseFragmentAdapter<>(getSupportFragmentManager());
        adapter.addFragment(BookFilesFragment.newInstance());
        adapter.addFragment(BookPathFragment.newInstance());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(onPageChangeListener);
    }

}
