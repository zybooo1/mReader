package com.zyb.reader.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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

    @BindView(R2.id.sb_brightness)
    SeekBar sb_brightness;
    @BindView(R2.id.tv_xitong)
    TextView tv_xitong;
    @BindView(R2.id.tv_size)
    TextView tv_size;
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

    @BindView(R2.id.tv_simulation)
    TextView tv_simulation;
    @BindView(R2.id.tv_cover)
    TextView tv_cover;
    @BindView(R2.id.tv_slide)
    TextView tv_slide;
    @BindView(R2.id.tv_none)
    TextView tv_none;


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
        this(context, R.style.reader_setting_dialog);
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

        FONT_SIZE_MIN = (int) getContext().getResources().getDimension(R.dimen.reader_reading_min_text_size);
        FONT_SIZE_MAX = (int) getContext().getResources().getDimension(R.dimen.reader_reading_max_text_size);

        config = Config.getInstance();

        //初始化亮度
        isSystem = config.isSystemLight();
        setTextViewSelect(tv_xitong, isSystem);
        setPageMode(config.getPageMode());
        setBrightness(config.getLight());



        //初始化字体大小
        currentFontSize = (int) config.getFontSize();
        tv_size.setText(currentFontSize + "");

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
        iv_bg_default.setBorderColor(ContextCompat.getColor(getContext(),R.color.white20));
        iv_bg1.setBorderColor(ContextCompat.getColor(getContext(),R.color.white20));
        iv_bg2.setBorderColor(ContextCompat.getColor(getContext(),R.color.white20));
        iv_bg3.setBorderColor(ContextCompat.getColor(getContext(),R.color.white20));
        iv_bg4.setBorderColor(ContextCompat.getColor(getContext(),R.color.white20));
        switch (type) {
            case Config.BOOK_BG_DEFAULT:
                iv_bg_default.setBorderColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
                break;
            case Config.BOOK_BG_1:
                iv_bg1.setBorderColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
                break;
            case Config.BOOK_BG_2:
                iv_bg2.setBorderColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
                break;
            case Config.BOOK_BG_3:
                iv_bg3.setBorderColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
                break;
            case Config.BOOK_BG_4:
                iv_bg4.setBorderColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
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

    //设置亮度
    public void setBrightness(float brightness) {
        sb_brightness.setProgress((int) (brightness * 100));
    }

    //设置按钮选择的背景
    private void setTextViewSelect(TextView textView, Boolean isSelect) {
        if (isSelect) {
            textView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.reader_button_select_bg));
            textView.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
        } else {
            textView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.reader_button_bg));
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


    @OnClick({R2.id.tv_dark, R2.id.tv_bright, R2.id.tv_xitong, R2.id.tv_subtract, R2.id.tv_add, R2.id.tv_size_default,
             R2.id.iv_bg_default, R2.id.iv_bg_1, R2.id.iv_bg_2, R2.id.iv_bg_3, R2.id.iv_bg_4})
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
        }  else if (i == R.id.iv_bg_default) {
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

    @OnClick({R2.id.tv_simulation, R2.id.tv_cover, R2.id.tv_slide, R2.id.tv_none})
    public void onClickPageMode(View view) {
        int i = view.getId();
        if (i == R.id.tv_simulation) {
            setPageMode(Config.PAGE_MODE_SIMULATION);
        } else if (i == R.id.tv_cover) {
            setPageMode(Config.PAGE_MODE_COVER);
        } else if (i == R.id.tv_slide) {
            setPageMode(Config.PAGE_MODE_SLIDE);
        } else if (i == R.id.tv_none) {
            setPageMode(Config.PAGE_MODE_NONE);
        }
    }

    //设置翻页
    public void setPageMode(int pageMode) {
        config.setPageMode(pageMode);
        if (mSettingListener != null) {
            mSettingListener.changePageMode(pageMode);
        }

        setTextViewSelect(tv_simulation, false);
        setTextViewSelect(tv_cover, false);
        setTextViewSelect(tv_slide, false);
        setTextViewSelect(tv_none, false);
        if (pageMode == Config.PAGE_MODE_SIMULATION) {
            setTextViewSelect(tv_simulation, true);
        } else if (pageMode == Config.PAGE_MODE_COVER) {
            setTextViewSelect(tv_cover, true);
        } else if (pageMode == Config.PAGE_MODE_SLIDE) {
            setTextViewSelect(tv_slide, true);
        } else if (pageMode == Config.PAGE_MODE_NONE) {
            setTextViewSelect(tv_none, true);
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
        currentFontSize = (int) getContext().getResources().getDimension(R.dimen.reader_reading_default_text_size);
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

        void changeBookBg(int type);

        void changePageMode(int mode);
    }

}