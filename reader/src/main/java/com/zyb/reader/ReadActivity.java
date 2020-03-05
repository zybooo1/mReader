package com.zyb.reader;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.annotation.NonNull;
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
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.gyf.barlibrary.ImmersionBar;
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialog.util.BaseDialog;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.tencent.bugly.crashreport.CrashReport;
import com.xw.repo.VectorCompatTextView;
import com.zyb.base.base.activity.MyActivity;
import com.zyb.base.base.fragment.BaseFragmentStateAdapter;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.utils.EventBusUtil;
import com.zyb.base.utils.LogUtil;
import com.zyb.base.utils.QMUIViewHelper;
import com.zyb.base.utils.TimeUtil;
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
    @BindView(R2.id.rgSpeaker)
    RadioGroup rgSpeaker;
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

    private Config config;
    private Book book;
    private PageFactory pageFactory;
    private SettingDialog mSettingDialog;
    private Boolean mIsNightMode;
    private boolean isSpeaking = false;
    RadioGroup.OnCheckedChangeListener onSpeakerCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int id) {
            OfflineResource.Speaker speaker = OfflineResource.Speaker.FEMALE;
            if (id == R.id.speaker1) {
                speaker = OfflineResource.Speaker.FEMALE;
            } else if (id == R.id.speaker2) {
                speaker = OfflineResource.Speaker.MALE;
            } else if (id == R.id.speaker3) {
                speaker = OfflineResource.Speaker.DUXY;
            } else if (id == R.id.speaker4) {
                speaker = OfflineResource.Speaker.DUYY;
            }
            if (speechService != null) speechService.switchVoice(speaker);
        }
    };


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
                    if (speechService != null) speechService.pause();
                }
            } else if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int i = intent.getIntExtra("state", 0);
                //正在朗读读且拔出耳机 则暂停朗读
                if (isSpeaking && intent.getIntExtra("state", 0) == 0) {
                    if (speechService != null) speechService.pause();
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

        rgSpeaker.setOnCheckedChangeListener(onSpeakerCheckedChangeListener);

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
                showError("打开电子书失败");
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
            public void changeTypeFace(Typeface typeface) {
                pageFactory.changeTypeface(typeface);
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

    private void hideSystemUI() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏
    }

    private void showSystemUI() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏
    }

    @OnClick(R2.id.btnAddBookMark)
    public void clickAddBookMark() {
        if (pageFactory.getCurrentPage() != null) {
            List<BookMarks> bookMarksList = DBFactory.getInstance().getBookMarksManage()
                    .getQueryBuilder()
                    .where(BookMarksDao.Properties.Bookpath.eq(pageFactory.getBookPath()), BookMarksDao.Properties.Begin.eq(pageFactory.getCurrentPage().getBegin()))
                    .list();
            if (!bookMarksList.isEmpty()) {
                showMsg("该书签已存在");
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
            showMsg("书签添加成功");
            EventBusUtil.sendEvent(new BaseEvent(EventConstants.EVENT_MARKS_REFRESH));
        }
    }

    @OnClick(R2.id.btnStartSpeech)
    public void clickStartSpeech() {
        initSpeech();
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
        if (isSpeaking) {
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
                speechService.resume();
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
    OnLoadMoreListener loadMoreListener = refreshLayout -> {
        search(true);
    };
    BookUtil.OnSearchResult onSearchResult = new BookUtil.OnSearchResult() {

        @Override
        public void onEmpty(boolean isLoadMore) {
            if (isLoadMore) {
                smartRefresh.finishLoadMore();
                showMsg("没有更多了");
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
     * 朗读
     */
    private boolean isSpeechPause = false; //是否暂停朗读（音频焦点被抢占时会暂停）
    private SpeechService speechService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SpeechService.SpeechBinder binder = (SpeechService.SpeechBinder) service;
            speechService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            speechService = null;
        }
    };

    private void initSpeech() {
        showDialogLoading();

        cbAutoTiming.setChecked(config.getIsAutoTiming());
        if (config.getIsAutoTiming() && config.getTimingTime() > 0) {
            sbTiming.setProgress(config.getTimingTime());
            showTimer(config.getTimingTime());
        }
        sbSpeed.setProgress(config.getSpeakSpeed());
        changeSpeaker(config.getSpeaker());

        Intent intent = new Intent(this, SpeechService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        //开始朗读
        String content = pageFactory.getCurrentPage().getLineToString() + pageFactory.getNextPageFirstSentence();
        BaseEvent<String> event = new BaseEvent<>(EventConstants.EVENT_SPEECH_STRING_DATA,
                content);
        EventBusUtil.sendStickyEvent(event);
        LogUtil.e(content);

    }

    /**
     * 暂停朗读
     */
    private void pauseSpeech() {
        isSpeechPause = true;
        btnStopSpeech.setText("继续播放");
    }

    /**
     * 停止朗读
     */
    private void stopSpeech() {
        if (rl_read_bottom.getVisibility() == View.VISIBLE) {
            QMUIViewHelper.slideOut(rl_read_bottom, ANIM_HIDE_DURATION, QMUIViewHelper.QMUIDirection.TOP_TO_BOTTOM);
        }
        isSpeaking = false;
        isSpeechPause = false;
        if (serviceConnection != null && speechService != null) {
            unbindService(serviceConnection);
            speechService = null;
        }
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
            case EventConstants.EVENT_SPEECH_STOP:
                hideDialogLoading();
                stopSpeech();
                break;
            case EventConstants.EVENT_SPEECH_FINISH_PAGE:
                pageFactory.nextPage();
                if (pageFactory.islastPage()) {
                    stopSpeech();
                    showMsg("小说已经读完了");
                } else {
                    isSpeaking = true;
                    //继续朗读
                    String content = pageFactory.getCurPageWithoutFirstSentence() + pageFactory.getNextPageFirstSentence();
                    BaseEvent<String> e = new BaseEvent<>(EventConstants.EVENT_SPEECH_STRING_DATA,
                            content);
                    EventBusUtil.sendStickyEvent(e);
                    LogUtil.e(content);
                }
                break;
            case EventConstants.EVENT_SPEECH_PAUSE:
                pauseSpeech();
                break;
            case EventConstants.EVENT_SPEECH_START:
                hideDialogLoading();
                isSpeechPause = false;
                btnStopSpeech.setText("停止播放");
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
            speechService.changeSpeed(seekBar.getProgress());
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
                ReadActivity.this.showMsg(trueProgress + "分钟后停止");
                config.setTimingTime(trueProgress);
            }
            showTimer(trueProgress);
        }
    };

    private void changeSpeaker(OfflineResource.Speaker speaker) {
        if (OfflineResource.Speaker.MALE.equals(speaker)) {
            rgSpeaker.check(R.id.speaker2);
        } else if (OfflineResource.Speaker.FEMALE.equals(speaker)) {
            rgSpeaker.check(R.id.speaker1);
        } else if (OfflineResource.Speaker.DUXY.equals(speaker)) {
            rgSpeaker.check(R.id.speaker3);
        } else if (OfflineResource.Speaker.DUYY.equals(speaker)) {
            rgSpeaker.check(R.id.speaker4);
        }
    }


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
        EventBusUtil.sendStickyEvent(new BaseEvent(EventConstants.EVENT_MAIN_REFRESH_BOOK_SHELF));
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
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
