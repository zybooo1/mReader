package com.zyb.mreader.core.db;


import com.zyb.mreader.base.bean.Book;
import com.zyb.mreader.base.bean.BookFiles;
import com.zyb.mreader.core.db.manage.AppDBFactory;

import java.util.List;

import javax.inject.Inject;

/**
 *
 */

public class GreenDaoHelper implements DbHelper {

    private AppDBFactory dbFactory;

    @Inject
    GreenDaoHelper() {
        dbFactory = AppDBFactory.getInstance();
    }

    @Override
    public List<Book> getAllBooks() {
        return dbFactory.getBooksManage().queryAll();
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
    public void removeBook(Book book) {
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

}
