package com.zyb.base.base.bean;

/**
 * V-K 对象
 * 用于需要展示名称与获取ID的Spinner
 */
public class ValueIdObj {
    private String Value = "";
    private String ID = "";


    public ValueIdObj(String ID, String Value) {
        this.ID = ID;
        this.Value = Value;
    }


    @Override
    public String toString() {
        return Value;
    }

    public String getValue() {
        return Value;
    }

    public String getID() {
        return ID;
    }

}
