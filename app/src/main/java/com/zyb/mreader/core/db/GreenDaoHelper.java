package com.zyb.mreader.core.db;


import com.zyb.base.utils.LogUtil;
import com.zyb.common.db.DBFactory;
import com.zyb.common.db.bean.Book;
import com.zyb.common.db.bean.BookDao;
import com.zyb.common.db.bean.BookFiles;
import com.zyb.common.db.manage.BooksManage;

import java.util.List;

import javax.inject.Inject;

/**
 *
 */

public class GreenDaoHelper implements DbHelper {

    private DBFactory dbFactory;

    @Inject
    GreenDaoHelper() {
        dbFactory = DBFactory.getInstance();
    }

    @Override
    public List<Book> getAllBooks() {
        return dbFactory.getBooksManage().getQueryBuilder()
                .orderAsc(BookDao.Properties.Sort)
                .list();
    }

    @Override
    public void addBook(Book book) {
        dbFactory.getBooksManage().insertOrUpdate(book);
    }

    @Override
    public void addBooks(List<Book> books) {
        dbFactory.getBooksManage().insertOrUpdate(books);
    }

    @Override
    public boolean isBookAdded(BookFiles book) {
        for (Book book1 : dbFactory.getBooksManage().queryAll()) {
            if (book1.getId().equals(book.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeBook(List<Book> book) {
        dbFactory.getBooksManage().delete(book);
    }

    @Override
    public boolean isBookFilesCached() {
        return dbFactory.getBookFilesManage().count() > 0;
    }

    @Override
    public List<BookFiles> getAllBookFiles() {
        return dbFactory.getBookFilesManage().queryAll();
    }

    @Override
    public void updateBookFiles(List<BookFiles> books) {
        dbFactory.getBookFilesManage().deleteAll();
        dbFactory.getBookFilesManage().insert(books);
    }

    @Override
    public void sortBook(Book book, int newPosition) {
        BooksManage booksManage =dbFactory.getBooksManage();
        int oldPosition =book.getSort();
        LogUtil.e("sortBook:"+oldPosition+"-->"+newPosition);
        if(oldPosition==newPosition) return;

        if(newPosition>oldPosition){//往后移
            List<Book> bookList = booksManage.getQueryBuilder()
                    .where(BookDao.Properties.Sort.gt(oldPosition), BookDao.Properties.Sort.le(newPosition))
                    .list();
            for (Book book1 : bookList) {
                book1.setSort(book1.getSort()-1);
            }
            booksManage.insertOrUpdate(bookList);
        }
        if(newPosition<oldPosition){//往前移
            List<Book> bookList = booksManage.getQueryBuilder()
                    .where(BookDao.Properties.Sort.ge(newPosition), BookDao.Properties.Sort.lt(oldPosition))
                    .list();
            for (Book book1 : bookList) {
                book1.setSort(book1.getSort()+1);
            }
            booksManage.insertOrUpdate(bookList);
        }

        book.setSort(newPosition);
        booksManage.insertOrUpdate(book);
    }

    @Override
    public void detachAll() {
        dbFactory.getBooksManage().detachAll();
    }

}
