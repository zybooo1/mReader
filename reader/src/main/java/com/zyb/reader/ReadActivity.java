package com.zyb.reader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.gyf.barlibrary.ImmersionBar;
import com.xw.repo.VectorCompatTextView;
import com.zyb.base.base.activity.MyActivity;
import com.zyb.base.base.fragment.BaseFragmentStateAdapter;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.utils.LogUtil;
import com.zyb.base.utils.QMUIViewHelper;
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
import com.zyb.reader.util.FileUtils;
import com.zyb.reader.util.PageFactory;
import com.zyb.reader.view.PageWidget;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 阅读界面
 */
public class ReadActivity extends MyActivity implements SpeechSynthesizerListener {
    private static final int ANIM_HIDE_DURATION = 200;
    private static final int ANIM_SHOW_DURATION = 400;

    private static final String TAG = "ReadActivity";
    public final static String EXTRA_BOOK = "bookList";
    private final static int MESSAGE_CHANGEPROGRESS = 1;

    @BindView(R2.id.bookpage)
    PageWidget bookpage;
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
    @BindView(R2.id.tv_stop_read)
    TextView tv_stop_read;
    @BindView(R2.id.rl_read_bottom)
    RelativeLayout rl_read_bottom;

    private Config config;
    private WindowManager.LayoutParams lp;
    private Book book;
    private PageFactory pageFactory;
    // popwindow是否显示
    private SettingDialog mSettingDialog;
    private PageModeDialog mPageModeDialog;
    private Boolean mDayOrNight;
    // 语音合成客户端
    private SpeechSynthesizer mSpeechSynthesizer;
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
        return R.layout.activity_read;
    }

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
        }
    }

    /**
     * 将sample工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    private void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String mSampleDirPath;

    private void initialEnv() {
        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + FileUtils.SAMPLE_DIR_NAME;
        }
        makeDir(mSampleDirPath);
        copyFromAssetsToSdcard(false,  FileUtils.SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/" +  FileUtils.SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false,  FileUtils.SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/" +  FileUtils.SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false,  FileUtils.TEXT_MODEL_NAME, mSampleDirPath + "/" +  FileUtils.TEXT_MODEL_NAME);
//        copyFromAssetsToSdcard(false, LICENSE_FILE_NAME, mSampleDirPath + "/" + LICENSE_FILE_NAME);
    }

    private void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public String getTTPath() {
        return mSampleDirPath;
    }

    @Override
    protected void initView() {
        ImmersionBar.setTitleBarMarginTop(this, rlTopBar);
        //禁止手势滑动
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        Config.createConfig(this);
        PageFactory.createPageFactory(this);

        initialEnv();

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

        bookpage.setPageMode(config.getPageMode());
        bookpage.post(new Runnable() {
            @Override
            public void run() {
                pageFactory.setPageWidget(bookpage);

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
                    Toast.makeText(ReadActivity.this, "打开电子书失败", Toast.LENGTH_SHORT).show();
                }

                initDayOrNight();
            }
        });

        sb_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            float pro;

            // 拖动
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

        mPageModeDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                hideSystemUI();
            }
        });

        mPageModeDialog.setPageModeListener(new PageModeDialog.PageModeListener() {
            @Override
            public void changePageMode(int pageMode) {
                bookpage.setPageMode(pageMode);
            }
        });

        mSettingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                hideSystemUI();
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
            }
        });

        pageFactory.setPageEvent(new PageFactory.PageEvent() {
            @Override
            public void changeProgress(float progress) {
                Message message = new Message();
                message.what = MESSAGE_CHANGEPROGRESS;
                message.obj = progress;
                mHandler.sendMessage(message);
            }
        });

        bookpage.setTouchListener(new PageWidget.TouchListener() {
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

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_CHANGEPROGRESS:
                    float progress = (float) msg.obj;
                    setSeekBarProgress(progress);
                    break;
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        if (!getMenuIsShowing()) {
            hideSystemUI();
        }
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.resume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageFactory.clear();
        bookpage = null;
        unregisterReceiver(myReceiver);
        isSpeaking = false;
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.release();
        }
    }

    @OnClick(R2.id.btnAddBookMark)
    public void clickAddBookMark() {
        if (pageFactory.getCurrentPage() != null) {
            // TODO: 2019/7/13
//List<BookMarks> bookMarksList = DataSupport.where("bookpath = ? and begin = ?", pageFactory.getBookPath(),pageFactory.getCurrentPage().getBegin() + "").find(BookMarks.class);
            List<BookMarks> bookMarksList = DBFactory.getInstance().getBookMarksManage()
                    .getQueryBuilder()
                    .where(BookMarksDao.Properties.Bookpath.eq(pageFactory.getBookPath()), BookMarksDao.Properties.Begin.eq(pageFactory.getCurrentPage().getBegin()))
                    .list();

            if (!bookMarksList.isEmpty()) {
                Toast.makeText(ReadActivity.this, "该书签已存在", Toast.LENGTH_SHORT).show();
            } else {
                BookMarks bookMarks = new BookMarks();
                String word = "";
                for (String line : pageFactory.getCurrentPage().getLines()) {
                    word += line;
                }
                try {
                    SimpleDateFormat sf = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm ss");
                    String time = sf.format(new Date());
                    bookMarks.setId(pageFactory.getBookPath() + pageFactory.getCurrentPage().getBegin());
                    bookMarks.setTime(time);
                    bookMarks.setBegin(pageFactory.getCurrentPage().getBegin());
                    bookMarks.setText(word);
                    bookMarks.setBookpath(pageFactory.getBookPath());
                    DBFactory.getInstance().getBookMarksManage().insertOrUpdate(bookMarks);

                    Toast.makeText(ReadActivity.this, "书签添加成功", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ReadActivity.this, "添加书签失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @OnClick(R2.id.btnStartSpeech)
    public void clickStartSpeech() {
        initialTts();
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "5");
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
            //                mSpeechSynthesizer.setParam(SpeechSynthesizer. MIX_MODE_DEFAULT);
            //                mSpeechSynthesizer.setParam(SpeechSynthesizer. AUDIO_ENCODE_AMR);
            //                mSpeechSynthesizer.setParam(SpeechSynthesizer. AUDIO_BITRA TE_AMR_15K85);
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOCODER_OPTIM_LEVEL, "0");
            int result = mSpeechSynthesizer.speak(pageFactory.getCurrentPage().getLineToString());
            if (result < 0) {
                LogUtil.e(TAG, "error,please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122 ");
            } else {
                toggleMenu();
                isSpeaking = true;
            }
        }
    }

    @OnClick(R2.id.ivBack)
    public void clickBack() {
        finish();
    }

    public static boolean openBook(final Book book, Activity context) {
        if (book == null) {
            throw new NullPointerException("BookList can not be null");
        }

        Intent intent = new Intent(context, ReadActivity.class);
        intent.putExtra(EXTRA_BOOK, book);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        context.startActivity(intent);
        return true;
    }


    public void initDayOrNight() {
        mDayOrNight = config.getDayOrNight();
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

    //改变显示模式
    public void changeDayOrNight() {
        if (mDayOrNight) {
            mDayOrNight = false;
            tv_dayornight.setText(getResources().getString(R.string.read_setting_night));
        } else {
            mDayOrNight = true;
            tv_dayornight.setText(getResources().getString(R.string.read_setting_day));
        }
        config.setDayOrNight(mDayOrNight);
        pageFactory.setDayOrNight(mDayOrNight);
    }

    public void setSeekBarProgress(float progress) {
        sb_progress.setProgress((int) (progress * 10000));
    }

    /**
     * 切换菜单栏的可视状态
     * 默认是隐藏的
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
            hideSystemUI();
        } else {
            QMUIViewHelper.slideIn(rlTopRoot, ANIM_SHOW_DURATION, QMUIViewHelper.QMUIDirection.TOP_TO_BOTTOM);
            QMUIViewHelper.slideIn(rl_bottom, ANIM_SHOW_DURATION, QMUIViewHelper.QMUIDirection.BOTTOM_TO_TOP);
            showSystemUI();
        }
    }

    private boolean getMenuIsShowing() {
        return rlTopRoot.getVisibility() == View.VISIBLE;
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

    private void initialTts() {
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        this.mSpeechSynthesizer.setContext(this);
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(this);
        // 文本模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, getTTPath() + "/"
                +  FileUtils.TEXT_MODEL_NAME);
        // 声学模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, getTTPath() + "/"
                +  FileUtils.SPEECH_FEMALE_MODEL_NAME);
        // 本地授权文件路径,如未设置将使用默认路径.设置临时授权文件路径，LICENCE_FILE_NAME请替换成临时授权文件的实际路径，仅在使用临时license文件时需要进行设置，如果在[应用管理]中开通了正式离线授权，不需要设置该参数，建议将该行代码删除（离线引擎）
        // 如果合成结果出现临时授权文件将要到期的提示，说明使用了临时授权文件，请删除临时授权即可。
        //        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_LICENCE_FILE, ((AppContext)getApplication()).getTTPath() + "/"
        //                + AppContext.LICENSE_FILE_NAME);
        // 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        this.mSpeechSynthesizer.setAppId("16840271"/*这里只是为了让Demo运行使用的APPID,请替换成自己的id。*/);
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        this.mSpeechSynthesizer.setApiKey("jpG13VVTMaWnjZ1C2n1KjsRG",
                "eqR2dWhQZsfiCf2ZPZj39OgkDC3isE3W"/*这里只是为了让Demo正常运行使用APIKey,请替换成自己的APIKey*/);
        // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置Mix模式的合成策略
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI);
        // 授权检测接口(只是通过AuthInfo进行检验授权是否成功。)
        // AuthInfo接口用于测试开发者是否成功申请了在线或者离线授权，如果测试授权成功了，可以删除AuthInfo部分的代码（该接口首次验证时比较耗时），不会影响正常使用（合成使用时SDK内部会自动验证授权）
        AuthInfo authInfo = this.mSpeechSynthesizer.auth(TtsMode.MIX);

        if (authInfo.isSuccess()) {
            LogUtil.e(TAG, "auth success");
        } else {
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            LogUtil.e(TAG, "auth failed errorMsg=" + errorMsg);
        }

        // 初始化tts
        mSpeechSynthesizer.initTts(TtsMode.MIX);
    }

    @OnClick({R2.id.tv_pre, R2.id.sb_progress, R2.id.tv_next, R2.id.tv_directory,
            R2.id.tv_dayornight, R2.id.tv_pagemode, R2.id.tv_setting, R2.id.bookpop_bottom,
            R2.id.rl_bottom, R2.id.tv_stop_read})
    public void onClick(View view) {
        int i = view.getId();//            case R.id.btn_return:
        if (i == R.id.tv_pre) {
            pageFactory.preChapter();
        } else if (i == R.id.sb_progress) {
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
        } else if (i == R.id.bookpop_bottom) {
        } else if (i == R.id.rl_bottom) {
        } else if (i == R.id.tv_stop_read) {
            if (mSpeechSynthesizer != null) {
                mSpeechSynthesizer.stop();
                isSpeaking = false;
                toggleMenu();
            }
        }
    }

    /*
     * @param arg0
     */
    @Override
    public void onSynthesizeStart(String s) {

    }

    /**
     * 合成数据和进度的回调接口，分多次回调
     *
     * @param utteranceId
     * @param data        合成的音频数据。该音频数据是采样率为16K，2字节精度，单声道的pcm数据。
     * @param progress    文本按字符划分的进度，比如:你好啊 进度是0-3
     */
    @Override
    public void onSynthesizeDataArrived(String utteranceId, byte[] data, int progress) {

    }

    /**
     * 合成正常结束，每句合成正常结束都会回调，如果过程中出错，则回调onError，不再回调此接口
     *
     * @param utteranceId
     */
    @Override
    public void onSynthesizeFinish(String utteranceId) {

    }

    /**
     * 播放开始，每句播放开始都会回调
     *
     * @param utteranceId
     */
    @Override
    public void onSpeechStart(String utteranceId) {

    }

    /**
     * 播放进度回调接口，分多次回调
     *
     * @param utteranceId
     * @param progress    文本按字符划分的进度，比如:你好啊 进度是0-3
     */
    @Override
    public void onSpeechProgressChanged(String utteranceId, int progress) {

    }

    /**
     * 播放正常结束，每句播放正常结束都会回调，如果过程中出错，则回调onError,不再回调此接口
     *
     * @param utteranceId
     */
    @Override
    public void onSpeechFinish(String utteranceId) {
        pageFactory.nextPage();
        if (pageFactory.islastPage()) {
            isSpeaking = false;
            Toast.makeText(ReadActivity.this, "小说已经读完了", Toast.LENGTH_SHORT);
        } else {
            isSpeaking = true;
            mSpeechSynthesizer.speak(pageFactory.getCurrentPage().getLineToString());
        }
    }

    /**
     * 当合成或者播放过程中出错时回调此接口
     *
     * @param utteranceId
     * @param error       包含错误码和错误信息
     */
    @Override
    public void onError(String utteranceId, SpeechError error) {
        mSpeechSynthesizer.stop();
        isSpeaking = false;
        LogUtil.e(TAG, error.description);
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
