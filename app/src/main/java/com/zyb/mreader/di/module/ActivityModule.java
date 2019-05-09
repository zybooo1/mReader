package com.zyb.mreader.di.module;



import com.zyb.base.base.activity.BaseActivity;
import com.zyb.base.di.scope.ActivityScope;

import dagger.Module;
import dagger.Provides;

/**
 */
@Module
public class ActivityModule {

    private BaseActivity mActivity;

    public ActivityModule(BaseActivity activity) {
        this.mActivity = activity;
    }

    @Provides
    @ActivityScope
    BaseActivity provideActivity() {
        return mActivity;
    }

}
