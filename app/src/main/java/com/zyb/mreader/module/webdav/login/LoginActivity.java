package com.zyb.mreader.module.webdav.login;


import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

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
import butterknife.OnItemSelected;
import butterknife.OnLongClick;

/**
 *
 */
public class LoginActivity extends MVPActivity<LoginPresenter> implements
        LoginContract.View {
    public static final int RESULT_SUCCESS = 0x586;
    @BindView(R.id.etUserName)
    public EditText etUserName;
    @BindView(R.id.etPassword)
    public EditText etPassword;
    @BindView(R.id.etWebDavHost)
    public EditText etWebDavHost;
    @BindView(R.id.btnLogin)
    public Button btnLogin;
    @BindView(R.id.spnHosts)
    public AppCompatSpinner spnHosts;
    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(etWebDavHost.getText().toString().trim().isEmpty()||
                    etUserName.getText().toString().trim().isEmpty()||
                    etPassword.getText().toString().trim().isEmpty()){
                btnLogin.setEnabled(false);
            }else {
                btnLogin.setEnabled(true);
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_webdav_login;
    }

    @Override
    protected int getTitleBarId() {
        return R.id.titleBar;
    }

    @Override
    protected void initView() {
        etUserName.addTextChangedListener(watcher);
        etPassword.addTextChangedListener(watcher);
        etWebDavHost.addTextChangedListener(watcher);

        etUserName.setText(mPresenter.getWebDavUserName());
        etPassword.setText(mPresenter.getWebDavPassword());
        etWebDavHost.setText(mPresenter.getWebDavHost());

        spnHosts.setSelection(0, true);

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

    @OnClick(R.id.btnLogin)
    public void btnLogin() {
        mPresenter.login(etUserName.getText().toString(), etPassword.getText().toString(), etWebDavHost.getText().toString());
    }

    @OnClick(R.id.webDavHelp)
    public void webDavHelp() {
        RouterUtils.getInstance().build(RouterConstants.PATH_BASE_ATY_WEB_VIEW)
                .withString(WebActivity.URL_FLAG, Constants.JIANGUOYUN_HELP_URL)
                .navigation();
    }

    @OnLongClick(R.id.webDavHelp)
    boolean onWebDavHelpLongClick() {
        if (BuildConfig.DEBUG) {
            etUserName.setText(Constants.JIANGUOYUN_USERNAME);
            etPassword.setText(Constants.JIANGUOYUN_PASSWORD);
            etWebDavHost.setText(Constants.JIANGUOYUN_HOST);
        }
        return true;
    }

    @OnItemSelected(value = R.id.spnHosts)
    void spnHosts(int position) {
        if (position == 0) {
            etWebDavHost.setText(Constants.JIANGUOYUN_HOST);
        } else {
            etWebDavHost.setText(Constants.BOX_HOST);
        }
    }


    @Override
    public void loginSuccess() {
        finish();
    }
}
