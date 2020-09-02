package com.zyb.reader;

import android.graphics.Bitmap;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.TextView;

import com.zyb.base.base.activity.MyActivity;
import com.zyb.base.utils.CommonUtils;
import com.zyb.base.utils.constant.Constants;

import butterknife.BindView;

/**
 * 文字转为图片分享界面
 */
public class TextImageActivity extends MyActivity {

    @BindView(R2.id.layoutImage)
    ConstraintLayout layoutImage;

    @BindView(R2.id.tvContent)
    TextView tvContent;

    @BindView(R2.id.tvBookName)
    TextView tvBookName;

    private String content; //内容
    private String title; //title

    @Override
    protected int getLayoutId() {
        return R.layout.reader_activity_text_image;
    }

    @Override
    protected void initView() {
        layoutImage.setMinHeight(CommonUtils.getScreenWidth());
        title = getIntent().getStringExtra(Constants.JUMP_PARAM_FLAG_STRING2);
        content = getIntent().getStringExtra(Constants.JUMP_PARAM_FLAG_STRING);

        tvContent.setText(content);
        tvBookName.setText("—— 《" + title + "》");
    }


    public Bitmap convertViewToBitmap(View view) {

        view.setDrawingCacheEnabled(true);

        view.buildDrawingCache();

        Bitmap bitmap = view.getDrawingCache();

        return bitmap;

    }

}