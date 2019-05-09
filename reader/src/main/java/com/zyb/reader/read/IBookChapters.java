package com.zyb.reader.read;

import com.zyb.reader.base.bean.BookChaptersBean;
import com.zyb.reader.base.IBaseLoadView;

/**
 * Created by Liang_Lu on 2017/12/11.
 */

public interface IBookChapters extends IBaseLoadView {
    void bookChapters(BookChaptersBean bookChaptersBean);

    void finishChapters();

    void errorChapters();

}
