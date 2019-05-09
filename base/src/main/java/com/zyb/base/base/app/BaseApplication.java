package com.zyb.base.base.app;

import android.content.Context;
import android.content.res.Resources;
import android.support.multidex.MultiDexApplication;

import com.alibaba.android.arouter.launcher.ARouter;
import com.hjq.bar.TitleBar;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.sinovoice.hcicloudsdk.api.HciCloudSys;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.InitParam;
import com.tencent.bugly.crashreport.CrashReport;
import com.zyb.base.BuildConfig;
import com.zyb.base.R;
import com.zyb.base.di.component.AppComponent;
import com.zyb.base.di.component.DaggerAppComponent;
import com.zyb.base.di.module.AppModule;
import com.zyb.base.di.module.HttpModule;
import com.zyb.base.di.module.ImageModule;
import com.zyb.base.utils.CommonUtils;
import com.zyb.base.utils.LogUtil;
import com.zyb.base.utils.Utils;
import com.zyb.base.utils.constant.Constants;


/**
 * APP
 */
public class BaseApplication extends MultiDexApplication {
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
    private static BaseApplication sInstance;

    public static BaseApplication getInstance() {
        return sInstance;
    }
    public static Resources getAppResources() {
        return sInstance.getResources();
    }
    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;
        mAppComponent = DaggerAppComponent
                .builder()
                .appModule(new AppModule(sInstance))////提供application
                .httpModule(new HttpModule())//用于提供okhttp和retrofit的单例
                .imageModule(new ImageModule())//图片加载框架默认使用glide
                .build();
        mAppComponent.inject(this);
        Utils.initAppComponent(mAppComponent);


        ARouter.init(sInstance);
        if (BuildConfig.DEBUG) ARouter.openDebug();
        initBugly();
        //init utils
        Utils.init(sInstance);
        TitleBar.initStyle(new MyTitleBarStyle(this));
        initHciCloud();
    }

    private void initHciCloud() {
        InitParam initparam = new InitParam();
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_AUTH_PATH, "/storage/emulated/0/myApp/auth");
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_CLOUD_URL, "/api.hcicloud.com:8888");

        String strConfig = initparam.getStringConfig();
        int errCode = HciCloudSys.hciInit(strConfig, this);
        if (errCode != HciErrorCode.HCI_ERR_NONE && errCode != HciErrorCode.HCI_ERR_SYS_ALREADY_INIT) {
            LogUtil.e("\nhciInit error: " + HciCloudSys.hciGetErrorInfo(errCode));
        } else {
            LogUtil.e("\nhciInit success");
        }
    }

    /**
     * 初始化Bugly错误日志上报
     */
    private void initBugly() {
        if (BuildConfig.DEBUG) return;
        // 获取当前包名
        String packageName = getApplicationContext().getPackageName();
        // 获取当前进程名
        String processName = CommonUtils.getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        CrashReport.initCrashReport(getApplicationContext(), Constants.BUGLY_ID, true, strategy);
    }

}
