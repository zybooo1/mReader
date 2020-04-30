package com.zyb.mreader.module.backup;


import android.view.View;
import android.widget.EditText;

import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialog.util.BaseDialog;
import com.kongzue.dialog.v3.FullScreenDialog;
import com.zyb.base.base.activity.MVPActivity;
import com.zyb.base.di.component.AppComponent;
import com.zyb.base.router.RouterConstants;
import com.zyb.base.router.RouterUtils;
import com.zyb.base.utils.constant.Constants;
import com.zyb.base.widget.WebActivity;
import com.zyb.mreader.BuildConfig;
import com.zyb.mreader.R;
import com.zyb.mreader.di.component.DaggerActivityComponent;
import com.zyb.mreader.di.module.ActivityModule;
import com.zyb.mreader.di.module.ApiModule;

import butterknife.OnClick;

/**
 *
 */
public class BackupActivity extends MVPActivity<BackupPresenter> implements
        BackupContract.View {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_backup;
    }

    @Override
    protected int getTitleBarId() {
        return R.id.titleBar;
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void setupActivityComponent(AppComponent appComponent) {
        DaggerActivityComponent.builder()
                .appComponent(appComponent)
                .apiModule(new ApiModule())
                .activityModule(new ActivityModule(this))
                .build()
                .inject(this);
    }

    //----------------- Login Start -------------------------
    @Override
    public void onRightClick(View v) {
        super.onRightClick(v);
        showLoginDialog();
    }

    @OnClick(R.id.backup)
    void backup(){
        mPresenter.backup();
    }

    @OnClick(R.id.recover)
    void recover(){
        mPresenter.recover();
    }

    private EditText etUserName, etPassword, etWebDevHost;
    private OnDialogButtonClickListener onOkButtonClickListener = new OnDialogButtonClickListener() {
        @Override
        public boolean onClick(BaseDialog baseDialog, View v) {
            mPresenter.login(etUserName.getText().toString(),etPassword.getText().toString(),etWebDevHost.getText().toString());
            return false;
        }
    };
    private View.OnClickListener helpTvClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RouterUtils.getInstance().build(RouterConstants.PATH_BASE_ATY_WEB_VIEW)
                    .withString(WebActivity.URL_FLAG, Constants.JIANGUOYUN_HELP_URL)
                    .navigation();
        }
    };

    private void showLoginDialog() {
        FullScreenDialog
                .show(this, R.layout.dialog_webdev_login, new FullScreenDialog.OnBindView() {
                    @Override
                    public void onBind(FullScreenDialog dialog, View rootView) {
                        etUserName = rootView.findViewById(R.id.etUserName);
                        etPassword = rootView.findViewById(R.id.etPassword);
                        etWebDevHost = rootView.findViewById(R.id.etWebDevHost);
//                        rootView.findViewById(R.id.tvProtocol).setOnClickListener(helpTvClick);
                        if(BuildConfig.DEBUG){
                            etUserName.setText(Constants.JIANGUOYUN_USERNAME);
                            etPassword.setText(Constants.JIANGUOYUN_PASSWORD);
                            etWebDevHost.setText(Constants.JIANGUOYUN_HOST);
                        }
                    }
                })
                .setOkButton("完成", onOkButtonClickListener)
                .setCancelButton("取消")
                .setTitle("登录WebDev");
    }

    @Override
    public void loginSuccess() {
    }
    //----------------- Login End -------------------------

}
