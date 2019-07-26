package com.zyb.reader.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zyb.base.utils.CommonUtils;
import com.zyb.reader.Config;
import com.zyb.reader.R;
import com.zyb.reader.R2;
import com.zyb.reader.view.CircleImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 阅读设置弹窗
 */
public class SettingDialog extends Dialog {

    @BindView(R2.id.tv_dark)
    TextView tv_dark;
    @BindView(R2.id.sb_brightness)
    SeekBar sb_brightness;
    @BindView(R2.id.tv_bright)
    TextView tv_bright;
    @BindView(R2.id.tv_xitong)
    TextView tv_xitong;
    @BindView(R2.id.tv_subtract)
    TextView tv_subtract;
    @BindView(R2.id.tv_size)
    TextView tv_size;
    @BindView(R2.id.tv_add)
    TextView tv_add;
    @BindView(R2.id.tv_qihei)
    TextView tv_qihei;
    @BindView(R2.id.tv_default)
    TextView tv_default;
    @BindView(R2.id.iv_bg_default)
    CircleImageView iv_bg_default;
    @BindView(R2.id.iv_bg_1)
    CircleImageView iv_bg1;
    @BindView(R2.id.iv_bg_2)
    CircleImageView iv_bg2;
    @BindView(R2.id.iv_bg_3)
    CircleImageView iv_bg3;
    @BindView(R2.id.iv_bg_4)
    CircleImageView iv_bg4;
    @BindView(R2.id.tv_size_default)
    TextView tv_size_default;
    @BindView(R2.id.tv_fzxinghei)
    TextView tv_fzxinghei;
    @BindView(R2.id.tv_fzkatong)
    TextView tv_fzkatong;
    @BindView(R2.id.tv_bysong)
    TextView tv_bysong;


    private Config config;
    private Boolean isSystem;
    private SettingListener mSettingListener;
    private int FONT_SIZE_MIN;
    private int FONT_SIZE_MAX;
    private int currentFontSize;

    private SettingDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    public SettingDialog(Context context) {
        this(context, R.style.setting_dialog);
    }

    public SettingDialog(Context context, int themeResId) {
        super(context, themeResId);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setGravity(Gravity.BOTTOM);
        setContentView(R.layout.reader_dialog_setting);
        // 初始化View注入
        ButterKnife.bind(this);

        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = d.getWidth();
        getWindow().setAttributes(p);

        FONT_SIZE_MIN = (int) getContext().getResources().getDimension(R.dimen.reading_min_text_size);
        FONT_SIZE_MAX = (int) getContext().getResources().getDimension(R.dimen.reading_max_text_size);

        config = Config.getInstance();

        //初始化亮度
        isSystem = config.isSystemLight();
        setTextViewSelect(tv_xitong, isSystem);
        setBrightness(config.getLight());

        //初始化字体大小
        currentFontSize = (int) config.getFontSize();
        tv_size.setText(currentFontSize + "");

        //初始化字体
        tv_default.setTypeface(config.getTypeface(Config.FONTTYPE_DEFAULT));
        tv_qihei.setTypeface(config.getTypeface(Config.FONTTYPE_QIHEI));
//        tv_fzxinghei.setTypeface(config.getTypeface(Config.FONTTYPE_FZXINGHEI));
        tv_fzkatong.setTypeface(config.getTypeface(Config.FONTTYPE_FZKATONG));
        tv_bysong.setTypeface(config.getTypeface(Config.FONTTYPE_BYSONG));
//        tv_xinshou.setTypeface(config.getTypeface(Config.FONTTYPE_XINSHOU));
//        tv_wawa.setTypeface(config.getTypeface(Config.FONTTYPE_WAWA));
        selectTypeface(config.getTypefacePath());

        selectBg(config.getBookBgType());

        sb_brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 10) {
                    changeBright(false, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //选择背景
    private void selectBg(int type) {
        switch (type) {
            case Config.BOOK_BG_DEFAULT:
                iv_bg_default.setBorderWidth(CommonUtils.dp2px( 2));
                iv_bg1.setBorderWidth(CommonUtils.dp2px(0));
                iv_bg2.setBorderWidth(CommonUtils.dp2px(0));
                iv_bg3.setBorderWidth(CommonUtils.dp2px(0));
                iv_bg4.setBorderWidth(CommonUtils.dp2px(0));
                break;
            case Config.BOOK_BG_1:
                iv_bg_default.setBorderWidth(CommonUtils.dp2px( 0));
                iv_bg1.setBorderWidth(CommonUtils.dp2px( 2));
                iv_bg2.setBorderWidth(CommonUtils.dp2px( 0));
                iv_bg3.setBorderWidth(CommonUtils.dp2px( 0));
                iv_bg4.setBorderWidth(CommonUtils.dp2px( 0));
                break;
            case Config.BOOK_BG_2:
                iv_bg_default.setBorderWidth(CommonUtils.dp2px( 0));
                iv_bg1.setBorderWidth(CommonUtils.dp2px( 0));
                iv_bg2.setBorderWidth(CommonUtils.dp2px( 2));
                iv_bg3.setBorderWidth(CommonUtils.dp2px( 0));
                iv_bg4.setBorderWidth(CommonUtils.dp2px( 0));
                break;
            case Config.BOOK_BG_3:
                iv_bg_default.setBorderWidth(CommonUtils.dp2px(0));
                iv_bg1.setBorderWidth(CommonUtils.dp2px(0));
                iv_bg2.setBorderWidth(CommonUtils.dp2px(0));
                iv_bg3.setBorderWidth(CommonUtils.dp2px(2));
                iv_bg4.setBorderWidth(CommonUtils.dp2px(0));
                break;
            case Config.BOOK_BG_4:
                iv_bg_default.setBorderWidth(CommonUtils.dp2px( 0));
                iv_bg1.setBorderWidth(CommonUtils.dp2px( 0));
                iv_bg2.setBorderWidth(CommonUtils.dp2px( 0));
                iv_bg3.setBorderWidth(CommonUtils.dp2px( 0));
                iv_bg4.setBorderWidth(CommonUtils.dp2px( 2));
                break;
        }
    }

    //设置字体
    public void setBookBg(int type) {
        config.setDayOrNight(false);
        config.setBookBg(type);
        if (mSettingListener != null) {
            mSettingListener.changeBookBg(type);
        }
    }

    //选择字体
    private void selectTypeface(String typeface) {
        if (typeface.equals(Config.FONTTYPE_DEFAULT)) {
            setTextViewSelect(tv_default, true);
            setTextViewSelect(tv_qihei, false);
            setTextViewSelect(tv_fzxinghei, false);
            setTextViewSelect(tv_fzkatong, false);
            setTextViewSelect(tv_bysong, false);
//            setTextViewSelect(tv_xinshou, false);
//            setTextViewSelect(tv_wawa, false);
        } else if (typeface.equals(Config.FONTTYPE_QIHEI)) {
            setTextViewSelect(tv_default, false);
            setTextViewSelect(tv_qihei, true);
            setTextViewSelect(tv_fzxinghei, false);
            setTextViewSelect(tv_fzkatong, false);
            setTextViewSelect(tv_bysong, false);
//            setTextViewSelect(tv_xinshou, false);
//            setTextViewSelect(tv_wawa, false);
        } else if (typeface.equals(Config.FONTTYPE_FZXINGHEI)) {
            setTextViewSelect(tv_default, false);
            setTextViewSelect(tv_qihei, false);
            setTextViewSelect(tv_fzxinghei, true);
            setTextViewSelect(tv_fzkatong, false);
            setTextViewSelect(tv_bysong, false);
//            setTextViewSelect(tv_xinshou, true);
//            setTextViewSelect(tv_wawa, false);
        } else if (typeface.equals(Config.FONTTYPE_FZKATONG)) {
            setTextViewSelect(tv_default, false);
            setTextViewSelect(tv_qihei, false);
            setTextViewSelect(tv_fzxinghei, false);
            setTextViewSelect(tv_fzkatong, true);
            setTextViewSelect(tv_bysong, false);
//            setTextViewSelect(tv_xinshou, false);
//            setTextViewSelect(tv_wawa, true);
        } else if (typeface.equals(Config.FONTTYPE_BYSONG)) {
            setTextViewSelect(tv_default, false);
            setTextViewSelect(tv_qihei, false);
            setTextViewSelect(tv_fzxinghei, false);
            setTextViewSelect(tv_fzkatong, false);
            setTextViewSelect(tv_bysong, true);
//            setTextViewSelect(tv_xinshou, false);
//            setTextViewSelect(tv_wawa, true);
        }
    }

    //设置字体
    public void setTypeface(String typeface) {
        config.setTypeface(typeface);
        Typeface tface = config.getTypeface(typeface);
        if (mSettingListener != null) {
            mSettingListener.changeTypeFace(tface);
        }
    }

    //设置亮度
    public void setBrightness(float brightness) {
        sb_brightness.setProgress((int) (brightness * 100));
    }

    //设置按钮选择的背景
    private void setTextViewSelect(TextView textView, Boolean isSelect) {
        if (isSelect) {
            textView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_select_bg));
            textView.setTextColor(getContext().getResources().getColor(R.color.read_dialog_button_select));
        } else {
            textView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_bg));
            textView.setTextColor(getContext().getResources().getColor(R.color.white));
        }
    }

    private void applyCompat() {
        if (Build.VERSION.SDK_INT < 19) {
            return;
        }
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
    }

    public Boolean isShow() {
        return isShowing();
    }


    @OnClick({R2.id.tv_dark, R2.id.tv_bright, R2.id.tv_xitong, R2.id.tv_subtract, R2.id.tv_add, R2.id.tv_size_default, R2.id.tv_qihei, R2.id.tv_fzxinghei, R2.id.tv_fzkatong, R2.id.tv_bysong,
            R2.id.tv_default, R2.id.iv_bg_default, R2.id.iv_bg_1, R2.id.iv_bg_2, R2.id.iv_bg_3, R2.id.iv_bg_4})
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.tv_dark) {
        } else if (i == R.id.tv_bright) {
        } else if (i == R.id.tv_xitong) {
            isSystem = !isSystem;
            changeBright(isSystem, sb_brightness.getProgress());
        } else if (i == R.id.tv_subtract) {
            subtractFontSize();
        } else if (i == R.id.tv_add) {
            addFontSize();
        } else if (i == R.id.tv_size_default) {
            defaultFontSize();
        } else if (i == R.id.tv_qihei) {
            selectTypeface(Config.FONTTYPE_QIHEI);
            setTypeface(Config.FONTTYPE_QIHEI);
        } else if (i == R.id.tv_fzxinghei) {
            selectTypeface(Config.FONTTYPE_FZXINGHEI);
            setTypeface(Config.FONTTYPE_FZXINGHEI);
        } else if (i == R.id.tv_fzkatong) {
            selectTypeface(Config.FONTTYPE_FZKATONG);
            setTypeface(Config.FONTTYPE_FZKATONG);
        } else if (i == R.id.tv_bysong) {
            selectTypeface(Config.FONTTYPE_BYSONG);
            setTypeface(Config.FONTTYPE_BYSONG);
            //            case R.id.tv_xinshou:
//                selectTypeface(Config.FONTTYPE_XINSHOU);
//                setTypeface(Config.FONTTYPE_XINSHOU);
//                break;
//            case R.id.tv_wawa:
//                selectTypeface(Config.FONTTYPE_WAWA);
//                setTypeface(Config.FONTTYPE_WAWA);
//                break;
        } else if (i == R.id.tv_default) {
            selectTypeface(Config.FONTTYPE_DEFAULT);
            setTypeface(Config.FONTTYPE_DEFAULT);
        } else if (i == R.id.iv_bg_default) {
            setBookBg(Config.BOOK_BG_DEFAULT);
            selectBg(Config.BOOK_BG_DEFAULT);
        } else if (i == R.id.iv_bg_1) {
            setBookBg(Config.BOOK_BG_1);
            selectBg(Config.BOOK_BG_1);
        } else if (i == R.id.iv_bg_2) {
            setBookBg(Config.BOOK_BG_2);
            selectBg(Config.BOOK_BG_2);
        } else if (i == R.id.iv_bg_3) {
            setBookBg(Config.BOOK_BG_3);
            selectBg(Config.BOOK_BG_3);
        } else if (i == R.id.iv_bg_4) {
            setBookBg(Config.BOOK_BG_4);
            selectBg(Config.BOOK_BG_4);
        }
    }

    //变大书本字体
    private void addFontSize() {
        if (currentFontSize < FONT_SIZE_MAX) {
            currentFontSize += 1;
            tv_size.setText(currentFontSize + "");
            config.setFontSize(currentFontSize);
            if (mSettingListener != null) {
                mSettingListener.changeFontSize(currentFontSize);
            }
        }
    }

    private void defaultFontSize() {
        currentFontSize = (int) getContext().getResources().getDimension(R.dimen.reading_default_text_size);
        tv_size.setText(currentFontSize + "");
        config.setFontSize(currentFontSize);
        if (mSettingListener != null) {
            mSettingListener.changeFontSize(currentFontSize);
        }
    }

    //变小书本字体
    private void subtractFontSize() {
        if (currentFontSize > FONT_SIZE_MIN) {
            currentFontSize -= 1;
            tv_size.setText(currentFontSize + "");
            config.setFontSize(currentFontSize);
            if (mSettingListener != null) {
                mSettingListener.changeFontSize(currentFontSize);
            }
        }
    }

    //改变亮度
    public void changeBright(Boolean isSystem, int brightness) {
        float light = (float) (brightness / 100.0);
        setTextViewSelect(tv_xitong, isSystem);
        config.setSystemLight(isSystem);
        config.setLight(light);
        if (mSettingListener != null) {
            mSettingListener.changeSystemBright(isSystem, light);
        }
    }

    public void setSettingListener(SettingListener settingListener) {
        this.mSettingListener = settingListener;
    }

    public interface SettingListener {
        void changeSystemBright(Boolean isSystem, float brightness);

        void changeFontSize(int fontSize);

        void changeTypeFace(Typeface typeface);

        void changeBookBg(int type);
    }

}