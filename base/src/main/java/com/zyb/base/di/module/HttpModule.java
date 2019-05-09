package com.zyb.base.di.module;



import com.google.gson.Gson;
import com.zyb.base.BuildConfig;
import com.zyb.base.di.qualifier.CommonUrl;
import com.zyb.base.utils.CommonUtils;
import com.zyb.base.utils.constant.ApiConstants;
import com.zyb.base.utils.constant.MemoryConstants;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 */
@Module
public class HttpModule {

    private static final int CONNECT_TIME_OUT = 15;
    private static final int READ_WRITE_TIME_OUT = 25;

    @Singleton
    @Provides
    @CommonUrl
    Retrofit provideOutterRetrofit(Retrofit.Builder builder, OkHttpClient client, Gson gson) {
        return builder
                .baseUrl(ApiConstants.BASE_OUTTER_URL)//域名
                .client(client)//设置okhttp
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//使用rxjava
                .addConverterFactory(GsonConverterFactory.create(gson))//使用自定义Gson，处理空字符串问题
                .build();
    }


    /**
     * 提供OkhttpClient
     */
    @Singleton
    @Provides
    OkHttpClient provideClient(OkHttpClient.Builder builder,Interceptor cacheInterceptor) {
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }
        //设置缓存
        File cacheFile = new File(MemoryConstants.PATH_CACHE);
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 50);
        builder.addNetworkInterceptor(cacheInterceptor);
        builder.addInterceptor(cacheInterceptor);
        builder.cache(cache);
        //设置超时
        builder.connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS);
        builder.readTimeout(READ_WRITE_TIME_OUT, TimeUnit.SECONDS);
        builder.writeTimeout(READ_WRITE_TIME_OUT, TimeUnit.SECONDS);
        //错误重连
        builder.retryOnConnectionFailure(true);
        return builder.build();
    }


    @Singleton
    @Provides
    Retrofit.Builder provideRetrofitBuilder() {
        return new Retrofit.Builder();
    }


    @Singleton
    @Provides
    OkHttpClient.Builder provideClientBuilder() {
        return new OkHttpClient.Builder();
    }

    @Singleton
    @Provides
    Interceptor provideCacheInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                if (!CommonUtils.isNetworkConnected()) {
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
//                            .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                            .build();
                }
//                request = request.newBuilder()
//                        .removeHeader("Content-Type")
//                        .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
//                        .build();
                Response response = chain.proceed(request);
                if (CommonUtils.isNetworkConnected()) {
                    int maxAge = 0;
                    // 有网络时, 不缓存, 最大保存时长为0
                    response.newBuilder()
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .removeHeader("Pragma")
                            .build();
                } else {
                    // 无网络时，设置超时为4周
                    int maxStale = 60 * 60 * 24 * 28;
                    response.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .removeHeader("Pragma")
                            .build();
                }
                return response;
            }
        };
    }

}
