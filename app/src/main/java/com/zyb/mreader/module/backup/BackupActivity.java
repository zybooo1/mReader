package com.zyb.mreader.module.backup;


import android.graphics.Color;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
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

import butterknife.BindView;
import butterknife.OnClick;

/**
 *
 */
public class BackupActivity extends MVPActivity<BackupPresenter> implements
        BackupContract.View {

    @BindView(R.id.layoutLogin)
    View layoutLogin;

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
        refreshView();
    }

    private void refreshView() {
        if(mPresenter.getWebDavUserName().isEmpty()){
            layoutLogin.setVisibility(View.VISIBLE);
        }else {
            layoutLogin.setVisibility(View.GONE);
        }
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

    @OnClick(R.id.tvLogin)
    void tvLogin() {
        showLoginDialog();
    }

    @OnClick(R.id.backup)
    void backup() {
        mPresenter.backup();
    }

    @OnClick(R.id.recover)
    void recover() {
        mPresenter.recover();
    }

    private EditText etUserName, etPassword, etWebDavHost;
    private AdapterView.OnItemSelectedListener onHostsItemSelect = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                etWebDavHost.setText(Constants.JIANGUOYUN_HOST);
            } else {
                etWebDavHost.setText(Constants.BOX_HOST);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private OnDialogButtonClickListener onOkButtonClickListener = new OnDialogButtonClickListener() {
        @Override
        public boolean onClick(BaseDialog baseDialog, View v) {
            mPresenter.login(etUserName.getText().toString(), etPassword.getText().toString(), etWebDavHost.getText().toString());
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
                .show(this, R.layout.dialog_webdav_login, new FullScreenDialog.OnBindView() {
                    @Override
                    public void onBind(FullScreenDialog dialog, View rootView) {
                        etUserName = rootView.findViewById(R.id.etUserName);
                        etPassword = rootView.findViewById(R.id.etPassword);
                        etWebDavHost = rootView.findViewById(R.id.etWebDavHost);
                        rootView.findViewById(R.id.webDavHelp).setOnClickListener(helpTvClick);
                        ((AppCompatSpinner) rootView.findViewById(R.id.spnHosts)).setSelection(0,true);
                        ((AppCompatSpinner) rootView.findViewById(R.id.spnHosts))
                                .setOnItemSelectedListener(onHostsItemSelect);
                        etUserName.setText(mPresenter.getWebDavUserName());
                        etPassword.setText(mPresenter.getWebDavPassword());
                        etWebDavHost.setText(mPresenter.getWebDavHost());

                        if (BuildConfig.DEBUG) {
                            rootView.findViewById(R.id.webDavHelp).setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    etUserName.setText(Constants.JIANGUOYUN_USERNAME);
                                    etPassword.setText(Constants.JIANGUOYUN_PASSWORD);
                                    etWebDavHost.setText(Constants.JIANGUOYUN_HOST);
                                    return false;
                                }
                            });
                        }
                    }
                })
                .setBackgroundColor(Color.WHITE)
                .setOkButton("完成", onOkButtonClickListener)
                .setCancelButton("取消")
                .setTitle("登录WebDav");
    }

    @Override
    public void loginSuccess() {
        refreshView();
    }
    //----------------- Login End -------------------------

}
