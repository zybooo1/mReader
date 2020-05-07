package com.zyb.mreader;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.zyb.base.utils.QMUIViewHelper;
import com.zyb.base.utils.RxUtil;
import com.zyb.base.utils.constant.Constants;
import com.zyb.base.widget.MyClickSpan;
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
    private static final int DELAY_TIME = 600;

    PreferenceHelperImpl preferenceHelper;

    private Disposable disposable;

    @BindView(R.id.ivLogo)
    ImageView ivLogo;

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

        QMUIViewHelper.fadeIn(ivLogo, 400, null, true);

        disposable = Observable.just(true)
                .delay(DELAY_TIME, TimeUnit.MILLISECONDS)
                .compose(RxUtil.rxObservableSchedulerHelper())
                .subscribe(aBoolean -> checkIsShowDialog(), throwable -> {
                });
    }

    @Override
    public int navigationBarColor() {
        return R.color.colorPrimary;
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

        TextView content =view.findViewById(R.id.tv4);
        content.setMovementMethod(LinkMovementMethod.getInstance());
        SpannableString spannableString = new SpannableString("同时，猫豆阅读采用严格的数据安全措施保护你的个人信息安全。你选择" +
                "「同意」即表示充分阅读、理解并接受《猫豆阅读用户协议》、《猫豆阅读隐私政策》的全部内容。你也可以选择「不同意」，猫豆阅读将无法为你提供产品和服务。");
        spannableString.setSpan(new MyClickSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                RouterUtils.getInstance().build(RouterConstants.PATH_BASE_ATY_WEB_VIEW)
                        .withString(WebActivity.URL_FLAG, Constants.PROTOCOL_HTML)
                        .navigation();
            }
        }, 49, 59, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new MyClickSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                RouterUtils.getInstance().build(RouterConstants.PATH_BASE_ATY_WEB_VIEW)
                        .withString(WebActivity.URL_FLAG, Constants.PRIVACY_HTML)
                        .navigation();
            }
        }, 60, 70, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        content.setHighlightColor(Color.TRANSPARENT);
        content.setText(spannableString);


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
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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