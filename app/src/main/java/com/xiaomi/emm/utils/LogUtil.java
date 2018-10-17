package com.xiaomi.emm.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.xiaomi.emm.base.BaseApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Duan on 17/7/18.
 */

public class LogUtil {
    private static String TAG = "LogUtil";
    //log日志存放路径
    private static String logPath = null;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);//日期格式;

    static String fileName;
    static File file;
    /**
     * 初始化，须在使用之前设置，最好在Application创建时调用
     *
     * @param context
     */
    public static void init(Context context) {
        logPath = BaseApplication.baseLogsPath;//获得文件储存路径,在后面加"/Logs"建立子文件夹
        fileName = logPath + "/log_" + dateFormat.format(new Date()) + ".log";//log日志名，使用时间命名，保证不重复
        file = new File(logPath);
        LogUtil.writeToFile(TAG, "LogUtil init!");
    }

    /**
     * 将log信息写入文件中
     *
     * @param tag
     * @param msg
     */
    public static void writeToFile(String tag, String msg) {//todo baii util fileio

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

}
