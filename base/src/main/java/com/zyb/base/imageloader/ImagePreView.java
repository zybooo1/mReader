package com.zyb.base.imageloader;

import android.app.Activity;

import java.util.List;

import cc.shinichi.library.ImagePreview;
import cc.shinichi.library.bean.ImageInfo;

public class ImagePreView {

    public static void preViewImage(Activity activity, List<ImageInfo> imageInfoList) {
        ImagePreview
                .getInstance()
                .setContext(activity)
                .setIndex(0)
                .setImageInfoList(imageInfoList)
                .setLoadStrategy(ImagePreview.LoadStrategy.AlwaysOrigin)
                .start();
    }
}
