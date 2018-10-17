package com.xiaomi.emm.utils;

import com.xiaomi.emm.base.BaseApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class LogFileUtil {//todo baii util file
    public static File excuteFile(String date) {

        List<String> list_info = new ArrayList<>();
        list_info = getOldDate( date, 7 );//七天内的Log

        File tempFile = mergeFile( list_info );

        File zipTempFile = null;

        //压缩临时log文件
        try {
            zipTempFile = File.createTempFile( "crash_temp_zip", ".zip" );
        } catch (IOException e) {
            e.printStackTrace();
        }

        zipTempFile = zip( tempFile, zipTempFile );

        return zipTempFile;
    }

    /**
     * 文件打包
     *
     * @param srcFile
     * @param desFile
     */
    public static File zip(File srcFile, File desFile) {//todo baii util fileio
        GZIPOutputStream gZIPOutputStream = null;
        FileInputStream fileInputStream = null;
        //创建压缩输出流,将目标文件传入
        try {
            gZIPOutputStream = new GZIPOutputStream( new FileOutputStream( desFile ) );
            fileInputStream = new FileInputStream( srcFile );
            byte[] buffer = new byte[1024];
            int len = -1;
            //利用IO流写入写出的形式将源文件写入到目标文件中进行压缩
            while ((len = (fileInputStream.read( buffer ))) != -1) {
                gZIPOutputStream.write( buffer, 0, len );
            }
            gZIPOutputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return desFile;
    }

    /**
     * 获得前后日期
     *
     * @param distanceDay
     * @return
     */
    public static List<String> getOldDate(String mDate, int distanceDay) {//todo baii util time

        List<String> dateList = new ArrayList<>();

        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        Date beginDate = null;
        try {
            beginDate = mSimpleDateFormat.parse( mDate );//String 转 日期
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (int i = 0; i <= distanceDay; i++) {

            Calendar date = Calendar.getInstance();
            date.setTime( beginDate );

            date.set( Calendar.DATE, date.get( Calendar.DATE ) - i );

            String endDate = null;

            endDate = mSimpleDateFormat.format( date.getTime() );

            dateList.add( endDate );
        }
        return dateList;
    }

    /**
     * 合并文件
     *
     * @throws IOException
     */
    private static File mergeFile(List<String> file_list) {//todo baii util fileio

        //创建临时文件，用于合并文件
        File tempFile = null;
        try {
            tempFile = File.createTempFile( "crash_temp", ".log" );
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (tempFile !=null && tempFile.exists()) {
            tempFile.setWritable( true );
            tempFile.setReadable( true );
        }

        InputStream inputStream = null;
        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile( tempFile, "rw" );

            for (int i = 0; i < file_list.size(); i++) {

                int len = 0;

                File file = new File( BaseApplication.baseLogsPath + "/crash/crash" + file_list.get( i ) + ".log" );

                if (!file.exists()) {
                    continue;
                }

                inputStream = new FileInputStream( file );

                byte[] buff = new byte[1024];

                while ((len = inputStream.read( buff )) != -1) {
                    raf.write( buff, 0, len );
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
