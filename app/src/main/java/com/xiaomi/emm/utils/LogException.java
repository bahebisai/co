package com.xiaomi.emm.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.xiaomi.emm.base.BaseApplication;
import java.io.File;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/15.
 */

public class LogException implements Thread.UncaughtExceptionHandler {

    public static final String TAG = "LogException";

    //系统默认的UncaughtException处理类
    private static Thread.UncaughtExceptionHandler mDefaultHandler;

    private static LogException mLogException = null;
    //程序的Context对象
    private static Context mContext;
    //用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();

    //用于格式化日期,作为日志文件名的一部分
    private DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS", Locale.US );
    private DateFormat fileDateFormat = new SimpleDateFormat( "yyyy-MM-dd", Locale.US );

    private LogException() {
    }

    /**
     * 单例
     *
     * @return
     */
    public static LogException getSingleInstance() {
        if (null == mLogException) {
            synchronized (LogException.class) {
                if (null == mLogException) {
                    mLogException = new LogException();
                }
            }
        }
        return mLogException;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {

        mContext = context;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler( this );
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (!handleException( throwable ) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException( thread, throwable );
        } /*else {
            try {
                Thread.sleep( 3000 );
            } catch (InterruptedException e) {
                Log.e( TAG, "error : ", e );
            }
            //退出程序
            android.os.Process.killProcess( android.os.Process.myPid() );
            System.exit( 1 );
        }*/
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param throwable
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        //使用Toast来显示异常信息
        /*new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText( mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG ).show();
                Looper.loop();
            }
        }.start();*/
        //收集设备参数信息
        collectDeviceInfo( mContext );
        //保存日志文件
        saveCrashInfoToFile( throwable );
        return true;
    }

    /**
     * c
     * 收集设备参数信息
     *
     * @param context
     */
    public void collectDeviceInfo(Context context) {

        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;

        try {
            packageInfo = packageManager.getPackageInfo( context.getPackageName(), PackageManager.GET_ACTIVITIES );
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (packageInfo != null) {
            String versionName = packageInfo.versionName == null ? "null" : packageInfo.versionName;
            String versionCode = packageInfo.versionCode + "";
            infos.put( "versionName", versionName );
            infos.put( "versionCode", versionCode );
            infos.put( "time", dateFormat.format( System.currentTimeMillis() ) );
        }

        Field[] fields = Build.class.getDeclaredFields();//获得build构造函数的参数
        for (Field field : fields) {
            try {
                field.setAccessible( true );
                infos.put( field.getName(), field.get( null ).toString() );
                Log.d( TAG, field.getName() + " : " + field.get( null ) );
            } catch (Exception e) {
                Log.e( TAG, "an error occured when collect crash info", e );
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param throwable
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCrashInfoToFile(Throwable throwable) {

        StringBuffer sb = new StringBuffer();

        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append( key + "=" + value + "\n" );
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter( writer );
        throwable.printStackTrace( printWriter );

        Throwable cause = throwable.getCause();//异常转换

        while (cause != null) {
            cause.printStackTrace( printWriter );
            cause = cause.getCause();
        }

        printWriter.close();
        String result = writer.toString();
        sb.append( result );

        try {
            String crash_path = BaseApplication.baseLogsPath + "/crash";

            File crash_dir = new File( crash_path );

            if ( !crash_dir.exists() ) {
                crash_dir.mkdir();
            }

            String path = BaseApplication.baseLogsPath + "/crash/crash" + fileDateFormat.format( System.currentTimeMillis() ) + ".log";

            File file = new File( path );

            if (!file.exists()) {
                Log.w( TAG, "file is not exists!" );
                file.createNewFile();
            }

            RandomAccessFile raf = null;

            raf = new RandomAccessFile( file, "rw" );
            raf.seek( file.length() );
            raf.write( sb.toString().getBytes() );

            raf.close();

            return path;
        } catch (Exception e) {
            Log.e( TAG, "an error occured while writing file...", e );
        }
        return null;
    }
}
