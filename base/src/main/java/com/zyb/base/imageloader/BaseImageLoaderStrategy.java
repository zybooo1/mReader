package com.zyb.base.imageloader;

import android.content.Context;


public interface BaseImageLoaderStrategy<T extends ImageConfig> {

    void loadImage(Context ctx, T config);
}