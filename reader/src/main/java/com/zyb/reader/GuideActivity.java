package com.zyb.reader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;


import com.zyb.reader.db.BookList;
import com.zyb.reader.util.Fileutil;

import java.io.File;

/**
 * Created by Administrator on 2016/8/22 0022.
 */
public class GuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutRes());

        initData();
        initListener();
    }

    public int getLayoutRes() {
        return R.layout.activity_guide;
    }

    protected void initData() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                File file = new File("/storage/emulated/0/iBook/三体全集.txt");
                BookList bookList = new BookList();
                String bookName = Fileutil.getFileNameNoEx(file.getName());
                bookList.setBookname(bookName);
                bookList.setBookpath(file.getAbsolutePath());
                ReadActivity.openBook(bookList, GuideActivity.this);
            }
        },1000);
    }

    protected void initListener() {

    }

}
