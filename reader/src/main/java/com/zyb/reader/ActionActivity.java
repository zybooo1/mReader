package com.zyb.reader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.zyb.base.base.activity.MyActivity;
import com.zyb.base.utils.CommonUtils;
import com.zyb.base.utils.constant.Constants;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 长按阅读后的操作界面
 */
public class ActionActivity extends MyActivity {

    @BindView(R2.id.layoutRoot)
    View layoutRoot;

    @BindView(R2.id.tvContent)
    TextView tvContent;

    private String content; //内容
    private String title; //title

    @Override
    protected int getLayoutId() {
        return R.layout.reader_activity_action;
    }

    @Override
    protected void initView() {
        Config.createConfig(this);

        title = getIntent().getStringExtra(Constants.JUMP_PARAM_FLAG_STRING2);

        content = getIntent().getStringExtra(Constants.JUMP_PARAM_FLAG_STRING);
        tvContent.setText(content);
        tvContent.setTextSize(CommonUtils.px2dp(Config.getInstance().getFontSize()));

        initColor();
    }

    private void initColor() {
        if (Config.getInstance().getDayOrNight()) {
            layoutRoot.setBackgroundResource(android.R.color.black);
            tvContent.setTextColor(ContextCompat.getColor(this, R.color.reader_read_font_night));
            return;
        }

        switch (Config.getInstance().getBookBgType()) {
            case Config.BOOK_BG_DEFAULT:
                layoutRoot.setBackgroundResource(R.drawable.reader_paper);
                tvContent.setTextColor(ContextCompat.getColor(this, R.color.reader_read_font_default));
                break;
            case Config.BOOK_BG_1:
                layoutRoot.setBackgroundResource(R.color.reader_read_bg_1);
                tvContent.setTextColor(ContextCompat.getColor(this, R.color.reader_read_font_1));
                break;
            case Config.BOOK_BG_2:
                layoutRoot.setBackgroundResource(R.color.reader_read_bg_2);
                tvContent.setTextColor(ContextCompat.getColor(this, R.color.reader_read_font_2));
                break;
            case Config.BOOK_BG_3:
                layoutRoot.setBackgroundResource(R.color.reader_read_bg_3);
                tvContent.setTextColor(ContextCompat.getColor(this, R.color.reader_read_font_3));
                break;
            case Config.BOOK_BG_4:
                layoutRoot.setBackgroundResource(R.color.reader_read_bg_4);
                tvContent.setTextColor(ContextCompat.getColor(this, R.color.reader_read_font_4));
                break;
        }
    }

    @OnClick(R2.id.btnCopy)
    public void btnCopy() {
        if (tvContent.getSelectionStart() <= 0 || tvContent.getSelectionEnd() <= 0) {
            toast("请长按选中文字");
            return;
        }
        String text = tvContent.getText().toString().substring(tvContent.getSelectionStart(), tvContent.getSelectionEnd());
        CommonUtils.copy(text);
    }

    @OnClick(R2.id.btnTextImage)
    public void btnTextImage() {
        if (tvContent.getSelectionStart() <= 0 || tvContent.getSelectionEnd() <= 0) {
            toast("请长按选中文字");
            return;
        }
        String text = tvContent.getText().toString().substring(tvContent.getSelectionStart(), tvContent.getSelectionEnd());

        Intent intent =new Intent(this,TextImageActivity.class);
        intent.putExtra(Constants.JUMP_PARAM_FLAG_STRING,text);
        intent.putExtra(Constants.JUMP_PARAM_FLAG_STRING2,title);
        startActivity(intent);
    }

    public Bitmap convertViewToBitmap(View view){

        view.setDrawingCacheEnabled(true);

        view.buildDrawingCache();

        Bitmap bitmap=view.getDrawingCache();

        return bitmap;

    }

}