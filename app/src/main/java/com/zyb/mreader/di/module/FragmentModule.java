package com.zyb.mreader.di.module;



import com.zyb.base.di.scope.FragmentScope;
import com.zyb.base.mvp.BaseView;

import dagger.Module;
import dagger.Provides;

/**
 */
@Module
public class FragmentModule {

    private BaseView view;

    public FragmentModule(BaseView view) {
        this.view = view;
    }

    @FragmentScope
    @Provides
    BaseView providerFragment() {
        return this.view;
    }

}
