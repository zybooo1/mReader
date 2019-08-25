package com.zyb.mreader;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;

import com.tencent.bugly.crashreport.CrashReport;
import com.zyb.base.base.activity.MyActivity;
import com.zyb.base.router.RouterConstants;
import com.zyb.base.router.RouterUtils;
import com.zyb.base.widget.WebActivity;

import butterknife.OnClick;

/**
 * 关于界面
 */
public class AboutActivity extends MyActivity {

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

    }

    @Override
    protected void initData() {

    }

    @OnClick({R.id.githubUrl})
    public void clickGitHub(View view) {
        RouterUtils.getInstance().build(RouterConstants.PATH_BASE_ATY_WEB_VIEW)
                .withString(WebActivity.URL_FLAG, getString(R.string.github_url))
                .navigation();
    }

    @OnClick({R.id.tvQQ})
    public void clickQQ(View view) {
        copy(getString(R.string.my_qq));
    }

    private void copy(String string) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("内容", string);
        cm.setPrimaryClip(mClipData);
        showSuccess("已复制");
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
        if(isCount)count++;
        if (count > 23) {
            showMsg("恭喜你找到了彩蛋！");
            CrashReport.postCatchedException(new Throwable("竟然有人找到了彩蛋！"));
            count = 0;
        }
    }
}