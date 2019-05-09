package com.zyb.mreader.core.db.manage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.zyb.base.base.db.DBMigrationHelper;
import com.zyb.mreader.base.bean.BookDao;
import com.zyb.mreader.base.bean.DaoMaster;


public class MyOpenHelper extends DaoMaster.OpenHelper {
    public MyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    /**
     * 自定义数据库安全升级
     *
     * @param db         数据库
     * @param oldVersion 旧版本 在build.gradle 里配置schemaVersion
     * @param newVersion 新版本 在build.gradle 里配置schemaVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            DBMigrationHelper dbMigrationHelper = new DBMigrationHelper();
            Class[] classes = {BookDao.class};
            dbMigrationHelper.onUpgrade(db, classes);
        }
    }
}