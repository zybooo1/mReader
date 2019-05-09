package com.zyb.base.widget.adapter;

import android.support.annotation.DrawableRes;

public class MenuItem {
    private int imgId = 0;
    private String name = "";

    public MenuItem(@DrawableRes int imgId, String name) {
        this.imgId = imgId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }
}
