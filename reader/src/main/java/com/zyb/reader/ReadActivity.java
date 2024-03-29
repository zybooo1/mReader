package com.zyb.reader;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialog.interfaces.OnMenuItemClickListener;
import com.kongzue.dialog.util.BaseDialog;
import com.kongzue.dialog.util.DialogSettings;
import com.kongzue.dialog.v3.BottomMenu;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.tencent.bugly.crashreport.CrashReport;
import com.xw.repo.VectorCompatTextView;
import com.zyb.base.base.activity.MyActivity;
import com.zyb.base.base.fragment.BaseFragmentStateAdapter;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.utils.CommonUtils;
import com.zyb.base.utils.EventBusUtil;
import com.zyb.base.utils.LogUtil;
import com.zyb.base.utils.QMUIViewHelper;
import com.zyb.base.utils.TimeUtil;
import com.zyb.base.utils.constant.Constants;
import com.zyb.base.widget.ClearEditText;
import com.zyb.base.widget.RoundButton;
import com.zyb.base.widget.decoration.VerticalItemLineDecoration;
import com.zyb.common.db.DBFactory;
import com.zyb.common.db.bean.Book;
import com.zyb.common.db.bean.BookMarks;
import com.zyb.common.db.bean.BookMarksDao;
import com.zyb.reader.adapter.SearchAdapter;
import com.zyb.reader.bean.SearchResultBean;
import com.zyb.reader.dialog.SettingDialog;
import com.zyb.reader.fragment.BookMarkFragment;
import com.zyb.reader.fragment.CatalogFragment;
import com.zyb.reader.util.BookUtil;
import com.zyb.reader.util.BrightnessUtil;
import com.zyb.reader.util.PageFactory;
import com.zyb.reader.view.PageWidget;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import cn.iwgang.countdownview.CountdownView;


/**
 * 阅读界面
 */
public class ReadActivity extends MyActivity {
    private static final int ANIM_HIDE_DURATION = 200;
    private static final int ANIM_SHOW_DURATION = 400;

    private static final String TAG = "ReadActivity";
    public final static String EXTRA_BOOK = "bookList";

    @BindView(R2.id.bookpage)
    PageWidget pageWidget;
    @BindView(R2.id.rl_top_root)
    RelativeLayout rlTopRoot;
    @BindView(R2.id.rl_top_bar)
    RelativeLayout rlTopBar;
    @BindView(R2.id.tvTitle)
    TextView tvTitle;
    @BindView(R2.id.viewProgressPercent)
    RoundButton viewProgressPercent;
    @BindView(R2.id.tv_pre)
    TextView tv_pre;
    @BindView(R2.id.sb_progress)
    SeekBar sb_progress;
    @BindView(R2.id.tv_next)
    TextView tv_next;
    @BindView(R2.id.tv_dayornight)
    VectorCompatTextView tv_dayornight;
    @BindView(R2.id.bookpop_bottom)
    LinearLayout bookpop_bottom;
    @BindView(R2.id.rl_bottom)
    ConstraintLayout rl_bottom;
    @BindView(R2.id.btnStartSpeech)
    ImageView btnStartSpeech;
    @BindView(R2.id.btnAddBookMark)
    ImageView btnAddBookMark;
    @BindView(R2.id.rl_read_bottom)
    ConstraintLayout rl_read_bottom;
    @BindView(R2.id.layoutRoot)
    ConstraintLayout layoutRoot;
    @BindView(R2.id.tvSpeechEngine)
    TextView tvSpeechEngine;

    private Config config;
    private Book book;
    private PageFactory pageFactory;
    private SettingDialog mSettingDialog;
    private Boolean mIsNightMode;


    // 接收电池信息更新、时间更新的广播
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) return;
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                LogUtil.e(TAG, Intent.ACTION_BATTERY_CHANGED);
                int level = intent.getIntExtra("level", 50);
                pageFactory.updateBattery(level);
            } else if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                LogUtil.e(TAG, Intent.ACTION_TIME_TICK);
                pageFactory.updateTime();
            } else if (intent.getAction().equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                //正在朗读读且断开蓝牙耳机 则暂停朗读
                if (isSpeaking && BluetoothProfile.STATE_DISCONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                    pauseSpeech();
                }
            } else if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", 0);
                //正在朗读读且拔出耳机 则暂停朗读
                if (isSpeaking && state == 0) {
                    pauseSpeech();
                }
            }
        }
    };


    @Override
    protected int getLayoutId() {
        return R.layout.reader_activity_read;
    }

    @Override
    public boolean statusBarDarkFont() {
        return false;
    }

    @Override
    protected void initView() {
        ImmersionBar.setTitleBarMarginTop(this, rlTopBar);
        //禁止手势滑动
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawerLayout.addDrawerListener(drawerListener);

        Config.createConfig(this);
        PageFactory.createPageFactory(this);

        config = Config.getInstance();
        pageFactory = PageFactory.getInstance();

        IntentFilter mfilter = new IntentFilter();
        mfilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mfilter.addAction(Intent.ACTION_TIME_TICK);
        mfilter.addAction(Intent.ACTION_HEADSET_PLUG);
        mfilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(myReceiver, mfilter);

        mSettingDialog = new SettingDialog(this);
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //改变屏幕亮度
        if (!config.isSystemLight()) {
            BrightnessUtil.setBrightness(this, config.getLight());
        }
        //获取intent中的携带的信息
        Intent intent = getIntent();
        book = (Book) intent.getSerializableExtra(EXTRA_BOOK);

        tvTitle.setText(book.getTitle());

        sbSpeed.setOnSeekBarChangeListener(onSpeedChangeListener);
        sbTiming.setOnSeekBarChangeListener(onTimingChangeListener);

        searchAdapter = new SearchAdapter(searchResultList);
        rvSearch.setLayoutManager(new LinearLayoutManager(getActivity()));
        VerticalItemLineDecoration decoration = new VerticalItemLineDecoration.Builder(this)
                .colorRes(R.color.reader_list_item_divider)
                .build();
        rvSearch.addItemDecoration(decoration);
        rvSearch.setAdapter(searchAdapter);
        searchAdapter.bindToRecyclerView(rvSearch);
        searchAdapter.setOnItemClickListener(searchItemClicklistener);
        smartRefresh.setOnLoadMoreListener(loadMoreListener);
        etSearch.setOnKeyListener(onKeyListener);
        etSearch.addTextChangedListener(searchTextWatcher);

        //View绘制完毕后初始化
        pageWidget.post(() -> {
            pageFactory.setPageWidget(pageWidget);

            try {
                pageFactory.openBook(book);
                tabLayout.addTab(tabLayout.newTab().setText("目录"));
                tabLayout.addTab(tabLayout.newTab().setText("书签"));
                tabLayout.addOnTabSelectedListener(onTabSelectedListener);
                ArrayList<Fragment> fragments = new ArrayList<>();
                fragments.add(CatalogFragment.newInstance(pageFactory.getBookPath()));
                fragments.add(BookMarkFragment.newInstance(pageFactory.getBookPath()));
                adapter = new BaseFragmentStateAdapter(getSupportFragmentManager(), fragments);
                viewPager.setAdapter(adapter);
                viewPager.addOnPageChangeListener(onPageChangeListener);
            } catch (IOException e) {
                e.printStackTrace();
                CrashReport.postCatchedException(e);
                toast("打开电子书失败");
            }

            pageWidget.setPageMode(config.getPageMode());
            mIsNightMode = config.getDayOrNight();
            toggleDayOrNight();
        });

        sb_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            float pro;

            /**
             * 拖动
             * @param fromUser 是否是用户拖动 否则就是 setProgress()的方式
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pro = (float) (progress / 10000.0);
                DecimalFormat decimalFormat = new DecimalFormat("00.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                String p = decimalFormat.format(pro * 100.0);//format 返回的是字符串
                viewProgressPercent.setText(String.format("%s%%", p));
                if (rl_bottom.getVisibility() == View.VISIBLE && fromUser) {
                    viewProgressPercent.setVisibility(View.VISIBLE);
                }
            }

            // 表示进度条刚开始拖动，开始拖动时候触发的操作
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            // 停止拖动时候
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                pageFactory.changeProgress(pro);
                viewProgressPercent.setVisibility(View.GONE);
            }
        });


        mSettingDialog.setSettingListener(new SettingDialog.SettingListener() {
            @Override
            public void changeSystemBright(Boolean isSystem, float brightness) {
                if (!isSystem) {
                    BrightnessUtil.setBrightness(ReadActivity.this, brightness);
                } else {
                    int bh = BrightnessUtil.getScreenBrightness(ReadActivity.this);
                    BrightnessUtil.setBrightness(ReadActivity.this, bh);
                }
            }

            @Override
            public void changeFontSize(int fontSize) {
                pageFactory.changeFontSize(fontSize);
            }

            @Override
            public void changeBookBg(int type) {
                pageFactory.changeBookBg(type);
                //选择任一背景就是打开了日间模式
                mIsNightMode = false;
                toggleDayOrNight();
            }

            @Override
            public void changePageMode(int mode) {
                pageWidget.setPageMode(mode);
            }
        });

        pageFactory.setPageEvent(this::setSeekBarProgress);

        pageWidget.setTouchListener(new PageWidget.TouchListener() {
            @Override
            public void center() {
                toggleMenu();
            }

            @Override
            public void longClick() {
                toActionActivity();
            }

            @Override
            public Boolean prePage() {
                if (getMenuIsShowing() || isSpeaking) {
                    return false;
                }

                pageFactory.prePage();
                return !pageFactory.isfirstPage();
            }

            @Override
            public Boolean nextPage() {
                LogUtil.e("setTouchListener", "nextPage");
                if (getMenuIsShowing() || isSpeaking) {
                    return false;
                }

                pageFactory.nextPage();
                return !pageFactory.islastPage();
            }

            @Override
            public void cancel() {
                pageFactory.cancelPage();
            }
        });
    }

    private void toActionActivity() {
        if(isSpeaking){
            toast("清先退出朗读");
            return;
        }
        Intent intent = new Intent(this, ActionActivity.class);
        intent.putExtra(Constants.JUMP_PARAM_FLAG_STRING,
                pageFactory.getCurrentPage().getLineToString()
                        + pageFactory.getNextPage().getLineToString());
        intent.putExtra(Constants.JUMP_PARAM_FLAG_STRING2, book.getTitle());
        startActivity(intent);
    }

    private void hideSystemUI() {
        getStatusBarConfig().hideBar(BarHide.FLAG_HIDE_BAR).init();
    }

    private void showSystemUI() {
        getStatusBarConfig().hideBar(BarHide.FLAG_SHOW_BAR).init();
    }

    @Override
    public int navigationBarColor() {
        return R.color.reader_MenucolorReadMenu;
    }

    @OnClick(R2.id.btnAddBookMark)
    public void clickAddBookMark() {
        if (pageFactory.getCurrentPage() != null) {
            List<BookMarks> bookMarksList = DBFactory.getInstance().getBookMarksManage()
                    .getQueryBuilder()
                    .where(BookMarksDao.Properties.Bookpath.eq(pageFactory.getBookPath()), BookMarksDao.Properties.Begin.eq(pageFactory.getCurrentPage().getBegin()))
                    .list();
            if (!bookMarksList.isEmpty()) {
                toast("该书签已存在");
                return;
            }
            BookMarks bookMarks = new BookMarks();
            StringBuilder word = new StringBuilder();
            for (String line : pageFactory.getCurrentPage().getLines()) {
                word.append(line);
            }
            bookMarks.setId(pageFactory.getBookPath() + pageFactory.getCurrentPage().getBegin());
            bookMarks.setTime(TimeUtil.parseDateTime(System.currentTimeMillis()));
            bookMarks.setBegin(pageFactory.getCurrentPage().getBegin());
            bookMarks.setText(word.toString());
            bookMarks.setBookpath(pageFactory.getBookPath());
            DBFactory.getInstance().getBookMarksManage().insertOrUpdate(bookMarks);
            toast("书签添加成功");
            EventBusUtil.sendEvent(new BaseEvent<>(EventConstants.EVENT_MARKS_REFRESH));
        }
    }

    @OnClick(R2.id.btnStartSpeech)
    public void clickStartSpeech() {
        initTTS(config.getSpeechEngine());
        toggleMenu();
        isSpeaking = true;
    }

    @OnClick(R2.id.ivBack)
    public void clickBack() {
        finish();
    }

    /**
     * 切换日夜间模式
     */
    public void changeDayOrNight() {
        mIsNightMode = !mIsNightMode;
        toggleDayOrNight();
        config.setDayOrNight(mIsNightMode);
        pageFactory.setDayOrNight(mIsNightMode);
    }

    /**
     * 切换日夜间模式按钮UI
     */
    public void toggleDayOrNight() {
        if (mIsNightMode) {
            tv_dayornight.setText(getResources().getString(R.string.reader_read_setting_day));
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.reader_svg_brightness_up);
            tv_dayornight.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);

        } else {
            tv_dayornight.setText(getResources().getString(R.string.reader_read_setting_night));
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.reader_svg_night);
            tv_dayornight.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
        }
    }

    /**
     * 设置阅读进度条进度
     */
    public void setSeekBarProgress(float progress) {
        int p = (int) (progress * 100);
        book.setProgress(p == 0 ? "" : p + "%");
        DBFactory.getInstance().getBooksManage().insertOrUpdate(book);
        sb_progress.setProgress((int) (progress * 10000));
    }

    /**
     * 切换菜单栏的可视状态 默认是隐藏的
     */
    private void toggleMenu() {
        if (isSpeaking || isSpeechPause) {
            if (rl_read_bottom.getVisibility() == View.VISIBLE) {
                QMUIViewHelper.slideOut(rl_read_bottom, ANIM_HIDE_DURATION, QMUIViewHelper.QMUIDirection.TOP_TO_BOTTOM);
            } else {
                QMUIViewHelper.slideIn(rl_read_bottom, ANIM_SHOW_DURATION, QMUIViewHelper.QMUIDirection.BOTTOM_TO_TOP);
            }
            return;
        }

        if (getMenuIsShowing()) {
            QMUIViewHelper.slideOut(rlTopRoot, ANIM_HIDE_DURATION, QMUIViewHelper.QMUIDirection.BOTTOM_TO_TOP);
            QMUIViewHelper.slideOut(rl_bottom, ANIM_HIDE_DURATION, QMUIViewHelper.QMUIDirection.TOP_TO_BOTTOM);
            QMUIViewHelper.fadeOut(btnAddBookMark, ANIM_HIDE_DURATION, null, true);
            QMUIViewHelper.fadeOut(btnStartSpeech, ANIM_HIDE_DURATION, null, true);
            hideSystemUI();
        } else {
            QMUIViewHelper.slideIn(rlTopRoot, ANIM_SHOW_DURATION, QMUIViewHelper.QMUIDirection.TOP_TO_BOTTOM);
            QMUIViewHelper.slideIn(rl_bottom, ANIM_SHOW_DURATION, QMUIViewHelper.QMUIDirection.BOTTOM_TO_TOP);
            QMUIViewHelper.fadeIn(btnAddBookMark, ANIM_HIDE_DURATION, null, true);
            QMUIViewHelper.fadeIn(btnStartSpeech, ANIM_HIDE_DURATION, null, true);
            showSystemUI();
        }
    }

    /**
     * 当前是否有菜单UI显示
     */
    private boolean getMenuIsShowing() {
        return rlTopRoot.getVisibility() == View.VISIBLE;
    }

    @OnClick({R2.id.tv_pre, R2.id.tv_next, R2.id.tv_directory, R2.id.tv_dayornight,
            R2.id.tv_setting, R2.id.bookpop_bottom, R2.id.rl_bottom, R2.id.tv_stop_read,
            R2.id.ivSearch})
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.tv_pre) {
            pageFactory.preChapter();
        } else if (i == R.id.tv_next) {
            pageFactory.nextChapter();
        } else if (i == R.id.tv_directory) {
            drawerLayout.openDrawer(Gravity.START);
            toggleMenu();
        } else if (i == R.id.tv_dayornight) {
            changeDayOrNight();
        } else if (i == R.id.tv_setting) {
            toggleMenu();
            mSettingDialog.show();
        } else if (i == R.id.tv_stop_read) {
            if (isSpeechPause) {
                resumeSpeech();
            } else {
                stopSpeech();
            }
        } else if (i == R.id.ivSearch) {
            drawerLayout.openDrawer(Gravity.END);
            toggleMenu();
        }

    }

    /**
     * 左侧滑：目录&书签
     */
    @BindView(R2.id.drawerLayout)
    DrawerLayout drawerLayout;
    @BindView(R2.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R2.id.viewPager)
    ViewPager viewPager;
    private BaseFragmentStateAdapter adapter;
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
    TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            int position = tab.getPosition();
            viewPager.setCurrentItem(position);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }
    };

    /**
     * 右侧滑：搜索
     */
    @BindView(R2.id.etSearch)
    ClearEditText etSearch;
    @BindView(R2.id.tvSearchEmpty)
    TextView tvSearchEmpty;
    @BindView(R2.id.smartRefresh)
    SmartRefreshLayout smartRefresh;
    @BindView(R2.id.rvSearch)
    RecyclerView rvSearch;
    private SearchAdapter searchAdapter;
    List<SearchResultBean> searchResultList = new ArrayList<>();
    BaseQuickAdapter.OnItemClickListener searchItemClicklistener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            drawerLayout.closeDrawer(Gravity.END);
            pageFactory.changeChapter(searchResultList.get(position).getBegin());
        }
    };
    OnLoadMoreListener loadMoreListener = refreshLayout -> search(true);
    BookUtil.OnSearchResult onSearchResult = new BookUtil.OnSearchResult() {

        @Override
        public void onEmpty(boolean isLoadMore) {
            if (isLoadMore) {
                smartRefresh.finishLoadMore();
                toast("没有更多了");
            } else {
                searchResultList.clear();
                searchAdapter.notifyDataSetChanged();
                tvSearchEmpty.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onResult(List<SearchResultBean> beans, boolean isLoadMore) {
            smartRefresh.finishLoadMore();
            smartRefresh.setEnableLoadMore(beans.size() >= BookUtil.A_PAGE_NUM);

            tvSearchEmpty.setVisibility(View.GONE);
            if (!isLoadMore) searchResultList.clear();
            searchResultList.addAll(beans);
            searchAdapter.notifyDataSetChanged();

        }

        @Override
        public void onError() {
            tvSearchEmpty.setVisibility(View.VISIBLE);
        }
    };
    TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            search(false);
        }
    };
    private View.OnKeyListener onKeyListener = (v, keyCode, event) -> {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
            search(false);
            hideKeyboard();
        }
        return false;
    };
    DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
            hideKeyboard();
        }

        @Override
        public void onDrawerStateChanged(int newState) {
        }
    };


    public void hideKeyboard() {
        etSearch.clearFocus();
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }


    private void search(boolean isLoadMore) {
        String text = etSearch.getText().toString();
        if (text.trim().isEmpty()) {
            onSearchResult.onEmpty(false);
            return;
        }
        pageFactory.mBookUtil.searchContent(text, isLoadMore, onSearchResult);
    }

    @OnClick(R2.id.btnCancelSearch)
    public void cancelSearch() {
        searchResultList.clear();
        searchAdapter.notifyDataSetChanged();
        etSearch.setText("");
        hideKeyboard();
        drawerLayout.closeDrawer(Gravity.END);
    }

    /**
     * 事件
     */
    @Override
    protected boolean isRegisterEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventReceived(BaseEvent<Object> event) {
        if (event == null) return;
        switch (event.getCode()) {
            case EventConstants.EVENT_CLOSE_READ_DRAWER:
                drawerLayout.closeDrawer(Gravity.START);
                break;
            default:
                break;
        }
    }

    /**
     * 朗读设置
     */
    @BindView(R2.id.cbAutoTiming)
    public CheckBox cbAutoTiming;
    //计时器的布局
    @BindView(R2.id.timer_layout)
    public LinearLayout timerLayout;
    @BindView(R2.id.countDownView)
    public CountdownView countDownView;
    @BindView(R2.id.sbSpeed)
    SeekBar sbSpeed;
    @BindView(R2.id.sbTiming)
    SeekBar sbTiming;
    @BindView(R2.id.tv_stop_read)
    RoundButton btnStopSpeech;
    private SeekBar.OnSeekBarChangeListener onSpeedChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            if (progress == 0) progress = 1;
            config.setSpeakSpeed(progress);
            float speed = config.getSpeedForTTS();
            textToSpeech.setSpeechRate(speed);
            playSpeech(true);
            LogUtil.e("onStopTrackingTouch speed:" + speed);
        }
    };
    private SeekBar.OnSeekBarChangeListener onTimingChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            int trueProgress;
            if (progress <= 7) {
                trueProgress = 0;
            } else if (progress <= 22) {
                trueProgress = 15;
            } else if (progress <= 37) {
                trueProgress = 30;
            } else if (progress <= 52) {
                trueProgress = 45;
            } else {
                trueProgress = 60;
            }
            sbTiming.setProgress(trueProgress);
            if (trueProgress != 0) {
                ReadActivity.this.toast(trueProgress + "分钟后停止");
                config.setTimingTime(trueProgress);
            }
            showTimer(trueProgress);
        }
    };


    @OnCheckedChanged(R2.id.cbAutoTiming)
    void onAutoTimingChecked(boolean checked) {
        config.setIsAutoTiming(checked);
    }

    //显示行走计时器
    public void showTimer(int time) {
        timerLayout.setVisibility(View.VISIBLE);
        if (time <= 0) {
            hideTimer();
            return;
        }
        countDownView.start((long) (time * 60000));
        countDownView.setOnCountdownEndListener(cv -> {
            hideTimer();
            stopSpeech();
        });
    }

    //隐藏计时器
    public void hideTimer() {
        timerLayout.setVisibility(View.GONE);
        countDownView.stop();
    }

    /**
     * 生命周期
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!getMenuIsShowing()) {
            hideSystemUI();
        }
    }

    @Override
    protected void onDestroy() {
        EventBusUtil.sendStickyEvent(new BaseEvent<>(EventConstants.EVENT_MAIN_REFRESH_BOOK_SHELF));
        pageFactory.clear();
        pageWidget = null;
        unregisterReceiver(myReceiver);
        stopSpeech();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //注意顺序
        if (drawerLayout.isDrawerOpen(Gravity.END)) {
            drawerLayout.closeDrawer(Gravity.END);
            return;
        }
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
            return;
        }
        if (getMenuIsShowing() || rl_read_bottom.getVisibility() == View.VISIBLE) {
            toggleMenu();
            return;
        }
        if (mSettingDialog.isShowing()) {
            mSettingDialog.hide();
            return;
        }
        if (isSpeaking) {
            showDialog(true, "是否确认退出？", "继续看书", "退出",
                    new OnDialogButtonClickListener() {
                        @Override
                        public boolean onClick(BaseDialog baseDialog, View v) {
                            ReadActivity.this.finish();
                            return false;
                        }
                    }, null);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    //------------------ System TTS Start -------------------
    private TextToSpeech textToSpeech;
    List<TextToSpeech.EngineInfo> speechEngines = new ArrayList<>();
    private boolean isSpeechPause;
    private boolean isSpeaking = false;

    UtteranceProgressListener ttsListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            LogUtil.e("onStart");
        }

        @Override
        public void onDone(String utteranceId) {
            LogUtil.e("onDone");

            pageFactory.nextPage();
            if (pageFactory.islastPage()) {
                stopSpeech();
                toast("小说已经读完了");
            } else {
                playSpeech(false);
            }
        }

        @Override
        public void onError(String utteranceId) {
            LogUtil.e("onError");

            toast("语音朗读出现了错误~");
            stopSpeech();
        }
    };

    private void initTTS(@Nullable String name) {
        cbAutoTiming.setChecked(config.getIsAutoTiming());
        if (config.getIsAutoTiming() && config.getTimingTime() > 0) {
            sbTiming.setProgress(config.getTimingTime());
            showTimer(config.getTimingTime());
        }
        sbSpeed.setProgress(config.getSpeakSpeed());
        TextToSpeech.OnInitListener onInitListener = i -> {
            //系统语音初始化成功
            if (i == TextToSpeech.SUCCESS) {
                textToSpeech.setSpeechRate(config.getSpeedForTTS());//速度 0<speed<2
                textToSpeech.setOnUtteranceProgressListener(ttsListener);
                textToSpeech.setLanguage(Locale.CHINA);
                playSpeech(true);

                //保存获取到的语音引擎列表
                speechEngines.clear();
                speechEngines.addAll(textToSpeech.getEngines());

                //播放成功，为用户保存当前语音引擎选择
                config.setSpeechEngine(name);

                //显示语音引擎名称
                String engineLabel = "";
                for (TextToSpeech.EngineInfo speechEngine : speechEngines) {
                    if (speechEngine.name.equals(name)) engineLabel = speechEngine.label;
                }
                if (!engineLabel.isEmpty()) {
                    tvSpeechEngine.setText(engineLabel);
                } else {
                    if (speechEngines.size() > 0)
                        tvSpeechEngine.setText(speechEngines.get(0).label);
                }
            } else {
                toast("语音引擎初始化失败");
                stopSpeech();
            }
        };
        if (name != null && !name.isEmpty()) {
            textToSpeech = new TextToSpeech(this, onInitListener, name);
        } else {
            textToSpeech = new TextToSpeech(this, onInitListener);
        }
    }

    private void playSpeech(boolean isFirstPage) {
        if (textToSpeech == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            toast("暂不支持语音播放");
            stopSpeech();
            return;
        }
        isSpeaking = true;
        String content = pageFactory.getCurPageWithoutFirstSentence() + pageFactory.getNextPageFirstSentence();
        if (isFirstPage)
            content = pageFactory.getCurrentPage().getLineToString() + pageFactory.getNextPageFirstSentence();
        textToSpeech.stop();

        LogUtil.e("playSpeech---", content);
        textToSpeech.speak(content, TextToSpeech.QUEUE_ADD, null, CommonUtils.getUUID());
    }

    private void pauseSpeech() {
        textToSpeech.stop();
        isSpeechPause = true;
        btnStopSpeech.setText("继续播放");
    }

    private void resumeSpeech() {
        playSpeech(true);
        isSpeechPause = false;
        btnStopSpeech.setText("停止播放");
    }

    private void stopSpeech() {
        if (rl_read_bottom.getVisibility() == View.VISIBLE) {
            QMUIViewHelper.slideOut(rl_read_bottom, ANIM_HIDE_DURATION, QMUIViewHelper.QMUIDirection.TOP_TO_BOTTOM);
        }
        isSpeechPause = false;
        isSpeaking = false;
        if (textToSpeech != null) textToSpeech.shutdown();
    }


    @OnClick(R2.id.switchSpeechEngine)
    public void switchSpeechEngine() {
        if (speechEngines.size() <= 1) {
            toast("没有更多选择啦~");
        }
        List<String> engines = new ArrayList<>();
        for (TextToSpeech.EngineInfo speechEngine : speechEngines) {
            engines.add(speechEngine.label);
        }
        BottomMenu.build(this)
                .setStyle(DialogSettings.STYLE.STYLE_IOS)
                .setMenuTextList(engines)
                .setTitle("选择语音引擎")
                .setCancelable(true)
                .setOnCancelButtonClickListener(new OnDialogButtonClickListener() {
                    @Override
                    public boolean onClick(BaseDialog baseDialog, View v) {
                        return false;
                    }
                })
                .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    @Override
                    public void onClick(String text, int index) {
                        stopSpeech();
                        initTTS(speechEngines.get(index).name);
                    }
                })
                .show();
    }
    //------------------ System TTS End -------------------

}
