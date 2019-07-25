package com.zyb.base.event;

/**
 * eventBus相关常量
 */

public class EventConstants {
    public static final int EVENT_MAIN_REFRESH_BOOK_SHELF = 0x100;//刷新书架

    public static final int EVENT_SHOW_STATUS_BAR = 0x101;//显示状态栏
    public static final int EVENT_HIDE_STATUS_BAR = 0x102;//隐藏状态栏

    public static final int EVENT_ON_CATALOGS_LOADED = 0x103;//书籍目录加载完毕
    public static final int EVENT_CLOSE_READ_DRAWER = 0x104;//关闭读书界面侧滑

    //reader
    public static final int EVENT_SPEECH_STRING_DATA = 0x105;//开始朗读内容
    public static final int EVENT_SPEECH_FINISH_PAGE = 0x106;//朗读一页完毕
    public static final int EVENT_SPEECH_STOP = 0x107;//朗读停止
}
