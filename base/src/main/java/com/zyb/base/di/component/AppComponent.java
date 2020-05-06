package com.zyb.base.di.component;

import android.app.Application;

import com.google.gson.Gson;
import com.zyb.base.base.app.BaseApplication;
import com.zyb.base.di.module.AppModule;
import com.zyb.base.di.module.HttpModule;
import com.zyb.base.di.qualifier.CommonUrl;

import javax.inject.Singleton;

import dagger.Component;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;


@Singleton
@Component(modules = {AppModule.class, HttpModule.class})
public interface AppComponent {

    Application Application();

    @CommonUrl
    Retrofit getOutterRetrofit();

    OkHttpClient okHttpClient();

    //gson
    Gson gson();

    void inject(BaseApplication application);
}
