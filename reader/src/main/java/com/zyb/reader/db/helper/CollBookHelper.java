package com.zyb.reader.db.helper;

import com.zyb.reader.db.entity.CollBookBean;
import com.zyb.reader.db.entity.CollBookBeanDao;
import com.zyb.reader.db.entity.DaoSession;

/**
 * Created by Liang_Lu on 2017/12/1.
 * 书架数据库操作工具类
 */

public class CollBookHelper {
    private static volatile CollBookHelper sInstance;
    private static DaoSession daoSession;
    private static CollBookBeanDao collBookBeanDao;

    public static CollBookHelper getsInstance() {
        if (sInstance == null) {
            synchronized (CollBookHelper.class) {
                if (sInstance == null) {
                    sInstance = new CollBookHelper();
                    daoSession = DaoDbHelper.getInstance().getSession();
                    collBookBeanDao = daoSession.getCollBookBeanDao();
                }
            }
        }
        return sInstance;
    }

    /**
     * 保存一本书籍 同步
     *
     * @param collBookBean
     */
    public void saveBook(CollBookBean collBookBean) {
        collBookBeanDao.insertOrReplace(collBookBean);
    }

}
