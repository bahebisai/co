package com.xiaomi.emm.features.policy.appFence;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.DPoint;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaomi.emm.R;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.model.TimeFenceData;
import com.xiaomi.emm.utils.CoordinateUtils;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.features.presenter.MDM;
import com.xiaomi.emm.features.manager.PreferencesManager;
import com.xiaomi.emm.features.presenter.TheTang;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.amap.api.fence.GeoFenceClient.GEOFENCE_IN;
import static com.amap.api.fence.GeoFenceClient.GEOFENCE_OUT;
import static com.amap.api.fence.GeoFenceClient.GEOFENCE_STAYED;

/**
 * Created by Administrator on 2018/1/16.
 */

public class AppFenceService extends Service {

    public static final String TAG = "AppFenceService";

    //定义接收广播的action字符串
    public static final String GEOFENCE_BROADCAST_ACTION = "com.location.apis.geofencedemo.broadcast_1";
    GeoFenceClient mGeoFenceClient = null;

    //获取当前时间
    private static SimpleDateFormat sDateFormat = new SimpleDateFormat( "HH:mm" );
    private static SimpleDateFormat mm = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
    static AlarmManager am = (AlarmManager) TheTang.getSingleInstance().getContext().getSystemService( ALARM_SERVICE );

    static PreferencesManager mPreferencesManager = PreferencesManager.getSingleInstance();

    static String tpye = null;
    static String fenceType = null;

    public static boolean insideGeo = false;
    public static boolean insideTime = false;

    //表示启动service
    public static int first = 0;

    GeoFenceReceiver mGeoFenceReceiver;
    AppFenceReceiver mAppFenceReceiver;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TheTang.getSingleInstance().startForeground(this,getResources().getString(R.string.app_fence_is_running),"EMM",2);

        mAppFenceReceiver = new AppFenceReceiver();
        IntentFilter intentFilter = new IntentFilter( );
        intentFilter.addAction( "app_startTimeRage" );
        intentFilter.addAction( "app_endTimeRage" );
        intentFilter.addAction( "app_alarm_start" );
        intentFilter.addAction( "app_alarm_end" );
        registerReceiver( mAppFenceReceiver, intentFilter );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //TheTang.getSingleInstance().startForeground(this,getResources().getString(R.string.app_fence_is_running),"EMM",2);

        insideGeo = false;
        insideTime = false;
        first = 0;

        fenceType = intent.getStringExtra( "fenceType" );

        switch (Integer.valueOf( fenceType )) {
            case 0:

                if (first == 0) {
                    first = 1;
                }

                geoFence();

                try {
                    Thread.sleep( 2000 );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                doSendBroadcast();

                break;
            case 1:
                geoFence();
                break;
            case 2:
                doSendBroadcast();
                break;
        }

        /*PendingIntent pendingIntent = PendingIntent.getService( this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        Notification notification = new Notification.Builder( this )
                .setContentTitle( "EMM" )
                .setContentText( "应用围栏正在运行!" )
                .setSmallIcon( R.mipmap.logo )
                .setContentIntent( pendingIntent )
                .build();

        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        //让该service前台运行，避免手机休眠时系统自动杀掉该服务
        //如果 id 为 0 ，那么状态栏的 notification 将不会显示。
        startForeground( 112, notification );*/

        return START_REDELIVER_INTENT;
    }

    public void geoFence() {

        MDM.forceLocationService();

        mGeoFenceClient = new GeoFenceClient( getApplicationContext() );

        //设置希望侦测的围栏触发行为，默认只侦测用户进入围栏的行为
        //public static final int GEOFENCE_IN 进入地理围栏
        //public static final int GEOFENCE_OUT 退出地理围栏
        //public static final int GEOFENCE_STAYED 停留在地理围栏内10分钟

        mGeoFenceClient.setActivateAction( GEOFENCE_IN | GEOFENCE_OUT | GEOFENCE_STAYED );

        DPoint centerPoint = new DPoint();

        String[] coordinate = mPreferencesManager.getAppFenceData( Common.appFenceCoordinate ).split( "," );

        double lng = Double.valueOf( coordinate[0] );
        double lat = Double.valueOf( coordinate[1] );
        float radius = Float.valueOf( mPreferencesManager.getAppFenceData( Common.appFenceRadius ) );
        double[] gps = CoordinateUtils.bd09_To_Gcj02( lat, lng );
        //设置中心点纬度
        centerPoint.setLatitude( gps[0] );
        //设置中心点经度
        centerPoint.setLongitude( gps[1] );
        mGeoFenceClient.addGeoFence( centerPoint, radius, "EMM_app" );
        //创建围栏回调
        mGeoFenceClient.setGeoFenceListener( fenceListenter );
        //创建并设置PendingIntent
        mGeoFenceClient.createPendingIntent( GEOFENCE_BROADCAST_ACTION );

        mGeoFenceReceiver = new GeoFenceReceiver();

        IntentFilter filter = new IntentFilter( ConnectivityManager.CONNECTIVITY_ACTION );
        filter.addAction( GEOFENCE_BROADCAST_ACTION );

        registerReceiver( mGeoFenceReceiver, filter );
    }

    public void doSendBroadcast() {

        /**r如果之前有过时间围栏的闹钟应该先取消闹钟---防止之前下发过时间围栏策略又重新发过一次*/
        AppFenceExcute.cancelBrodcast();
        /*********************/

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        //获取时间日期范围
        String startTimeRage = preferencesManager.getAppFenceData( Common.appFenceStartDateRange );
        String endTimeRage = preferencesManager.getAppFenceData( Common.appFenceEndDateRange );


        if (startTimeRage != null && endTimeRage != null) {
            /*************************/
            SimpleDateFormat formats = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
            // String    date    =    sDateFormat.format(new    java.util.Date());
            try {
                if (startTimeRage.contains( "T" )) {
                    startTimeRage = startTimeRage.split( "T" )[0].trim();
                }
                if (endTimeRage.contains( "T" )) {
                    endTimeRage = endTimeRage.split( "T" )[0].trim();
                }

                Date parse = formats.parse( startTimeRage + " 00:00" );
                Date parse1 = formats.parse( endTimeRage + " 23:59" );

                AlarmManager am = (AlarmManager) TheTang.getSingleInstance().getContext().getSystemService( ALARM_SERVICE );
                /**如果当前时间已经超过时间范围：执行围栏外*/
                if (System.currentTimeMillis() > parse1.getTime()) {
                    Log.w( TAG, "如果当前时间已经超过时间围栏设置的结束时间范围，不知执行闹钟，同时取消策略(可以把本地的时间围栏数据删除掉)" );
                    LogUtil.writeToFile( TAG, "当前时间已经超过时间围栏设置的结束时间范围，不知执行闹钟--结束所有的广播，同时取消策略(可以把本地的时间围栏数据删除掉)" );
                    /**结束所有的广播，同时取消策略(可以把本地的时间围栏数据删除掉)*/
                    Intent intent_startTimeRage = new Intent(  );
                    intent_startTimeRage.setAction( "app_endTimeRage" );
                    PendingIntent pi = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(), 1, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT );

                    am.setExact( AlarmManager.RTC_WAKEUP, parse1.getTime(), pi ); //执行一次

                    return;
                }


                if (System.currentTimeMillis() < parse.getTime()){
                    //当前的时间小于开始的时间日期：执行围栏外
                    outsideTimeFence();
                }



                Log.w( TAG, "发送广播" );
                Intent intent_startTimeRage = new Intent( );
                intent_startTimeRage.setAction( "app_startTimeRage" );

                //第二个参数用于识别AlarmManager
                PendingIntent pi = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(), 1, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT );

                //如果设定的时间比当前时间还小则立即执行---设置开始时间的闹钟
                am.setExact( AlarmManager.RTC_WAKEUP, parse.getTime(), pi ); //执行一次

                /******************************************/
                Intent intent_endTimeRage = new Intent( );
                intent_endTimeRage.setAction( "app_endTimeRage" );
                PendingIntent pi2 = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(), 1, intent_endTimeRage, PendingIntent.FLAG_UPDATE_CURRENT );

                //如果设定的时间比当前时间还小则立即执行---设置结束时间的闹钟
                Log.w( TAG, startTimeRage + " 00:00==" + parse.getTime() + "==size==" + endTimeRage + " 23:59===" + parse1.getTime() );
                am.setExact( AlarmManager.RTC_WAKEUP, parse1.getTime(), pi2 ); //执行一次

            } catch (ParseException e) {
                e.printStackTrace();

            }
        }
    }

    public static void timeFence() {

        if (tpye == null || TextUtils.isEmpty( tpye )) {
            Log.w( "testAppfront", "到这里闹钟TimeFenceService===tpye为空" );
            return;
        }
        //执行应用围栏
        doTimeFence( tpye );
    }

    private static void doTimeFence(String type) {

        Calendar c = Calendar.getInstance();
        String dates = c.get( Calendar.YEAR ) + "-" + (c.get( Calendar.MONTH ) + 1) + "-" + c.get( Calendar.DAY_OF_MONTH );

        String startTimeRage = mPreferencesManager.getAppFenceData( Common.appFenceStartDateRange );

        if ("app_startTimeRage".equals( type )) {
            /**日期范围的开始的第一天*/

            String timeUnit = mPreferencesManager.getAppFenceData( Common.timeUnit );
            Type listType = new TypeToken<ArrayList<TimeFenceData.PolicyBean.TimeUnitBean>>() {
            }.getType();
            ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> timeUnits = new Gson().fromJson( timeUnit, listType );
            if (timeUnits == null) {
                Log.w( "testAppfront", "timeUnits为空===" + type );
                return;
            }
          /*初始化选择执行时间**/
            selcetTimeExcute( timeUnits );
        } else if ("app_endTimeRage".equals( type )) {
            /**如果接收到时间范围的结束广播*/
            Log.w( "testAppfront", "结束的的时间闹钟===" + type );
            /**结束所有的广播，同时取消策略(可以把本地的时间围栏数据删除掉)*/
            Intent intent_startTimeRage = new Intent( );
            PendingIntent pi = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(), 1, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT );
            //   AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            am.cancel( pi );

            contrastExcuteAppFence();
            //closeLocation();

            //if (mAppFenceReceiver != null) {
                //unregisterReceiver( mAppFenceReceiver );
            //}
            /**清除本地的时间围栏临时数据*/
            //  PreferencesManager.getSingleInstance().clearTimefenceData();
            //清除时间围栏本地数据
            // FenceManager.deleteTimeFenceData();
            /**执行时间范围外的操作*/
            //ExcuteTimeFence.excute_TiemFence(false); 应该没有这个


        } else if ("app_alarm_start".equals( type )) {

            //  执行应用围栏内的策略
            insideTimeFence();

            String extr = mPreferencesManager.getAppFenceData( "key" );
            if (extr != null) {

                String i = extr.split( "_" )[0];
                String data = extr.split( "_" )[1];
                int parseInt = Integer.parseInt( i );
                //    Type type = new TypeToken<List<TimeUnitBean>>() {}.getType();
                Type listType = new TypeToken<List<TimeFenceData.PolicyBean.TimeUnitBean>>() {
                }.getType();
                //   Log.w("testAppfront", "timeUnits分段好的时间===" + data);
                //  Log.w("testAppfront", "timeUnits分段好的时间=extr==" + extr);
                List<TimeFenceData.PolicyBean.TimeUnitBean> list = new Gson().fromJson( data, listType );

                if (list == null) {
                    Log.w( "testAppfront", "timeUnits解析错误===" );
                }
                Intent intents = new Intent( );
                intents.setAction( "app_alarm_end" );
                PendingIntent pi = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(), 1, intents, PendingIntent.FLAG_UPDATE_CURRENT );
                /*定结束闹钟*/
                try {
                    am.setExact( AlarmManager.RTC_WAKEUP, mm.parse( dates + " " + list.get( parseInt ).getEndTime() ).getTime(), pi );//定闹钟
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        } else if ("app_alarm_end".equals( type )) {

            /*执行围栏外的策略*/
            //  执行应用围栏外的策略
            outsideTimeFence();

            Log.w( TAG, "执行app_alarm_end" + type );

            String extr = mPreferencesManager.getAppFenceData( "key" );
            if (TextUtils.isEmpty( extr )) {
                return;
            }
            Log.w( TAG, extr );
            String i = extr.split( "_" )[0];
            String data = extr.split( "_" )[1];
            if (TextUtils.isEmpty( i ) || TextUtils.isEmpty( data )) {
                Log.w( TAG, "i 或者 data 为空" );
                return;
            }

            int parseInt = Integer.parseInt( i );
            //    Type type = new TypeToken<List<TimeUnitBean>>() {}.getType();
            Log.w( TAG, "timeUnits分PreferencesManager段好的时间=extr==" + extr );
            Type listType = new TypeToken<List<TimeFenceData.PolicyBean.TimeUnitBean>>() {
            }.getType();
            List<TimeFenceData.PolicyBean.TimeUnitBean> list = new Gson().fromJson( data, listType );
            if (parseInt < list.size() - 1) {

                Intent intents = new Intent( );
                intents.setAction( "app_alarm_start" );
                PendingIntent pi = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(), 1, intents, PendingIntent.FLAG_UPDATE_CURRENT );
                //定下个开始闹钟
                try {
                    am.setExact( AlarmManager.RTC_WAKEUP, mm.parse( dates + " " + list.get( parseInt + 1 ).getStartTime() )
                            .getTime(), pi );//定闹钟
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                mPreferencesManager.setAppFenceData( "key", (parseInt + 1) + "_" + data );
            } else {
                //1.最后一个结束闹钟，则定明天的闹钟
                //2.添加符合第二天的时间，排序，然后存储起来发明天开始的广播
                String date = c.get( Calendar.YEAR ) + "-" + (c.get( Calendar.MONTH ) + 1) + "-" + c.get( Calendar.DAY_OF_MONTH );


                String endTimeRage = mPreferencesManager.getAppFenceData( Common.appFenceEndDateRange );
                if (!date.equals( endTimeRage )) {

                    nextSelectExcuteTime( list );
                    Log.w( TAG, "最后一个结束闹钟，则定明天的闹钟" + type );
                } else {
                    Log.w( TAG, "最后一个结束闹钟，今天时最后一天的时间范围不定明天的闹钟了...." + type );
                }
            }

        }

    }


    /**
     * 初始化选择执行时间
     *
     * @param list
     */
    private static void selcetTimeExcute(List<TimeFenceData.PolicyBean.TimeUnitBean> list) {

        /**选择今天符合的条件时间段出来*/
        list = selectTodayTime( list );
        if (list == null || list.size() <= 0) {
            Log.w( TAG, "选择今天符合的条件时间段出来为null或size为0" );

            outsideTimeFence();

            Intent intent_startTimeRage = new Intent( );
            intent_startTimeRage.setAction( "app_startTimeRage" );

            //第二个参数用于识别AlarmManager
            PendingIntent pi = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(), 1, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT );
            //指定某个日期,执行一次
            Calendar c = Calendar.getInstance();
            c.add( Calendar.DAY_OF_MONTH, 1 );
            String date = c.get( Calendar.YEAR ) + "-" + (c.get( Calendar.MONTH ) + 1) + "-" + c.get( Calendar.DAY_OF_MONTH );

            SimpleDateFormat formats = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
            try {
                Date parse = formats.parse( date + " 00:00" );
                //如果设定的时间比当前时间还小则立即执行---设置开始时间的闹钟
                am.setExact( AlarmManager.RTC_WAKEUP, parse.getTime(), pi ); //执行一次
            } catch (ParseException e) {
                e.printStackTrace();
            }


            return;
        }

        //   SimpleDateFormat format =   new SimpleDateFormat("HH:mm");
        //  SimpleDateFormat formats = new SimpleDateFormat("yyyy-MM-dd");
        String json = new Gson().toJson( list );
        try {
            //  Date date = format.parse("01:40");
            // long   millis = date .getCallTime();
            Calendar c = Calendar.getInstance();
            String dates = c.get( Calendar.YEAR ) + "-" + (c.get( Calendar.MONTH ) + 1) + "-" + c.get( Calendar.DAY_OF_MONTH );
            //   long     times_Date = format.parse(dates).getCallTime();//今天 00:00的毫秒时间

            long millis = System.currentTimeMillis();
            Date date = new Date( millis );
            String v = list.get( list.size() - 1 ).getStartTime();
            String s = list.get( list.size() - 1 ).getEndTime();
            long x = mm.parse( dates + " " + s ).getTime();
            long y = mm.parse( dates + " " + v ).getTime();
            Log.w( TAG, dates + "现在此刻的时间--" + date.toString() );
            if (millis >= x) {
                //1.执行围栏外的策略

                outsideTimeFence();

                //2.添加符合第二天的时间，排序，然后存储起来发明天开始的广播
                Log.w( TAG, "--" + list.size() + "--" + "执行围栏外的策略后,添加符合第二天的时间，排序，然后存储起来发明天开始的广播" );
                Log.w( TAG, " FenceExcute.excuteGeographicalFence(false)" );
                nextSelectExcuteTime( list );

                return;

            } else if (list.size() == 1) {
                Intent intent = new Intent( );
                if (millis < y) {
                    //在第一个时间围栏之前
                    Log.w( TAG, "--" + 0 + "--" + millis+"在第一个时间围栏之前=" + v + "--发送" + v + "-de闹钟" +y);
                    Log.w( TAG, "-2-" + date.toString() );
                    //1.执行围栏外的策略
                    outsideTimeFence();
                    intent.setAction( "app_alarm_start" );
                    PendingIntent pi = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT );

                    am.setExact( AlarmManager.RTC_WAKEUP, y, pi );//定闹钟
                    /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                    mPreferencesManager.setAppFenceData( "key", 0 + "_" + json );
                    Log.w( TAG, "--" + 0 + "--" + "在第一个时间围栏之前闹钟===" + mPreferencesManager.getAppFenceData( "key" ) );
                } else if (y <= millis && millis < x) {
                    //在某个时间围栏内
                    Log.w( TAG, "--" + 0 + "--" + "在某个时间围栏内" + v + "==" + s );
                    Log.w( TAG, "-2-在某个时间围栏内命令-ExcuteTimeFence.excute_TiemFence(true)" );
                    //1.执行时间围栏内的策略
                    insideTimeFence();

                    intent.setAction( "app_alarm_end" );
                    PendingIntent pi = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT );

                    //2定结束闹钟
                    am.setExact( AlarmManager.RTC_WAKEUP, x, pi );//定闹钟
                    /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                    mPreferencesManager.setAppFenceData( "key", 0 + "_" + json );

                }


            } else {

                for (int i = 0; i < list.size(); i++) {

                    try {
                        Date parse = mm.parse( dates + " " + list.get( i ).getStartTime() );
                        long start = mm.parse( dates + " " + list.get( i ).getStartTime() ).getTime();
                        long end = mm.parse( dates + " " + list.get( i ).getEndTime() ).getTime();
                        Intent intent = new Intent( );
                        Log.w( TAG, "--" + i + "--" + "时间围--parse--" + parse + "==" + parse.getTime() );
                        // Log.w(TAG, "--"+i+"--"+"时间围--00:00--"+dates+"=="+times_Date);
                        Log.w( TAG, "--" + i + "--" + sDateFormat.parse( list.get( i ).getStartTime() ).getTime() + "时间围=" + list.get( i ).getStartTime() + "===" + start );
                        Log.w( TAG, "--" + i + "--" + sDateFormat.parse( list.get( i ).getEndTime() ).getTime() + "时间围=" + list.get( i ).getEndTime() + "==" + end );
                        Log.w( TAG, "--" + i + "--" + "时间围此刻时间=" + millis + "===" + date.toString() );
                        //   String result = formatData("yyyy-MM-dd HH:mm", millis);

                        if (millis < start) {
                            //在第一个时间围栏之前
                            Log.w( TAG, "--" + i + "--" + "在第一个时间围栏之前=" + list.get( i ).getStartTime() );
                            Log.w( TAG, "-2-" + date.toString() );
                            //1.执行围栏外的策略
                            outsideTimeFence();

                            intent.setAction( "app_alarm_start" );
                            PendingIntent pi = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT );

                            am.setExact( AlarmManager.RTC_WAKEUP, start, pi );//定闹钟
                            /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                            mPreferencesManager.setAppFenceData( "key", i + "_" + json );
                            break;
                        } else if (start <= millis && millis < end) {
                            //在某个时间围栏内
                            Log.w( TAG, "--" + i + "--" + "在某个时间围栏内" + list.get( i ).getStartTime() + "==" + list.get( i ).getEndTime() );
                            Log.w( TAG, "-2-在某个时间围栏内命令-ExcuteTimeFence.excute_TiemFence(true)" );
                            //1.执行时间围栏内的策略

                            insideTimeFence();

                            intent.setAction( "app_alarm_end" );
                            PendingIntent pi = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT );

                            //2定结束闹钟
                            am.setExact( AlarmManager.RTC_WAKEUP, end, pi );//定闹钟
                            /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                            mPreferencesManager.setAppFenceData( "key", i + "_" + json );
                            break;
                        } else if (millis >= end) {
                            if ((i + 1) >= (list.size())) {
                                continue;
                            } else if (millis < mm.parse( dates + " " + list.get( i + 1 ).getStartTime() ).getTime()) {

                                //在某个时间围栏外，下一个时间围栏之前
                                Log.w( TAG, "--" + i + "--" + "在某个时间围栏外=" + list.get( i ).getEndTime() + "，下一个时间围栏之前=" + list.get( i + 1 ).getStartTime() );
                                Log.w( TAG, "-2-在某个时间围栏内命令-ExcuteTimeFence.excute_TiemFence(false)" );
                                //1.执行围栏外的策略

                                outsideTimeFence();

                                intent.setAction( "app_alarm_start" );
                                PendingIntent pi = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT );

                                //2定下个开始闹钟
                                am.setExact( AlarmManager.RTC_WAKEUP, mm.parse( dates + " " + list.get( i + 1 ).getStartTime() )
                                        .getTime(), pi );//定闹钟

                                /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                                mPreferencesManager.setAppFenceData( "key", (i + 1) + "_" + json );
                                break;
                            }

                        } else {
                            Log.w( TAG, i + "--到这里时" + list.get( i ).getStartTime() + ",=" + list.get( i ).getEndTime() );
                        }


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> selectTodayTime(List<TimeFenceData.PolicyBean.TimeUnitBean> timeUnits) {
        if (timeUnits == null || timeUnits.size() <= 0) {

            return null;
        }

        ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> lists = new ArrayList<>();
        for (TimeFenceData.PolicyBean.TimeUnitBean bean : timeUnits) {
            if ("1".equals( bean.getUnitType() )) {
                lists.add( bean );
            } else if ("2".equals( bean.getUnitType() )) {
                int week = getDay_Week();
                //今天的周几跟设置是否一致
                if ((week + "").equals( bean.getTypeDate() )) {
                    lists.add( bean );
                }

            } else if ("3".equals( bean.getUnitType() )) {
                //工作日
                int week = getDay_Week();

                if (week < 6) {
                    lists.add( bean );
                }
            } else if ("4".equals( bean.getUnitType() )) {
                //指定某个日期,执行一次
                Calendar c = Calendar.getInstance();
                String date = c.get( Calendar.YEAR ) + "-" + (c.get( Calendar.MONTH ) + 1) + "-" + c.get( Calendar.DAY_OF_MONTH );
                String typeDate = bean.getTypeDate();
                if (typeDate.contains( "T" )) {
                    typeDate = typeDate.split( "T" )[0].trim();
                }
                if (date.equals( typeDate )) {
                    lists.add( bean );
                }
            }

        }
        Log.w( TAG, "lists=" + lists.toString() );
        /*排出各个时间段出来*/
        ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> arrayList = sortTime( lists );

        return arrayList;

    }

    /**
     * 下一天的执行时间
     *
     * @param timeUnits
     */
    private static void nextSelectExcuteTime(List<TimeFenceData.PolicyBean.TimeUnitBean> timeUnits) {
        ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> lists = new ArrayList<>();
        for (TimeFenceData.PolicyBean.TimeUnitBean bean : timeUnits) {
            if ("1".equals( bean.getUnitType() )) {
                lists.add( bean );
            } else if ("2".equals( bean.getUnitType() )) {

                int week = getDay_Week();
                if (week == 7) {
                    week = 1;
                } else {
                    week = week + 1;
                }
                //下一天的周几跟设置是否一致
                if ((week + "").equals( bean.getTypeDate() )) {
                    lists.add( bean );
                }

            } else if ("3".equals( bean.getUnitType() )) {
                //工作日
                int week = getDay_Week();
                if (week == 7) {
                    week = 1;
                } else {
                    week = week + 1;
                }
                if (week < 6) {
                    lists.add( bean );
                }
            } else if ("4".equals( bean.getUnitType() )) {
                //指定某个日期,执行一次
                Calendar c = Calendar.getInstance();
                c.add( Calendar.DAY_OF_MONTH, 1 );
                String date = c.get( Calendar.YEAR ) + "-" + (c.get( Calendar.MONTH ) + 1) + "-" + c.get( Calendar.DAY_OF_MONTH );
                String typeDate = bean.getTypeDate();
                if (typeDate.contains( "T" )) {
                    typeDate = typeDate.split( "T" )[0].trim();
                }
                if (date.equals( typeDate )) {
                    lists.add( bean );
                }
            }

        }
        /*排出各个时间段出来*/
        ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> arrayList = sortTime( lists );

        if (arrayList == null) {
            return;
        }

        /*把下一天分好的时间段转换成时间格式*/
        String json = new Gson().toJson( arrayList );
        /**选择第一个开始时间闹钟*/
        Intent intent = new Intent( );

        PendingIntent pi = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        intent.setAction( "app_alarm_start" );
        if (arrayList.size() > 0) {
            TimeFenceData.PolicyBean.TimeUnitBean unitBean = arrayList.get( 0 );
            try {
                //long time = sDateFormat.parse(unitBean.getStartTime()) .getCallTime();
                Calendar c = Calendar.getInstance();
                c.add( Calendar.DAY_OF_MONTH, 1 );
                String date = c.get( Calendar.YEAR ) + "-" + (c.get( Calendar.MONTH ) + 1) + "-" + c.get( Calendar.DAY_OF_MONTH );
                long time = mm.parse( date + " " + unitBean.getStartTime() ).getTime();//第一个开始时间第二天的时间
                // long     times_Date = format.parse(date).getCallTime();//第二天 00:00的毫秒时间
                am.setExact( AlarmManager.RTC_WAKEUP, time, pi );//执行
                /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                mPreferencesManager.setAppFenceData( "key", 0 + "_" + json );

            } catch (ParseException e) {
                e.printStackTrace();
            }


        }

    }

    private static int getDay_Week() {
        Calendar c = Calendar.getInstance();
        int week;
        switch (c.get( Calendar.DAY_OF_WEEK )) {
            case Calendar.SUNDAY:
                week = 7;
                //   Log.i("MainActivityFilter", "今天是周日");
                break;
            case Calendar.MONDAY:
                week = 1;
                //   Log.i("MainActivityFilter", "今天是周一");
                break;
            case Calendar.TUESDAY:
                week = 2;
                //   Log.i("MainActivityFilter", "今天是周二");
                break;
            case Calendar.WEDNESDAY:
                week = 3;
                //  Log.i("MainActivityFilter", "今天是周三");
                break;
            case Calendar.THURSDAY:
                week = 4;
                //   Log.i("MainActivityFilter", "今天是周四");
                break;
            case Calendar.FRIDAY:
                week = 5;
                //   Log.i("MainActivityFilter", "今天是周五");
                break;
            case Calendar.SATURDAY:
                week = 6;
                //  Log.i("MainActivityFilter", "今天是周六");
                break;
            default:
                week = 0;
                break;

        }
        return week;
    }


    /**
     * 分段时间出来
     *
     * @param list
     * @return
     */
    private static ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> sortTime(ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> list) {

        /*TimeUnitBean bean1 = new TimeUnitBean();
        bean1.setStartTime("02:30");
        bean1.setEndTime("03:58");
        list.add(bean1);
        TimeUnitBean bean2 = new TimeUnitBean();
        bean2.setStartTime("03:30");
        bean2.setEndTime("05:58");
        list.add(bean2);
        TimeUnitBean bean3 = new TimeUnitBean();
        bean3.setStartTime("18:30");
        bean3.setEndTime("20:58");
        list.add(bean3);
        TimeUnitBean bean4 = new TimeUnitBean();
        bean4.setStartTime("14:50");
        bean4.setEndTime("16:58");
        list.add(bean4);
        TimeUnitBean bean5 = new TimeUnitBean();
        bean5.setStartTime("06:50");
        bean5.setEndTime("9:08");
        list.add(bean5);
        TimeUnitBean bean6 = new TimeUnitBean();
        bean6.setStartTime("9:00");
        bean6.setEndTime("12:28");
        list.add(bean6);
        TimeUnitBean bean7 = new TimeUnitBean();
        bean7.setStartTime("13:00");
        bean7.setEndTime("14:28");
        list.add(bean7);
        TimeUnitBean bean8 = new TimeUnitBean();
        bean8.setStartTime("14:00");
        bean8.setEndTime("17:28");
        list.add(bean8);*/
        if (list == null) {
            return null;
        } else if (list.size() == 1) {
            return list;
        }


        Collections.sort( list, new Comparator<TimeFenceData.PolicyBean.TimeUnitBean>() {
            @Override
            public int compare(TimeFenceData.PolicyBean.TimeUnitBean o1, TimeFenceData.PolicyBean.TimeUnitBean o2) {

                int i = 0;
                try {
                    i = sDateFormat.parse( o1.getStartTime() ).compareTo( sDateFormat.parse( o2.getStartTime() ) );
                    //  Log.w("ss",i+"---");

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return i;
            }
        } );


        //  Log.w("ss", "--1--"+list.toString());
        for (int i = 0; i < list.size(); i++) {

            Log.w( TAG, "--" + i + "-按时间排序好-" + list.get( i ).toString() );

        }

        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get( i ) != null) {

                    try {
                        TimeFenceData.PolicyBean.TimeUnitBean unitBean = list.get( i );
                        Date date_End = sDateFormat.parse( unitBean.getEndTime() );
                        Date nextDate_End = sDateFormat.parse( list.get( j ).getEndTime() );
                        Date nextDate_Start = sDateFormat.parse( list.get( j ).getStartTime() );

                        if (nextDate_Start.before( date_End ) && nextDate_End.before( date_End )) {
                            //     Log.w(TAG,nextDate_Start+"nextDate_Start.before(date_End)&&nextDate_End.before(date_End)");
                            list.set( j, null );

                        } else if (nextDate_Start.before( date_End ) && nextDate_End.after( date_End )) {
                            unitBean.setEndTime( list.get( j ).getEndTime() );
                            list.set( i, unitBean );

                            list.set( j, null );

                        } else {
                            break;
                        }


                    } catch (ParseException e) {


                    }

                } else {
                    break;
                }
            }

        }

        ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> unitBeen = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {


            if (list.get( i ) != null) {
                unitBeen.add( list.get( i ) );
                // Log.w(TAG, "--"+i+"ee--"+list.get(i).toString());
            }
        }
        for (int i = 0; i < unitBeen.size(); i++) {
            Log.w( TAG, "--" + i + "-分好的时间段-" + unitBeen.get( i ).toString() );
        }

        return unitBeen;

    }

    public String formatData(String dataFormat, long timeStamp) {
        if (timeStamp == 0) {
            Log.d( TAG, "-----------result为空" );
            return "";
        }
        timeStamp = timeStamp * 1000;
        String result = "";
        SimpleDateFormat format = new SimpleDateFormat( dataFormat );
        result = format.format( new Date( timeStamp ) );
        Log.d( TAG, "-----------" + result );
        return result;
    }

    @Override
    public void onDestroy() {

        if (mAppFenceReceiver != null) {
            unregisterReceiver( mAppFenceReceiver );
        }

        if (mGeoFenceReceiver != null) {
            unregisterReceiver( mGeoFenceReceiver );
        }

        closeLocation();
        //TheTang.getSingleInstance().cancelNotification(2);
        super.onDestroy();
    }

    private void closeLocation() {

        if (mGeoFenceClient != null) {
            mGeoFenceClient.removeGeoFence(); //清除围栏
        }

        //如果没有地理围栏，则关闭强制定位
        if (TextUtils.isEmpty( PreferencesManager.getSingleInstance().getFenceData( Common.geographical_fence ) )) {
            MDM.closeForceLocation();

            if (PreferencesManager.getSingleInstance().getPolicyData( Common.middle_policy ) != null) {
                if ("0".equals( PreferencesManager.getSingleInstance().getPolicyData( Common.middle_allowLocation ) )) {
                    MDM.enableLocationService( false );
                } else {
                    MDM.enableLocationService( true );
                }
            } else {
                if ("0".equals( PreferencesManager.getSingleInstance().getPolicyData( Common.default_allowLocation ) )) {
                    MDM.enableLocationService( false );
                } else {
                    MDM.enableLocationService( true );
                }
            }
        }
    }


    //创建回调监听
    GeoFenceListener fenceListenter = new GeoFenceListener() {

        @Override
        public void onGeoFenceCreateFinished(List<GeoFence> list, int errorCode, String s) {
            if (errorCode == GeoFence.ADDGEOFENCE_SUCCESS) {//判断围栏是否创建成功
                Log.w( TAG, "添加应用地理围栏成功!!" );
                //geoFenceList就是已经添加的围栏列表，可据此查看创建的围栏
            } else {
                //geoFenceList就是已经添加的围栏列表
                Log.w( TAG, "添加应用地理围栏失败!!" );
            }
        }
    };


    class GeoFenceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals( GEOFENCE_BROADCAST_ACTION )) {
                //解析广播内容
                Bundle bundle = intent.getExtras();

                //获取围栏行为：
                int status = bundle.getInt( GeoFence.BUNDLE_KEY_FENCESTATUS );

                switch (status) {
                    case GEOFENCE_IN:
                        //监控的设备在围栏内
                        Log.w( TAG, "excuteAppFence" );
                        LogUtil.writeToFile( TAG, "excuteAppFence" );

                        switch (Integer.valueOf( fenceType )) {
                            case 0:
                                if (insideTime) {
                                    excuteAppFence();
                                } else {
                                    contrastExcuteAppFence();
                                }
                                break;
                            case 1:
                                excuteAppFence();
                                break;
                        }

                        insideGeo = true;
                        break;
                    case GEOFENCE_OUT:

                        Log.w( TAG, "contrastExcuteAppFence" );
                        LogUtil.writeToFile( TAG, "contrastExcuteAppFence" );

                        contrastExcuteAppFence();

                        insideGeo = false;

                        break;
                    default:
                        //监控的设备状态未知
                        break;
                }

            }
        }
    };

    /**
     * 执行围栏策略
     */
    private static void excuteAppFence() {
        if (!TextUtils.isEmpty( mPreferencesManager.getAppFenceData( Common.appFenceCoordinate ) )
                && !TextUtils.isEmpty( mPreferencesManager.getAppFenceData( Common.appFenceStartDateRange )) ) {
            if (first == 1) {
                first = 2;
                return;
            }
        }

        AppFenceExcute.whetherAppInSide = false;
        Log.w( TAG, "excuteAppFence" );
        TheTang.getSingleInstance().getContext().stopService( new Intent( TheTang.getSingleInstance().getContext(), WatchingAppStartService.class ) );
        voiceAndMessage( false);
    }

    /**
     * 反向执行围栏策略
     */
    private static void contrastExcuteAppFence() {
        //在同时存在地理与时间围栏时，第一次不执行，以解决语音播报2次的问题
        if (!TextUtils.isEmpty( mPreferencesManager.getAppFenceData( Common.appFenceCoordinate ) )
                && !TextUtils.isEmpty( mPreferencesManager.getAppFenceData( Common.appFenceStartDateRange )) ) {
            if (first == 1) {
                first = 2;
                return;
            }
        }
        AppFenceExcute.whetherAppInSide = true;
        TheTang.getSingleInstance().startService( new Intent( TheTang.getSingleInstance().getContext(), WatchingAppStartService.class ) );
        voiceAndMessage(true);
    }

    static MediaPlayer mMediaPlayer = null;

    private static void voiceAndMessage(boolean enter) {

        //MediaPlayer mMediaPlayer = null;

        if ("1".equals( mPreferencesManager.getAppFenceData( Common.appFenceNoticeBell ) )) {
            int voiceAddress = 0;
            if (enter) {

                voiceAddress = R.raw.left;

            } else {
                switch (Integer.valueOf( fenceType )) {
                    case 0:
                        voiceAddress = R.raw.time_geo_enter;
                        break;
                    case 1:
                        voiceAddress = R.raw.geo_enter;
                        break;
                    case 2:
                        voiceAddress = R.raw.time_enter;
                        break;
                }
            }

            mMediaPlayer = MediaPlayer.create( TheTang.getSingleInstance().getContext(), voiceAddress );
            mMediaPlayer.start();

            //final MediaPlayer finalMMediaPlayer = mMediaPlayer.;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(50000);
                        MDM.releasePlayer(mMediaPlayer);
                        mMediaPlayer = null;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        MDM.releasePlayer(mMediaPlayer);
                        mMediaPlayer = null;
                    }
                }
            });
        }

        /*if ("1".equals( mPreferencesManager.getAppFenceData( Common.appFenceNoticeMessage ) )) {

            String content = null;

            if (enter) {

                switch (Integer.valueOf( fenceType )) {
                    case 0:
                        content = "您已进入火箭发射作业范围，请安全作业";
                        break;
                    case 1:
                        content = "您已进入火箭发射作业区域，请安全作业";;
                        break;
                    case 2:
                        content = "您已进入火箭发射作业时间，请安全作业";;
                        break;
                }

            } else {

                content = "当前不可进行火箭发射作业";
            }

            TheTang.getSingleInstance().showNotification(content,
                    TheTang.getSingleInstance().getContext().getResources().getString(R.string.message1), 1001);
        }*/
    }

    /**
     * 时间围栏内
     */
    private static void insideTimeFence() {
        switch (Integer.valueOf( fenceType )) {
            case 0:
                if (insideGeo) {
                    excuteAppFence();
                } else {
                    contrastExcuteAppFence();
                }
                break;
            case 2:
                excuteAppFence();
                break;
        }

        insideTime = true;
    }

    /**
     * 时间围栏外
     */
    private static void outsideTimeFence() {
        contrastExcuteAppFence();
        insideTime = false;
    }

    public static class  AppFenceReceiver extends BroadcastReceiver {

        private static final String TAG = "AppFenceReceiver";

        public AppFenceReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w( TAG, "到这里闹钟AppFenceReceiver===intent.getAction()" );

            if (intent.getAction() == null) {
                Log.w( TAG, "到这里闹钟AlarmReceiver1===intent.getAction()为空" );
                return;
            }

            String action = intent.getAction().toString();

            Log.w( TAG, "时间围栏接收过来的广播" + action );
            /*if ("app_startTimeRage".equals( action )) {

                tpye = action;

            } else if ("app_endTimeRage".equals( action )) {

                tpye = action;

            } else if ("app_alarm_start".equals( action )) {

                tpye = action;

            } else if ("app_alarm_end".equals( action )) {

                tpye = action;
            }*/
            tpye = action;
            timeFence();
        }
    }

}