package com.zyb.mreader;

import android.view.View;
import android.widget.TextView;

import com.tencent.bugly.crashreport.CrashReport;
import com.zyb.base.base.activity.MyActivity;
import com.zyb.base.router.RouterConstants;
import com.zyb.base.router.RouterUtils;
import com.zyb.base.utils.CommonUtils;
import com.zyb.base.utils.constant.Constants;
import com.zyb.base.widget.WebActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 关于界面
 */
public class AboutActivity extends MyActivity {

    @BindView(R2.id.ivDebug)
    View ivDebug;
    @BindView(R.id.tvVersion)
    TextView tvVersion;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected int getTitleBarId() {
        return R.id.titleBar;
    }


    @Override
    public boolean statusBarDarkFont() {
        return true;
    }

    @Override
    protected void initView() {
        if (BuildConfig.DEBUG) ivDebug.setVisibility(View.VISIBLE);
        tvVersion.setText(CommonUtils.getVersionName(this));
    }

    @Override
    protected void initData() {

    }

//    private void copy(String string) {
//        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//        ClipData mClipData = ClipData.newPlainText("内容", string);
//        assert cm != null;
//        cm.setPrimaryClip(mClipData);
//        showSuccess("已复制");
//    }

    @OnClick({R.id.tvPrivacy})
    public void clickPrivacy(View view) {
        RouterUtils.getInstance().build(RouterConstants.PATH_BASE_ATY_WEB_VIEW)
                .withString(WebActivity.URL_FLAG, Constants.PRIVACY_HTML)
                .navigation();
    }

    @OnClick({R.id.tvProtocol})
    public void clickProtocol(View view) {
        RouterUtils.getInstance().build(RouterConstants.PATH_BASE_ATY_WEB_VIEW)
                .withString(WebActivity.URL_FLAG, Constants.PROTOCOL_HTML)
                .navigation();
    }

    //============= Bouns ================
    private int count = 0;
    private boolean isCount = false;

    @OnClick(R.id.tvAppName)
    void clickAppName() {
        isCount = !isCount;
    }

    @OnClick(R.id.ivLogo)
    void clickLogo() {
        if (isCount) count++;
        if (count > 23) {
            showMsg("恭喜你找到了彩蛋！");
            CrashReport.postCatchedException(new Throwable("竟然有人找到了彩蛋！"));
            count = 0;
        }
    }


}