package com.xiaomi.emm.features.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.xiaomi.emm.R;
import com.xiaomi.emm.features.complete.CompleteMessageManager;
import com.xiaomi.emm.features.event.CompleteEvent;
import com.xiaomi.emm.utils.SystemUtils;
import com.xiaomi.emm.utils.TheTang;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Administrator on 2018/1/12.
 */

public class WatchingOrderService extends Service {
    public final static String TAG = "WatchingOrderService";
    private TimeTaskReceiver mTimeTaskReceiver;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TheTang.getSingleInstance().startForeground(this,getResources().getString(R.string.order_back) ,"EMM",5);

        if (!EventBus.getDefault().isRegistered( this )) {
            //EventBus注册
            EventBus.getDefault().register( this );
        }

        //sendTimerBroadcast( );

        mTimeTaskReceiver = new TimeTaskReceiver();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("timer_task");
        registerReceiver(mTimeTaskReceiver,mIntentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //设置为前台Service
        /*Notification notification = null;
        try {
            PendingIntent pendingIntent = PendingIntent.getService( this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT );
            notification = new Notification.Builder( this )
                    .setContentTitle( "EMM" )
                    .setContentText( "命令反馈服务正在运行!" )
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
        startForeground(105, notification);*/
       // TheTang.getSingleInstance().startForeground(this, intent,"命令反馈服务正在运行!" ,"EMM",5);
        return START_REDELIVER_INTENT;
    }

    //发送定时广播
    private void sendTimerBroadcast() {
        AlarmManager alarmManager = (AlarmManager) getSystemService( Context.ALARM_SERVICE );
        Intent intent1 = new Intent( );
        intent1.setAction( "timer_task" );
        //第二个参数用于识别AlarmManager
        PendingIntent pendingIntent = PendingIntent.getBroadcast( this, 4, intent1, PendingIntent.FLAG_UPDATE_CURRENT );
        //alarmManager.setWindow( AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1 * 60 * 1000, pendingIntent );
        alarmManager.setExact( AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2 * 60 * 1000, pendingIntent );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister( this );
        unregisterReceiver( mTimeTaskReceiver );
        TheTang.getSingleInstance().cancelNotification(5);
    }

    /**
     * EventBus回调
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void excuteComplete(CompleteEvent event) {
        sendCompleteMessage(event.getType(), event.getResult(), event.getId());
    }

    /**
     * 发送消息
     * @param type
     */
    private void sendCompleteMessage(String type, String result, String id) {
        CompleteMessageManager mCompleteMessageManager = new CompleteMessageManager();
        mCompleteMessageManager.sendCompleteMessage(type, result, id);
    }

    private class TimeTaskReceiver extends BroadcastReceiver {

        public TimeTaskReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            TheTang.getSingleInstance().getThreadPoolObject().submit( new Runnable() {
                @Override
                public void run() {
                    Log.w( TAG, "TimerTaskReceiver!" );
                    sendTimerBroadcast();
                    sendCompleteMessage(null, null, null);
                }
            } );
        }
    }
}