package com.zyb.mreader.core.http;



import com.zyb.mreader.core.http.api.CommonApis;

import javax.inject.Inject;


/**
 */

public class RetrofitHelper implements HttpHelper {

    private CommonApis mCommonApis;

    @Inject
    RetrofitHelper( CommonApis commonApis) {
        mCommonApis = commonApis;
    }
}
