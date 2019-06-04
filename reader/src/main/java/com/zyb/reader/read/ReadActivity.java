package com.zyb.reader.read;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hjq.bar.TitleBar;
import com.xw.repo.VectorCompatTextView;
import com.zyb.base.base.activity.MVPActivity;
import com.zyb.base.base.app.BaseApplication;
import com.zyb.base.di.component.AppComponent;
import com.zyb.base.utils.LogUtil;
import com.zyb.base.utils.QMUIViewHelper;
import com.zyb.base.utils.constant.Constants;
import com.zyb.base.widget.RoundButton;
import com.zyb.base.widget.decoration.VerticalItemLineDecoration;
import com.zyb.common.db.MyOpenHelper;
import com.zyb.common.db.bean.CollBookBean;
import com.zyb.common.db.bean.DaoMaster;
import com.zyb.reader.R;
import com.zyb.reader.R2;
import com.zyb.reader.di.component.DaggerActivityComponent;
import com.zyb.reader.di.module.ActivityModule;
import com.zyb.reader.di.module.ApiModule;
import com.zyb.reader.read.adapter.ReadCategoryAdapter;
import com.zyb.reader.utils.BrightnessUtils;
import com.zyb.reader.widget.dialog.ReadSettingDialog;
import com.zyb.reader.widget.page.PageLoader;
import com.zyb.reader.widget.page.PageView;
import com.zyb.reader.widget.page.TxtChapter;

import org.greenrobot.greendao.database.Database;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ReadActivity extends MVPActivity<ReadPresenter> implements ReadContract.View {
    private static final int ANIM_HIDE_DURATION = 200;
    private static final int ANIM_SHOW_DURATION = 400;
    @BindView(R2.id.titleBar)
    TitleBar titleBar;
    @BindView(R2.id.pv_read_page)   //阅读界面
    PageView mPvReadPage;
    @BindView(R2.id.read_tv_page_tip)   //页数提示
    RoundButton mReadTvPageTip;
    @BindView(R2.id.read_tv_pre_chapter)   //上一章
    TextView mReadTvPreChapter;
    @BindView(R2.id.read_sb_chapter_progress)   //章数进度
    SeekBar mReadSbChapterProgress;
    @BindView(R2.id.read_tv_next_chapter)   //下一张
    TextView mReadTvNextChapter;
    @BindView(R2.id.read_tv_night_mode)   //日夜间模式
    VectorCompatTextView mReadTvNightMode;
    @BindView(R2.id.read_ll_bottom_menu)   //底部菜单
    LinearLayout mReadLlBottomMenu;
    @BindView(R2.id.rv_read_category)   //侧边章节目录
    RecyclerView mRvReadCategory;
    @BindView(R2.id.read_dl_slide)
    DrawerLayout mReadDlSlide;

    private static final String TAG = "ReadActivity";
    //注册 Brightness 的 uri
    private final Uri BRIGHTNESS_MODE_URI =
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE);
    private final Uri BRIGHTNESS_URI =
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
    private final Uri BRIGHTNESS_ADJ_URI =
            Settings.System.getUriFor("screen_auto_brightness_adj");

    public static final String EXTRA_COLL_BOOK = "extra_coll_book";
    public static final String EXTRA_IS_COLLECTED = "extra_is_collected";

    private boolean isRegistered = false;
    //设置弹窗
    private ReadSettingDialog mSettingDialog;
    private PageLoader mPageLoader;
    //书籍
    private CollBookBean mCollBook;
    //控制屏幕常亮
    private PowerManager.WakeLock mWakeLock;
    private boolean isCollected = false;   //是否已在书架（数据库）
    private boolean isNightMode = false;   //是否是夜间模式
    ReadCategoryAdapter mReadCategoryAdapter;
    List<TxtChapter> mTxtChapters = new ArrayList<>();

    // 接收电池信息和时间更新的广播
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra("level", 0);
                mPageLoader.updateBattery(level);
            }
            //监听分钟的变化
            else if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                mPageLoader.updateTime();
            }
        }
    };

    //亮度调节监听
    //由于亮度调节没有 Broadcast 而是直接修改 ContentProvider 的。所以需要创建一个 Observer 来监听 ContentProvider 的变化情况。
    private ContentObserver mBrightObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange);

            //判断当前是否跟随屏幕亮度，如果不是则返回
            if (selfChange || !mSettingDialog.isBrightFollowSystem()) return;

            //如果系统亮度改变，则修改当前 Activity 亮度
            if (BRIGHTNESS_MODE_URI.equals(uri)) {
                Log.d(TAG, "亮度模式改变");
            } else if (BRIGHTNESS_URI.equals(uri) && !BrightnessUtils.isAutoBrightness(ReadActivity.this)) {
                Log.d(TAG, "亮度模式为手动模式 值改变");
                BrightnessUtils.setBrightness(ReadActivity.this, BrightnessUtils.getScreenBrightness(ReadActivity.this));
            } else if (BRIGHTNESS_ADJ_URI.equals(uri) && BrightnessUtils.isAutoBrightness(ReadActivity.this)) {
                Log.d(TAG, "亮度模式为自动模式 值改变");
                BrightnessUtils.setBrightness(ReadActivity.this, BrightnessUtils.getScreenBrightness(ReadActivity.this));
            } else {
                Log.d(TAG, "亮度调整 其他");
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.reader_activity_read;
    }

    @Override
    protected int getTitleBarId() {
        return R.id.titleBar;
    }


    @Override
    protected void initView() {
        mCollBook = (CollBookBean) getIntent().getSerializableExtra(EXTRA_COLL_BOOK);
        isCollected = getIntent().getBooleanExtra(EXTRA_IS_COLLECTED, false);
        isNightMode = mPresenter.isNightMode();

        setTitle(mCollBook.getTitle());
        //获取页面加载器
        mPageLoader = mPvReadPage.getPageLoader();
        mReadDlSlide.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        //更多设置dialog
        mSettingDialog = new ReadSettingDialog(this, mPageLoader);

        setCategory();

        toggleNightMode();

        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mReceiver, intentFilter);

        //设置当前Activity的Brightness
        if (mPresenter.isBrightnessAuto()) {
            BrightnessUtils.setBrightness(this, BrightnessUtils.getScreenBrightness(this));
        } else {
            BrightnessUtils.setBrightness(this, mPresenter.getBrightness());
        }

        //初始化屏幕常亮类
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "mreader:keep_bright");
        }

        mPageLoader.setOnPageChangeListener(new PageLoader.OnPageChangeListener() {

            @Override
            public void onCategoryFinish(List<TxtChapter> chapters) {
                LogUtil.e("==========onCategoryFinish");
                mTxtChapters.clear();
                mTxtChapters.addAll(chapters);
                mReadCategoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPageCountChange(int count) {
                LogUtil.e("==========onPageCountChange");
                mReadSbChapterProgress.setEnabled(true);
                mReadSbChapterProgress.setMax(count - 1);
                mReadSbChapterProgress.setProgress(0);
            }

            @Override
            public void onPageChange(int pos) {
                LogUtil.e("==========onPageChange");
                mReadSbChapterProgress.post(() -> {
                    mReadSbChapterProgress.setProgress(pos);
                });
            }
        });


        mReadSbChapterProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mReadLlBottomMenu.getVisibility() == VISIBLE) {
                    mReadTvPageTip.setText(String.format(Locale.CHINA, "%d/%d",
                            progress + 1, mReadSbChapterProgress.getMax() + 1));
                    mReadTvPageTip.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //进行切换
                int pagePos = mReadSbChapterProgress.getProgress();
                LogUtil.e("onStopTrackingTouch:"+pagePos);
                if (pagePos != mPageLoader.getPagePos()) {
                    mPageLoader.skipToPage(pagePos);
                }
                //隐藏提示
                mReadTvPageTip.setVisibility(GONE);
            }
        });

        mPvReadPage.setTouchListener(new PageView.TouchListener() {
            @Override
            public void center() {
                toggleMenu();
            }

            @Override
            public boolean onTouch() {
                return !hideReadMenu();
            }

            @Override
            public boolean prePage() {
                return true;
            }

            @Override
            public boolean nextPage() {
                return true;
            }

            @Override
            public void cancel() { }
        });
    }

    @Override
    protected void initData() {
        mPageLoader.openBook(mCollBook);
    }


    private void setCategory() {
        mRvReadCategory.setLayoutManager(new LinearLayoutManager(this));
        mRvReadCategory.addItemDecoration(new VerticalItemLineDecoration(this));
        mReadCategoryAdapter = new ReadCategoryAdapter(mTxtChapters);
        mRvReadCategory.setAdapter(mReadCategoryAdapter);

        if (mTxtChapters.size() > 0) {
            setCategorySelect(0);
        }

        mReadCategoryAdapter.setOnItemClickListener((adapter, view, position) -> {
            setCategorySelect(position);
            mReadDlSlide.closeDrawer(Gravity.START);
            mPageLoader.skipToChapter(position);
        });

    }

    /**
     * 设置选中目录
     */
    private void setCategorySelect(int selectPos) {
        for (int i = 0; i < mTxtChapters.size(); i++) {
            TxtChapter chapter = mTxtChapters.get(i);
            if (i == selectPos) {
                chapter.setSelect(true);
            } else {
                chapter.setSelect(false);
            }
        }

        mReadCategoryAdapter.notifyDataSetChanged();
    }

    private void toggleNightMode() {
        if (isNightMode) {
            mReadTvNightMode.setText(getString(R.string.wy_mode_morning));
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.svg_brightness_up);
            mReadTvNightMode.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
        } else {
            mReadTvNightMode.setText(getString(R.string.wy_mode_night));
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.svg_night);
            mReadTvNightMode.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
        }
    }


    /**
     * 隐藏阅读界面的菜单显示
     *
     * @return 是否隐藏成功
     */
    private boolean hideReadMenu() {
        if (titleBar.getVisibility() == VISIBLE) {
            toggleMenu();
            return true;
        } else if (mSettingDialog.isShowing()) {
            mSettingDialog.dismiss();
            return true;
        }
        return false;
    }

    /**
     * 切换菜单栏的可视状态
     * 默认是隐藏的
     */
    private void toggleMenu() {
        if (titleBar.getVisibility() == View.VISIBLE) {
            mReadTvPageTip.setVisibility(GONE);
            QMUIViewHelper.slideOut(titleBar, ANIM_HIDE_DURATION, QMUIViewHelper.QMUIDirection.BOTTOM_TO_TOP);
            QMUIViewHelper.slideOut(mReadLlBottomMenu, ANIM_HIDE_DURATION, QMUIViewHelper.QMUIDirection.TOP_TO_BOTTOM);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏
        } else {
            QMUIViewHelper.slideIn(titleBar, ANIM_SHOW_DURATION, QMUIViewHelper.QMUIDirection.TOP_TO_BOTTOM);
            QMUIViewHelper.slideIn(mReadLlBottomMenu, ANIM_SHOW_DURATION, QMUIViewHelper.QMUIDirection.BOTTOM_TO_TOP);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏
        }
    }


    @OnClick({R2.id.read_tv_pre_chapter, R2.id.read_tv_next_chapter, R2.id.read_tv_category,
            R2.id.read_tv_night_mode, R2.id.read_tv_setting})
    public void onViewClicked(View view) {
        int i = view.getId();
        if (i == R.id.read_tv_pre_chapter) {
            setCategorySelect(mPageLoader.skipPreChapter());
        } else if (i == R.id.read_tv_next_chapter) {
            setCategorySelect(mPageLoader.skipNextChapter());
        } else if (i == R.id.read_tv_category) {
            //切换菜单
            toggleMenu();
            //打开侧滑动栏
            mReadDlSlide.openDrawer(Gravity.START);
        } else if (i == R.id.read_tv_night_mode) {
            isNightMode = !isNightMode;
            mPageLoader.setNightMode(isNightMode);
            toggleNightMode();
        } else if (i == R.id.read_tv_setting) {
            toggleMenu();
            mSettingDialog.show();
        }
    }

    //注册亮度观察者
    private void registerBrightObserver() {
        try {
            if (mBrightObserver != null) {
                if (!isRegistered) {
                    final ContentResolver cr = getContentResolver();
                    cr.unregisterContentObserver(mBrightObserver);
                    cr.registerContentObserver(BRIGHTNESS_MODE_URI, false, mBrightObserver);
                    cr.registerContentObserver(BRIGHTNESS_URI, false, mBrightObserver);
                    cr.registerContentObserver(BRIGHTNESS_ADJ_URI, false, mBrightObserver);
                    isRegistered = true;
                }
            }
        } catch (Throwable throwable) {
            Log.e(TAG, "[ouyangyj] register mBrightObserver error! " + throwable);
        }
    }

    //解注册
    private void unregisterBrightObserver() {
        try {
            if (mBrightObserver != null) {
                if (isRegistered) {
                    getContentResolver().unregisterContentObserver(mBrightObserver);
                    isRegistered = false;
                }
            }
        } catch (Throwable throwable) {
            Log.e(TAG, "unregister BrightnessObserver error! " + throwable);
        }
    }

    @Override
    public void onBackPressed() {
        if (titleBar.getVisibility() == View.VISIBLE) {
            toggleMenu();
            return;
        } else if (mSettingDialog.isShowing()) {
            mSettingDialog.dismiss();
            return;
        } else if (mReadDlSlide.isDrawerOpen(Gravity.START)) {
            mReadDlSlide.closeDrawer(Gravity.START);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBrightObserver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mWakeLock!=null)mWakeLock.acquire(10*60*1000L /*10 minutes*/);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mWakeLock!=null)mWakeLock.release();
        if (isCollected) {
            mPageLoader.saveRecord();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterBrightObserver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mPageLoader.closeBook();
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
}
