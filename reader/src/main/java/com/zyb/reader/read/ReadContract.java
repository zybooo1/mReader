package com.zyb.reader.read;


import com.zyb.base.mvp.BasePresenter;
import com.zyb.base.mvp.BaseView;
import com.zyb.reader.widget.page.TxtChapter;

import java.util.List;

public interface ReadContract {
    interface View extends BaseView {
        void finishChapters();

        void errorChapters();
    }

    interface Presenter extends BasePresenter<View> {
        void loadContent(String bookId, List<TxtChapter> bookChapterList);
    }
}
