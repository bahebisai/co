package com.zoomtech.emm.base;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.zoomtech.emm.R;
import com.zoomtech.emm.features.db.DatabaseOperate;
import com.zoomtech.emm.features.excute.XiaomiMDMController;
import com.zoomtech.emm.features.lockscreen.NewsLifecycleHandler;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;

import java.io.File;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Administrator on 2017/5/31.
 */

public class BaseApplication extends Application {
    public static final String TAG = "BaseApplication";
    public static String baseLogsPath = null;
    public static String baseAppsPath = null;
    public static String baseFilesPath = null;
    public static String baseImagesPath = null;
    private static NewsLifecycleHandler callback;
    //public Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    //判断是不是主进程
    private boolean isMainProcess() {
        ActivityManager activityManager = ((ActivityManager) getSystemService( Context.ACTIVITY_SERVICE ));
        List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals( info.processName )) {
                return true;
            }
        }
        return false;
    }

    /**
     * 初始化
     */
    private void init() {
        //context = this;
        CalligraphyConfig.initDefault( "main/asserts/fonts/Roboto-Italic.ttf", R.attr.fontPath );
        callback = new NewsLifecycleHandler();
        registerActivityLifecycleCallbacks(callback);
        initPathAndCreate();

        LogUtil.getSingleInstance().init(this);
        //极光推送初始化
        //JPushInterface.setDebugMode( true );
        // JPushInterface.init( this );

        XiaomiMDMController.getSingleInstance().init(this);
        TheTang.getSingleInstance().init( this);
        MDM.getSingleInstance().init( this );
        DatabaseOperate.getSingleInstance().init( this );
        PreferencesManager.getSingleInstance().init( this );
        //全局uncheckedException捕获

    }

    /**
     * 创建文件
     *
     */
    private void initPathAndCreate() {
        String basePath = getFilePath( this );
        baseLogsPath = basePath + "/MDM/Logs";
        baseAppsPath = basePath + "/MDM/Apps";
        baseFilesPath = basePath + "/MDM/Files";
        baseImagesPath = basePath + "/MDM/Images";

        File logsDir = new File( baseLogsPath );
        File appsDir = new File( baseAppsPath );
        File filesDir = new File( baseFilesPath );
        File imagesDir = new File( baseImagesPath );

        if (!logsDir.exists()) {
            logsDir.mkdir();
        }

        if (!appsDir.exists()) {
            appsDir.mkdir();
        }

        if (!filesDir.exists()) {
            filesDir.mkdir();
        }

        if (!imagesDir.exists()) {
            imagesDir.mkdir();
        }

    }

    /**
     * 获得文件存储路径
     *
     * @return
     */
    private static String getFilePath(Context context) {
        //如果外部储存可用
        if (Environment.MEDIA_MOUNTED.equals( Environment.MEDIA_MOUNTED ) || !Environment.isExternalStorageRemovable()) {
            //获得外部存储路径,默认路径为 /storage/emulated/0/Android/data/com.zoomtech.emm/files//MDM/Logs/.log
            return context.getExternalFilesDir( null ).getPath();
        } else {
            //直接存在/data/data里，非root手机是看不到的
            return context.getFilesDir().getPath();
        }
    }

    public static NewsLifecycleHandler getNewsLifecycleHandler(){
        return callback;
    }
}