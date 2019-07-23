package com.zyb.common.db.bean;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * 目录
 */
@Entity
public class BookCatalogue {
    @Id
    private String id;
    private String bookpath;
    private String bookCatalogue;
    private long bookCatalogueStartPos;
    private long bookCatalogueEndPos;
    @Generated(hash = 1592484948)
    public BookCatalogue(String id, String bookpath, String bookCatalogue,
            long bookCatalogueStartPos, long bookCatalogueEndPos) {
        this.id = id;
        this.bookpath = bookpath;
        this.bookCatalogue = bookCatalogue;
        this.bookCatalogueStartPos = bookCatalogueStartPos;
        this.bookCatalogueEndPos = bookCatalogueEndPos;
    }
    @Generated(hash = 1988414870)
    public BookCatalogue() {
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getBookpath() {
        return this.bookpath;
    }
    public void setBookpath(String bookpath) {
        this.bookpath = bookpath;
    }
    public String getBookCatalogue() {
        return this.bookCatalogue;
    }
    public void setBookCatalogue(String bookCatalogue) {
        this.bookCatalogue = bookCatalogue;
    }
    public long getBookCatalogueStartPos() {
        return this.bookCatalogueStartPos;
    }
    public void setBookCatalogueStartPos(long bookCatalogueStartPos) {
        this.bookCatalogueStartPos = bookCatalogueStartPos;
    }
    public long getBookCatalogueEndPos() {
        return this.bookCatalogueEndPos;
    }
    public void setBookCatalogueEndPos(long bookCatalogueEndPos) {
        this.bookCatalogueEndPos = bookCatalogueEndPos;
    }
}

