package com.xiaomi.emm.utils;

import java.io.File;
import java.io.FileFilter;
import java.security.MessageDigest;
import java.util.regex.Pattern;

/**
 * 工具类
 * Created by Administrator on 2017/6/16.
 */

public class ToolUtils {//todo baii util encrypt

    //MD5加密
    public static String MD5(String passWord) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }

        char[] charArray = passWord.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }

        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();

        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    //MD5解密
    public static String encryptmd5(String str) {
        char[] pass = str.toCharArray();
        for (int i = 0; i < pass.length; i++) {
            pass[i] = (char) (pass[i] ^ 'l');
        }
        return new String(pass);
    }

    /**
     * 获取CPU核心数
     *
     * @return
     */
    public static int getNumCores() {//todo baii util device

        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                // Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            // Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            // Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            e.printStackTrace();
            // Default to return 1 core
            return 1;
        }
    }
}
