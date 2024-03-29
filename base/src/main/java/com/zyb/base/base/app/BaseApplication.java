package com.zyb.base.base.app;


import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.alibaba.android.arouter.launcher.ARouter;
import com.hjq.toast.ToastUtils;
import com.kongzue.dialog.util.DialogSettings;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.tencent.bugly.crashreport.CrashReport;
import com.zyb.base.BuildConfig;
import com.zyb.base.R;
import com.zyb.base.di.component.AppComponent;
import com.zyb.base.di.component.DaggerAppComponent;
import com.zyb.base.di.module.AppModule;
import com.zyb.base.di.module.HttpModule;
import com.zyb.base.umeng.UmengClient;
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
    private static Application sInstance;

    public static Application getInstance() {
        return sInstance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        initBugly();


        initDagger();

        ARouter.init(sInstance);
        if (BuildConfig.DEBUG) ARouter.openDebug();

        // 初始化吐司工具类
        ToastUtils.init(sInstance);
//        TitleBar.initStyle(new MyTitleBarStyle(getInstance()));


        //init utils
        Utils.init(sInstance);

        UmengClient.init(sInstance);

        initDialog();
    }

    private void initDialog() {
        DialogSettings.isUseBlur = true;                   //是否开启模糊效果，默认关闭
        DialogSettings.style = DialogSettings.STYLE.STYLE_IOS;          //全局主题风格，提供三种可选风格，STYLE_MATERIAL, STYLE_KONGZUE, STYLE_IOS
        DialogSettings.theme = DialogSettings.THEME.DARK;          //全局对话框明暗风格，提供两种可选主题，LIGHT, DARK
        DialogSettings.tipTheme = DialogSettings.THEME.DARK;       //全局提示框明暗风格，提供两种可选主题，LIGHT, DARK
//        DialogSettings.titleTextInfo = (TextInfo);              //全局标题文字样式
//        DialogSettings.contentTextInfo = (TextInfo);            //全局正文文字样式
//        DialogSettings.buttonTextInfo = (TextInfo);             //全局默认按钮文字样式
//        DialogSettings.buttonPositiveTextInfo = (TextInfo);     //全局焦点按钮文字样式（一般指确定按钮）
//        DialogSettings.inputInfo = (InputInfo);                 //全局输入框文本样式
//        DialogSettings.backgroundColor = (ColorInt);            //全局对话框背景颜色，值0时不生效
//        DialogSettings.cancelable = (boolean);                  //全局对话框默认是否可以点击外围遮罩区域或返回键关闭，此开关不影响提示框（TipDialog）以及等待框（TipDialog）
//        DialogSettings.cancelableTipDialog = (boolean);         //全局提示框及等待框（WaitDialog、TipDialog）默认是否可以关闭
//        DialogSettings.DEBUGMODE = (boolean);                   //是否允许打印日志
//        DialogSettings.blurAlpha = (int);                       //开启模糊后的透明度（0~255）
//        DialogSettings.systemDialogStyle = (styleResId);        //自定义系统对话框style，注意设置此功能会导致原对话框风格和动画失效
//        DialogSettings.dialogLifeCycleListener = (DialogLifeCycleListener);  //全局Dialog生命周期监听器
//        DialogSettings.defaultCancelButtonText = (String);      //设置 BottomDialog 和 ShareDialog 默认“取消”按钮的文字
//        DialogSettings.tipBackgroundResId = (drawableResId);    //设置 TipDialog 和 WaitDialog 的背景资源
//        DialogSettings.tipTextInfo = (InputInfo);               //设置 TipDialog 和 WaitDialog 文字样式
    }

    private void initDagger() {
        mAppComponent = DaggerAppComponent
                .builder()
                .appModule(new AppModule(sInstance))////提供application
                .httpModule(new HttpModule())//用于提供okhttp和retrofit的单例
                .build();
        mAppComponent.inject(this);
        Utils.initAppComponent(mAppComponent);
    }


    /**
     * 初始化Bugly错误日志上报
     */
    private void initBugly() {
        if (!BuildConfig.DEBUG) CrashReport.initCrashReport(getApplicationContext(), Constants.BUGLY_ID, false);
    }

}
