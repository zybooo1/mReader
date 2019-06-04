package com.zyb.reader.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zyb.base.utils.CommonUtils;
import com.zyb.reader.R;
import com.zyb.reader.R2;
import com.zyb.reader.core.bean.ReadBgBean;
import com.zyb.reader.core.prefs.PreferenceHelperImpl;
import com.zyb.reader.read.adapter.ReadBgAdapter;
import com.zyb.reader.utils.BrightnessUtils;
import com.zyb.reader.widget.page.PageLoader;
import com.zyb.reader.widget.page.PageView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.zyb.base.utils.constant.Constants.TEXT_SIZE_SP_DEFAULT;

/**
 * Created by newbiechen on 17-5-18.
 */

public class ReadSettingDialog extends Dialog {
    private static final String TAG = "ReadSettingDialog";

    int[] colorBg = {R.color.color_cec29c, R.color.color_ccebcc,
            R.color.color_aaa, R.color.color_d1cec5, R.color.color_001c27};

    @BindView(R2.id.read_setting_iv_brightness_minus)
    ImageView mIvBrightnessMinus;
    @BindView(R2.id.read_setting_sb_brightness)
    SeekBar mSbBrightness;
    @BindView(R2.id.read_setting_iv_brightness_plus)
    ImageView mIvBrightnessPlus;
    @BindView(R2.id.read_setting_cb_brightness_auto)
    CheckBox mCbBrightnessAuto;
    @BindView(R2.id.read_setting_tv_font_minus)
    TextView mTvFontMinus;
    @BindView(R2.id.read_setting_tv_font)
    TextView mTvFont;
    @BindView(R2.id.read_setting_tv_font_plus)
    TextView mTvFontPlus;
    @BindView(R2.id.read_setting_cb_font_default)
    CheckBox mCbFontDefault;
    @BindView(R2.id.read_setting_rg_page_mode)
    RadioGroup mRgPageMode;

    @BindView(R2.id.read_setting_rb_simulation)
    RadioButton mRbSimulation;
    @BindView(R2.id.read_setting_rb_cover)
    RadioButton mRbCover;
    @BindView(R2.id.read_setting_rb_slide)
    RadioButton mRbSlide;
    @BindView(R2.id.read_setting_rb_scroll)
    RadioButton mRbScroll;
    @BindView(R2.id.read_setting_rb_none)
    RadioButton mRbNone;
    @BindView(R2.id.read_setting_rv_bg)
    RecyclerView mRvBg;
    /************************************/
    PreferenceHelperImpl preferenceHelper =new PreferenceHelperImpl();

    private ReadBgAdapter mReadBgAdapter;
    private PageLoader mPageLoader;
    private Activity mActivity;

    private int mBrightness;
    private boolean isBrightnessAuto;
    private int mTextSize;
    private boolean isTextDefault;
    private int mPageMode;
    private int mReadBgTheme;
    private List<ReadBgBean> mReadBgBeans = new ArrayList<>();


    public ReadSettingDialog(@NonNull Activity activity, PageLoader mPageLoader) {
        super(activity, R.style.ReadSettingDialog);
        mActivity = activity;
        this.mPageLoader = mPageLoader;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_dialog_read_setting);
        ButterKnife.bind(this);
        setUpWindow();
        initData();
        initWidget();
        initClick();
    }

    //设置Dialog显示的位置
    private void setUpWindow() {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
    }

    private void initData() {
        isBrightnessAuto = preferenceHelper.isBrightnessAuto();
        mBrightness = preferenceHelper.getBrightness();
        mTextSize = preferenceHelper.getTextSize();
        isTextDefault = preferenceHelper.isDefaultTextSize();
        mPageMode = preferenceHelper.getPageMode();
        mReadBgTheme = preferenceHelper.getReadBgTheme();
    }

    private void initWidget() {
        mSbBrightness.setProgress(mBrightness);
        mTvFont.setText(mTextSize + "");
        mCbBrightnessAuto.setChecked(isBrightnessAuto);
        mCbFontDefault.setChecked(isTextDefault);
        initPageMode();
        //RecyclerView
        setUpAdapter();
    }

    private void setUpAdapter() {
        setReadBg(0);
        mReadBgAdapter = new ReadBgAdapter(mReadBgBeans);
        //横向列表
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        mRvBg.setLayoutManager(linearLayoutManager);
        mRvBg.setLayoutManager(new GridLayoutManager(getContext(), 5));
        mRvBg.setAdapter(mReadBgAdapter);

    }

    /**
     * 设置选择背景数据
     *
     * @param selectPos 选中下标
     */
    private void setReadBg(int selectPos) {
        mReadBgBeans.clear();
        for (int i = 0; i < colorBg.length; i++) {
            ReadBgBean readBgBean = new ReadBgBean();
            readBgBean.setBgColor(colorBg[i]);
            if (i == selectPos) {
                readBgBean.setSelect(true);
            } else {
                readBgBean.setSelect(false);
            }
            mReadBgBeans.add(readBgBean);
        }
    }

    private void initPageMode() {
        switch (mPageMode) {
            case PageView.PAGE_MODE_SIMULATION:
                mRbSimulation.setChecked(true);
                break;
            case PageView.PAGE_MODE_COVER:
                mRbCover.setChecked(true);
                break;
            case PageView.PAGE_MODE_SLIDE:
                mRbSlide.setChecked(true);
                break;
            case PageView.PAGE_MODE_NONE:
                mRbNone.setChecked(true);
                break;
            case PageView.PAGE_MODE_SCROLL:
                mRbScroll.setChecked(true);
                break;
        }
    }

    private Drawable getDrawable(int drawRes) {
        return ContextCompat.getDrawable(getContext(), drawRes);
    }

    private void initClick() {
        //亮度调节
        mIvBrightnessMinus.setOnClickListener(
                (v) -> {
                    if (mCbBrightnessAuto.isChecked()) {
                        mCbBrightnessAuto.setChecked(false);
                    }
                    int progress = mSbBrightness.getProgress() - 1;
                    if (progress < 0) return;
                    mSbBrightness.setProgress(progress);
                    BrightnessUtils.setBrightness(mActivity, progress);
                }
        );
        mIvBrightnessPlus.setOnClickListener(
                (v) -> {
                    if (mCbBrightnessAuto.isChecked()) {
                        mCbBrightnessAuto.setChecked(false);
                    }
                    int progress = mSbBrightness.getProgress() + 1;
                    if (progress > mSbBrightness.getMax()) return;
                    mSbBrightness.setProgress(progress);
                    BrightnessUtils.setBrightness(mActivity, progress);
                    //设置进度
                    preferenceHelper.setBrightness(progress);
                }
        );

        mSbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (mCbBrightnessAuto.isChecked()) {
                    mCbBrightnessAuto.setChecked(false);
                }
                //设置当前 Activity 的亮度
                BrightnessUtils.setBrightness(mActivity, progress);
                //存储亮度的进度条
                preferenceHelper.setBrightness(progress);
            }
        });

        mCbBrightnessAuto.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        //获取屏幕的亮度
                        BrightnessUtils.setBrightness(mActivity, BrightnessUtils.getScreenBrightness(mActivity));
                    } else {
                        //获取进度条的亮度
                        BrightnessUtils.setBrightness(mActivity, mSbBrightness.getProgress());
                    }
                    preferenceHelper.setAutoBrightness(isChecked);
                }
        );

        //字体大小调节
        mTvFontMinus.setOnClickListener(
                (v) -> {
                    if (mCbFontDefault.isChecked()) {
                        mCbFontDefault.setChecked(false);
                    }
                    int fontSize = Integer.valueOf(mTvFont.getText().toString()) - 1;
                    if (fontSize < 0) return;
                    mTvFont.setText(fontSize + "");
                    mPageLoader.setTextSize(fontSize);
                }
        );

        mTvFontPlus.setOnClickListener(
                (v) -> {
                    if (mCbFontDefault.isChecked()) {
                        mCbFontDefault.setChecked(false);
                    }
                    int fontSize = Integer.valueOf(mTvFont.getText().toString()) + 1;
                    mTvFont.setText(fontSize + "");
                    mPageLoader.setTextSize(fontSize);
                }
        );

        mCbFontDefault.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        int fontSize = CommonUtils.dp2px(TEXT_SIZE_SP_DEFAULT);
                        mTvFont.setText(fontSize + "");
                        mPageLoader.setTextSize(fontSize);
                    }
                }
        );

        //Page Mode 切换
        mRgPageMode.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        int pageMode = 0;
                        if (checkedId == R.id.read_setting_rb_simulation) {
                            pageMode = PageView.PAGE_MODE_SIMULATION;
                        } else if (checkedId == R.id.read_setting_rb_cover) {
                            pageMode = PageView.PAGE_MODE_COVER;
                        } else if (checkedId == R.id.read_setting_rb_slide) {
                            pageMode = PageView.PAGE_MODE_SLIDE;
                        } else if (checkedId == R.id.read_setting_rb_scroll) {
                            pageMode = PageView.PAGE_MODE_SCROLL;
                        } else if (checkedId == R.id.read_setting_rb_none) {
                            pageMode = PageView.PAGE_MODE_NONE;
                        }
                        mPageLoader.setPageMode(pageMode);
                    }
                }
        );

        //背景的点击事件
        mReadBgAdapter.setOnItemClickListener((adapter, view, position) -> {
            mPageLoader.setBgColor(position);
            setReadBg(position);
            adapter.notifyDataSetChanged();
        });

    }

    public boolean isBrightFollowSystem() {
        if (mCbBrightnessAuto == null) {
            return false;
        }
        return mCbBrightnessAuto.isChecked();
    }
}
