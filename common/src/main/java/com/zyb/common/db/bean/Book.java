package com.zyb.common.db.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;


/**
 * 书架上的书
 */
@Entity
public class Book implements Serializable {
    static final long serialVersionUID = 42L;

    @Id
    private String id;

    private String title = "";      //name
    private String path = "";      //路径
    private String size = "";      //大小
    private String progress = "";      //进度

    private String charset;

    private int sort;

    private long addTime;
    private long lastReadTime;
    private long begin;
    //不写入数据库
    @Transient
    private boolean isSelected;

    @Generated(hash = 728180004)
    public Book(String id, String title, String path, String size, String progress,
                String charset, int sort, long addTime, long lastReadTime, long begin) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.size = size;
        this.progress = progress;
        this.charset = charset;
        this.sort = sort;
        this.addTime = addTime;
        this.lastReadTime = lastReadTime;
        this.begin = begin;
    }

    @Generated(hash = 1839243756)
    public Book() {
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSize() {
        return this.size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getProgress() {
        return this.progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getCharset() {
        return this.charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getSort() {
        return this.sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public long getAddTime() {
        return this.addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public long getLastReadTime() {
        return this.lastReadTime;
    }

    public void setLastReadTime(long lastReadTime) {
        this.lastReadTime = lastReadTime;
    }

    public long getBegin() {
        return this.begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

}
