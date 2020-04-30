package com.zyb.base.widget;

import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.ClickableSpan;

public abstract class MyClickSpan extends ClickableSpan {
    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        ds.setColor(ds.linkColor);
        ds.setUnderlineText(false);
    }
}
