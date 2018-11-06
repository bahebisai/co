package com.xiaomi.emm.features.policy.appFence;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.xiaomi.emm.R;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.presenter.MDM;
import com.xiaomi.emm.features.manager.PreferencesManager;
import com.xiaomi.emm.features.presenter.TheTang;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Administrator on 2018/1/11.
 */

public class WatchingAppStartService extends Service {
    public final static String TAG = "WatchingAppStartService";

    private List<String> mPackageNames = new ArrayList<>();
    ActivityManager mActivityManager = null;
    AppTaskReceiver mAppTaskReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        TheTang.getSingleInstance().startForeground(this,getResources().getString(R.string.app_fence_service),"EMM",3);

        mActivityManager = (ActivityManager) TheTang.getSingleInstance()
                .getContext().getSystemService( ACTIVITY_SERVICE );

        mAppTaskReceiver = new AppTaskReceiver();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("app_task");
        registerReceiver(mAppTaskReceiver,mIntentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //TheTang.getSingleInstance().startForeground(this, intent,"应用围栏正在运行!","EMM",3);

        String appPackages = PreferencesManager.getSingleInstance().getAppFenceData( Common.appFenceAppPageName );

        if (appPackages != null) {

            String[] packages = appPackages.split( "," );

            if (packages != null) {

                for (int i = 0; i < packages.length; i++) {
                    mPackageNames.add( packages[i] );
                }
            }

            sendTimerBroadcast();

            //设置为前台Service
            /*Notification notification = null;
            try {
                PendingIntent pendingIntent = PendingIntent.getService( this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT );
                notification = new Notification.Builder( this )
                        .setContentTitle( "EMM" )
                        .setContentText( "应用启动监控正在运行!" )
                        .setSmallIcon( R.mipmap.logo )
                        .setContentIntent( pendingIntent )
                        .build();
            } catch (Exception e) {
                Log.w( TAG, e.toString() );
            }
            notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            //让该service前台运行，避免手机休眠时系统自动杀掉该服务
            //如果 id 为 0 ，那么状态栏的 notification 将不会显示。
            startForeground( , notification );*/
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver( mAppTaskReceiver );
        TheTang.getSingleInstance().cancelNotification(3);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //发送定时广播
    private void sendTimerBroadcast() {
        AlarmManager alarmManager = (AlarmManager) getSystemService( Context.ALARM_SERVICE );
        Intent intent1 = new Intent( );
        intent1.setAction( "app_task" );
        //第二个参数用于识别AlarmManager
        PendingIntent pendingIntent = PendingIntent.getBroadcast( this, 5, intent1, PendingIntent.FLAG_UPDATE_CURRENT );
        alarmManager.setWindow( AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 500, pendingIntent );
    }

    /**
     * 查询当前应用是否为管控应用
     */
    public void excuteAppCurrent() {

            String currentApp = null;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

                UsageStatsManager usm = (UsageStatsManager) TheTang.getSingleInstance()
                        .getContext().getSystemService( Context.USAGE_STATS_SERVICE );

                long time = System.currentTimeMillis();
                List<UsageStats> appList = usm.queryUsageStats( UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time );
                if (appList != null && appList.size() > 0) {
                    SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                    for (UsageStats usageStats : appList) {
                        mySortedMap.put( usageStats.getLastTimeUsed(), usageStats );
                    }
                    if (mySortedMap != null && !mySortedMap.isEmpty()) {
                        currentApp = mySortedMap.get( mySortedMap.lastKey() ).getPackageName();
                    }
                }
            } else {
                List<ActivityManager.RunningAppProcessInfo> tasks = mActivityManager.getRunningAppProcesses();
                currentApp = tasks.get( 0 ).processName;
            }

            if (currentApp != null) {
                if (mPackageNames != null) {
                    for (String packageName : mPackageNames) {
                        if (currentApp.equals( packageName )) {
                            MDM.killProcess( currentApp );
                        }
                    }
                }
            }
    }

    private class AppTaskReceiver extends BroadcastReceiver {

        public AppTaskReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            TheTang.getSingleInstance().getThreadPoolObject().submit( new Runnable() {
                @Override
                public void run() {
                    Log.w( TAG, "AppTaskReceiver!" );
                    excuteAppCurrent();
                    sendTimerBroadcast();
                }
            } );
        }
    }
}
