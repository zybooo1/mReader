package com.zyb.reader;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.hjq.toast.ToastUtils;
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
import com.zyb.base.widget.RoundButton;
import com.zyb.common.db.DBFactory;
import com.zyb.common.db.bean.Book;
import com.zyb.common.db.bean.BookMarks;
import com.zyb.common.db.bean.BookMarksDao;
import com.zyb.reader.dialog.PageModeDialog;
import com.zyb.reader.dialog.SettingDialog;
import com.zyb.reader.fragment.BookMarkFragment;
import com.zyb.reader.fragment.CatalogFragment;
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
import butterknife.OnClick;


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
    @BindView(R2.id.tv_stop_read)
    TextView tv_stop_read;
    @BindView(R2.id.rl_read_bottom)
    ConstraintLayout rl_read_bottom;

    private Config config;
    private Book book;
    private PageFactory pageFactory;
    private SettingDialog mSettingDialog;
    private PageModeDialog mPageModeDialog;
    private Boolean mDayOrNight;
    private boolean isSpeaking = false;

    // 接收电池信息更新的广播
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                LogUtil.e(TAG, Intent.ACTION_BATTERY_CHANGED);
                int level = intent.getIntExtra("level", 0);
                pageFactory.updateBattery(level);
            } else if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                LogUtil.e(TAG, Intent.ACTION_TIME_TICK);
                pageFactory.updateTime();
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.reader_activity_read;
    }

    @Override
    protected void initView() {
        ImmersionBar.setTitleBarMarginTop(this, rlTopBar);
        //禁止手势滑动
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        Config.createConfig(this);
        PageFactory.createPageFactory(this);

        config = Config.getInstance();
        pageFactory = PageFactory.getInstance();

        IntentFilter mfilter = new IntentFilter();
        mfilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mfilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(myReceiver, mfilter);

        mSettingDialog = new SettingDialog(this);
        mPageModeDialog = new PageModeDialog(this);
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

        pageWidget.setPageMode(config.getPageMode());
        pageWidget.post(new Runnable() {
            @Override
            public void run() {
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
                mDayOrNight = config.getDayOrNight();
                toggleDayOrNight();
            }
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

        // TODO: 2019/7/25
//        mPageModeDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialog) {
//                hideSystemUI();
//            }
//        });

        mPageModeDialog.setPageModeListener(new PageModeDialog.PageModeListener() {
            @Override
            public void changePageMode(int pageMode) {
                pageWidget.setPageMode(pageMode);
            }
        });

        // TODO: 2019/7/25
//        mSettingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialog) {
//                hideSystemUI();
//            }
//        });

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
            }
        });

        pageFactory.setPageEvent(new PageFactory.PageEvent() {
            @Override
            public void changeProgress(float progress) {
//                Message message = new Message();
//                message.what = MESSAGE_CHANGEPROGRESS;
//                message.obj = progress;
//                mHandler.sendMessage(message);
                setSeekBarProgress(progress);
            }
        });

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
                if (pageFactory.isfirstPage()) {
                    return false;
                }

                return true;
            }

            @Override
            public Boolean nextPage() {
                LogUtil.e("setTouchListener", "nextPage");
                if (getMenuIsShowing() || isSpeaking) {
                    return false;
                }

                pageFactory.nextPage();
                if (pageFactory.islastPage()) {
                    return false;
                }
                return true;
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

    @Override
    protected void initData() {

    }

//    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case MESSAGE_CHANGEPROGRESS:
//                    float progress = (float) msg.obj;
//                    setSeekBarProgress(progress);
//                    break;
//            }
//        }
//    };


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
            String word = "";
            for (String line : pageFactory.getCurrentPage().getLines()) {
                word += line;
            }
            bookMarks.setId(pageFactory.getBookPath() + pageFactory.getCurrentPage().getBegin());
            bookMarks.setTime(TimeUtil.parseDateTime(System.currentTimeMillis()));
            bookMarks.setBegin(pageFactory.getCurrentPage().getBegin());
            bookMarks.setText(word);
            bookMarks.setBookpath(pageFactory.getBookPath());
            DBFactory.getInstance().getBookMarksManage().insertOrUpdate(bookMarks);
            showMsg("书签添加成功");
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
        mDayOrNight = !mDayOrNight;
        toggleDayOrNight();
        config.setDayOrNight(mDayOrNight);
        pageFactory.setDayOrNight(mDayOrNight);
    }

    /**
     * 切换日夜间模式按钮UI
     */
    public void toggleDayOrNight() {
        if (mDayOrNight) {
            tv_dayornight.setText(getResources().getString(R.string.read_setting_day));
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.svg_brightness_up);
            tv_dayornight.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);

        } else {
            tv_dayornight.setText(getResources().getString(R.string.read_setting_night));
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.svg_night);
            tv_dayornight.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
        }
    }

    /**
     * 设置阅读进度条进度
     */
    public void setSeekBarProgress(float progress) {
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
    // TODO: 2019/7/25 考虑朗读菜单、设置菜单
    private boolean getMenuIsShowing() {
        return rlTopRoot.getVisibility() == View.VISIBLE;
    }

    @OnClick({R2.id.tv_pre, R2.id.tv_next, R2.id.tv_directory, R2.id.tv_dayornight,
            R2.id.tv_pagemode, R2.id.tv_setting, R2.id.bookpop_bottom, R2.id.rl_bottom,
            R2.id.tv_stop_read, R2.id.ivSearch})
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
        } else if (i == R.id.tv_pagemode) {
            toggleMenu();
            mPageModeDialog.show();
        } else if (i == R.id.tv_setting) {
            toggleMenu();
            mSettingDialog.show();
        } else if (i == R.id.tv_stop_read) {
            stopSpeechService();
            toggleMenu();
        } else if (i == R.id.ivSearch) {
            drawerLayout.openDrawer(Gravity.END);
            toggleMenu();
        }

    }

    /**
     * 侧滑
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
     * 朗读
     */
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
        Intent intent = new Intent(this, SpeechService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        //开始朗读
        BaseEvent<String> event = new BaseEvent<>(EventConstants.EVENT_SPEECH_STRING_DATA,
                pageFactory.getCurrentPage().getLineToString());
        EventBusUtil.sendStickyEvent(event);
    }

    /**
     * 停止朗读
     */
    private void stopSpeechService() {
        if (serviceConnection != null && speechService != null) {
            unbindService(serviceConnection);
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
                isSpeaking = false;
                toggleMenu();
                break;
            case EventConstants.EVENT_SPEECH_FINISH_PAGE:
                pageFactory.nextPage();
                if (pageFactory.islastPage()) {
                    isSpeaking = false;
                    toggleMenu();
                    showMsg("小说已经读完了");
                } else {
                    isSpeaking = true;
                    //继续朗读
                    BaseEvent<String> e = new BaseEvent<>(EventConstants.EVENT_SPEECH_STRING_DATA,
                            pageFactory.getCurrentPage().getLineToString());
                    EventBusUtil.sendStickyEvent(e);
                }
                break;
        }
    }

    /**
     * 朗读设置
     */
    @BindView(R2.id.tvSpeaker)
    TextView tvSpeaker;
    @BindView(R2.id.sbSpeed)
    SeekBar sbSpeed;
    @BindView(R2.id.sbTiming)
    SeekBar sbTiming;
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

        }
    };

    @OnClick(R2.id.speakerChangeLeft)
    public void speakerChangeLeft() {
        ToastUtils.show("aaaaaaa");
        speechService.switchVoice(OfflineResource.VOICE_DUYY);
    }

    @OnClick(R2.id.speakerChangeRight)
    public void speakerChangeRight() {
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
        super.onDestroy();
        pageFactory.clear();
        pageWidget = null;
        unregisterReceiver(myReceiver);
        stopSpeechService();
    }

    @Override
    public void onBackPressed() {
        if (getMenuIsShowing()) {
            toggleMenu();
            return;
        }
        if (mSettingDialog.isShowing()) {
            mSettingDialog.hide();
            return;
        }
        if (mPageModeDialog.isShowing()) {
            mPageModeDialog.hide();
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
