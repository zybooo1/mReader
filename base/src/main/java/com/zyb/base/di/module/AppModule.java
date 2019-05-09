package com.zyb.base.di.module;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zyb.base.http.gson.NullStringToEmptyAdapterFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class AppModule {

    private Application mApplication;

    public AppModule(Application application) {
        this.mApplication = application;
    }

    @Singleton
    @Provides
    public Application provideApplication() {
        return mApplication;
    }

    @Singleton
    @Provides
    Gson provideGson(NullStringToEmptyAdapterFactory nullStringToEmptyAdapterFactory) {
        return new GsonBuilder()
                .registerTypeAdapterFactory(nullStringToEmptyAdapterFactory)
                .create();
    }

    @Singleton
    @Provides
    NullStringToEmptyAdapterFactory provideNullStringToEmptyAdapterFactory() {
        return new NullStringToEmptyAdapterFactory();
    }
}
