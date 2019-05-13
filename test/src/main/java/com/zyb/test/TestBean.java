package com.zyb.test;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;


@Entity
public class TestBean implements Serializable{
    static final long serialVersionUID = 42L;

    @Id
    private String id;

    private String title="";      //name
    private String path="";      //路径
    private String size="";      //大小
    @Generated(hash = 1925491776)
    public TestBean(String id, String title, String path, String size) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.size = size;
    }
    @Generated(hash = 2087637710)
    public TestBean() {
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
}
