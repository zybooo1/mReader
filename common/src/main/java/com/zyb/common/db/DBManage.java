package com.zyb.common.db;


import android.content.Context;

import com.zyb.base.utils.constant.Constants;
import com.zyb.common.db.bean.DaoMaster;
import com.zyb.common.db.bean.DaoSession;

import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.query.QueryBuilder;

/**
 */

public class DBManage {


    private static DBManage dbManage;

    private Context context;

    public static DBManage getInstance(Context context) {
        if (dbManage == null) {
            synchronized (DBManage.class) {
                if (dbManage == null) {
                    dbManage = new DBManage(context);
                }
            }
        }
        return dbManage;
    }

    private DBManage(Context context) {
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
            //不使用缓存
//            mDaoSession = getDaoMaster().newSession(IdentityScopeType.None);
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
            mHelper = new MyOpenHelper(context, Constants.DB_NAME, null);
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
