package com.zyb.base.umeng;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2019/04/03
 *    desc   : 友盟客户端
 */
public final class UmengClient {

    /**
     * 初始化友盟相关 SDK
     */
    public static void init(Application application) {

        try {
            Bundle metaData = application.getPackageManager().getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA).metaData;
            //注意：如果您已经在AndroidManifest.xml中配置过appkey和channel值，可以调用此版本初始化函数。
            // 友盟统计(appkey & channel 已在manifest配置，这里不必再传）
            UMConfigure.init(application,UMConfigure.DEVICE_TYPE_PHONE,"");
            // 初始化各个平台的 Key
            PlatformConfig.setWeixin(String.valueOf(metaData.get("WX_APPID")), String.valueOf(metaData.get("WX_APPKEY")));
            PlatformConfig.setQQZone(String.valueOf(metaData.get("QQ_APPID")), String.valueOf(metaData.get("QQ_APPKEY")));
            PlatformConfig.setSinaWeibo(String.valueOf(metaData.get("SN_APPID")), String.valueOf(metaData.get("SN_APPKEY")),"http://sns.whalecloud.com");

            // 选用AUTO页面采集模式，Activity不用手动采集，非Activity需要手动采集
            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Activity 统计
     */
    public static void onResume(Activity activity) {
        // 友盟统计
        MobclickAgent.onResume(activity);
    }

    /**
     * Activity 统计
     */
    public static void onPause(Activity activity) {
        // 友盟统计
        MobclickAgent.onPause(activity);
    }

    /**
     * Fragment 统计
     */
    public static void onResume(Fragment fragment) {
        // 友盟统计
        MobclickAgent.onPageStart(fragment.getClass().getSimpleName());
    }

    /**
     * Fragment 统计
     */
    public static void onPause(Fragment fragment) {
        // 友盟统计
        MobclickAgent.onPageEnd(fragment.getClass().getSimpleName());
    }

    /**
     * 分享
     *
     * @param activity              Activity对象
     * @param platform              分享平台
     * @param data                  分享内容
     * @param listener              分享监听
     */
    public static void share(Activity activity, Platform platform, UmengShare.ShareData data, UmengShare.OnShareListener listener) {
        if (isAppInstalled(activity, platform.getPackageName())) {
            new ShareAction(activity)
                    .setPlatform(platform.getThirdParty())
                    .withMedia(data.create())
                    .setCallback(listener != null ? new UmengShare.ShareListenerWrapper(platform.getThirdParty(), listener) : null)
                    .share();
        } else {
            // 当分享的平台软件可能没有被安装的时候
            if (listener != null) {
                listener.onError(platform, new PackageManager.NameNotFoundException("Is not installed"));
            }
        }
    }

    /**
     * 登录
     *
     * @param activity              Activity对象
     * @param platform              登录平台
     * @param listener              登录监听
     */
    public static void login(Activity activity, Platform platform, UmengLogin.OnLoginListener listener) {
        if (isAppInstalled(activity, platform.getPackageName())) {

            try {
                // 删除旧的第三方登录授权
                UMShareAPI.get(activity).deleteOauth(activity, platform.getThirdParty(), null);
                // 要先等上面的代码执行完毕之后
                Thread.sleep(200);
                // 开启新的第三方登录授权
                UMShareAPI.get(activity).getPlatformInfo(activity, platform.getThirdParty(), listener != null ? new UmengLogin.LoginListenerWrapper(platform.getThirdParty(), listener) : null);
            } catch (InterruptedException ignored) {}

        } else {
            // 当登录的平台软件可能没有被安装的时候
            if (listener != null) {
                listener.onError(platform, new PackageManager.NameNotFoundException("Is not installed"));
            }
        }
    }

    /**
     * 设置回调
     */
    public static void onActivityResult(Activity activity, int requestCode, int resultCode, @Nullable Intent data) {
        UMShareAPI.get(activity).onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 判断 App 是否安装
     */
    private static boolean isAppInstalled(Context context, @NonNull final String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}