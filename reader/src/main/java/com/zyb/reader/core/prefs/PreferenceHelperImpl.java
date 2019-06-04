package com.zyb.reader.core.prefs;


import com.zyb.base.base.sp.BaseSharePreference;
import com.zyb.base.utils.CommonUtils;
import com.zyb.reader.widget.page.PageView;

import javax.inject.Inject;

import static com.zyb.base.utils.constant.Constants.READ_BG_DEFAULT;
import static com.zyb.base.utils.constant.Constants.SHARED_READ_BG;
import static com.zyb.base.utils.constant.Constants.SHARED_READ_BRIGHTNESS;
import static com.zyb.base.utils.constant.Constants.SHARED_READ_IS_BRIGHTNESS_AUTO;
import static com.zyb.base.utils.constant.Constants.SHARED_READ_IS_TEXT_DEFAULT;
import static com.zyb.base.utils.constant.Constants.SHARED_READ_NIGHT_MODE;
import static com.zyb.base.utils.constant.Constants.SHARED_READ_PAGE_MODE;
import static com.zyb.base.utils.constant.Constants.SHARED_READ_TEXT_SIZE;
import static com.zyb.base.utils.constant.Constants.TEXT_SIZE_SP_DEFAULT;

/**
 *
 */

public class PreferenceHelperImpl extends BaseSharePreference implements PreferenceHelper {
    @Inject
    public PreferenceHelperImpl() {
        super();
    }

    @Override
    public void setReadBackground(int theme) {
      putInt(SHARED_READ_BG, theme);
    }

    @Override
    public void setBrightness(int progress) {
        putInt(SHARED_READ_BRIGHTNESS, progress);
    }

    @Override
    public void setAutoBrightness(boolean isAuto) {
        putBoolean(SHARED_READ_IS_BRIGHTNESS_AUTO, isAuto);
    }

    @Override
    public void setTextSize(int textSize) {
        putInt(SHARED_READ_TEXT_SIZE, textSize);
    }

    @Override
    public void setPageMode(int mode) {
        putInt(SHARED_READ_PAGE_MODE, mode);
    }

    @Override
    public void setNightMode(boolean isNight) {
        putBoolean(SHARED_READ_NIGHT_MODE, isNight);
    }

    @Override
    public int getBrightness() {
        return getInt(SHARED_READ_BRIGHTNESS, 40);
    }

    @Override
    public boolean isBrightnessAuto() {
        return getBoolean(SHARED_READ_IS_BRIGHTNESS_AUTO, true);
    }

    @Override
    public int getTextSize() {
        return getInt(SHARED_READ_TEXT_SIZE, CommonUtils.sp2px(TEXT_SIZE_SP_DEFAULT));
    }

    @Override
    public boolean isDefaultTextSize() {
        return getBoolean(SHARED_READ_IS_TEXT_DEFAULT, false);
    }

    @Override
    public int getPageMode() {
        return getInt(SHARED_READ_PAGE_MODE, PageView.PAGE_MODE_COVER);
    }

    @Override
    public int getReadBgTheme() {
        return getInt(SHARED_READ_BG, READ_BG_DEFAULT);
    }

    @Override
    public boolean isNightMode() {
        return getBoolean(SHARED_READ_NIGHT_MODE, false);
    }
}
