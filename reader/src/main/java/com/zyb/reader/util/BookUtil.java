package com.zyb.reader.util;

import android.content.ContentValues;
import android.os.Environment;
import android.text.TextUtils;

import com.zyb.base.base.app.BaseApplication;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.utils.EventBusUtil;
import com.zyb.base.utils.LogUtil;
import com.zyb.common.db.DBFactory;
import com.zyb.common.db.bean.Book;
import com.zyb.common.db.bean.BookCatalogue;
import com.zyb.reader.bean.Cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/8/11 0011.
 */
public class BookUtil {
    private static final String cachedPath = BaseApplication.getInstance().getExternalCacheDir()+ "/bookCache/";
    //存储的字符数
    public static final int cachedSize = 30000;
//    protected final ArrayList<WeakReference<char[]>> myArray = new ArrayList<>();

    protected final ArrayList<Cache> myArray = new ArrayList<>();
    //目录
    private List<BookCatalogue> directoryList = new ArrayList<>();


    //默认从文件中获取数据的长度
    private final static int BUFFER_SIZE = 512 * 1024;
    //章节解析模式
    private Pattern mChapterPattern = null;
    //没有标题的时候，每个章节的最大长度
    private final static int MAX_LENGTH_WITH_NO_CHAPTER = 10 * 1024;
    public static final byte BLANK = 0x0a;
    //正则表达式章节匹配模式
    // "(第)([0-9零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,10})([章节回集卷])(.*)"
    private static final String[] CHAPTER_PATTERNS = new String[]{"^(.{0,8})(\u7b2c)([0-9\u96f6\u4e00\u4e8c\u4e24\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,10})([\u7ae0\u8282\u56de\u96c6\u5377])(.{0,30})$",
            "^(\\s{0,4})([\\(\u3010\u300a]?(\u5377)?)([0-9\u96f6\u4e00\u4e8c\u4e24\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,10})([\\.:\uff1a\u0020\f\t])(.{0,30})$",
            "^(\\s{0,4})([\\(\uff08\u3010\u300a])(.{0,30})([\\)\uff09\u3011\u300b])(\\s{0,2})$",
            "^(\\s{0,4})(\u6b63\u6587)(.{0,20})$",
            "^(.{0,4})(Chapter|chapter)(\\s{0,4})([0-9]{1,4})(.{0,30})$"};

    private String m_strCharsetName;
    private String bookName;
    private String bookPath;
    private long bookLen;
    private long position;
    private Book book;

    public BookUtil() {
        File file = new File(cachedPath);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public synchronized void openBook(Book book) throws IOException {
        this.book = book;
        //如果当前缓存不是要打开的书本就缓存书本同时删除缓存

        if (bookPath == null || !bookPath.equals(book.getPath())) {
            cleanCacheFile();
            this.bookPath = book.getPath();
            bookName = FileUtils.getFileName(bookPath);
            cacheBook();
        }
    }

    private void cleanCacheFile() {
        File file = new File(cachedPath);
        if (!file.exists()) {
            file.mkdir();
        } else {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
        }
    }

    public int next(boolean back) {
        position += 1;
        if (position > bookLen) {
            position = bookLen;
            return -1;
        }
        char result = current();
        if (back) {
            position -= 1;
        }
        return result;
    }

    public char[] nextLine() {
        if (position >= bookLen) {
            return null;
        }
        String line = "";
        while (position < bookLen) {
            int word = next(false);
            if (word == -1) {
                break;
            }
            char wordChar = (char) word;
            if ((wordChar + "").equals("\r") && (((char) next(true)) + "").equals("\n")) {
                next(false);
                break;
            }
            line += wordChar;
        }
        return line.toCharArray();
    }

    public char[] preLine() {
        if (position <= 0) {
            return null;
        }
        String line = "";
        while (position >= 0) {
            int word = pre(false);
            if (word == -1) {
                break;
            }
            char wordChar = (char) word;
            if ((wordChar + "").equals("\n") && (((char) pre(true)) + "").equals("\r")) {
                pre(false);
//                line = "\r\n" + line;
                break;
            }
            line = wordChar + line;
        }
        return line.toCharArray();
    }

    public char current() {
//        int pos = (int) (position % cachedSize);
//        int cachePos = (int) (position / cachedSize);
        int cachePos = 0;
        int pos = 0;
        int len = 0;
        for (int i = 0; i < myArray.size(); i++) {
            long size = myArray.get(i).getSize();
            if (size + len - 1 >= position) {
                cachePos = i;
                pos = (int) (position - len);
                break;
            }
            len += size;
        }

        char[] charArray = block(cachePos);
        return charArray[pos];
    }

    public int pre(boolean back) {
        position -= 1;
        if (position < 0) {
            position = 0;
            return -1;
        }
        char result = current();
        if (back) {
            position += 1;
        }
        return result;
    }

    public long getPosition() {
        return position;
    }

    public void setPostition(long position) {
        this.position = position;
    }

    //缓存书本
    private void cacheBook() throws IOException {
        if (TextUtils.isEmpty(book.getCharset())) {
            m_strCharsetName = FileUtils.getCharset(bookPath);
            if (m_strCharsetName == null) {
                m_strCharsetName = "utf-8";
            }
            ContentValues values = new ContentValues();
            values.put("charset", m_strCharsetName);
            // TODO: 2019/7/13
//            DataSupport.update(BookList.class,values,bookList.getId());
            book.setCharset(m_strCharsetName);
            DBFactory.getInstance().getBooksManage().insertOrUpdate(book);
        } else {
            m_strCharsetName = book.getCharset();
        }

        File file = new File(bookPath);
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file), m_strCharsetName);
        int index = 0;
        bookLen = 0;
        directoryList.clear();
        myArray.clear();
        while (true) {
            char[] buf = new char[cachedSize];
            int result = reader.read(buf);
            if (result == -1) {
                reader.close();
                break;
            }

            String bufStr = new String(buf);
//            bufStr = bufStr.replaceAll("\r\n","\r\n\u3000\u3000");
//            bufStr = bufStr.replaceAll("\u3000\u3000+[ ]*","\u3000\u3000");
            bufStr = bufStr.replaceAll("\r\n+\\s*", "\r\n\u3000\u3000");
//            bufStr = bufStr.replaceAll("\r\n[ {0,}]","\r\n\u3000\u3000");
//            bufStr = bufStr.replaceAll(" ","");
            bufStr = bufStr.replaceAll("\u0000", "");
            buf = bufStr.toCharArray();
            bookLen += buf.length;

            Cache cache = new Cache();
            cache.setSize(buf.length);
            cache.setData(new WeakReference<char[]>(buf));

//            bookLen += result;
            myArray.add(cache);
//            myArray.add(new WeakReference<char[]>(buf));
//            myArray.set(index,);
            try {
                File cacheBook = new File(fileName(index));
                if (!cacheBook.exists()) {
                    cacheBook.createNewFile();
                }
                final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName(index)), "UTF-16LE");
                writer.write(buf);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Error during writing " + fileName(index));
            }
            index++;
        }

        new Thread() {
            @Override
            public void run() {
                getChapter();
            }
        }.start();
    }

    //获取章节
    public synchronized void getChapter() {
//        try {
//            long size = 0;
//            for (int i = 0; i < myArray.size(); i++) {
//                char[] buf = block(i);
//                String bufStr = new String(buf);
//                String[] paragraphs = bufStr.split("\r\n");
//                for (String str : paragraphs) {
//                    if (str.length() <= 30 && (str.matches(".*第.{1,8}章.*") || str.matches(".*第.{1,8}节.*"))) {
//                        BookCatalogue bookCatalogue = new BookCatalogue();
//                        bookCatalogue.setBookCatalogueStartPos(size);
//                        bookCatalogue.setBookCatalogue(str);
//                        bookCatalogue.setBookpath(bookPath);
//                        directoryList.add(bookCatalogue);
//                    }
//                    if (str.contains("\u3000\u3000")) {
//                        size += str.length() + 2;
//                    }else if (str.contains("\u3000")){
//                        size += str.length() + 1;
//                    }else {
//                        size += str.length();
//                    }
//                }
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        try {
            RandomAccessFile bookStream = new RandomAccessFile(new File(bookPath), "r");
            //首先获取128k的数据
            byte[] buffer = new byte[BUFFER_SIZE / 4];
            int length = bookStream.read(buffer, 0, buffer.length);
            //进行章节匹配
            for (String str : CHAPTER_PATTERNS) {
                Pattern pattern = Pattern.compile(str, Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(new String(buffer, 0, length, m_strCharsetName));
                //如果匹配存在，那么就表示当前章节使用这种匹配方式
                if (matcher.find()) {
                    mChapterPattern = pattern;
                    //重置指针位置
                    bookStream.seek(0);
                }
            }
            //重置指针位置
            bookStream.seek(0);
            loadChapters(bookStream);
            bookStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 未完成的部分:
     * 1. 序章的添加
     * 2. 章节存在的书本的虚拟分章效果
     *
     * @throws IOException
     */
    private void loadChapters(RandomAccessFile bookStream) throws IOException {
        //加载章节
        byte[] buffer = new byte[BUFFER_SIZE];
        //获取到的块起始点，在文件中的位置
        long curOffset = 0;
        //block的个数
        int blockPos = 0;
        //读取的长度
        int length;

        //获取文件中的数据到buffer，直到没有数据为止
        while ((length = bookStream.read(buffer, 0, buffer.length)) > 0) {
            ++blockPos;
            //如果存在Chapter
            if (mChapterPattern != null) {
                //将数据转换成String
                String blockContent = new String(buffer, 0, length, m_strCharsetName);
                //当前Block下使过的String的指针
                int seekPos = 0;
                //进行正则匹配
                Matcher matcher = mChapterPattern.matcher(blockContent);
                //如果存在相应章节
                while (matcher.find()) {
                    //获取匹配到的字符在字符串中的起始位置
                    int chapterStart = matcher.start();

                    //如果 seekPos == 0 && nextChapterPos != 0 表示当前block处前面有一段内容
                    //第一种情况一定是序章 第二种情况可能是上一个章节的内容
                    if (seekPos == 0 && chapterStart != 0) {
                        //获取当前章节的内容
                        String chapterContent = blockContent.substring(seekPos, chapterStart);
                        //设置指针偏移
                        seekPos += chapterContent.length();

                        //如果当前对整个文件的偏移位置为0的话，那么就是序章
                        if (curOffset == 0) {
                            //创建序章
                            int length1 = chapterContent.getBytes(m_strCharsetName).length;

                            BookCatalogue bookCatalogue = new BookCatalogue();
                            bookCatalogue.setBookCatalogueStartPos(0);
                            bookCatalogue.setBookCatalogueEndPos(length1);
                            bookCatalogue.setBookCatalogue("序章");
                            bookCatalogue.setBookpath(bookPath);


                            //如果序章大小大于30才添加进去
                            if (length1 > 30) {
                                directoryList.add(bookCatalogue);
                            }

                            //创建当前章节
                            BookCatalogue curChapter = new BookCatalogue();
                            curChapter.setBookCatalogueStartPos(length1);
                            curChapter.setBookCatalogue(matcher.group().replaceAll("\\s*", ""));
                            curChapter.setBookpath(bookPath);
                            directoryList.add(curChapter);
                        }
                        //否则就block分割之后，上一个章节的剩余内容
                        else {
                            //获取上一章节
                            BookCatalogue lastChapter = directoryList.get(directoryList.size() - 1);
                            //将当前段落添加上一章去
                            lastChapter.setBookCatalogueEndPos(lastChapter.getBookCatalogueEndPos() +chapterContent.getBytes(m_strCharsetName).length);

                            //如果章节内容太小，则移除
                            if (lastChapter.getBookCatalogueEndPos() - lastChapter.getBookCatalogueStartPos() < 30) {
                                directoryList.remove(lastChapter);
                            }

                            //创建当前章节
                            BookCatalogue curChapter = new BookCatalogue();
                            curChapter.setBookCatalogueStartPos(lastChapter.getBookCatalogueEndPos());
                            curChapter.setBookCatalogue(matcher.group().replaceAll("\\s*", ""));
                            curChapter.setBookpath(bookPath);
                            directoryList.add(curChapter);
                        }
                    } else {
                        //是否存在章节
                        if (directoryList.size() != 0) {
                            //获取章节内容
                            String chapterContent = blockContent.substring(seekPos, matcher.start());
                            seekPos += chapterContent.length();

                            //获取上一章节
                            BookCatalogue lastChapter = directoryList.get(directoryList.size() - 1);
                            lastChapter.setBookCatalogueEndPos(lastChapter.getBookCatalogueStartPos() + chapterContent.getBytes(m_strCharsetName).length);

                            //如果章节内容太小，则移除
                            if (lastChapter.getBookCatalogueEndPos() - lastChapter.getBookCatalogueStartPos() < 30) {
                                directoryList.remove(lastChapter);
                            }

                            //创建当前章节
                            BookCatalogue curChapter = new BookCatalogue();
                            curChapter.setBookCatalogueStartPos(lastChapter.getBookCatalogueEndPos());
                            curChapter.setBookCatalogue(matcher.group().replaceAll("\\s*", ""));
                            curChapter.setBookpath(bookPath);
                            directoryList.add(curChapter);
                        }
                        //如果章节不存在则创建章节
                        else {
                            BookCatalogue curChapter = new BookCatalogue();
                            curChapter.setBookCatalogueStartPos(0);
                            curChapter.setBookCatalogue(matcher.group().replaceAll("\\s*", ""));
                            curChapter.setBookpath(bookPath);
                            directoryList.add(curChapter);
                        }
                    }
                }
            }
            //进行本地虚拟分章
            else {
                //章节在buffer的偏移量
                int chapterOffset = 0;
                //当前剩余可分配的长度
                int strLength = length;
                //分章的位置
                int chapterPos = 0;

                while (strLength > 0) {
                    ++chapterPos;
                    //是否长度超过一章
                    if (strLength > MAX_LENGTH_WITH_NO_CHAPTER) {
                        //在buffer中一章的终止点
                        int end = length;
                        //寻找换行符作为终止点
                        for (int i = chapterOffset + MAX_LENGTH_WITH_NO_CHAPTER; i < length; ++i) {
                            if (buffer[i] == BLANK) {
                                end = i;
                                break;
                            }
                        }
                        BookCatalogue chapter = new BookCatalogue();
                        chapter.setBookCatalogue("第" + blockPos + "章" + "(" + chapterPos + ")");
                        chapter.setBookCatalogueStartPos(curOffset + chapterOffset + 1);
                        chapter.setBookCatalogueEndPos(curOffset + end);
                        chapter.setBookpath(bookPath);
                        directoryList.add(chapter);
                        //减去已经被分配的长度
                        strLength = strLength - (end - chapterOffset);
                        //设置偏移的位置
                        chapterOffset = end;
                    } else {
                        BookCatalogue chapter = new BookCatalogue();
                        chapter.setBookCatalogue("第" + blockPos + "章" + "(" + chapterPos + ")");
                        chapter.setBookCatalogueStartPos(curOffset + chapterOffset + 1);
                        chapter.setBookCatalogueEndPos(curOffset + length);
                        chapter.setBookpath(bookPath);
                        directoryList.add(chapter);
                        strLength = 0;
                    }
                }
            }

            //block的偏移点
            curOffset += length;

            if (mChapterPattern!=null) {
                //设置上一章的结尾
                BookCatalogue lastChapter = directoryList.get(directoryList.size() - 1);
                lastChapter.setBookCatalogueEndPos(curOffset);
            }

            //当添加的block太多的时候，执行GC
            if (blockPos % 15 == 0) {
                System.gc();
                System.runFinalization();
            }
        }
        System.gc();
        System.runFinalization();
        EventBusUtil.sendEvent(new BaseEvent(EventConstants.EVENT_ON_CATALOGS_LOADED));
        for (BookCatalogue bookCatalogue : directoryList) {
            LogUtil.e("bookCatalogue==="+bookCatalogue.getBookCatalogue());
        }
    }

    public List<BookCatalogue> getDirectoryList() {
        return directoryList;
    }

    public long getBookLen() {
        return bookLen;
    }

    protected String fileName(int index) {
        return cachedPath + bookName + index;
    }

    //获取书本缓存
    public char[] block(int index) {
        if (myArray.size() == 0) {
            return new char[1];
        }
        char[] block = myArray.get(index).getData().get();
        if (block == null) {
            try {
                File file = new File(fileName(index));
                int size = (int) file.length();
                if (size < 0) {
                    throw new RuntimeException("Error during reading " + fileName(index));
                }
                block = new char[size / 2];
                InputStreamReader reader =
                        new InputStreamReader(
                                new FileInputStream(file),
                                "UTF-16LE"
                        );
                if (reader.read(block) != block.length) {
                    throw new RuntimeException("Error during reading " + fileName(index));
                }
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException("Error during reading " + fileName(index));
            }
            Cache cache = myArray.get(index);
            cache.setData(new WeakReference<char[]>(block));
//            myArray.set(index, new WeakReference<char[]>(block));
        }
        return block;
    }

}
