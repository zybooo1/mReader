package com.zyb.reader.util;

import android.text.TextUtils;

import com.tencent.bugly.crashreport.CrashReport;
import com.zyb.base.base.app.BaseApplication;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.utils.EventBusUtil;
import com.zyb.base.utils.LogUtil;
import com.zyb.base.utils.RxUtil;
import com.zyb.common.db.DBFactory;
import com.zyb.common.db.bean.Book;
import com.zyb.common.db.bean.BookCatalogue;
import com.zyb.reader.bean.Cache;
import com.zyb.reader.bean.SearchResultBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 *
 */
public class BookUtil {
    private static final String cachedPath = BaseApplication.getInstance().getExternalCacheDir() + "/bookCache/";
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
        if (position >= bookLen) {
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
        try {
            long size = 0;

            //匹配的章节格式
            String mChapterPattern = "";
            for (int i = 0; i < myArray.size(); i++) {
                char[] buf = block(i);
                String bufStr = new String(buf);
                String[] paragraphs = bufStr.split("\r\n");
                for (String str : paragraphs) {

                    boolean isMatches = false;

                    //获取匹配的章节格式（若没有）
                    if (mChapterPattern.isEmpty()) {
                        for (String chapterPattern : CHAPTER_PATTERNS) {
                            if (str.matches(chapterPattern)) {
                                mChapterPattern = chapterPattern;
                                isMatches = true;
                            }
                        }
                    } else {
                        isMatches = str.matches(mChapterPattern);
                    }

                    if (isMatches) {
                        BookCatalogue bookCatalogue = new BookCatalogue();
                        bookCatalogue.setBookCatalogueStartPos(size);
                        bookCatalogue.setBookCatalogue(str.replaceAll("\\s*", ""));
                        bookCatalogue.setBookpath(bookPath);
                        directoryList.add(bookCatalogue);
                    }
                    if (str.contains("\u3000\u3000")) {
                        size += str.length() + 2;
                    } else if (str.contains("\u3000")) {
                        size += str.length() + 1;
                    } else {
                        size += str.length();
                    }
                }
            }
            EventBusUtil.sendEvent(new BaseEvent(EventConstants.EVENT_ON_CATALOGS_LOADED));
            for (BookCatalogue bookCatalogue : directoryList) {
                LogUtil.e("bookCatalogue===" + bookCatalogue.getBookCatalogue());
            }
        } catch (Exception e) {
            CrashReport.postCatchedException(e);
            e.printStackTrace();
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


    /**
     * 搜索关键词功能
     */
    private Disposable disposable;
    //一页的数量
    public static final int A_PAGE_NUM = 50;
    //记录读取缓存的下标
    private int index = 0;

    public void searchContent(String key, boolean isLoadMore, OnSearchResult onSearchResult) {
        if (disposable != null) disposable.dispose();

        if (!isLoadMore) {
            index = 0;
        }
        disposable = Observable.create(new ObservableOnSubscribe<List<SearchResultBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<SearchResultBean>> emitter) throws Exception {
                List<SearchResultBean> searchResultBeanList = new ArrayList<>();
                long size = 0;

                for (int i = 0; i <index; i++) {
                    size += myArray.get(i).getSize();
                }

                for (int i = index; i < myArray.size(); i++) {
                    char[] buf = block(i);
                    String bufStr = new String(buf);
                    String[] paragraphs = bufStr.split("\r\n");

                    //满一页就终止
                    boolean canBreak = false;
                    for (String str : paragraphs) {
                        if (str.contains(key)) {
                            LogUtil.e("searchContent---", str);
                            SearchResultBean searchResultBean = new SearchResultBean();
                            searchResultBean.setBegin(size);
                            //这里replace并不会改变当前str，只是返回结果被replace了
                            searchResultBean.setText(str.replaceAll("\\s*", ""));
                            searchResultBeanList.add(searchResultBean);
                            if (searchResultBeanList.size() >= A_PAGE_NUM) canBreak = true;
                        }
                        if (str.contains("\u3000\u3000")) {
                            size += str.length() + 2;
                        } else if (str.contains("\u3000")) {
                            size += str.length() + 1;
                        } else {
                            size += str.length();
                        }
                    }

                    if (canBreak) {
                        //不是最后一组
                        if (i != myArray.size() - 1) index = i+1;
                        break;
                    }
                }
                emitter.onNext(searchResultBeanList);
                emitter.onComplete();
            }
        })
                .compose(RxUtil.rxObservableSchedulerHelper())
                .subscribe(new Consumer<List<SearchResultBean>>() {
                    @Override
                    public void accept(List<SearchResultBean> searchResultBeans) throws Exception {
                        if (searchResultBeans.size() <= 0) {
                            onSearchResult.onEmpty(isLoadMore);
                            return;
                        }
                        onSearchResult.onResult(searchResultBeans, isLoadMore);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        onSearchResult.onError();
                    }
                });
    }

    public interface OnSearchResult {
        void onEmpty(boolean isLoadMore);

        void onResult(List<SearchResultBean> beans, boolean isLoadMore);

        void onError();
    }

    /**
     * 字符串是否包含中文
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        return m.find();
    }
}
