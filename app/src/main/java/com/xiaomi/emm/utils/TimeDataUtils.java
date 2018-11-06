package com.xiaomi.emm.utils;

import android.content.Context;

import com.xiaomi.emm.R;
import com.xiaomi.emm.features.presenter.TheTang;
import com.xiaomi.emm.model.TimeData;

import java.util.Date;

import static com.xiaomi.emm.utils.TimeUtils.getHmString;

/**
 * 专为{@link TimeData}设置的工具类，判断后台下发的时间单元是否过期等
 */
public class TimeDataUtils {
    private static final String TAG = TimeDataUtils.class.getName();
    public static final int TYPE_EVERY_DAY = 1;
    public static final int TYPE_EVERY_WEEK = 2;
    public static final int TYPE_WEEK_DAY = 3;//周一到周五
    public static final int TYPE_SPECIFIC_DAY = 4;//特定时间


    /**
     * 判断所给时间是否过期
     *
     * @param time     需要判断的时间
     * @param timeData 时间单元
     * @return
     */
    public static boolean isExpired(long time, TimeData timeData) {
        Date dateEnd = TimeUtils.getEndDate(timeData.getEndDateRange());
        Date now = new Date(time);
        if (now.after(dateEnd)) {
//            Log.d("baii", "isExpired-------------------------------");
            LogUtil.writeToFile(TAG, "isExpired-------------------------------");
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断是否在时间单元规定的时间内
     *
     * @param time
     * @param timeData
     * @return true 在时间范围内， false 不在时间范围内
     */
    public static boolean isInTimeUnitRange(long time, TimeData timeData) {
        int weekDay = TimeUtils.getWeekDay(time);
        for (TimeData.TimeUnit timeUnit : timeData.getTimeUnits()) {
            String type = timeUnit.getUnitType();
            boolean isInDayRange = false;
//            Log.d("baii", "type " + type);
            switch (Integer.parseInt(type)) {
                case TYPE_EVERY_DAY: //每天
                    isInDayRange = true;
                    break;
                case TYPE_EVERY_WEEK: //每周
                    if (String.valueOf(weekDay).equals(timeUnit.getTypeDate())) {
                        isInDayRange = true;
                    }
                    break;
                case TYPE_WEEK_DAY: //工作日，周一到周五
                    if (weekDay >= 0 && weekDay <= 5) {
                        isInDayRange = true;
                    }
                    break;
                case TYPE_SPECIFIC_DAY:  //特定时间
                    String date = TimeUtils.getMonthDay(time);
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

    /**
     * 判断是否在日期范围内
     *
     * @param time
     * @param timeData
     * @return
     */
    public static boolean isInDateRange(long time, TimeData timeData) {
        Date dateStart = TimeUtils.getStartDate(timeData.getStartDateRange());
        Date dateEnd = TimeUtils.getEndDate(timeData.getEndDateRange());
        Date dateSms = new Date(time);
        if (dateSms.after(dateStart) && dateSms.before(dateEnd)) {
//            Log.d("baii", "in date-------------------------------");
            return true;
        } else {
            return false;
        }
    }

    private static boolean isInTimeRange(String start, String end, String time) {
        int startTime = Integer.parseInt(start);
        int endTime = Integer.parseInt(end);
        int toCompare = Integer.parseInt(time);
        if (toCompare >= startTime && toCompare <= endTime) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取策略详情中药显示的时间单元信息
     *
     * @param timeData
     * @return
     */
    public static String getDisplayTimeString(TimeData timeData) {
        StringBuilder stringBuilder = new StringBuilder();
        Context context = TheTang.getSingleInstance().getContext();

        stringBuilder.append(context.getResources().getString(R.string.time_range, timeData.getStartDateRange() + " ~ " + timeData.getEndDateRange()))
                .append("\n");
        for (TimeData.TimeUnit timeUnit : timeData.getTimeUnits()) {
            String type = timeUnit.getUnitType();
//            Log.d("baii", "type " + type);
            switch (Integer.parseInt(type)) {
                case TYPE_EVERY_DAY: //每天
                    stringBuilder.append(context.getResources().getString(R.string.every_day));
                    break;
                case TYPE_EVERY_WEEK: //每周
                    stringBuilder.append(context.getResources().getString(R.string.every_week)).append(timeUnit.getTypeDate()).append("\n");
                    break;
                case TYPE_WEEK_DAY: //工作日，周一到周五
                    stringBuilder.append(context.getResources().getString(R.string.time_work));
                    break;
                case TYPE_SPECIFIC_DAY:  //特定时间
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
