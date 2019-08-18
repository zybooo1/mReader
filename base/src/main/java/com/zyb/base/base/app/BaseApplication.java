package com.zyb.base.base.app;


import com.tencent.tinker.loader.app.TinkerApplication;
import com.tencent.tinker.loader.shareutil.ShareConstants;


/**
 * APP
 */
public class BaseApplication extends TinkerApplication {
    public BaseApplication() {
        super(ShareConstants.TINKER_ENABLE_ALL,
                "com.zyb.base.base.app.BaseApplicationLike",
                "com.tencent.tinker.loader.TinkerLoader",
                false);
    }

}
