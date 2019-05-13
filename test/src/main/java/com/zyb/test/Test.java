package com.zyb.test;


import com.zyb.base.base.activity.MyActivity;
import com.zyb.base.base.app.BaseApplication;
import com.zyb.base.utils.constant.Constants;

import org.greenrobot.greendao.database.Database;

/**
 */
public class Test extends MyActivity {


    @Override
    protected int getLayoutId() {
        return R.layout.layout_page_status;
    }

    @Override
    protected int getTitleBarId() {
        return 0;
    }

    @Override
    protected void initView() {
        DaoMaster.OpenHelper mHelper = new DaoMaster.OpenHelper(
                BaseApplication.getInstance(), Constants.DB_NAME) {
            @Override
            public void onCreate(Database db) {
                //This method is not executed
                super.onCreate(db);
            }
        };
        DaoMaster mDaoMaster = new DaoMaster(mHelper.getWritableDb());
        //crashed: no such table: COLL_BOOK_BEAN (code 1)
        long i = mDaoMaster.newSession().getTestBeanDao().count();
    }

    @Override
    protected void initData() {
    }


}