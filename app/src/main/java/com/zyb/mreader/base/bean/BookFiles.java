package com.zyb.mreader.base.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.File;

import static com.zyb.mreader.module.addBook.path.adapter.PathAdapter.ITEM_BOOK;
import static com.zyb.mreader.module.addBook.path.adapter.PathAdapter.ITEM_PATH;


@Entity
public class BookFiles implements MultiItemEntity {
    static final long serialVersionUID = 42L;

    @Id
    private String id;

    private String title = "";      //name
    private String path = "";      //路径
    private String size = "";      //大小
    private boolean isChecked = false;      //是否选中

    private boolean isFile;

    @Generated(hash = 470027827)
    public BookFiles(String id, String title, String path, String size,
            boolean isChecked, boolean isFile) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.size = size;
        this.isChecked = isChecked;
        this.isFile = isFile;
    }


    @Generated(hash = 285052994)
    public BookFiles() {
    }


    public Book toBook() {
        Book book = new Book();
        book.setTitle(getTitle());
        book.setPath(getPath());
        book.setSize(getSize());
        return book;
    }

    @Override
    public int getItemType() {
        return getIsFile() ? ITEM_BOOK : ITEM_PATH;
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


    public boolean getIsChecked() {
        return this.isChecked;
    }


    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }


    public boolean getIsFile() {
        return this.isFile;
    }


    public void setIsFile(boolean isFile) {
        this.isFile = isFile;
    }


}
