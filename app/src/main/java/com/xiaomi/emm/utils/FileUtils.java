package com.xiaomi.emm.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * File相关工具类，如获取文件后缀、复制文件、文件打包等。
 */
public class FileUtils {
    private static final String TAG = FileUtils.class.getName();

    /**
     * 获得文件后缀
     *
     * @param fileName 文件名
     * @param node     一般为"."
     * @return
     */
    public String getFileEnds(String fileName, String node) {
        if (fileName != null) {
//            int position = fileName.indexOf(node);
            int position = fileName.lastIndexOf(node);
            return fileName.substring(position + 1, fileName.length());
        }
        return null;
    }

    /**
     * 获取文件中的数据，转字符，并添加到String
     *
     * @param filePath
     * @return
     */
    public String getFileData(String filePath) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean true 复制成功，false复制失败
     */
    public static boolean copyFile(String oldPath, String newPath) {
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
     * 文件重命名
     *
     * @param basePath
     * @param oldName
     * @param newName
     */
    public static void renameFile(String basePath, String oldName, String newName) {
        File mFile = new File(basePath + File.separator + oldName);
        mFile.renameTo(new File(basePath + File.separator + newName));
    }

    /**
     * 文件打包zip
     *
     * @param srcFile
     * @param desFile
     */
    public static File zip(File srcFile, File desFile) {
        GZIPOutputStream gZIPOutputStream = null;
        FileInputStream fileInputStream = null;
        //创建压缩输出流,将目标文件传入
        try {
            gZIPOutputStream = new GZIPOutputStream(new FileOutputStream(desFile));
            fileInputStream = new FileInputStream(srcFile);
            byte[] buffer = new byte[1024];
            int len = -1;
            //利用IO流写入写出的形式将源文件写入到目标文件中进行压缩
            while ((len = (fileInputStream.read(buffer))) != -1) {
                gZIPOutputStream.write(buffer, 0, len);
            }
            gZIPOutputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return desFile;
    }

    /**
     * 生成log压缩包
     *
     * @param dates 需要上传日志的日期
     * @return
     */
    public static File generateLogZip(List<String> dates, String path) {
        File tempFile = mergeFile(dates, path);
        File zipTempFile = null;
        //压缩临时log文件
        try {
            zipTempFile = File.createTempFile("crash_temp_zip", ".zip");
        } catch (IOException e) {
            e.printStackTrace();
        }
        zipTempFile = zip(tempFile, zipTempFile);
        return zipTempFile;
    }

    /**
     * 合并文件
     *
     * @throws IOException
     */
    private static File mergeFile(List<String> srcFiles, String desPath) {
        //创建临时文件，用于合并文件
        File tempFile = null;
        try {
            tempFile = File.createTempFile("crash_temp", ".log");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (tempFile != null && tempFile.exists()) {
            tempFile.setWritable(true);
            tempFile.setReadable(true);
        }
        InputStream inputStream = null;
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tempFile, "rw");
            for (int i = 0; i < srcFiles.size(); i++) {
                int len = 0;
                File file = new File(desPath + srcFiles.get(i) + ".log");
                if (!file.exists()) {
                    continue;
                }
                inputStream = new FileInputStream(file);
                byte[] buff = new byte[1024];
                while ((len = inputStream.read(buff)) != -1) {
                    raf.write(buff, 0, len);
                }
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (raf != null) {
                raf.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }
}
