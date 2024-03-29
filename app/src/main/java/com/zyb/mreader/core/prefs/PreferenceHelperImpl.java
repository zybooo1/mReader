package com.zyb.mreader.core.prefs;


import com.zyb.base.base.sp.BaseSharePreference;
import com.zyb.base.utils.constant.Constants;
import com.zyb.mreader.utils.FileUtils;

import javax.inject.Inject;

/**
 *
 */

public class PreferenceHelperImpl extends BaseSharePreference implements PreferenceHelper {
    @Inject
    public PreferenceHelperImpl() {
        super();
    }

    @Override
    public void setIsFilterENfiles(boolean isFilterENfiles) {
        putBoolean(Constants.IS_FILTER_EN_FILE, isFilterENfiles);
    }

    @Override
    public void setFilterSize(long filterSize) {
        putLong(Constants.FILTER_SIZE, filterSize);

    }

    @Override
    public boolean getIsFilterENfiles() {
        return getBoolean(Constants.IS_FILTER_EN_FILE, FileUtils.IS_FILTER_EN_FILES);
    }

    @Override
    public long getFilterSize() {
        return getLong(Constants.FILTER_SIZE, FileUtils.MIN_TXT_FILE_SIZE);

    }

    @Override
    public boolean isShowedContract() {
        return getBoolean(Constants.IS_SHOWED_CONTRACT, false);
    }

    @Override
    public void setIsShowedContract(boolean isShowedContract) {
        putBoolean(Constants.IS_SHOWED_CONTRACT, isShowedContract);
    }

    @Override
    public boolean isFirst() {
        return getBoolean(Constants.IS_FIRST_IN_APP, true);
    }

    @Override
    public void setIsFirst(boolean isFirst) {
        putBoolean(Constants.IS_FIRST_IN_APP, isFirst);
    }

    @Override
    public String getWebDavUserName() {
        return getString(Constants.WEBDAV_USER_NAME, "");
    }

    @Override
    public void setWebDavUserName(String s) {
        putString(Constants.WEBDAV_USER_NAME, s);
    }

    @Override
    public String getWebDavPassword() {
        return getString(Constants.WEBDAV_PASSWORD, "");

    }

    @Override
    public void setWebDavPassword(String s) {
        putString(Constants.WEBDAV_PASSWORD, s);
    }

    @Override
    public String getWebDavHost() {
        return getString(Constants.WEBDAV_HOST, "");

    }

    @Override
    public void setWebDavHost(String s) {
        putString(Constants.WEBDAV_HOST, s);
    }
}
