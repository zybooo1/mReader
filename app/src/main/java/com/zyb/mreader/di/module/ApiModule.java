package com.zyb.mreader.di.module;


import com.zyb.base.di.qualifier.CommonUrl;
import com.zyb.mreader.core.http.api.CommonApis;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

/**
 * 提供两种API
 */
@Module
public class ApiModule {

    /**
     * 提供API
     *
     * @return API
     */
    @Provides
    CommonApis provideCommonApis(@CommonUrl Retrofit retrofit) {
        return retrofit.create(CommonApis.class);
    }
}
