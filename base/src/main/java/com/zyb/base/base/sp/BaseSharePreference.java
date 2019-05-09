package com.zyb.base.base.sp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.zyb.base.base.app.BaseApplication;
import com.zyb.base.base.bean.UserBean;
import com.zyb.base.utils.constant.Constants;

import javax.inject.Inject;

import static com.zyb.base.utils.constant.Constants.BASE_SHARED_PREFERENCE;


/**
 * Base SharePreference
 */
public class BaseSharePreference {

    private final SharedPreferences mPreferences;

    @Inject
    public BaseSharePreference() {
        mPreferences = BaseApplication.getInstance().getSharedPreferences(BASE_SHARED_PREFERENCE, Context.MODE_PRIVATE);
    }

}
