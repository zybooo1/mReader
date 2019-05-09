package com.zyb.base.di.component;

import android.app.Application;

import com.google.gson.Gson;
import com.zyb.base.base.app.BaseApplication;
import com.zyb.base.di.module.AppModule;
import com.zyb.base.di.module.HttpModule;
import com.zyb.base.di.module.ImageModule;
import com.zyb.base.di.qualifier.CommonUrl;
import com.zyb.base.imageloader.ImageLoader;

import javax.inject.Singleton;

import dagger.Component;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;


@Singleton
@Component(modules = {AppModule.class, HttpModule.class, ImageModule.class})
public interface AppComponent {

    Application Application();

    @CommonUrl
    Retrofit getOutterRetrofit();

    OkHttpClient okHttpClient();

    //图片管理器,用于加载图片的管理类,默认使用glide,使用策略模式,可替换框架
    ImageLoader imageLoader();

    //gson
    Gson gson();

    void inject(BaseApplication application);
}
