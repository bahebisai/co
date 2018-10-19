package com.xiaomi.emm.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {
    private static final String TAG = FileUtils.class.getName();

    /**
     * 获得文件后缀
     *
     * @param fileName
     * @param node
     * @return
     */
    public String getFileEnds(String fileName, String node) {
        if (fileName != null) {
            int position = fileName.indexOf(node);
            return fileName.substring(position + 1, fileName.length());
        }
        return null;
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static boolean copyFile(String oldPath, String newPath) {//todo baii util file
        if (TextUtils.isEmpty(oldPath) && TextUtils.isEmpty(newPath)) {
            return false;
        }
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "复制单个文件操作出错");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 更换文件名
     *
     * @param basePath
     * @param oldName
     * @param newName
     */
    public static void renameFile(String basePath, String oldName, String newName) {//todo baii util file
        File mFile = new File(basePath + File.separator + oldName);
        mFile.renameTo(new File(basePath + File.separator + newName));
    }
}
