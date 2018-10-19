package com.xiaomi.emm.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ConvertUtils {
    private static final String TAG = ConvertUtils.class.getName();

    /**
     * 将Map转String
     *
     * @param data
     */
    public static String formatStringFromMap(Map<String, String> data) {
        if (data == null) {
            return null;
        }

        JSONArray jsonArray = new JSONArray();
        Iterator<Map.Entry<String, String>> iterator = data.entrySet().iterator();

        JSONObject object = new JSONObject();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            try {
                object.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {

            }
        }
        jsonArray.put(object);
        return jsonArray.toString();
    }

    /**
     * String转Map
     *
     * @param result
     * @return
     */
    public static Map<String, String> formatMapFromString(String result) {
        if (result == null)
            return null;

        Map<String, String> data = new HashMap<String, String>();
        try {
            JSONArray array = new JSONArray(result);
            for (int i = 0; i < array.length(); i++) {
                JSONObject itemObject = array.getJSONObject(i);
                JSONArray names = itemObject.names();
                if (names != null) {
                    for (int j = 0; j < names.length(); j++) {
                        String name = names.getString(j);
                        String value = itemObject.getString(name);
                        data.put(name, value);
                    }
                }
            }
        } catch (JSONException e) {
            LogUtil.writeToFile(TAG, " formatMapFromString: " + e.getCause().toString());
        }
        return data;
    }

    // 流量转化
    public static String convertTraffic(long traffic) {
        BigDecimal trafficKB;
        BigDecimal trafficMB;
        BigDecimal trafficGB;

        BigDecimal temp = new BigDecimal(traffic);
        BigDecimal divide = new BigDecimal(1000);
        trafficKB = temp.divide(divide, 2, 1);
        if (trafficKB.compareTo(divide) > 0) {
            trafficMB = trafficKB.divide(divide, 2, 1);
            if (trafficMB.compareTo(divide) > 0) {
                trafficGB = trafficMB.divide(divide, 2, 1);
                return trafficGB.doubleValue() + "GB";
            } else {
                return trafficMB.doubleValue() + "MB";
            }
        } else {
            return trafficKB.doubleValue() + "KB";
        }
    }

    /**
     * 将时间转为时长
     *
     * @param time
     * @return
     */
    public static String formatTimeLength(long time) {//todo baii util time
        String date = null;
        String hour = null;
        String minute = null;
        String timeResult = null;

        if (time > 24 * 3600 * 1000) {
            date = time / (24 * 3600 * 1000) + "天";
        }

        time = time % (24 * 3600 * 1000);

        if (time > 3600 * 1000) {
            hour = time / (3600 * 1000) + "小时";
        }
        time = time % (3600 * 1000);

        if (time > 60 * 1000) {
            minute = time / (60 * 1000) + "分钟";
        }
        if (date != null) {
            timeResult = date;
        }
        if (hour != null) {
            if (timeResult == null) {
                timeResult = hour;
            } else {
                timeResult += hour;
            }
        }
        if (minute != null) {
            if (timeResult == null) {
                timeResult = minute;
            } else {
                timeResult += minute;
            }
        }
        if (timeResult == null) {
            timeResult = "0分钟";
        }
        return timeResult;
    }

    /**
     * 带单位的数据格式转换
     *
     * @param fileS 文件大小  单位为byte
     * @return
     */
    public static String formatFileSize(long fileS) {//todo baii util file
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 数据单位转换
     *
     * @param fileS 文件大小  单位为byte
     * @return
     */
    public static String formatFile(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS);//+ "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024); //+ "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576); //+ "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824);//+ "GB";
        }
        return fileSizeString;
    }

    /**
     * 获取数据单位
     *
     * @param size 文件大小  单位为byte
     * @return
     */
    public static String getUnit(float size) {
        String unit = "";
        if (size < 1024) {
            unit = "B";
        } else if (size < 1048576) {
            unit = "KB";
        } else if (size < 1073741824) {
            unit = "MB";
        } else {
            unit = "GB";
        }
        return unit;
    }
}
