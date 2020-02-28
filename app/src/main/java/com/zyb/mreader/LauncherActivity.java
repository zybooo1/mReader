package com.zyb.mreader;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.ToastUtils;
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialog.util.BaseDialog;
import com.kongzue.dialog.util.DialogSettings;
import com.kongzue.dialog.v3.MessageDialog;
import com.zyb.base.base.activity.MyActivity;
import com.zyb.base.router.RouterConstants;
import com.zyb.base.router.RouterUtils;
import com.zyb.base.utils.RxUtil;
import com.zyb.base.utils.constant.Constants;
import com.zyb.base.widget.WebActivity;
import com.zyb.common.db.DBFactory;
import com.zyb.common.db.bean.Book;
import com.zyb.mreader.core.prefs.PreferenceHelperImpl;
import com.zyb.mreader.module.main.MainActivity;
import com.zyb.reader.util.FileUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * 启动界面
 */
public class LauncherActivity extends MyActivity
        implements OnPermission, ViewTreeObserver.OnGlobalLayoutListener {
    private static final int DELAY_TIME = 200;

    @Inject
    PreferenceHelperImpl preferenceHelper;

    @BindView(R.id.iv_launcher_bg)
    View mImageView;
    private Disposable disposable;

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
        preferenceHelper = new PreferenceHelperImpl();

        //第一次进入 添加介绍txt
        if (preferenceHelper.isFirst()) {
            addInductionTxt();
            preferenceHelper.setIsFirst(false);
        }

        disposable = Observable.just(true)
                .delay(DELAY_TIME, TimeUnit.MILLISECONDS)
                .compose(RxUtil.rxObservableSchedulerHelper())
                .subscribe(aBoolean -> checkIsShowDialog(), throwable -> {
                });
    }

    @Override
    public boolean statusBarDarkFont() {
        return false;
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

    private void checkIsShowDialog() {
        if (!preferenceHelper.isShowedContract()) {
            showContract();
            return;
        }
        requestPermission();

    }

    private void showContract() {
        View view = getLayoutInflater().inflate(R.layout.dialog_permission, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        view.findViewById(R.id.tvProtocol).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterUtils.getInstance().build(RouterConstants.PATH_BASE_ATY_WEB_VIEW)
                        .withString(WebActivity.URL_FLAG, Constants.PROTOCOL_HTML)
                        .navigation();
            }
        });
        view.findViewById(R.id.tvPrivacy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterUtils.getInstance().build(RouterConstants.PATH_BASE_ATY_WEB_VIEW)
                        .withString(WebActivity.URL_FLAG, Constants.PRIVACY_HTML)
                        .navigation();
            }
        });

        MessageDialog.build(this)
                .setCustomView(view)
                .setTheme(DialogSettings.THEME.LIGHT)
                .setCancelable(false)
                .setTitle("欢迎使用猫豆阅读")
                .setMessage(null)
                .setOkButton("同意", new OnDialogButtonClickListener() {
                    @Override
                    public boolean onClick(BaseDialog baseDialog, View v) {
                        preferenceHelper.setIsShowedContract(true);
                        checkIsShowDialog();
                        return false;
                    }
                })
                .setCancelButton("不同意", new OnDialogButtonClickListener() {
                    @Override
                    public boolean onClick(BaseDialog baseDialog, View v) {
                        finish();
                        return true;
                    }
                }).show();
    }

    private void toMain() {
        startActivityFinish(MainActivity.class);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void noPermission(List<String> denied, boolean quick) {
        if (quick) {
            ToastUtils.show("缺少必要权限，请手动授予权限");
            XXPermissions.gotoPermissionSettings(LauncherActivity.this, true);
        } else {
            ToastUtils.show("请先授予应用权限");
            getWindow().getDecorView().postDelayed(this::checkIsShowDialog, DELAY_TIME);
        }
    }

    @Override
    public void onBackPressed() {
        //注释这行禁用返回键
        //super.onBackPressed();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkIsShowDialog();
    }

    @Override
    protected void onDestroy() {
        disposable.dispose();
        super.onDestroy();
    }


    //==============添加默认APP介绍TXT文件功能Start================
    public void addInductionTxt() {
        try {
            String txtName = "introduction.txt";

            File tmpDir = getFilesDir();
            if (tmpDir == null || !tmpDir.exists()) tmpDir = getExternalFilesDir("tmpTxt");

            assert tmpDir != null;
            FileUtils.copyFromAssets(getAssets(), txtName, tmpDir.getAbsolutePath()+File.separator + txtName, true);

            File resultFile = new File(tmpDir.getAbsolutePath() + File.separator + txtName);

            Book book = new Book();
            book.setId(resultFile.getAbsolutePath());
            book.setTitle("欢迎使用");
            book.setPath(resultFile.getAbsolutePath());
            book.setSize(resultFile.length() + "");
            long time = System.currentTimeMillis();
            book.setAddTime(time);
            book.setLastReadTime(time);
            book.setSort((int) DBFactory.getInstance().getBooksManage().count());

            DBFactory.getInstance().getBooksManage().insertOrUpdate(book);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //==============添加默认APP介绍TXT文件功能End================
}