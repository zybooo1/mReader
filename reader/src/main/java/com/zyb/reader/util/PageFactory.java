package com.zyb.reader.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.hjq.toast.ToastUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.zyb.base.utils.CommonUtils;
import com.zyb.common.db.DBFactory;
import com.zyb.common.db.bean.Book;
import com.zyb.common.db.bean.BookCatalogue;
import com.zyb.reader.Config;
import com.zyb.reader.R;
import com.zyb.reader.bean.TRPage;
import com.zyb.reader.view.PageWidget;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public class PageFactory {
    private static final String TAG = "PageFactory";
    private static PageFactory pageFactory;

    private Context mContext;
    private Config config;
    //当前的书本
//    private File book_file = null;
    // 默认背景颜色
    private int m_backColor = 0xffff9e85;
    //页面宽
    private int mWidth;
    //页面高
    private int mHeight;
    //文字字体大小
    private float m_fontSize;
    //时间格式
    private SimpleDateFormat sdf;
    //时间
    private String date;
    //进度格式
    private DecimalFormat df;
    //电池边界宽度
    private float mBorderWidth = 3;
    // 上下与边缘的距离
    private float marginHeight;
    // 左右与边缘的距离
    private float measureMarginWidth;
    // 左右与边缘的距离
    private float marginWidth;
    //状态栏距离底部高度
    private float statusMarginBottom;
    //行间距
    private float lineSpace;
    //段间距
    private float paragraphSpace;
    //字高度
    private float fontHeight;
    //文字画笔
    private Paint mPaint;
    //标题、时间等的画笔
    private Paint mTipPaint;
    //时间、标题等字体大小
    private float mTipTextSize;
    //文字颜色
    private int mTextColor = Color.rgb(50, 65, 78);
    // 绘制内容的宽
    private float mVisibleHeight;
    // 绘制内容的宽
    private float mVisibleWidth;
    // 每页可以显示的行数
    private int mLineCount;
    //电池边框画笔
    private Paint mBatterryBorderPaint;
    //电池画笔
    private Paint mBatterryPaint;
    //背景图片
    private Bitmap m_book_bg = null;
    //当前显示的文字
//    private StringBuilder word = new StringBuilder();
    //当前总共的行
//    private Vector<String> m_lines = new Vector<>();
//    // 当前页起始位置
//    private long m_mbBufBegin = 0;
//    // 当前页终点位置
//    private long m_mbBufEnd = 0;
//    // 之前页起始位置
//    private long m_preBegin = 0;
//    // 之前页终点位置
//    private long m_preEnd = 0;
    // 图书总长度
//    private long m_mbBufLen = 0;
    private Intent batteryInfoIntent;
    //电池电量百分比
    private float mBatteryPercentage;
    //电池外边框
    private RectF batteryBorderRect = new RectF();
    //电池内边框
    private RectF batteryRect = new RectF();
    //文件编码
//    private String m_strCharsetName = "GBK";
    //当前是否为第一页
    private boolean m_isfirstPage;
    //当前是否为最后一页
    private boolean m_islastPage;
    //书本widget
    private PageWidget mBookPageWidget;
    //    //书本所有段
//    List<String> allParagraph;
//    //书本所有行
//    List<String> allLines = new ArrayList<>();
    //现在的进度
    private float currentProgress;
    //目录
//    private List<BookCatalogue> directoryList = new ArrayList<>();
    //书本路径
    private String bookPath = "";
    //书本名字
    private String bookName = "";
    private Book book;
    //书本章节
    private int currentCharter = 0;
    //当前电量
    private int level = 0;
    public BookUtil mBookUtil;
    private PageEvent mPageEvent;
    private TRPage currentPage;
    private TRPage prePage;
    private TRPage cancelPage;
    private BookTask bookTask;

    private static Status mStatus = Status.OPENING;

    public enum Status {
        OPENING,
        FINISH,
        FAIL,
    }

    public static synchronized PageFactory getInstance() {
        return pageFactory;
    }

    public static synchronized PageFactory createPageFactory(Context context) {
        if (pageFactory == null) {
            pageFactory = new PageFactory(context);
        }
        return pageFactory;
    }

    private PageFactory(Context context) {
        mBookUtil = new BookUtil();
        mContext = context.getApplicationContext();
        config = Config.getInstance();
        //获取屏幕宽高
        mWidth = CommonUtils.getScreenWidth();
        mHeight = CommonUtils.getOriginScreenHight();

        sdf = new SimpleDateFormat("HH:mm", Locale.CHINA);//HH:mm为24小时制,hh:mm为12小时制
        date = sdf.format(new java.util.Date());
        df = new DecimalFormat("#0.0");

        marginWidth = mContext.getResources().getDimension(R.dimen.reader_readingMarginWidth);
        marginHeight = mContext.getResources().getDimension(R.dimen.reader_readingMarginHeight);
        statusMarginBottom = CommonUtils.getStatusBarHeight(context);
        lineSpace = context.getResources().getDimension(R.dimen.reader_reading_line_spacing);
        paragraphSpace = context.getResources().getDimension(R.dimen.reader_reading_paragraph_spacing);
        mVisibleWidth = mWidth - marginWidth * 2;
        mVisibleHeight = mHeight - marginHeight * 2 - statusMarginBottom * 2;

        m_fontSize = config.getFontSize();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);// 画笔
        mPaint.setTextAlign(Paint.Align.LEFT);// 左对齐
        mPaint.setTextSize(m_fontSize);// 字体大小
        mPaint.setColor(mTextColor);// 字体颜色
        mPaint.setSubpixelText(true);// 设置该项为true，将有助于文本在LCD屏幕上的显示效果

        calculateLineCount();

        mBatterryBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBatterryBorderPaint.setStyle(Paint.Style.STROKE);
        mBatterryBorderPaint.setStrokeWidth(2f);
        mBatterryBorderPaint.setStrokeJoin(Paint.Join.ROUND);

        mTipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTipTextSize = CommonUtils.sp2px(12);
        mTipPaint.setTextSize(mTipTextSize);
        mTipPaint.setTextAlign(Paint.Align.LEFT);

        mBatterryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBatterryPaint.setStyle(Paint.Style.FILL);


        batteryInfoIntent = context.getApplicationContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));//注册广播,随时获取到电池电量信息

        initBg(config.getDayOrNight());
        measureMarginWidth();
    }

    private void measureMarginWidth() {
        float wordWidth = mPaint.measureText("\u3000");
        float width = mVisibleWidth % wordWidth;
        measureMarginWidth = marginWidth + width / 2;

    }

    //初始化背景
    private void initBg(Boolean isNight) {
        if (isNight) {
            //设置背景
//            setBgBitmap(BitmapUtil.decodeSampledBitmapFromResource(
//                    mContext.getResources(), R.drawable.main_bg, mWidth, mHeight));
            Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(mContext.getResources().getColor(R.color.reader_read_bg_night));
            setBgBitmap(bitmap);
            //设置字体颜色
            setmTextColor(ContextCompat.getColor(mContext,R.color.reader_read_font_night));
            setBookPageBg(Color.BLACK);
        } else {
            //设置背景
            setBookBg(config.getBookBgType());
        }
    }

    private void calculateLineCount() {
        mLineCount = (int) (mVisibleHeight / (m_fontSize + lineSpace));// 可显示的行数
    }

    private void drawStatus(Bitmap bitmap) {
        String status = "";
        switch (mStatus) {
            case OPENING:
                status = "加载中...";
                break;
            case FAIL:
                status = "出现了错误";
                break;
        }

        Canvas canvas = new Canvas(bitmap);
//        canvas.drawBitmap(getBgBitmap(), 0, 0, null);
        RectF rectF = new RectF(0, 0, mWidth, mHeight);
        canvas.drawBitmap(getBgBitmap(), null, rectF, null);

        Rect targetRect = new Rect(0, 0, mWidth, mHeight);
//        canvas.drawRect(targetRect, mPaint);
        Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
        // 转载请注明出处：http://blog.csdn.net/hursing
        int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
        canvas.drawText(status, targetRect.centerX() - (mPaint.measureText(status) / 2), baseline, mPaint);
        mBookPageWidget.postInvalidate();
    }

    public void onDraw(Bitmap bitmap, List<String> m_lines, Boolean updateCharter) {
        if (getDirectoryList().size() > 0 && updateCharter) {
            currentCharter = getCurrentCharter();
        }
        //更新数据库进度
        if (currentPage != null && book != null) {
            book.setBegin(currentPage.getBegin());
            DBFactory.getInstance().getBooksManage().insertOrUpdate(book);
        }

        Canvas canvas = new Canvas(bitmap);
        RectF rectF = new RectF(0, 0, mWidth, mHeight);
        canvas.drawBitmap(getBgBitmap(), null, rectF, null);
//        canvas.drawBitmap(getBgBitmap(), 0, 0, null);
//        word.setLength(0);
        mPaint.setTextSize(getFontSize());
        mPaint.setColor(getTextColor());
        if (m_lines.size() == 0) {
            return;
        }

        float y = marginHeight + statusMarginBottom;
        for (String strLine : m_lines) {
            y += m_fontSize + lineSpace;
            canvas.drawText(strLine, measureMarginWidth, y, mPaint);
//                word.append(strLine);
        }

        mBatterryBorderPaint.setColor(getTipTextColor());
        mTipPaint.setColor(getTipTextColor());
        mBatterryPaint.setColor(getTipTextColor());

        // 画电池
        level = batteryInfoIntent.getIntExtra("level", 50);
        int scale = batteryInfoIntent.getIntExtra("scale", 100);
        mBatteryPercentage = (float) level / scale;
        //模拟电量变化
//        mBatteryPercentage = new Random().nextFloat();
        //画电池外框
        float batteryWidth = CommonUtils.dp2px(20);//电池宽
        float batteryHeight = CommonUtils.dp2px(10);//电池高
        float batteryLeft = measureMarginWidth;//电池外框left位置
        float batteryTop = mHeight - batteryHeight - statusMarginBottom;//电池外框Top位置
        float batteryBottom = mHeight - statusMarginBottom;//电池外框Bottom位置
        batteryBorderRect.set(batteryLeft, batteryTop, batteryLeft + batteryWidth, batteryBottom);
        canvas.drawRoundRect(batteryBorderRect, batteryHeight / 2, batteryHeight / 2, mBatterryBorderPaint);
        //画电量部分
        Path path = new Path();
        float radius = batteryRect.height() / 2;
        float[] radiusArray = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        RectF clipRect = new RectF(batteryLeft + mBorderWidth, batteryTop + mBorderWidth, batteryLeft + batteryWidth - mBorderWidth, mHeight - statusMarginBottom - mBorderWidth);
        path.addRoundRect(clipRect, radiusArray, Path.Direction.CW);
        //这个rect大小根据电量变化
        batteryRect.set(batteryLeft + mBorderWidth, batteryTop + mBorderWidth, batteryLeft + batteryWidth * mBatteryPercentage - mBorderWidth, mHeight - statusMarginBottom - mBorderWidth);
        //根据电量值裁剪
        canvas.save();
        canvas.clipPath(path);
        canvas.drawRect(batteryRect, mBatterryPaint);
        canvas.restore();

        //画时间及进度
        float fPercent = (float) (currentPage.getBegin() * 1.0 / mBookUtil.getBookLen());//进度
        currentProgress = fPercent;
        if (mPageEvent != null) {
            mPageEvent.changeProgress(fPercent);
        }
        String strPercent = df.format(fPercent * 100) + "%";//进度文字
        int nPercentWidth = (int) mTipPaint.measureText(strPercent);  //Paint.measureText直接返回參數字串所佔用的寬度
        //文字保持和电池居中对齐
        Paint.FontMetrics fontMetrics = mTipPaint.getFontMetrics();
        float textY = (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent
                + (batteryBottom - batteryTop) / 2 + batteryTop;
        canvas.drawText(strPercent, mWidth - nPercentWidth - measureMarginWidth, textY, mTipPaint);//x y为坐标值
        canvas.drawText(date, batteryLeft + batteryWidth + 10, textY, mTipPaint);

        //画标题
        canvas.drawText(CommonUtils.subString(bookName, 12), marginWidth, statusMarginBottom + mTipTextSize, mTipPaint);

        mBookPageWidget.postInvalidate();
    }

    //向前翻页
    public void prePage() {
        if (currentPage.getBegin() <= 0) {
            Log.e(TAG, "当前是第一页");
            if (!m_isfirstPage) {
                ToastUtils.show("当前是第一页");
            }
            m_isfirstPage = true;
            return;
        } else {
            m_isfirstPage = false;
        }

        cancelPage = currentPage;
        onDraw(mBookPageWidget.getCurPage(), currentPage.getLines(), true);
        currentPage = getPrePage();
        onDraw(mBookPageWidget.getNextPage(), currentPage.getLines(), true);
    }

    //向后翻页
    public void nextPage() {
        if (currentPage.getEnd() >= mBookUtil.getBookLen()) {
            Log.e(TAG, "已经是最后一页了");
            if (!m_islastPage) {
                ToastUtils.show("已经是最后一页了");
            }
            m_islastPage = true;
            return;
        } else {
            m_islastPage = false;
        }

        cancelPage = currentPage;
        onDraw(mBookPageWidget.getCurPage(), currentPage.getLines(), true);
        prePage = currentPage;
        currentPage = getNextPage();
        onDraw(mBookPageWidget.getNextPage(), currentPage.getLines(), true);
        Log.e("nextPage", "nextPagenext");
    }

    //取消翻页
    public void cancelPage() {
        currentPage = cancelPage;
    }

    /**
     * 打开书本
     *
     * @throws IOException
     */
    public void openBook(Book book) throws IOException {
        //清空数据
        currentCharter = 0;
//        m_mbBufLen = 0;
        initBg(config.getDayOrNight());

        this.book = book;
        bookPath = book.getPath();
        bookName = FileUtils.getFileName(bookPath);

        mStatus = Status.OPENING;
        drawStatus(mBookPageWidget.getCurPage());
        drawStatus(mBookPageWidget.getNextPage());
        if (bookTask != null && bookTask.getStatus() != AsyncTask.Status.FINISHED) {
            bookTask.cancel(true);
        }
        bookTask = new BookTask();
        bookTask.execute(book.getBegin());
    }

    private class BookTask extends AsyncTask<Long, Void, Boolean> {
        private long begin = 0;

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Log.e("onPostExecute", isCancelled() + "");
            if (isCancelled()) {
                return;
            }
            if (result) {
                PageFactory.mStatus = PageFactory.Status.FINISH;
//                m_mbBufLen = mBookUtil.getBookLen();
                currentPage = getPageForBegin(begin);
                if (mBookPageWidget != null) {
                    currentPage(true);
                }
            } else {
                PageFactory.mStatus = PageFactory.Status.FAIL;
                drawStatus(mBookPageWidget.getCurPage());
                drawStatus(mBookPageWidget.getNextPage());
                ToastUtils.show("打开书本失败！");
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Boolean doInBackground(Long... params) {
            begin = params[0];
            try {
                mBookUtil.openBook(book);
            } catch (Exception e) {
                CrashReport.postCatchedException(e);
                e.printStackTrace();
                return false;
            }
            return true;
        }

    }

    public TRPage getNextPage() {
        mBookUtil.setPostition(currentPage.getEnd());

        TRPage trPage = new TRPage();
        trPage.setBegin(currentPage.getEnd() + 1);
        Log.e("begin", currentPage.getEnd() + 1 + "");
        trPage.setLines(getNextLines());
        Log.e("end", mBookUtil.getPosition() + "");
        trPage.setEnd(mBookUtil.getPosition());
        return trPage;
    }

    public TRPage getPrePage() {
        mBookUtil.setPostition(currentPage.getBegin());

        TRPage trPage = new TRPage();
        trPage.setEnd(mBookUtil.getPosition() - 1);
        Log.e("end", mBookUtil.getPosition() - 1 + "");
        trPage.setLines(getPreLines());
        Log.e("begin", mBookUtil.getPosition() + "");
        trPage.setBegin(mBookUtil.getPosition());
        return trPage;
    }

    /**
     * 获取下一页第一句（第一个标点符号前的字符）
     * 用于拼接到当前页进行朗读
     */
    public String getNextPageFirstSentence() {
        mBookUtil.setPostition(currentPage.getEnd());
        String string = getNextPage().getLineToString();
        //匹配标点符号
        String[] strings = string.split("[\\p{P}\\p{Punct}]");
        if (strings.length > 0) {
            return strings[0];
        } else {
            return "";
        }
    }

    /**
     * 获取当前页去掉第一句（第一个标点符号前的字符）
     * 用于朗读
     */
    public String getCurPageWithoutFirstSentence() {
        mBookUtil.setPostition(currentPage.getEnd());

        String string = getCurrentPage().getLineToString();
        //匹配标点符号
        String[] strings = string.split("[\\p{P}\\p{Punct}]");
        if (strings.length > 0) {
            return string.substring(strings[0].length());
        } else {
            return string;
        }
    }

    public TRPage getPageForBegin(long begin) {
        TRPage trPage = new TRPage();
        trPage.setBegin(begin);

        mBookUtil.setPostition(begin - 1);
        trPage.setLines(getNextLines());
        trPage.setEnd(mBookUtil.getPosition());
        return trPage;
    }

    public List<String> getNextLines() {
        List<String> lines = new ArrayList<>();
        float width = 0;
        float height = 0;
        String line = "";
        while (mBookUtil.next(true) != -1) {
            char word = (char) mBookUtil.next(false);
            //判断是否换行
            if ((word + "").equals("\r") && (((char) mBookUtil.next(true)) + "").equals("\n")) {
                mBookUtil.next(false);
                if (!line.isEmpty()) {
                    lines.add(line);
                    line = "";
                    width = 0;
//                    height +=  paragraphSpace;
                    if (lines.size() == mLineCount) {
                        break;
                    }
                }
            } else {
                float widthChar = mPaint.measureText(word + "");
                width += widthChar;
                if (width > mVisibleWidth) {
                    width = widthChar;
                    lines.add(line);
                    line = word + "";
                } else {
                    line += word;
                }
            }

            if (lines.size() == mLineCount) {
                if (!line.isEmpty()) {
                    mBookUtil.setPostition(mBookUtil.getPosition() - 1);
                }
                break;
            }
        }

        if (!line.isEmpty() && lines.size() < mLineCount) {
            lines.add(line);
        }
        for (String str : lines) {
            Log.e(TAG, str + "   ");
        }
        return lines;
    }

    public List<String> getPreLines() {
        List<String> lines = new ArrayList<>();
        float width = 0;
        String line = "";

        char[] par = mBookUtil.preLine();
        while (par != null) {
            List<String> preLines = new ArrayList<>();
            for (int i = 0; i < par.length; i++) {
                char word = par[i];
                float widthChar = mPaint.measureText(word + "");
                width += widthChar;
                if (width > mVisibleWidth) {
                    width = widthChar;
                    preLines.add(line);
                    line = word + "";
                } else {
                    line += word;
                }
            }
            if (!line.isEmpty()) {
                preLines.add(line);
            }

            lines.addAll(0, preLines);

            if (lines.size() >= mLineCount) {
                break;
            }
            width = 0;
            line = "";
            par = mBookUtil.preLine();
        }

        List<String> reLines = new ArrayList<>();
        int num = 0;
        for (int i = lines.size() - 1; i >= 0; i--) {
            if (reLines.size() < mLineCount) {
                reLines.add(0, lines.get(i));
            } else {
                num = num + lines.get(i).length();
            }
            Log.e(TAG, lines.get(i) + "   ");
        }

        if (num > 0) {
            if (mBookUtil.getPosition() > 0) {
                mBookUtil.setPostition(mBookUtil.getPosition() + num + 2);
            } else {
                mBookUtil.setPostition(mBookUtil.getPosition() + num);
            }
        }

        return reLines;
    }

    //上一章
    public void preChapter() {
        if (mBookUtil.getDirectoryList().size() > 0) {
            int num = currentCharter;
            if (num == 0) {
                num = getCurrentCharter();
            }
            num--;
            if (num >= 0) {
                long begin = mBookUtil.getDirectoryList().get(num).getBookCatalogueStartPos();
                currentPage = getPageForBegin(begin);
                currentPage(true);
                currentCharter = num;
            }
        }
    }

    //下一章
    public void nextChapter() {
        int num = currentCharter;
        if (num == 0) {
            num = getCurrentCharter();
        }
        num++;
        if (num < getDirectoryList().size()) {
            long begin = getDirectoryList().get(num).getBookCatalogueStartPos();
            currentPage = getPageForBegin(begin);
            currentPage(true);
            currentCharter = num;
        }
    }

    //获取现在的章
    public int getCurrentCharter() {
        int num = 0;
        for (int i = 0; getDirectoryList().size() > i; i++) {
            BookCatalogue bookCatalogue = getDirectoryList().get(i);
            if (currentPage.getEnd() >= bookCatalogue.getBookCatalogueStartPos()) {
                num = i;
            } else {
                break;
            }
        }
        return num;
    }

    //绘制当前页面
    public void currentPage(Boolean updateChapter) {
        onDraw(mBookPageWidget.getCurPage(), currentPage.getLines(), updateChapter);
        onDraw(mBookPageWidget.getNextPage(), currentPage.getLines(), updateChapter);
    }

    //更新电量
    public void updateBattery(int mLevel) {
        if (currentPage != null && mBookPageWidget != null && !mBookPageWidget.isRunning()) {
            if (level != mLevel) {
                level = mLevel;
                currentPage(false);
            }
        }
    }

    public void updateTime() {
        if (currentPage != null && mBookPageWidget != null && !mBookPageWidget.isRunning()) {
            String mDate = sdf.format(new java.util.Date());
            if (date != mDate) {
                date = mDate;
                currentPage(false);
            }
        }
    }

    //改变进度
    public void changeProgress(float progress) {
        long begin = (long) (mBookUtil.getBookLen() * progress);
        currentPage = getPageForBegin(begin);
        currentPage(true);
    }

    //改变进度
    public void changeChapter(long begin) {
        currentPage = getPageForBegin(begin);
        currentPage(true);
    }

    //改变字体大小
    public void changeFontSize(int fontSize) {
        this.m_fontSize = fontSize;
        mPaint.setTextSize(m_fontSize);
        calculateLineCount();
        measureMarginWidth();
        currentPage = getPageForBegin(currentPage.getBegin());
        currentPage(true);
    }

    //改变背景
    public void changeBookBg(int type) {
        setBookBg(type);
        currentPage(false);
    }

    //设置页面的背景
    public void setBookBg(int type) {
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        int color = 0;
        switch (type) {
            case Config.BOOK_BG_DEFAULT:
                canvas = null;
                bitmap.recycle();
                if (getBgBitmap() != null) {
                    getBgBitmap().recycle();
                }
                bitmap = BitmapUtil.decodeSampledBitmapFromResource(
                        mContext.getResources(), R.drawable.reader_paper, mWidth, mHeight);
                color = mContext.getResources().getColor(R.color.reader_read_font_default);
                setBookPageBg(mContext.getResources().getColor(R.color.reader_read_bg_default));
                break;
            case Config.BOOK_BG_1:
                canvas.drawColor(mContext.getResources().getColor(R.color.reader_read_bg_1));
                color = mContext.getResources().getColor(R.color.reader_read_font_1);
                setBookPageBg(mContext.getResources().getColor(R.color.reader_read_bg_1));
                break;
            case Config.BOOK_BG_2:
                canvas.drawColor(mContext.getResources().getColor(R.color.reader_read_bg_2));
                color = mContext.getResources().getColor(R.color.reader_read_font_2);
                setBookPageBg(mContext.getResources().getColor(R.color.reader_read_bg_2));
                break;
            case Config.BOOK_BG_3:
                canvas.drawColor(mContext.getResources().getColor(R.color.reader_read_bg_3));
                color = mContext.getResources().getColor(R.color.reader_read_font_3);
                setBookPageBg(mContext.getResources().getColor(R.color.reader_read_bg_3));
                break;
            case Config.BOOK_BG_4:
                canvas.drawColor(mContext.getResources().getColor(R.color.reader_read_bg_4));
                color = mContext.getResources().getColor(R.color.reader_read_font_4);
                setBookPageBg(mContext.getResources().getColor(R.color.reader_read_bg_4));
                break;
        }

        setBgBitmap(bitmap);
        //设置字体颜色
        setmTextColor(color);
    }

    public void setBookPageBg(int color) {
        if (mBookPageWidget != null) {
            mBookPageWidget.setBgColor(color);
        }
    }

    //设置日间或者夜间模式
    public void setDayOrNight(Boolean isNgiht) {
        initBg(isNgiht);
        currentPage(false);
    }

    public void clear() {
        currentCharter = 0;
        bookPath = "";
        bookName = "";
        book = null;
        mBookPageWidget = null;
        mPageEvent = null;
        cancelPage = null;
        prePage = null;
        currentPage = null;
    }

    public static Status getStatus() {
        return mStatus;
    }

    public long getBookLen() {
        return mBookUtil.getBookLen();
    }

    public TRPage getCurrentPage() {
        return currentPage;
    }

    //获取书本的章
    public List<BookCatalogue> getDirectoryList() {
        return mBookUtil.getDirectoryList();
    }

    public String getBookPath() {
        return bookPath;
    }

    //是否是第一页
    public boolean isfirstPage() {
        return m_isfirstPage;
    }

    //是否是最后一页
    public boolean islastPage() {
        return m_islastPage;
    }

    //设置页面背景
    public void setBgBitmap(Bitmap BG) {
        m_book_bg = BG;
    }

    //设置页面背景
    public Bitmap getBgBitmap() {
        return m_book_bg;
    }

    //设置文字颜色
    public void setmTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
    }

    //获取文字颜色
    public int getTextColor() {
        return this.mTextColor;
    }

    //获取 //时间、标题等颜色
    public int getTipTextColor() {
        return Color.argb(Color.alpha(this.mTextColor)-51,Color.red(mTextColor),
                Color.green(mTextColor),Color.blue(mTextColor));
    }

    //获取文字大小
    public float getFontSize() {
        return this.m_fontSize;
    }

    public void setPageWidget(PageWidget mBookPageWidget) {
        this.mBookPageWidget = mBookPageWidget;
    }

    public void setPageEvent(PageEvent pageEvent) {
        this.mPageEvent = pageEvent;
    }

    public interface PageEvent {
        void changeProgress(float progress);
    }

}
