package com.zyb.reader.core.db.manage;


import android.content.Context;

import com.zyb.base.utils.constant.Constants;
import com.zyb.reader.core.bean.DaoMaster;
import com.zyb.reader.core.bean.DaoSession;

import org.greenrobot.greendao.query.QueryBuilder;

/**
 */

public class ReaderDBManage {


    private static ReaderDBManage dbManage;

    private Context context;

    public static ReaderDBManage getInstance(Context context) {
        if (dbManage == null) {
            synchronized (ReaderDBManage.class) {
                if (dbManage == null) {
                    dbManage = new ReaderDBManage(context);
                }
            }
        }
        return dbManage;
    }

    private ReaderDBManage(Context context) {
        this.context = context;
    }

    /**
     * 设置debug模式开启或关闭，默认关闭 * * @param flag
     */
    public void setDebug(boolean flag) {
        QueryBuilder.LOG_SQL = flag;
        QueryBuilder.LOG_VALUES = flag;
    }


    private MyOpenHelper mHelper;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;


    /**
     * 获取DaoSession * * @return
     */
    public synchronized DaoSession getDaoSession() {
        if (null == mDaoSession) {
            mDaoSession = getDaoMaster().newSession();
        }
        return mDaoSession;
    }

    /**
     * 关闭数据库
     */
    public synchronized void closeDataBase() {
        closeHelper();
        closeDaoSession();
    }

    /**
     * 判断数据库是否存在，如果不存在则创建 * * @return
     */
    private DaoMaster getDaoMaster() {
        if (null == mDaoMaster) {
            mHelper = new MyOpenHelper(context, Constants.DB_NAME,null);
            mDaoMaster = new DaoMaster(mHelper.getWritableDb());
        }
        return mDaoMaster;
    }

    private void closeDaoSession() {
        if (null != mDaoSession) {
            mDaoSession.clear();
            mDaoSession = null;
        }
    }

    private void closeHelper() {
        if (mHelper != null) {
            mHelper.close();
            mHelper = null;
        }
    }
}
