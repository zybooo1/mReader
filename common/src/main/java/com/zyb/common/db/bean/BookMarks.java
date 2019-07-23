package com.zyb.common.db.bean;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * 书签
 */
@Entity
public class BookMarks {
    @Id
    private String id;
    private long begin; // 书签记录页面的结束点位置
    private String text = "";
    private String time = "";
    private String bookpath = "";
    @Generated(hash = 155063592)
    public BookMarks(String id, long begin, String text, String time,
            String bookpath) {
        this.id = id;
        this.begin = begin;
        this.text = text;
        this.time = time;
        this.bookpath = bookpath;
    }
    @Generated(hash = 2117420381)
    public BookMarks() {
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public long getBegin() {
        return this.begin;
    }
    public void setBegin(long begin) {
        this.begin = begin;
    }
    public String getText() {
        return this.text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getTime() {
        return this.time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getBookpath() {
        return this.bookpath;
    }
    public void setBookpath(String bookpath) {
        this.bookpath = bookpath;
    }

}
