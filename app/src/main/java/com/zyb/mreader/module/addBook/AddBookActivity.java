package com.zyb.mreader.module.addBook;

import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.zyb.base.base.activity.MVPActivity;
import com.zyb.base.base.fragment.BaseFragmentAdapter;
import com.zyb.base.base.fragment.BaseLazyFragment;
import com.zyb.base.di.component.AppComponent;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.utils.CommonUtils;
import com.zyb.base.utils.EventBusUtil;
import com.zyb.base.widget.dialog.BaseDialog;
import com.zyb.base.widget.dialog.BaseDialogFragment;
import com.zyb.mreader.R;
import com.zyb.mreader.di.component.DaggerActivityComponent;
import com.zyb.mreader.di.module.ActivityModule;
import com.zyb.mreader.di.module.ApiModule;
import com.zyb.mreader.module.addBook.file.BookFilesFragment;
import com.zyb.mreader.module.addBook.path.BookPathFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    public void onRightClick(View v) {
        super.onRightClick(v);
        showRuleDialog();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideRuleDialog();
    }

    /*----------设置弹窗 Begin------------*/
    BaseDialog ruleDialog;
    Switch fileFilterSwitch;
    RadioGroup radioGroup;
    private long currentFilterSize;
    private boolean isFilterENfile;

    private void showRuleDialog() {
        currentFilterSize = mPresenter.getFilterSize();
        isFilterENfile = mPresenter.getIsFilterENfiles();
        if (ruleDialog == null) {
            initRuleDialog();
        }
        fileFilterSwitch.setChecked(isFilterENfile);
        if (currentFilterSize == 0) {
            radioGroup.check(R.id.size0);
        } else if (currentFilterSize == 30 * 1024) {
            radioGroup.check(R.id.size30);
        } else if (currentFilterSize == 50 * 1024) {
            radioGroup.check(R.id.size50);
        } else if (currentFilterSize == 100 * 1024) {
            radioGroup.check(R.id.size100);
        } else {
            radioGroup.check(R.id.size10);
        }

        ruleDialog.show();
    }

    private void initRuleDialog() {
        View rootView = getLayoutInflater().inflate(R.layout.dialog_add_book_rule, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                CommonUtils.getScreenWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
        rootView.setLayoutParams(layoutParams);

        fileFilterSwitch = rootView.findViewById(R.id.fileFilterSwitch);
        radioGroup = rootView.findViewById(R.id.radioGroup);

        ruleDialog = new BaseDialogFragment.Builder(this)
                .setContentView(rootView)
                .setAnimStyle(BaseDialog.AnimStyle.BOTTOM)
                .setGravity(Gravity.BOTTOM)
                .setOnClickListener(R.id.tvOk, (BaseDialog.OnClickListener<TextView>) (dialog, view) -> {
                    hideRuleDialog();
                    onRuleSetting();
                })
                .setOnClickListener(R.id.tvCancel, (BaseDialog.OnClickListener<TextView>) (dialog, view) -> hideRuleDialog())
                .create();
    }

    public void hideRuleDialog() {
        if (ruleDialog != null) ruleDialog.dismiss();
    }

    private void onRuleSetting() {
        boolean isFilter = fileFilterSwitch.isChecked();
        long filterSize = 0;
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.size0:
                filterSize = 0;
                break;
            case R.id.size10:
                filterSize = 10 * 1024;
                break;
            case R.id.size30:
                filterSize = 30 * 1024;
                break;
            case R.id.size50:
                filterSize = 50 * 1024;
                break;
            case R.id.size100:
                filterSize = 100 * 1024;
                break;
        }
        if (filterSize != currentFilterSize || isFilter != isFilterENfile) {
            mPresenter.setFilterSize(filterSize);
            mPresenter.setIsFilterENfiles(isFilter);
            EventBusUtil.sendEvent(new BaseEvent(EventConstants.RESEARCH_BOOK));
        }
    }

    /*----------设置弹窗 End------------*/

}
