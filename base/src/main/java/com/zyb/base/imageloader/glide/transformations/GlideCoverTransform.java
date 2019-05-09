package com.zyb.base.imageloader.glide.transformations;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

/**
 * 蒙版
 * @author zyb
 * Create at 2019/4/25
 */
public class GlideCoverTransform extends CenterCrop {

    private static int coverColoer = 0x12000000;

    public GlideCoverTransform() {
        this(coverColoer);
    }

    public GlideCoverTransform(int coverColoer) {
        this.coverColoer = coverColoer;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        toTransform = super.transform(pool, toTransform, outWidth, outHeight);
        return cover(pool, toTransform);
    }

    private static Bitmap cover(BitmapPool pool, Bitmap source) {
        if (source == null) return null;

        Bitmap result = pool.get(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(result);
        canvas.drawColor(coverColoer, PorterDuff.Mode.DARKEN);
        return result;
    }
}
