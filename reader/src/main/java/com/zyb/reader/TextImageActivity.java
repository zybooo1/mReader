package com.zyb.reader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zyb.base.base.activity.MyActivity;
import com.zyb.base.base.app.BaseApplication;
import com.zyb.base.utils.CommonUtils;
import com.zyb.base.utils.MemoryUtils;
import com.zyb.base.utils.constant.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import butterknife.BindView;

/**
 * 文字转为图片分享界面
 */
public class TextImageActivity extends MyActivity {

    @BindView(R2.id.layoutImage)
    LinearLayout layoutImage;

    @BindView(R2.id.layoutText)
    ConstraintLayout layoutText;

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
        layoutText.setMinHeight(CommonUtils.getScreenWidth());
        title = getIntent().getStringExtra(Constants.JUMP_PARAM_FLAG_STRING2);
        content = getIntent().getStringExtra(Constants.JUMP_PARAM_FLAG_STRING);

        tvContent.setText("\u3000\u3000"+content);
        tvBookName.setText("—— 《" + title + "》");
    }

    @Override
    public void onRightClick(View v) {
        super.onRightClick(v);
       File img = convertViewToFile();
       if(img==null){
           toast("分享失败");
           return;
       }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, getUriForFile(img));
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, "分享"));
    }

    public static Uri getUriForFile(File file) {
       return FileProvider.getUriForFile(BaseApplication.getInstance(),Constants.DEFAULT_FILEPROVIDER,file);
    }


    public File convertViewToFile() {
        layoutImage.destroyDrawingCache();
        layoutImage.setDrawingCacheEnabled(true);
        layoutImage.buildDrawingCache();
        Bitmap bitmap = layoutImage.getDrawingCache();

        String fileName = UUID.randomUUID()+ ".jpg";
        File file = new File(MemoryUtils.getInnerFileCachePath(), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}