package com.zyb.common.db.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;


/**
 * 书架上的书
 */
@Entity
public class Book implements Serializable{
    static final long serialVersionUID = 42L;

    @Id
    private String id;

    private String title="";      //name
    private String path="";      //路径
    private String size="";      //大小

    private long begin;
    private String charset;
    @Generated(hash = 753008462)
    public Book(String id, String title, String path, String size, long begin,
            String charset) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.size = size;
        this.begin = begin;
        this.charset = charset;
    }
    @Generated(hash = 1839243756)
    public Book() {
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
    public long getBegin() {
        return this.begin;
    }
    public void setBegin(long begin) {
        this.begin = begin;
    }
    public String getCharset() {
        return this.charset;
    }
    public void setCharset(String charset) {
        this.charset = charset;
    }

}
