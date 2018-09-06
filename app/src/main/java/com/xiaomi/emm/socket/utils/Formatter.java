package com.xiaomi.emm.socket.utils;

import android.text.TextUtils;


// 主要用于地址转换，比如 二进制转  string , string 转 二进制
public class Formatter {

    final static String TAG = "Formatter";

    // 将浮点数转化 为指定小数位数的 字符串
    public static String FormatFloatData(float value, int decimal) {
        int[] arrayOfInt = { 1, 10, 100, 1000, 10000, 100000 };

        StringBuilder sb = new StringBuilder();
        int i = (int) (0.5D + value * arrayOfInt[decimal]);
        sb.append(i / arrayOfInt[decimal]);
        int j = sb.length();
        sb.append(i % arrayOfInt[decimal] + arrayOfInt[decimal]);
        sb.setCharAt(j, '.');
        return sb.toString();
    }

    public static String FormatToString(byte[] buffer) {
        StringBuffer sb = new StringBuffer(buffer.length * 2);

        int j = 0;
        for (int i = 0; i < buffer.length; i++) {
            sb.append("");
            sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 15, 16));
            sb.append(" ");
        }
        String str = sb.toString();
        str = str.toUpperCase();
        return str;
    }

    public static byte[] FormatToByteArray(String buffer) {

        buffer = buffer.trim();
        String str[] = buffer.split(" ");
        byte[] addr = new byte[str.length];
        for (int i = 0; i < str.length; i++) {
            addr[i] = (byte) Integer.parseInt(str[i], 16);
        }
        return addr;
    }

    // 替换原来的程序
    public static String FormatToString(byte[] buffer, int len) {
        StringBuffer sb = new StringBuffer(buffer.length * 2);

        int j = 0;
        for (int i = 0; i < len; i++) {
            sb.append("");
            sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 15, 16));
            sb.append(" ");
            if (((i + 1) % 8) == 0) {
                sb.append("\n");
            }
        }
        String str = sb.toString();
        str = str.toUpperCase();
        return str;
    }

    // byte 数组 转化 为二进制 字符串，用于不同程序间传递参数
    // 比如 "汉字啊" 的GB内码 以 "babad7d6b0a1" 的格式传给代理
    public static String byte2String(byte[] buffer) {
        StringBuffer sb = new StringBuffer(buffer.length * 2);

        int j = 0;
        for (int i = 0; i < buffer.length; i++) {
            sb.append("");
            sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 15, 16));
            sb.append("");
        }
        String str = sb.toString();
        str = str.toLowerCase();
        return str;
    }

    // UNicode String 串 转化为汉字内码的二进制 字符串，用于不同程序间传递参数
    // 比如 "汉字啊" 的GB内码 以 "babad7d6b0a1" 的格式传给代理
    public static String get_gb_byte(String unicode_str) throws Exception {
        byte[] buffer = unicode_str.getBytes("GBK");
        StringBuffer sb = new StringBuffer(buffer.length * 2);

        int j = 0;
        for (int i = 0; i < buffer.length; i++) {
            sb.append("");
            sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 15, 16));
            sb.append("");
        }
        String str = sb.toString();
        str = str.toLowerCase();
        return str;
    }
    // 二进制转 string, 不换行
    public static String FormatToStringLine(byte[] buffer, int len) {
        StringBuffer sb = new StringBuffer(buffer.length * 2);

        int j = 0;
        for (int i = 0; i < len; i++) {
            sb.append("");
            sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 15, 16));
            sb.append(" ");
        }
        String str = sb.toString();
        str = str.toUpperCase();
        return str;
    }

    // 将 4 位的家庭网络地址转换为字符串
    public static String net2str(byte[] buffer) {
        StringBuffer sb = new StringBuffer(4 * 2);

        int j = 0;
        for (int i = 0; i < 4; i++) {
            sb.append("");
            sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 15, 16));
            sb.append(" ");
        }
        String str = sb.toString();
        str = str.toLowerCase();
        return str;
    }

    // 将 5 位的地址转换为字符串
    public static String addr2str(byte[] buffer) {
        if (buffer == null) {
            String str = "null";
            return str;
        }
        StringBuffer sb = new StringBuffer(5 * 2);

        int j = 0;
        for (int i = 0; i < 5; i++) {
            sb.append("");
            sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 15, 16));
            if (i != 4) {
                sb.append(":");
            }
        }
        String str = sb.toString();
        str = str.toLowerCase();
        return str;
    }

    // 将 字符串转换为 5 位的地址
    public static byte[] str2addr(String addr_str) {
        if(TextUtils.isEmpty(addr_str)) {
          //  ILog.w(TAG, "str2addr() 传入地址为空。");
            return null;
        }
        byte[] addr = new byte[5];
        String[] addr_array = addr_str.split(":");
        if (addr_array.length == 5) {
            for (int i = 0; i < 5; i++) {
                addr[i] = (byte) Integer.parseInt(addr_array[i], 16);
            }
        } else {
        //    ILog.e(TAG, "地址有错误。addr_str = " + addr_str);
            return null;
        }
        return addr;
    }

    public static String FormatToString(byte paramByte) {
        StringBuffer sb = new StringBuffer();
        sb.append(Character.forDigit((paramByte & 240) >> 4, 16));
        sb.append(Character.forDigit(paramByte & 15, 16));
        String str = sb.toString();
        str = str.toLowerCase();
        return str;
    }

    public static String byte2str(byte bb) {
        StringBuffer sb = new StringBuffer();
        sb.append("0x");
        sb.append(Character.forDigit((bb & 240) >> 4, 16));
        sb.append(Character.forDigit(bb & 15, 16));
        String str = sb.toString();
        str = str.toLowerCase();
        return str;
    }

    // mac 地址转换成 string , 长度为 6 位
    public static String mac2str(byte[] buffer) {
        if (buffer == null) {
            String str = "";
            return str;
        }
        StringBuffer sb = new StringBuffer(6 * 2);

        int j = 0;
        for (int i = 0; i < 6; i++) {
            sb.append("");
            sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 15, 16));
            if (i != 5) {
                sb.append(":");
            }
        }
        String str = sb.toString();
        str = str.toLowerCase();
        return str;
    }

    // 将 字符串转换为 6 位的地址
    public static byte[] str2mac(String addr_str) {
        byte[] addr = new byte[6];
        String[] addr_array = addr_str.split(":");
        if (addr_array.length == 6) {
            for (int i = 0; i < 6; i++) {
                addr[i] = (byte) Integer.parseInt(addr_array[i], 16);
            }
        } else {
       //     ILog.e(TAG, "str2mac() 地址有错误。addr_str = " + addr_str);
            return null;
        }
        return addr;
    }

    // chip_id 地址转换成 string , 长度为 7 位
    public static String chip_id2str(byte[] buffer) {
        if (buffer == null) {
            String str = "";
            return str;
        }
        StringBuffer sb = new StringBuffer(7 * 2);

        int j = 0;
        for (int i = 0; i < 7; i++) {
            sb.append("");
            sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 15, 16));
            if (i != 6) {
                sb.append(":");
            }
        }
        String str = sb.toString();
        str = str.toLowerCase();
        return str;
    }

    // 将 字符串转换为 6 位的地址
    public static byte[] str2chip_id(String chip_id_str) {
        if (TextUtils.isEmpty(chip_id_str)){
        //    ILog.e(TAG, "str2chip_id() 地址为空");
            return null;
        }
        byte[] addr = new byte[7];
        String[] addr_array = chip_id_str.split(":");
        if (addr_array.length == 7) {
            for (int i = 0; i < 7; i++) {
                addr[i] = (byte) Integer.parseInt(addr_array[i], 16);
            }
        } else {
         //   ILog.e(TAG, "str2chip_id() 地址有错误。chip_id = " + chip_id_str);
            return null;
        }
        return addr;
    }

    // channel_id 地址转换成 string , 长度为 16 位
    public static String channel_id2str(byte[] buffer) {
        if (buffer == null) {
            String str = "";
            return str;
        }
        StringBuffer sb = new StringBuffer(16 * 2);

        int j = 0;
        for (int i = 0; i < 16; i++) {
            sb.append("");
            sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 15, 16));
            // 最后一位不加上 : 符号
            if (i != 15) {
                sb.append(":");
            }
        }
        String str = sb.toString();
        str = str.toLowerCase();
        return str;
    }

    // 将 字符串转换为 16 位的地址
    public static byte[] str2channel_id(String channel_id_str) {
        if (TextUtils.isEmpty(channel_id_str)){
         //   ILog.e(TAG, "str2channel_id() 地址为空");
            return null;
        }
        byte[] addr = new byte[16];
        String[] addr_array = channel_id_str.split(":");
        if (addr_array.length == 16) {
            for (int i = 0; i < 16; i++) {
                addr[i] = (byte) Integer.parseInt(addr_array[i], 16);
            }
        } else {
        //    ILog.e(TAG, "str2channel_id() 地址有错误。channel_id = " + channel_id_str);
            return null;
        }
        return addr;
    }
}
