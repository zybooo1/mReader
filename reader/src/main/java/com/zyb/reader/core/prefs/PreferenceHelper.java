package com.zyb.reader.core.prefs;


import com.zyb.base.base.sp.BasePreferenceHelper;
import com.zyb.base.utils.CommonUtils;
import com.zyb.reader.widget.page.PageView;

/**
 */
public interface PreferenceHelper extends BasePreferenceHelper {

    void setReadBackground(int theme);

    void setBrightness(int progress);

    void setAutoBrightness(boolean isAuto);

    void setTextSize(int textSize);

    void setPageMode(int mode);

    void setNightMode(boolean isNight);

    int getBrightness();

    boolean isBrightnessAuto();

    int getTextSize();
    boolean isDefaultTextSize();

    int getPageMode();

    int getReadBgTheme();

    boolean isNightMode();
}
