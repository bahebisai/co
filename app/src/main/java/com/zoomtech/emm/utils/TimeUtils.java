package com.zoomtech.emm.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {
    private static final String TAG = TimeUtils.class.getName();

    /**
     * 根据后台下发时间的开始时间字串获取该天零点的Date对象
     *
     * @param startTime
     * @return
     */
    public static Date getStartDate(String startTime) {
//            String dateStr = "2018-1-1";
        startTime = startTime.replaceAll("/", "-");
        DateFormat dateFormat;
        if (startTime.length() >= 10) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd");//bai 与下发时间格式保持一致
        } else {
            dateFormat = new SimpleDateFormat("yyyy-M-d");//bai 与下发时间格式保持一致
        }
        Date date = new Date();
        try {
            date = dateFormat.parse(startTime);//date 0 Wed Jan 31 00:00:00 GMT+08:00 2018
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 根据后台下发时间的结束时间字串获取第二天零点的Date对象
     *
     * @param endTime
     * @return
     */
    public static Date getEndDate(String endTime) {
//        endTime = endTime.replaceAll()
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");//bai 与下发时间格式保持一致

        endTime = endTime.replaceAll("/", "-");
        DateFormat dateFormat;
        if (endTime.length() >= 10) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd");//bai 与下发时间格式保持一致
        } else {
            dateFormat = new SimpleDateFormat("yyyy-M-d");//bai 与下发时间格式保持一致
        }

        Date date = new Date();
        try {
            date = dateFormat.parse(endTime);//date 0 Wed Jan 31 00:00:00 GMT+08:00 2018
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            date = calendar.getTime();//date 1 Thu Feb 01 00:00:00 GMT+08:00 2018
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 毫秒得到时分yyyyMMdd HHmmss
     *
     * @return 字符串 HHmm
     */
    public static String getHmString(long time) {
        Date currentTime = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HHmmss");
        String dateString = formatter.format(currentTime);
        String hmString = dateString.substring(9, 13);
        return hmString;
    }

    /**
     * 得到星期几，与后台类型相同的表示
     *
     * @return 字符串 HHmm
     */
    public static int getWeekDay(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int weekDay = day - 1; //使与下发的json表示一致, eg.Calendar.MONDAY = 0
        return weekDay;
    }

    /**
     * 通过毫秒获取到当前日期
     *
     * @param time
     * @return pattern eg.2018-1-1
     */
    public static String getMonthDay(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-d");
        Date date = new Date(time);
        String dateString = formatter.format(date);
        return dateString;
    }

    public static boolean isNumeric(String str) {//baii util ??? todo
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 获取毫秒对应的日期格式yyyy-MM-dd HH:mm:ss
     *
     * @param time
     * @return
     */
    public static String getDateString(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        String dateString = formatter.format(date);
        return dateString;
    }

    /**
     * 根据时间字串yyyyMMddHHmmss获取yyyy-MM-dd HH:mm:ss类型字串
     *
     * @param startTime
     * @return
     */
    public static String getDateString(String startTime) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        try {
            date = dateFormat.parse(startTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    /**
     * 获得duration对应的日期
     *
     * @param duration 天数，如7
     * @return
     */
    public static List<String> getDates(String mDate, int duration) {
        List<String> dateList = new ArrayList<>();
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date beginDate = null;
        try {
            beginDate = mSimpleDateFormat.parse(mDate);//String 转 日期
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (int i = 0; i <= duration; i++) {
            Calendar date = Calendar.getInstance();
            date.setTime(beginDate);
            date.set(Calendar.DATE, date.get(Calendar.DATE) - i);
            String endDate = null;
            endDate = mSimpleDateFormat.format(date.getTime());
            dateList.add(endDate);
        }
        return dateList;
    }

    /**
     * 获取截至今天零点本月的毫秒时间
     *
     * @return
     */
    public static long getTimeOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return (cal.getTimeInMillis());
        //  return new Date().getTime();
    }

    /**
     * 获得本月第一天0点时间
     *
     * @return
     */
    public static long getFirstDayTimeOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));

        SimpleDateFormat mm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String format1 = mm.format(cal.getTime());
        long time = 0;
        try {
            time = mm.parse(format1).getTime();
        } catch (ParseException e) {
            return time;
        }
        return time;
    }

    /**
     * 获得本周日24点时间
     *
     * @return
     */
    public static int getTimesWeeknight() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return (int) ((cal.getTime().getTime() + (7 * 24 * 60 * 60 * 1000)) / 1000);
    }

    /**
     * //获得当天24点时间
     *
     * @return
     */
    public static int getTimesnight() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 24);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (int) (cal.getTimeInMillis() / 1000);
    }

    /**
     * 获得本周一0点时间
     *
     * @return
     */
    public static long getMondayTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        SimpleDateFormat mm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String format1 = mm.format(cal.getTime());

        try {
            return mm.parse(format1).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }
}
