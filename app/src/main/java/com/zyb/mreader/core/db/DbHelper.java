package com.zyb.mreader.core.db;


import com.zyb.mreader.base.bean.Book;
import com.zyb.mreader.base.bean.BookFiles;

import java.util.List;

/**
 */

public interface DbHelper {

    /**
     * 获取所有的
     */
    List<Book> getAllBooks();

    /**
     * 添加
     */
    void addBook(Book book);
    void addBooks(List<Book> books);
    boolean isBookAdded(BookFiles book);
    void removeBook(Book book);

    boolean isBookFilesCached();

    List<BookFiles> getAllBookFiles();
    void updateBookFiles(List<BookFiles> books);
}
