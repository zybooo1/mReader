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
    PreferenceHelperImpl() {
        super();
    }

    @Override
    public void setIsFilterENfiles(boolean isFilterENfiles) {
        putBoolean(Constants.IS_FILTER_EN_FILE,isFilterENfiles);
    }

    @Override
    public void setFilterSize(long filterSize) {
        putLong(Constants.FILTER_SIZE,filterSize);

    }

    @Override
    public boolean getIsFilterENfiles() {
        return getBoolean(Constants.IS_FILTER_EN_FILE,false);
    }

    @Override
    public long getFilterSize() {
        return getLong(Constants.FILTER_SIZE, FileUtils.MIN_TXT_FILE_SIZE);

    }
}
