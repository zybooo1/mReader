package com.zyb.mreader.base.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.io.Serializable;


@Entity
public class Book implements Serializable{
    static final long serialVersionUID = 42L;

    @Id
    private String id;

    private String title="";      //name
    private String path="";      //路径
    private String size="";      //大小
    @Generated(hash = 654036272)
    public Book(String id, String title, String path, String size) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.size = size;
    }
    @Generated(hash = 1839243756)
    public Book() {
    }
    public BookFiles toBookFile() {
        BookFiles bookFile = new BookFiles();
        bookFile.setTitle(getTitle());
        bookFile.setPath(getPath());
        bookFile.setSize(getSize());
        return bookFile;
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
