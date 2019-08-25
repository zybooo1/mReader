package com.zyb.common.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.zyb.base.base.db.DBMigrationHelper;
import com.zyb.common.db.bean.BookDao;
import com.zyb.common.db.bean.DaoMaster;

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
            Class[] classes = {BookDao.class};
            DBMigrationHelper.migrate(db, classes);
        }
    }
}