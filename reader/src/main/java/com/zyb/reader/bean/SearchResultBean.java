package com.zyb.reader.bean;


/**
 * 搜索结果
 */
public class SearchResultBean {
    private long begin; // 书签记录页面的结束点位置
    private String text = "";

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
