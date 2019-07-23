package com.zyb.mreader;

import android.view.View;
import android.view.ViewTreeObserver;

import com.gyf.barlibrary.BarHide;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.ToastUtils;
import com.zyb.base.base.activity.MyActivity;
import com.zyb.base.utils.RxUtil;
import com.zyb.mreader.module.main.MainActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 启动界面
 */
public class LauncherActivity extends MyActivity
        implements OnPermission, ViewTreeObserver.OnGlobalLayoutListener {
    private static final int DELAY_TIME = 500;

    @BindView(R.id.iv_launcher_bg)
    View mImageView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_launcher;
    }

    @Override
    protected int getTitleBarId() {
        return 0;
    }

    @Override
    protected void initView() {
        //设置状态栏和导航栏参数
        getStatusBarConfig()
                .fullScreen(true)//有导航栏的情况下，activity全屏显示，也就是activity最下面被导航栏覆盖，不写默认非全屏
                .hideBar(BarHide.FLAG_HIDE_STATUS_BAR)//隐藏状态栏
                .transparentNavigationBar()//透明导航栏，不写默认黑色(设置此方法，fullScreen()方法自动为true)
                .init();
        Disposable disposable = Observable.just(true)
                .delay(DELAY_TIME, TimeUnit.MILLISECONDS)
                .compose(RxUtil.rxObservableSchedulerHelper())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        requestPermission();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                });
    }

    @Override
    protected void initData() {
    }

    private void requestPermission() {
        XXPermissions.with(this)
                .permission(Permission.Group.STORAGE)
                .request(this);
    }

    /**
     * {@link OnPermission}
     */
    @Override
    public void hasPermission(List<String> granted, boolean isAll) {
     toMain();
    }

    private void toMain() {
        startActivityFinish(MainActivity.class);
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
    }

    @Override
    public void noPermission(List<String> denied, boolean quick) {
        if (quick) {
            ToastUtils.show("没有获取必要权限，请手动授予权限");
            XXPermissions.gotoPermissionSettings(LauncherActivity.this, true);
        } else {
            ToastUtils.show("请先授予应用权限");
            getWindow().getDecorView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestPermission();
                }
            }, 2000);
        }
    }

    @Override
    public void onBackPressed() {
        //禁用返回键
        //super.onBackPressed();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (XXPermissions.isHasPermission(LauncherActivity.this, Permission.Group.STORAGE)) {
            hasPermission(null, true);
        } else {
            requestPermission();
        }
    }


}