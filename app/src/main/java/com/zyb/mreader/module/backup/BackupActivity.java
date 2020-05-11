package com.zyb.mreader.module.backup;


import android.view.View;

import com.zyb.base.base.activity.MVPActivity;
import com.zyb.base.di.component.AppComponent;
import com.zyb.mreader.R;
import com.zyb.mreader.di.component.DaggerActivityComponent;
import com.zyb.mreader.di.module.ActivityModule;
import com.zyb.mreader.di.module.ApiModule;
import com.zyb.mreader.module.backup.login.LoginActivity;

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
        if (mPresenter.getWebDavUserName().isEmpty()) {
            layoutLogin.setVisibility(View.VISIBLE);
        } else {
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
        startActivity(LoginActivity.class);
    }

    @OnClick(R.id.tvLogin)
    void tvLogin() {
        startActivity(LoginActivity.class);
    }

    //----------------- Login End -------------------------

}
