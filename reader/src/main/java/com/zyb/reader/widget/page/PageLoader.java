package com.zyb.reader.widget.page;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;

import com.hjq.toast.ToastUtils;
import com.zyb.base.base.app.BaseApplication;
import com.zyb.base.utils.CloseUtils;
import com.zyb.base.utils.CommonUtils;
import com.zyb.base.utils.LogUtil;
import com.zyb.base.utils.RxUtil;
import com.zyb.base.utils.TimeUtil;
import com.zyb.common.db.DBFactory;
import com.zyb.common.db.bean.BookRecordBean;
import com.zyb.common.db.bean.BookRecordBeanDao;
import com.zyb.common.db.bean.CollBookBean;
import com.zyb.reader.R;
import com.zyb.reader.core.bean.Void;
import com.zyb.reader.core.prefs.PreferenceHelperImpl;
import com.zyb.reader.utils.Charset;
import com.zyb.reader.utils.ReadUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import static com.zyb.base.utils.constant.Constants.NIGHT_MODE;
import static com.zyb.base.utils.constant.Constants.READ_BG_1;
import static com.zyb.base.utils.constant.Constants.READ_BG_2;
import static com.zyb.base.utils.constant.Constants.READ_BG_3;
import static com.zyb.base.utils.constant.Constants.READ_BG_4;
import static com.zyb.base.utils.constant.Constants.READ_BG_DEFAULT;

/**
 * 页加载器
 */
public  class PageLoader {
    //当前页面的状态
    public static final int STATUS_LOADING = 1;  //正在加载
    public static final int STATUS_FINISH = 2;   //加载完成
    public static final int STATUS_ERROR = 3;    //加载错误 (一般是网络加载情况)
    public static final int STATUS_EMPTY = 4;    //空数据
    public static final int STATUS_PARSE = 5;    //正在解析 (一般用于本地数据加载)
    public static final int STATUS_PARSE_ERROR = 6; //本地文件解析错误(暂未被使用)

    static final int DEFAULT_MARGIN_HEIGHT = 28;
    static final int DEFAULT_MARGIN_WIDTH = 12;

    //默认的显示参数配置
    private static final int DEFAULT_TIP_SIZE = 12;
    private static final int EXTRA_TITLE_SIZE = 4;
    //当前章节列表
    protected List<TxtChapter> mChapterList;
    //书本对象
    protected CollBookBean mCollBook;
    //监听器
    protected OnPageChangeListener mPageChangeListener;

    //页面显示类
    private PageView mPageView;
    //当前显示的页
    private TxtPage mCurPage;
    //当前章节的页面列表
    private List<TxtPage> mCurPageList = new ArrayList<>();
    //绘制电池的画笔
    private Paint mBatteryPaint;
    //绘制提示的画笔
    private Paint mTipPaint;
    //绘制标题的画笔
    protected Paint mTitlePaint;
    //绘制背景颜色的画笔(用来擦除需要重绘的部分)
    private Paint mBgPaint;
    //绘制小说内容的画笔
    protected TextPaint mTextPaint;
    //阅读器的配置选项
    PreferenceHelperImpl preferenceHelper = new PreferenceHelperImpl();
    //存储阅读记录类
    private BookRecordBean mBookRecord;
    //当前的状态
    protected int mStatus = STATUS_LOADING;
    //书本是否打开
    protected boolean isBookOpen = false;
    //书籍绘制区域的宽高
    protected int mVisibleWidth;
    protected int mVisibleHeight;
    //应用的宽高
    private int mDisplayWidth;
    private int mDisplayHeight;
    //间距
    private int mMarginWidth;
    private int mMarginHeight;
    //字体的颜色
    private int mTextColor;
    //标题的大小
    private int mTitleSize;
    //字体的大小
    private int mTextSize;
    //行间距
    protected int mTextInterval;
    //标题的行间距
    protected int mTitleInterval;
    //段落距离(基于行间距的额外距离)
    protected int mTextPara;
    protected int mTitlePara;
    //电池的百分比
    private int mBatteryLevel;
    //页面的翻页效果模式
    private int mPageMode;
    //加载器的颜色主题
    private int mBgTheme;
    //当前页面的背景
    private int mPageBg;
    //当前是否是夜间模式
    private boolean isNightMode;
    //默认从文件中获取数据的长度
    private final static int BUFFER_SIZE = 512 * 1024;
    //没有标题的时候，每个章节的最大长度
    private final static int MAX_LENGTH_WITH_NO_CHAPTER = 10 * 1024;
    // "序(章)|前言"
    private final static Pattern mPreChapterPattern = Pattern.compile("^(\\s{0,10})((\u5e8f[\u7ae0\u8a00]?)|(\u524d\u8a00)|(\u6954\u5b50))(\\s{0,10})$", Pattern.MULTILINE);
    //正则表达式章节匹配模式
    // "(第)([0-9零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,10})([章节回集卷])(.*)"
    private static final String[] CHAPTER_PATTERNS = new String[]{"^(.{0,8})(\u7b2c)([0-9\u96f6\u4e00\u4e8c\u4e24\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,10})([\u7ae0\u8282\u56de\u96c6\u5377])(.{0,30})$",
            "^(\\s{0,4})([\\(\u3010\u300a]?(\u5377)?)([0-9\u96f6\u4e00\u4e8c\u4e24\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,10})([\\.:\uff1a\u0020\f\t])(.{0,30})$",
            "^(\\s{0,4})([\\(\uff08\u3010\u300a])(.{0,30})([\\)\uff09\u3011\u300b])(\\s{0,2})$",
            "^(\\s{0,4})(\u6b63\u6587)(.{0,20})$",
            "^(.{0,4})(Chapter|chapter)(\\s{0,4})([0-9]{1,4})(.{0,30})$"};
    //书本的大小
    private long mBookSize;
    //章节解析模式
    private Pattern mChapterPattern = null;
    //获取书本的文件
    private File mBookFile;
    //编码类型
    private Charset mCharset;

    public PageLoader(PageView pageView) {
        mPageView = pageView;
        //初始化数据
        initData();
        //初始化画笔
        initPaint();
        //初始化PageView
        initPageView();
    }

    private void initData() {
        mTextSize = preferenceHelper.getTextSize();
        mTitleSize = mTextSize + CommonUtils.sp2px(EXTRA_TITLE_SIZE);
        mPageMode = preferenceHelper.getPageMode();
        isNightMode = preferenceHelper.isNightMode();
        mBgTheme = preferenceHelper.getReadBgTheme();

        if (isNightMode) {
            setBgColor(NIGHT_MODE);
        } else {
            setBgColor(mBgTheme);
        }

        //初始化参数
        mMarginWidth = CommonUtils.dp2px(DEFAULT_MARGIN_WIDTH);
        mMarginHeight = CommonUtils.dp2px(DEFAULT_MARGIN_HEIGHT);
        mTextInterval = mTextSize / 2;
        mTitleInterval = mTitleSize / 2;
        mTextPara = mTextSize; //段落间距由 text 的高度决定。
        mTitlePara = mTitleSize;
    }

    private void initPaint() {
        //绘制提示的画笔
        mTipPaint = new Paint();
        mTipPaint.setColor(mTextColor);
        mTipPaint.setTextAlign(Paint.Align.LEFT);//绘制的起始点
        mTipPaint.setTextSize(CommonUtils.sp2px(DEFAULT_TIP_SIZE));//Tip默认的字体大小
        mTipPaint.setAntiAlias(true);
        mTipPaint.setSubpixelText(true);

        //绘制页面内容的画笔
        mTextPaint = new TextPaint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setAntiAlias(true);

        //绘制标题的画笔
        mTitlePaint = new TextPaint();
        mTitlePaint.setColor(mTextColor);
        mTitlePaint.setTextSize(mTitleSize);
        mTitlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTitlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTitlePaint.setAntiAlias(true);

        //绘制背景的画笔
        mBgPaint = new Paint();
        mBgPaint.setColor(mPageBg);

        mBatteryPaint = new Paint();
        mBatteryPaint.setAntiAlias(true);
        mBatteryPaint.setDither(true);
        if (isNightMode) {
            mBatteryPaint.setColor(Color.WHITE);
        } else {
            mBatteryPaint.setColor(Color.BLACK);
        }
    }

    private void initPageView() {
        //配置参数
        mPageView.setPageMode(mPageMode);
        mPageView.setBgColor(mPageBg);
    }

    /****************************** public method***************************/
    //跳转到上一章
    public int skipPreChapter() {
        return 0;
    }

    //跳转到下一章
    public int skipNextChapter() {
        return 0;
    }

    //跳转到具体的页
    public void skipToPage(int pos) {
        mCurPage = getCurPage(pos);
        if (mCurPage == null) return;
        mPageView.refreshPage();
    }

    //更新时间
    public void updateTime() {
        if (mPageView.isPrepare() && !mPageView.isRunning()) {
            mPageView.drawCurPage(true);
        }
    }

    //更新电量
    public void updateBattery(int level) {
        mBatteryLevel = level;
        if (mPageView.isPrepare() && !mPageView.isRunning()) {
            mPageView.drawCurPage(true);
        }
    }

    //设置文字大小
    public void setTextSize(int textSize) {
        if (!isBookOpen) return;

        //设置textSize
        mTextSize = textSize;
        mTextInterval = mTextSize / 2;
        mTextPara = mTextSize;

        mTitleSize = mTextSize + CommonUtils.sp2px(EXTRA_TITLE_SIZE);
        mTitleInterval = mTitleInterval / 2;
        mTitlePara = mTitleSize;

        //设置画笔的字体大小
        mTextPaint.setTextSize(mTextSize);
        //设置标题的字体大小
        mTitlePaint.setTextSize(mTitleSize);
        //存储状态
        preferenceHelper.setTextSize(mTextSize);
        //如果当前为完成状态。
        if (mStatus == STATUS_FINISH) {
            //重新计算页面

            //防止在最后一页，通过修改字体，以至于页面数减少导致崩溃的问题
            if (mCurPage.position >= mCurPageList.size()) {
                mCurPage.position = mCurPageList.size() - 1;
            }
        }
        //重新设置文章指针的位置
        mCurPage = getCurPage(mCurPage.position);
        //绘制
        mPageView.refreshPage();
    }

    //
    public void updatePages(List<TxtPage> pages) {
        //重新计算页面
        mCurPageList.addAll(pages);

//        加载完成
        mStatus = STATUS_FINISH;
        //获取制定页面
        if (!isBookOpen) {
            isBookOpen = true;
            //可能会出现当前页的大小大于记录页的情况。
            int position = mBookRecord.getPagePos();
            if (position >= mCurPageList.size()) {
                position = mCurPageList.size() - 1;
            }
            mCurPage = getCurPage(position);
        } else {
            mCurPage = getCurPage(0);
        }
        //绘制
        mPageView.refreshPage();
    }

    //设置夜间模式
    public void setNightMode(boolean nightMode) {
        isNightMode = nightMode;
        if (isNightMode) {
            mBatteryPaint.setColor(Color.WHITE);
            setBgColor(NIGHT_MODE);
        } else {
            mBatteryPaint.setColor(Color.BLACK);
            setBgColor(mBgTheme);
        }
        preferenceHelper.setNightMode(nightMode);
    }

    //绘制背景
    public void setBgColor(int theme) {
        if (isNightMode && theme == NIGHT_MODE) {
            mTextColor = ContextCompat.getColor(BaseApplication.getInstance(), R.color.color_fff_99);
            mPageBg = ContextCompat.getColor(BaseApplication.getInstance(), R.color.black);
        } else if (isNightMode) {
            mBgTheme = theme;
            preferenceHelper.setReadBackground(theme);
        } else {
            preferenceHelper.setReadBackground(theme);
            switch (theme) {
                case READ_BG_DEFAULT:
                    mTextColor = ContextCompat.getColor(BaseApplication.getInstance(), R.color.color_2c);
                    mPageBg = ContextCompat.getColor(BaseApplication.getInstance(), R.color.color_cec29c);
                    break;
                case READ_BG_1:
                    mTextColor = ContextCompat.getColor(BaseApplication.getInstance(), R.color.color_2f332d);
                    mPageBg = ContextCompat.getColor(BaseApplication.getInstance(), R.color.color_ccebcc);
                    break;
                case READ_BG_2:
                    mTextColor = ContextCompat.getColor(BaseApplication.getInstance(), R.color.color_92918c);
                    mPageBg = ContextCompat.getColor(BaseApplication.getInstance(), R.color.color_aaa);
                    break;
                case READ_BG_3:
                    mTextColor = ContextCompat.getColor(BaseApplication.getInstance(), R.color.color_383429);
                    mPageBg = ContextCompat.getColor(BaseApplication.getInstance(), R.color.color_d1cec5);
                    break;
                case READ_BG_4:
                    mTextColor = ContextCompat.getColor(BaseApplication.getInstance(), R.color.color_627176);
                    mPageBg = ContextCompat.getColor(BaseApplication.getInstance(), R.color.color_001c27);
                    break;
            }
        }

        if (isBookOpen) {
            //设置参数
            mPageView.setBgColor(mPageBg);
            mTextPaint.setColor(mTextColor);
            //重绘
            mPageView.refreshPage();
        }
    }

    //翻页动画
    public void setPageMode(int pageMode) {
        mPageMode = pageMode;
        mPageView.setPageMode(mPageMode);
        preferenceHelper.setPageMode(mPageMode);
        //重绘
        mPageView.drawCurPage(false);
    }

    //设置页面切换监听
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mPageChangeListener = listener;
        //额，写的不太优雅，之后再改
        if (mChapterList != null && mChapterList.size() != 0) {
            mPageChangeListener.onCategoryFinish(mChapterList);
        }
    }

    //获取当前页的状态
    public int getPageStatus() {
        return mStatus;
    }

    //获取当前页的页码
    public int getPagePos() {
        return mCurPage.position;
    }

    //保存阅读记录
    public void saveRecord() {
        //书没打开，就没有记录
        if (!isBookOpen) return;

        mBookRecord.setBookId(mCollBook.get_id());
        mBookRecord.setPagePos(mCurPage.position);

        //存储到数据库
        DBFactory.getInstance().getBookRecordManage().insertOrUpdate(mBookRecord);
        //修改当前COllBook记录
        if (mCollBook != null && isBookOpen) {
            //表示当前CollBook已经阅读
            mCollBook.setUpdate(false);
            mCollBook.setLastRead(TimeUtil.parseDateTime(System.currentTimeMillis()));
            //直接更新
            DBFactory.getInstance().getCollBooksManage().insertOrUpdate(mCollBook);
        }
    }

    //打开书本，初始化书籍
    public void openBook(CollBookBean collBook) {
        mCollBook = collBook;
        //init book record

        //从数据库取阅读数据
        mBookRecord = DBFactory.getInstance().getBookRecordManage().getQueryBuilder()
                .where(BookRecordBeanDao.Properties.BookId.eq(mCollBook.get_id())).unique();
        if (mBookRecord == null) {
            mBookRecord = new BookRecordBean();
        }

        //这里id表示本地文件的路径
        mBookFile = new File(collBook.get_id());

        //判断是否文件存在
        if (!mBookFile.exists()) return;

        //获取文件的大小
        mBookSize = mBookFile.length();

        //文件内容为空
        if (mBookSize == 0) {
            mStatus = STATUS_EMPTY;
            return;
        }

        isBookOpen = false;
        //通过RxJava异步处理分章事件
        Single.create(new SingleOnSubscribe<Void>() {
            @Override
            public void subscribe(SingleEmitter<Void> e) throws Exception {
                loadBook(mBookFile);
                e.onSuccess(new Void());
            }
        }).compose(RxUtil::toSimpleSingle)
                .subscribe(new SingleObserver<Void>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(Void value) {
                        //提示目录加载完成
                        if (mPageChangeListener != null) {
                            mPageChangeListener.onCategoryFinish(mChapterList);
                        }
                        //打开章节，并加载当前章节
                        openChapter();
                    }

                    @Override
                    public void onError(Throwable e) {
                        //数据读取错误(弄个文章解析错误的Tip,会不会好一点)
                        mStatus = STATUS_ERROR;
                        ToastUtils.show("数据解析错误");
                    }
                });
    }
    //采用的是随机读取
    private void loadBook(File bookFile) throws IOException {
        //获取文件编码
        mCharset = ReadUtils.getCharset(bookFile.getAbsolutePath());
        //查找章节，分配章节
        loadChapters();
    }

    /**
     * 未完成的部分:
     * 1. 序章的添加
     * 2. 章节存在的书本的虚拟分章效果
     */
    private void loadChapters() throws IOException {
        List<TxtChapter> chapters = new ArrayList<>();
        //获取文件流
        RandomAccessFile bookStream = new RandomAccessFile(mBookFile, "r");
        //寻找匹配文章标题的正则表达式，判断是否存在章节名
        boolean hasChapter = checkChapterType(bookStream);
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
            if (hasChapter) {
                //将数据转换成String
                String blockContent = new String(buffer, 0, length, mCharset.getName());
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
                            TxtChapter preChapter = new TxtChapter();
                            preChapter.title = "序章";
                            preChapter.start = 0;
                            preChapter.end = chapterContent.getBytes(mCharset.getName()).length; //获取String的byte值,作为最终值

                            //如果序章大小大于30才添加进去
                            if (preChapter.end - preChapter.start > 30) {
                                chapters.add(preChapter);
                            }

                            //创建当前章节
                            TxtChapter curChapter = new TxtChapter();
                            curChapter.title = matcher.group();
                            curChapter.start = preChapter.end;
                            chapters.add(curChapter);
                        }
                        //否则就block分割之后，上一个章节的剩余内容
                        else {
                            //获取上一章节
                            TxtChapter lastChapter = chapters.get(chapters.size() - 1);
                            //将当前段落添加上一章去
                            lastChapter.end += chapterContent.getBytes(mCharset.getName()).length;

                            //如果章节内容太小，则移除
                            if (lastChapter.end - lastChapter.start < 30) {
                                chapters.remove(lastChapter);
                            }

                            //创建当前章节
                            TxtChapter curChapter = new TxtChapter();
                            curChapter.title = matcher.group();
                            curChapter.start = lastChapter.end;
                            chapters.add(curChapter);
                        }
                    } else {
                        //是否存在章节
                        if (chapters.size() != 0) {
                            //获取章节内容
                            String chapterContent = blockContent.substring(seekPos, matcher.start());
                            seekPos += chapterContent.length();

                            //获取上一章节
                            TxtChapter lastChapter = chapters.get(chapters.size() - 1);
                            lastChapter.end = lastChapter.start + chapterContent.getBytes(mCharset.getName()).length;

                            //如果章节内容太小，则移除
                            if (lastChapter.end - lastChapter.start < 30) {
                                chapters.remove(lastChapter);
                            }

                            //创建当前章节
                            TxtChapter curChapter = new TxtChapter();
                            curChapter.title = matcher.group();
                            curChapter.start = lastChapter.end;
                            chapters.add(curChapter);
                        }
                        //如果章节不存在则创建章节
                        else {
                            TxtChapter curChapter = new TxtChapter();
                            curChapter.title = matcher.group();
                            curChapter.start = 0;
                            chapters.add(curChapter);
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
                            if (buffer[i] == Charset.BLANK) {
                                end = i;
                                break;
                            }
                        }
                        TxtChapter chapter = new TxtChapter();
                        chapter.title = "第" + blockPos + "章" + "(" + chapterPos + ")";
                        chapter.start = curOffset + chapterOffset + 1;
                        chapter.end = curOffset + end;
                        chapters.add(chapter);
                        //减去已经被分配的长度
                        strLength = strLength - (end - chapterOffset);
                        //设置偏移的位置
                        chapterOffset = end;
                    } else {
                        TxtChapter chapter = new TxtChapter();
                        chapter.title = "第" + blockPos + "章" + "(" + chapterPos + ")";
                        chapter.start = curOffset + chapterOffset + 1;
                        chapter.end = curOffset + length;
                        chapters.add(chapter);
                        strLength = 0;
                    }
                }
            }

            //block的偏移点
            curOffset += length;

            if (hasChapter) {
                //设置上一章的结尾
                TxtChapter lastChapter = chapters.get(chapters.size() - 1);
                lastChapter.end = curOffset;
            }

            //当添加的block太多的时候，执行GC
            if (blockPos % 15 == 0) {
                System.gc();
                System.runFinalization();
            }
        }

        mChapterList = chapters;
        CloseUtils.closeIO(bookStream);

        System.gc();
        System.runFinalization();
    }
    /**
     * 1. 检查文件中是否存在章节名
     * 2. 判断文件中使用的章节名类型的正则表达式
     *
     * @return 是否存在章节名
     */
    private boolean checkChapterType(RandomAccessFile bookStream) throws IOException {
        //首先获取128k的数据
        byte[] buffer = new byte[BUFFER_SIZE / 4];
        int length = bookStream.read(buffer, 0, buffer.length);
        //进行章节匹配
        for (String str : CHAPTER_PATTERNS) {
            Pattern pattern = Pattern.compile(str, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(new String(buffer, 0, length, mCharset.getName()));
            //如果匹配存在，那么就表示当前章节使用这种匹配方式
            if (matcher.find()) {
                mChapterPattern = pattern;
                //重置指针位置
                bookStream.seek(0);
                return true;
            }
        }

        //重置指针位置
        bookStream.seek(0);
        return false;
    }

    //打开具体章节
    public void openChapter() {
        syncPages();
    }

    //清除记录，并设定是否缓存数据
    public void closeBook() {
        isBookOpen = false;
        mPageView = null;
    }

    void onDraw(Bitmap bitmap, boolean isUpdate) {
        drawBackground(mPageView.getBgBitmap(), isUpdate);
        if (!isUpdate) {
            drawContent(bitmap);
        }
        //更新绘制
        mPageView.invalidate();
    }

    void drawBackground(Bitmap bitmap, boolean isUpdate) {
        Canvas canvas = new Canvas(bitmap);
        int tipMarginHeight = CommonUtils.dp2px(3);
        if (!isUpdate) {
            /****绘制背景****/
            canvas.drawColor(mPageBg);
            // TODO: 2019/5/10
//            Bitmap bitmap1 = BitmapFactory.decodeResource(BaseApplication.getInstance().getResources(), R.mipmap.theme_leather_bg);
//            canvas.drawBitmap(bitmap1,0,0,null);
//            canvas.drawBitmap(bitmap1,null,new RectF(0,0,mDisplayWidth,mDisplayHeight),null);
            /*****初始化标题的参数********/
            //需要注意的是:绘制text的y的起始点是text的基准线的位置，而不是从text的头部的位置
            float tipTop = tipMarginHeight - mTipPaint.getFontMetrics().top;
            //根据状态不一样，数据不一样
            if (mStatus != STATUS_FINISH) {
                if (mChapterList != null && mChapterList.size() != 0) {
                    // TODO: 2019/6/4
//                    canvas.drawText(mChapterList.get().getTitle()
//                            , mMarginWidth, tipTop, mTipPaint);
                }
            } else {
                canvas.drawText(mCurPage.title, mMarginWidth, tipTop, mTipPaint);
            }

            /******绘制页码********/
            //底部的字显示的位置Y
            float y = mDisplayHeight - mTipPaint.getFontMetrics().bottom - tipMarginHeight;
            //只有finish的时候采用页码
            if (mStatus == STATUS_FINISH) {
                String percent = (mCurPage.position + 1) + "/" + mCurPageList.size();
                canvas.drawText(percent, mMarginWidth, y, mTipPaint);
            }
        } else {
            //擦除区域
            mBgPaint.setColor(mPageBg);
            canvas.drawRect(mDisplayWidth / 2, mDisplayHeight - mMarginHeight + CommonUtils.dp2px(2), mDisplayWidth, mDisplayHeight, mBgPaint);
        }
        /******绘制电池********/

        int visibleRight = mDisplayWidth - mMarginWidth;
        int visibleBottom = mDisplayHeight - tipMarginHeight;

        int outFrameWidth = (int) mTipPaint.measureText("xxx");
        int outFrameHeight = (int) mTipPaint.getTextSize();

        int polarHeight = CommonUtils.dp2px(6);
        int polarWidth = CommonUtils.dp2px(2);
        int border = 1;
        int innerMargin = 1;

        //电极的制作
        int polarLeft = visibleRight - polarWidth;
        int polarTop = visibleBottom - (outFrameHeight + polarHeight) / 2;
        Rect polar = new Rect(polarLeft, polarTop, visibleRight,
                polarTop + polarHeight - CommonUtils.dp2px(2));

        mBatteryPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(polar, mBatteryPaint);

        //外框的制作
        int outFrameLeft = polarLeft - outFrameWidth;
        int outFrameTop = visibleBottom - outFrameHeight;
        int outFrameBottom = visibleBottom - CommonUtils.dp2px(2);
        Rect outFrame = new Rect(outFrameLeft, outFrameTop, polarLeft, outFrameBottom);

        mBatteryPaint.setStyle(Paint.Style.STROKE);
        mBatteryPaint.setStrokeWidth(border);
        canvas.drawRect(outFrame, mBatteryPaint);

        //内框的制作
        float innerWidth = (outFrame.width() - innerMargin * 2 - border) * (mBatteryLevel / 100.0f);
        RectF innerFrame = new RectF(outFrameLeft + border + innerMargin, outFrameTop + border + innerMargin,
                outFrameLeft + border + innerMargin + innerWidth, outFrameBottom - border - innerMargin);

        mBatteryPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(innerFrame, mBatteryPaint);

        /******绘制当前时间********/
        //底部的字显示的位置Y
        float y = mDisplayHeight - mTipPaint.getFontMetrics().bottom - tipMarginHeight;
        String time = TimeUtil.parseHHmm(System.currentTimeMillis());
        float x = outFrameLeft - mTipPaint.measureText(time) - CommonUtils.dp2px(4);
        canvas.drawText(time, x, y, mTipPaint);
    }

    void drawContent(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);

        if (mPageMode == PageView.PAGE_MODE_SCROLL) {
            canvas.drawColor(mPageBg);
        }
        /******绘制内容****/
        if (mStatus != STATUS_FINISH) {
            //绘制字体
            String tip = "";
            switch (mStatus) {
                case STATUS_LOADING:
                    tip = "加载中...";
                    break;
                case STATUS_ERROR:
                    tip = "加载失败(点击边缘重试)";
                    break;
                case STATUS_EMPTY:
                    tip = "内容为空";
                    break;
                case STATUS_PARSE:
                    tip = "加载中...";
                    break;
                case STATUS_PARSE_ERROR:
                    tip = "文件解析错误";
                    break;
            }

            //将提示语句放到正中间
            Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
            float textHeight = fontMetrics.top - fontMetrics.bottom;
            float textWidth = mTextPaint.measureText(tip);
            float pivotX = (mDisplayWidth - textWidth) / 2;
            float pivotY = (mDisplayHeight - textHeight) / 2;
            canvas.drawText(tip, pivotX, pivotY, mTextPaint);
        } else {
            float top;

            if (mPageMode == PageView.PAGE_MODE_SCROLL) {
                top = -mTextPaint.getFontMetrics().top;
            } else {
                top = mMarginHeight - mTextPaint.getFontMetrics().top;
            }

            //设置总距离
            int interval = mTextInterval + (int) mTextPaint.getTextSize();
            int para = mTextPara + (int) mTextPaint.getTextSize();
            int titleInterval = mTitleInterval + (int) mTitlePaint.getTextSize();
            int titlePara = mTitlePara + (int) mTextPaint.getTextSize();
            String str = null;

            //对标题进行绘制
            for (int i = 0; i < mCurPage.titleLines; ++i) {
                str = mCurPage.lines.get(i);

                //设置顶部间距
                if (i == 0) {
                    top += mTitlePara;
                }

                //计算文字显示的起始点
                int start = (int) (mDisplayWidth - mTitlePaint.measureText(str)) / 2;
                //进行绘制
                canvas.drawText(str, start, top, mTitlePaint);

                //设置尾部间距
                if (i == mCurPage.titleLines - 1) {
                    top += titlePara;
                } else {
                    //行间距
                    top += titleInterval;
                }
            }

            //对内容进行绘制
            for (int i = mCurPage.titleLines; i < mCurPage.lines.size(); ++i) {
                str = mCurPage.lines.get(i);

                canvas.drawText(str, mMarginWidth, top, mTextPaint);
                if (str.endsWith("\n")) {
                    top += para;
                } else {
                    top += interval;
                }
            }
        }
    }

    void setDisplaySize(int w, int h) {
        //获取PageView的宽高
        mDisplayWidth = w;
        mDisplayHeight = h;

        //获取内容显示位置的大小
        mVisibleWidth = mDisplayWidth - mMarginWidth * 2;
        mVisibleHeight = mDisplayHeight - mMarginHeight * 2;

        //如果章节已显示，那么就重新计算页面
        if (mStatus == STATUS_FINISH) {
            //重新设置文章指针的位置
            mCurPage = getCurPage(mCurPage.position);
        }

        mPageView.drawCurPage(false);
    }

    //翻阅上一页
    boolean prev() {
        if (!checkStatus()) return false;

        //判断是否达到章节的起始点
        TxtPage prevPage = getPrevPage();
        if (prevPage == null) {
            //载入上一章。
            mCurPage = getPrevLastPage();
            mPageView.drawNextPage();
            return true;
        }

        mCurPage = prevPage;

        mPageView.drawNextPage();
        return true;
    }


    //翻阅下一页
    boolean next() {
        if (!checkStatus()) return false;
        //判断是否到最后一页了
        TxtPage nextPage = getNextPage();

        if (nextPage == null) {
                mCurPage = getCurPage(0);
                mPageView.drawNextPage();
                return true;
        }

        mCurPage = nextPage;
        mPageView.drawNextPage();
        //为下一页做缓冲
        //加载下一页的文章
        return true;
    }

    /**
     * @return 获取初始显示的页面
     */
    TxtPage getCurPage(int pos) {
        if (mPageChangeListener != null) {
            mPageChangeListener.onPageChange(pos);
        }
        if (mCurPageList.size() < pos + 1) return null;
        if (mCurPageList.size() <= 0) return null;
//        return mCurPageList.get(pos);
        return mCurPageList.get(0);
    }

    /**
     * @return 获取上一个页面
     */
    private TxtPage getPrevPage() {
        int pos = mCurPage.position - 1;
        if (pos < 0) {
            return null;
        }
        if (mPageChangeListener != null) {
            mPageChangeListener.onPageChange(pos);
        }
        return mCurPageList.get(pos);
    }

    /**
     * @return 获取下一的页面
     */
    private TxtPage getNextPage() {
        if (null != mCurPage) {
            int pos = mCurPage.position + 1;
            if (pos >= mCurPageList.size()) {
                return null;
            }
            if (mPageChangeListener != null) {
                mPageChangeListener.onPageChange(pos);
            }
            return mCurPageList.get(pos);
        }
        return new TxtPage();
    }

    /**
     * @return 获取上一个章节的最后一页
     */
    private TxtPage getPrevLastPage() {
        int pos = mCurPageList.size() - 1;
        return mCurPageList.get(pos);
    }

    /**
     * 检测当前状态是否能够进行加载章节数据
     */
    private boolean checkStatus() {
        if (mStatus == STATUS_LOADING) {
            ToastUtils.show("正在加载中，请稍等");
            return false;
        } else if (mStatus == STATUS_ERROR) {
            //点击重试
            mStatus = STATUS_LOADING;
            mPageView.drawCurPage(false);
            return false;
        }
        //由于解析失败，让其退出
        return true;
    }

    /*****************************************interface*****************************************/

    public interface OnPageChangeListener {
        //当目录加载完成的回调(必须要在创建的时候，就要存在了)
        void onCategoryFinish(List<TxtChapter> chapters);

        //页码改变
        void onPageCountChange(int count);

        //页面改变
        void onPageChange(int pos);
    }

    // 将 char[] 强转为 byte[]
    public static byte[] getBytes(char[] chars) {
        byte[] result = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            result[i] = (byte) chars[i];
        }
        return result;
    }

    protected void syncPages() {
        int loadCount = 100;
        Disposable disposable = Observable.create(new ObservableOnSubscribe<List<TxtPage>>() {
            @Override
            public void subscribe(ObservableEmitter<List<TxtPage>> emitter) throws Exception {
                RandomAccessFile bookStream = new RandomAccessFile(mBookFile, "r");
                TxtChapter txtChapter = new TxtChapter();
                txtChapter.setTitle("麦田里的守望者");
                //生成的页面
                List<TxtPage> pages = new ArrayList<>();
                //使用流的方式加载
                List<String> lines = new ArrayList<>();
                int rHeight = mVisibleHeight; //由于匹配到最后，会多删除行间距，所以在这里多加个行间距
                int titleLinesCount = 0;
                boolean isTitle = true; //不存在没有 Title 的情况，所以默认设置为 true。
                String paragraph = txtChapter.getTitle();//默认展示标题
                while (isTitle || (paragraph = bookStream.readLine()) != null) {

                    //重置段落
                    if (!isTitle) {
//                    paragraph = paragraph.replaceAll("\\s", "");
                        //如果只有换行符，那么就不执行
                        if (paragraph.equals("")) continue;
                        byte[] bytes = getBytes(paragraph.toCharArray());
//                    paragraph = ReadUtils.halfToFull("  " + paragraph + "\n");
                        paragraph = "  " + new String(bytes) + "\n";
                    } else {
                        //设置 title 的顶部间距
                        rHeight -= mTitlePara;
                    }

                    int wordCount = 0;
                    String subStr = null;
                    while (paragraph.length() > 0) {
                        //当前空间，是否容得下一行文字
                        if (isTitle) {
                            rHeight -= mTitlePaint.getTextSize();
                        } else {
                            rHeight -= mTextPaint.getTextSize();
                        }

                        //一页已经填充满了，创建 TextPage
                        if (rHeight < 0) {
                            //创建Page
                            TxtPage page = new TxtPage();
                            page.position = 0;
                            page.title = txtChapter.getTitle();
                            page.lines = new ArrayList<>(lines);
                            page.titleLines = titleLinesCount;
                            pages.add(page);
                            if (pages.size() >= loadCount) {
                                emitter.onNext(pages);
                                pages.clear();
                            }
                            //重置Lines
                            lines.clear();
                            rHeight = mVisibleHeight;
                            titleLinesCount = 0;
                            continue;
                        }

                        //测量一行占用的字节数
                        if (isTitle) {
                            wordCount = mTitlePaint.breakText(paragraph, true, mVisibleWidth, null);
                        } else {
                            wordCount = mTextPaint.breakText(paragraph, true, mVisibleWidth, null);
                        }

                        subStr = paragraph.substring(0, wordCount);
                        if (!subStr.equals("\n")) {
                            //将一行字节，存储到lines中
                            lines.add(subStr);

                            //设置段落间距
                            if (isTitle) {
                                titleLinesCount += 1;
                                rHeight -= mTitleInterval;
                            } else {
                                rHeight -= mTextInterval;
                            }
                        }
                        //裁剪
                        paragraph = paragraph.substring(wordCount);
                    }

                    //增加段落的间距
                    if (!isTitle && lines.size() != 0) {
                        rHeight = rHeight - mTextPara + mTextInterval;
                    }

                    if (isTitle) {
                        rHeight = rHeight - mTitlePara + mTitleInterval;
                        isTitle = false;
                    }
                }

                if (lines.size() != 0) {
                    //创建Page
                    TxtPage page = new TxtPage();
                    page.position = 0;
                    page.title = txtChapter.getTitle();
                    page.lines = new ArrayList<>(lines);
                    page.titleLines = titleLinesCount;
                    pages.add(page);
                    //重置Lines
                    lines.clear();
                }
                emitter.onNext(pages);
                bookStream.close();
            }
        }).subscribe(new Consumer<List<TxtPage>>() {
            @Override
            public void accept(List<TxtPage> txtPages) throws Exception {
                for (TxtPage page : txtPages) {
                    for (String line : page.lines) {
                        LogUtil.e("syncPages onNext----" + line);
                    }
                }
                updatePages(txtPages);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                LogUtil.e("syncPages onError----" + throwable.toString());
            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                LogUtil.e("syncPages onComplete----");
            }
        });
    }
}
