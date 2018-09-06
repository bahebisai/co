package com.xiaomi.emm.features.impl;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.xiaomi.emm.base.BaseApplication;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.OrderConfig;
import com.xiaomi.emm.features.http.LogUpLoadService;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.xiaomi.emm.R.string.file;


/**
 * Created by Administrator on 2017/9/20.
 */

public class LogUploadImpl extends BaseImpl<LogUpLoadService> {

    public final static String TAG = "LogUploadImpl";
    Context mContext;

    public LogUploadImpl(Context context) {
        mContext = context;
    }

    public void logUpload(String id, String date) {

        String version = android.os.Build.VERSION.RELEASE; //系统版本号
        String model = android.os.Build.MODEL; //系统型号
        String alias = PreferencesManager.getSingleInstance().getData( Common.alias ); //alias

        JSONObject json = new JSONObject();
        try {
            json.put( "version", version );
            json.put( "model", model );
            json.put( "alias", alias );
            json.put( "id", Integer.parseInt( id ) );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody description = RequestBody.create( MediaType.parse( "multipart/form-data" ), json.toString() );


        File file = excuteFile( date );

        RequestBody requestFile = RequestBody.create( MediaType.parse( "multipart/form-data" ), file );

        MultipartBody.Part body = MultipartBody.Part.createFormData( "file", file.getName(), requestFile );

        mService.logUpload( description, body ).enqueue( new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //如果log未上传成功将在网络变化时继续上传
                if (TheTang.getSingleInstance().whetherSendSuccess(response)) {
                    PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
                    preferencesManager.removeLogData( "logId" );
                    preferencesManager.removeLogData( "isWifiUpload" );
                    preferencesManager.removeLogData( "date" );
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //执行成功
               // TheTang.getSingleInstance().sendExcuteComplete( String.valueOf( OrderConfig.upload_debug_log ), "false" );
            }
        } );
    }

    private File excuteFile(String date) {

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
    public static File zip(File srcFile, File desFile) {
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
    public static List<String> getOldDate(String mDate, int distanceDay) {

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
    private static File mergeFile(List<String> file_list) {

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
