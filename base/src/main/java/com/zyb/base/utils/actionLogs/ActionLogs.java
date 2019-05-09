package com.zyb.base.utils.actionLogs;

/**
 * 操作日志
 */
public enum ActionLogs {
    MODULE_MESSAGE(0, "模块0"),
    MODULE_TEST(1, "模块1"),
    MODULE_LEARNING(2, "模块2"),
    MODULE_PLAN(3, "模块3"),
    MODULE_MINE(4, "模块4");
    private final int code;     //操作模块代码
    private final String name;    //操作模块名称

    ActionLogs(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }
}