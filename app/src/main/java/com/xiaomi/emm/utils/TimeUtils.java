package com.xiaomi.emm.utils;

import android.content.Context;
import android.util.Log;

import com.xiaomi.emm.R;
import com.xiaomi.emm.model.TimeData;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {//todo baii util time separate to time and timeData
    private static final String TAG = TimeUtils.class.getName();
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
            date = dateFormat.parse(startTime);
//            Log.d("baii", "date 0 " + date.toString());//date 0 Wed Jan 31 00:00:00 GMT+08:00 2018
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

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
            date = dateFormat.parse(endTime);
//            Log.d("baii", "date 0 " + date.toString());//date 0 Wed Jan 31 00:00:00 GMT+08:00 2018
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            date = calendar.getTime();
//            Log.d("baii", "date 1 " + date.toString());//date 1 Thu Feb 01 00:00:00 GMT+08:00 2018
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static boolean isExpired(long time, TimeData timeData) {
        Date dateEnd = getEndDate(timeData.getEndDateRange());
        Date now = new Date(time);
        if (now.after(dateEnd)) {
//            Log.d("baii", "isExpired-------------------------------");
            LogUtil.writeToFile(TAG,"isExpired-------------------------------");
            return true;
        } else {
            return false;
        }
    }

    /**
     * 得到时分
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
     * 得到星期几
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
     * @param time
     * @return pattern eg.2018-1-1
     */
    public static String getMonthDay(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-d");
        Date date = new Date(time);
        String dateString = formatter.format(date);
        return dateString;
    }

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }


    public static boolean isInTimeUnitRange(long time, TimeData timeData) {
        int weekDay = getWeekDay(time);
        for (TimeData.TimeUnit timeUnit : timeData.getTimeUnits()) {
            String type = timeUnit.getUnitType();
            boolean isInDayRange = false;
//            Log.d("baii", "type " + type);
            switch (type) {
                case "1": //每天
                    isInDayRange = true;
                    break;
                case "2": //每周
                    if (String.valueOf(weekDay).equals(timeUnit.getTypeDate())) {
                        isInDayRange = true;
                    }
                    break;
                case "3": //工作日，周一到周五
                    if (weekDay >= 0 && weekDay <= 5) {
                        isInDayRange = true;
                    }
                    break;
                case "4":  //特定时间
                    String date = getMonthDay(time);
//                    Log.d("baii", "date sms " + date);
//                    Log.d("baii", "date type " + timeUnit.getTypeDate());
                    if (date.equals(timeUnit.getTypeDate())) {
                        isInDayRange = true;
                    }
                    break;
                default:
                    break;
            }
            if (isInDayRange) {
//                Log.d("baii", "in day-------------------------------");
                String hmTime = getHmString(time);
                String startTime = timeUnit.getStartTime().replace(":", "");
                String endTime = timeUnit.getEndTime().replace(":", "");
                if (isInTimeRange(startTime, endTime, hmTime)) {
//                    Log.d("baii", "in time-------------------------------");
                    LogUtil.writeToFile(TAG, "in time-------------------------------");
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isInDateRange(long time, TimeData timeData) {
        Date dateStart = getStartDate(timeData.getStartDateRange());
        Date dateEnd = getEndDate(timeData.getEndDateRange());
        Date dateSms = new Date(time);
        if (dateSms.after(dateStart) && dateSms.before(dateEnd)) {
//            Log.d("baii", "in date-------------------------------");
            return true;
        } else {
            return false;
        }
    }

    private static boolean isInTimeRange(String start, String end, String smsTime) {
        int startTime = Integer.parseInt(start);
        int endTime = Integer.parseInt(end);
        int toCompare = Integer.parseInt(smsTime);
        if (toCompare >= startTime && toCompare <= endTime) {
            return true;
        } else {
            return false;
        }
    }

    public static String getDateString(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        String dateString = formatter.format(date);
        return dateString;
    }

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


    public static String getDisplayTimeString(TimeData timeData) {
        StringBuilder stringBuilder = new StringBuilder();
        Context context = TheTang.getSingleInstance().getContext();

        stringBuilder.append(context.getResources().getString(R.string.time_range, timeData.getStartDateRange() + " ~ " + timeData.getEndDateRange()))
                .append("\n");
        for (TimeData.TimeUnit timeUnit : timeData.getTimeUnits()) {
            String type = timeUnit.getUnitType();
//            Log.d("baii", "type " + type);
            switch (type) {
                case "1": //每天
                    stringBuilder.append(context.getResources().getString(R.string.every_day));
                    break;
                case "2": //每周
                    stringBuilder.append(context.getResources().getString(R.string.every_week)).append(timeUnit.getTypeDate()).append("\n");
                    break;
                case "3": //工作日，周一到周五
                    stringBuilder.append(context.getResources().getString(R.string.time_work));
                    break;
                case "4":  //特定时间
                    stringBuilder.append(context.getResources().getString(R.string.special_time)).append(timeUnit.getTypeDate()).append("\n");
                    break;
                default:
                    break;
            }
            String startTime = timeUnit.getStartTime();
            String endTime = timeUnit.getEndTime();
            stringBuilder.append(startTime).append(" - ").append(endTime).append("\n");
        }
//        Log.d("baii", stringBuilder.toString());
        return stringBuilder.toString();
    }
}
