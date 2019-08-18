package com.zyb.base.base.app;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.multidex.MultiDex;

import com.alibaba.android.arouter.launcher.ARouter;
import com.google.gson.Gson;
import com.hjq.bar.TitleBar;
import com.hjq.toast.ToastUtils;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.tinker.TinkerManager;
import com.tencent.tinker.entry.DefaultApplicationLike;
import com.umeng.commonsdk.UMConfigure;
import com.zyb.base.BuildConfig;
import com.zyb.base.R;
import com.zyb.base.base.bean.UserBean;
import com.zyb.base.di.component.AppComponent;
import com.zyb.base.di.component.DaggerAppComponent;
import com.zyb.base.di.module.AppModule;
import com.zyb.base.di.module.HttpModule;
import com.zyb.base.di.module.ImageModule;
import com.zyb.base.utils.LogUtil;
import com.zyb.base.utils.Utils;
import com.zyb.base.utils.constant.Constants;


/**
 * Application代理
 *
 * @link https://bugly.qq.com/docs/user-guide/instruction-manual-android-hotfix/?v=20181014122344
 */
public class BaseApplicationLike extends DefaultApplicationLike {
    //static 代码段可以防止内存泄露
    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                layout.setPrimaryColorsId(R.color.colorPrimary);//全局设置主题颜色
                return new MaterialHeader(context);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                layout.setPrimaryColorsId(R.color.colorPrimary, R.color.colorPrimary);//全局设置主题颜色
                //指定为经典Footer，默认是 BallPulseFooter
                return new BallPulseFooter(context);
            }
        });
    }

    private AppComponent mAppComponent;
    //application context 实例
    private static Application sInstance;

    public static Application getInstance() {
        return sInstance;
    }

    public BaseApplicationLike(Application application, int tinkerFlags,
                               boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime,
                               long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime,
                applicationStartMillisTime, tinkerResultIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = TinkerManager.getApplication();
        initBugly();


        initDagger();

        ARouter.init(sInstance);
        if (BuildConfig.DEBUG) ARouter.openDebug();

        // 初始化吐司工具类
        ToastUtils.init(sInstance);
        TitleBar.initStyle(new MyTitleBarStyle(getInstance()));


        //init utils
        Utils.init(sInstance);
        /**
         * 注意：如果您已经在AndroidManifest.xml中配置过appkey和channel值，可以调用此版本初始化函数。
         */
        UMConfigure.init(sInstance, UMConfigure.DEVICE_TYPE_PHONE,"");

    }

    private void initDagger() {
        mAppComponent = DaggerAppComponent
                .builder()
                .appModule(new AppModule(sInstance))////提供application
                .httpModule(new HttpModule())//用于提供okhttp和retrofit的单例
                .imageModule(new ImageModule())//图片加载框架默认使用glide
                .build();
        mAppComponent.inject(this);
        Utils.initAppComponent(mAppComponent);
    }



    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);

        Beta.installTinker(this);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallback(
            Application.ActivityLifecycleCallbacks callbacks) {
        getApplication().registerActivityLifecycleCallbacks(callbacks);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Beta.unInit();
    }

    /**
     * 初始化Bugly错误日志上报
     */
    private void initBugly() {
        // 设置是否开启热更新能力，默认为true
//        Beta.enableHotfix = true;
        // 设置是否自动下载补丁，默认为true
//        Beta.canAutoDownloadPatch = true;
        // 设置是否自动合成补丁，默认为true
//        Beta.canAutoPatch = true;
        // 设置是否提示用户重启，默认为false
//        Beta.canNotifyUserRestart = true;
        // 补丁回调接口
//        Beta.betaPatchListener = new BetaPatchListener() {}

        // 设置开发设备，默认为false，上传补丁如果下发范围指定为“开发设备”，需要调用此接口来标识开发设备
        Bugly.setIsDevelopmentDevice(getApplication(), true);
        // 多渠道需求塞入
        // String channel = WalleChannelReader.getChannel(getApplication());
        // Bugly.setAppChannel(getApplication(), channel);
        // 这里实现SDK初始化，appId替换成你的在Bugly平台申请的appId
        if (!BuildConfig.DEBUG) Bugly.init(getApplication(), Constants.BUGLY_ID, false);
    }

}
