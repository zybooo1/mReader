package com.zyb.mreader.module.addBook;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.List;

public class MyPagerAdapter extends android.support.v4.app.FragmentPagerAdapter{
    private List<Fragment> fragments;

    public MyPagerAdapter(FragmentManager fragmentManager, List<Fragment> fragments ){
        super(fragmentManager);
        this.fragments =fragments;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }


    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

}
