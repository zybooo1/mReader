package com.zyb.base.event;

/**
 * eventBus相关常量
 */

public class EventConstants {
    public static final int EVENT_MAIN_REFRESH_BOOK_SHELF = 0x100;//刷新书架

    public static final int EVENT_SHOW_STATUS_BAR = 0x200;//显示状态栏
    public static final int EVENT_HIDE_STATUS_BAR = 0x201;//隐藏状态栏

    public static final int EVENT_ON_CATALOGS_LOADED = 0x300;//书籍目录加载完毕
    public static final int EVENT_MARKS_REFRESH = 0x301;//书签刷新
    public static final int EVENT_CLOSE_READ_DRAWER = 0x302;//关闭读书界面侧滑

    //reader
    public static final int EVENT_SPEECH_STRING_DATA = 0x400;//开始朗读内容
    public static final int EVENT_SPEECH_FINISH_PAGE = 0x401;//朗读一页完毕
    public static final int EVENT_SPEECH_STOP = 0x402;//朗读停止
}
