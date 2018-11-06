package com.xiaomi.emm.utils;

import java.security.MessageDigest;

/**
 * 加密解密工具类
 */

public class EncryptUtils {

    /**
     * MD5加密
     *
     * @param passWord
     * @return
     */
    public static String MD5(String passWord) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
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

    /**
     * MD5解密
     *
     * @param str
     * @return
     */
    public static String encryptmd5(String str) {
        char[] pass = str.toCharArray();
        for (int i = 0; i < pass.length; i++) {
            pass[i] = (char) (pass[i] ^ 'l');
        }
        return new String(pass);
    }
}
