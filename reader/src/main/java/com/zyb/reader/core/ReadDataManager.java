package com.zyb.reader.core;


import com.zyb.base.mvp.BaseDataManager;
import com.zyb.common.db.bean.CollBookBean;
import com.zyb.reader.core.db.DbHelper;
import com.zyb.reader.core.db.GreenDaoHelper;
import com.zyb.reader.core.http.HttpHelper;
import com.zyb.reader.core.http.RetrofitHelper;
import com.zyb.reader.core.prefs.PreferenceHelper;
import com.zyb.reader.core.prefs.PreferenceHelperImpl;

import javax.inject.Inject;

/**
 * 数据处理中心
 */

public class ReadDataManager implements HttpHelper, PreferenceHelper, DbHelper, BaseDataManager {
    private HttpHelper mHttpHelper;
    private PreferenceHelper mPreferenceHelper;
    private GreenDaoHelper mGreenDaoHelper;

    @Inject
    public ReadDataManager(RetrofitHelper httpHelper, PreferenceHelperImpl preferencesHelper, GreenDaoHelper greenDaoHelper) {
        mHttpHelper = httpHelper;
        mPreferenceHelper = preferencesHelper;
        mGreenDaoHelper = greenDaoHelper;
    }

    @Override
    public void saveBook(CollBookBean book) {
        mGreenDaoHelper.saveBook(book);
    }

    @Override
    public void setReadBackground(int theme) {
        mPreferenceHelper.setReadBackground(theme);
    }

    @Override
    public void setBrightness(int progress) {
        mPreferenceHelper.setBrightness(progress);
    }

    @Override
    public void setAutoBrightness(boolean isAuto) {
        mPreferenceHelper.setAutoBrightness(isAuto);
    }

    @Override
    public void setTextSize(int textSize) {
        mPreferenceHelper.setTextSize(textSize);
    }

    @Override
    public void setPageMode(int mode) {
        mPreferenceHelper.setPageMode(mode);
    }

    @Override
    public void setNightMode(boolean isNight) {
        mPreferenceHelper.setNightMode(isNight);
    }

    @Override
    public int getBrightness() {
        return mPreferenceHelper.getBrightness();
    }

    @Override
    public boolean isBrightnessAuto() {
        return mPreferenceHelper.isBrightnessAuto();
    }

    @Override
    public int getTextSize() {
        return mPreferenceHelper.getTextSize();
    }

    @Override
    public boolean isDefaultTextSize() {
        return mPreferenceHelper.isDefaultTextSize();
    }

    @Override
    public int getPageMode() {
        return mPreferenceHelper.getPageMode();
    }

    @Override
    public int getReadBgTheme() {
        return mPreferenceHelper.getReadBgTheme();
    }

    @Override
    public boolean isNightMode() {
        return mPreferenceHelper.isNightMode();
    }
}
