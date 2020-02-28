package com.zyb.mreader.core.prefs;


import com.zyb.base.base.sp.BasePreferenceHelper;

/**
 *
 */
public interface PreferenceHelper extends BasePreferenceHelper {
    void setIsFilterENfiles(boolean isFilterENfiles);

    void setFilterSize(long filterSize);

    boolean getIsFilterENfiles();

    long getFilterSize();

    boolean isShowedContract();

    void setIsShowedContract(boolean isShowedContract);
    boolean isFirst();

    void setIsFirst(boolean isFirst);
}
