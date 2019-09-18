package com.zyb.base.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 常用时间工具类
 */
public class TimeUtil {


    private static SimpleDateFormat formatHHmm = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static SimpleDateFormat formatYear = new SimpleDateFormat("yyyy", Locale.getDefault());
    private static SimpleDateFormat formatMonth = new SimpleDateFormat("MM", Locale.getDefault());
    private static SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static SimpleDateFormat formatDate2 = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    private static SimpleDateFormat formatDay = new SimpleDateFormat("d", Locale.getDefault());
    private static SimpleDateFormat formatMonthDay = new SimpleDateFormat("M-d", Locale.getDefault());
    private static SimpleDateFormat formatDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    /**
     * 格式化日期
     *
     * @return 年月日
     */
    public static String formatDate(Date date) {
        return formatDate.format(date);
    }

    /**
     * 格式化日期
     *
     * @return 年月日
     */
    public static String formatDate2(Date date) {
        return formatDate2.format(date);
    }

    /**
     * 格式化日期
     *
     * @return 年月日 时分秒
     */
    public static String formatDateTime(Date date) {
        return formatDateTime.format(date);
    }


    /**
     * 将时间戳解析成日期
     *
     * @return 年
     */
    public static String parseYear(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        Date date = calendar.getTime();
        return formatYear.format(date);
    }

    /**
     * 将时间戳解析成日期
     *
     * @return 年
     */
    public static String parseHHmm(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        Date date = calendar.getTime();
        return formatHHmm.format(date);
    }

    /**
     * 将时间戳解析成日期
     *
     * @return 月
     */
    public static String parseMonth(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        Date date = calendar.getTime();
        return formatMonth.format(date);
    }

    /**
     * 将时间戳解析成日期
     *
     * @return 年月日
     */
    public static String parseDate(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        Date date = calendar.getTime();
        return formatDate(date);
    }

    /**
     * 将时间戳解析成日期
     *
     * @return 年月日
     */
    public static String parseDate2(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        Date date = calendar.getTime();
        return formatDate2(date);
    }


    /**
     * 将时间戳解析成日期
     *
     * @return 年月日 时分秒
     */
    public static String parseDateTime(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        Date date = calendar.getTime();
        return formatDateTime(date);
    }


    /**
     * 解析日期
     */
    public static Date parseDate(String date) {
        Date mDate = null;
        try {
            mDate = formatDate.parse(date);
        } catch (ParseException e) {
            return null;
        }
        return mDate;
    }


    /**
     * 解析日期
     */
    public static Date parseDateTime(String datetime) {
        Date mDate = null;
        try {
            mDate = formatDateTime.parse(datetime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return mDate;
    }


    /**
     * 以友好的方式显示时间
     */
    public static String friendlyTime(String sdate) {
        Date time = parseDateTime(sdate);
        if (time == null) {
            return "Unknown";
        }
        String ftime = "";
        Calendar cal = Calendar.getInstance();
        // 判断是否是同一天
        String curDate = formatDate.format(cal.getTime());
        String paramDate = formatDate.format(time);
        if (curDate.equals(paramDate)) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0) {
                ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000, 1) + "分钟前";
            } else {
                ftime = hour + "小时前";
            }
            return ftime;
        }
        long lt = time.getTime() / 86400000;
        long ct = cal.getTimeInMillis() / 86400000;
        int days = (int) (ct - lt);
        if (days == 0) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0) {
                ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000, 1) + "分钟前";
            } else {
                ftime = hour + "小时前";
            }
        } else if (days == 1) {
            ftime = "昨天";
        } else if (days == 2) {
            ftime = "前天";
        } else if (days > 2 && days <= 10) {
            ftime = days + "天前";
        } else if (days > 10) {
            ftime = formatDate.format(time);
        }
        return ftime;
    }


    /**
     * 根据日期获取当期是周几
     */
    public static String getWeek(Date date) {
        String[] weeks = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (week_index < 0) {
            week_index = 0;
        }
        return weeks[week_index];
    }

    /**
     * 通过时间秒毫秒数判断两个时间的间隔
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24));
        return days;
    }

    public static final int SECONDS_IN_DAY = 60 * 60 * 24;
    public static final long MILLIS_IN_DAY = 1000L * SECONDS_IN_DAY;

    public static boolean isSameDay(final long ms1, final long ms2) {
        final long interval = ms1 - ms2;
        return interval < MILLIS_IN_DAY
                && interval > -1L * MILLIS_IN_DAY
                && toDay(ms1) == toDay(ms2);
    }
    private static long toDay(long millis) {
        return (millis + TimeZone.getDefault().getOffset(millis)) / MILLIS_IN_DAY;
    }

}
