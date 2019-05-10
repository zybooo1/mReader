package com.zyb.reader.core;


import com.zyb.base.mvp.BaseDataManager;
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

}
