package com.xiaomi.emm.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.xiaomi.emm.base.BaseApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Log记录工具类，使用前先init
 */

public class LogUtil implements Thread.UncaughtExceptionHandler {
    private static String TAG = "LogUtil";
    //log日志存放路径
    private static String logPath = null;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);//日期格式;
    static String fileName;
    static File file;

    //below is for Exception
    //系统默认的UncaughtException处理类
    private static Thread.UncaughtExceptionHandler mDefaultHandler;
    private static LogUtil mLogUtil = null;
    //程序的Context对象
    private static Context mContext;
    //用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();
    //用于格式化日期,作为日志文件名的一部分
    private DateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public static LogUtil getSingleInstance() {
        if (null == mLogUtil) {
            synchronized (LogUtil.class) {
                if (null == mLogUtil) {
                    mLogUtil = new LogUtil();
                }
            }
        }
        return mLogUtil;
    }

    /**
     * 初始化，须在使用之前设置，最好在Application创建时调用
     *
     * @param context
     */
    public void init(Context context) {
        logPath = BaseApplication.baseLogsPath;//获得文件储存路径,在后面加"/Logs"建立子文件夹
        fileName = logPath + "/log_" + dateFormat.format(new Date()) + ".log";//log日志名，使用时间命名，保证不重复
        file = new File(logPath);
        LogUtil.writeToFile(TAG, "LogUtil init!");

        mContext = context;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 将log信息写入文件中
     *
     * @param tag
     * @param msg
     */
    public static void writeToFile(String tag, String msg) {
        if (null == logPath) {
            LogUtil.writeToFile(TAG, "logPath == null ，未初始化LogToFile");
            return;
        }
        String log = dateFormat.format(new Date()) + /*" " + type + */" " + tag + " " + msg + "\n";
        //如果父路径不存在
        if (!file.exists()) {
            //创建父路径
            file.mkdirs();
        }
        //FileOutputStream会自动调用底层的close()方法，不用关闭
        FileOutputStream fos = null;
        BufferedWriter bw = null;
        try {
            //这里的第二个参数代表追加还是覆盖，true为追加，flase为覆盖
            fos = new FileOutputStream(fileName, true);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(log);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    //关闭缓冲流
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (!handleException(throwable) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, throwable);
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
        collectDeviceInfo(mContext);
        //保存日志文件
        saveCrashInfoToFile(throwable);
        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param context
     */
    public void collectDeviceInfo(Context context) {
        infos.put("versionName", AppUtils.getAppVersionName(context, context.getPackageName()));
        infos.put("versionCode", String.valueOf(AppUtils.getAppVersionCode(context, context.getPackageName())));
        infos.put("time", dateFormat.format(System.currentTimeMillis()));

        Field[] fields = Build.class.getDeclaredFields();//获得build构造函数的参数
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
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
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        Throwable cause = throwable.getCause();//异常转换
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);

        try {
            String crash_path = BaseApplication.baseLogsPath + "/crash";
            File crash_dir = new File(crash_path);
            if (!crash_dir.exists()) {
                crash_dir.mkdir();
            }

            String path = BaseApplication.baseLogsPath + "/crash/crash" + fileDateFormat.format(System.currentTimeMillis()) + ".log";
            File file = new File(path);
            if (!file.exists()) {
                Log.w(TAG, "file is not exists!");
                file.createNewFile();
            }
            RandomAccessFile raf = null;
            raf = new RandomAccessFile(file, "rw");
            raf.seek(file.length());
            raf.write(sb.toString().getBytes());
            raf.close();
            return path;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }
        return null;
    }

    /**
     * 获取异常的详细信息
     *
     * @param e
     * @return
     */
    public static String getExceptionInfo(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }
}
