package com.xiaomi.emm.features.policy.fence;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.xiaomi.emm.R;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;
import java.util.List;
import static com.amap.api.fence.GeoFenceClient.GEOFENCE_IN;
import static com.amap.api.fence.GeoFenceClient.GEOFENCE_OUT;
import static com.amap.api.fence.GeoFenceClient.GEOFENCE_STAYED;

/**
 * Created by Administrator on 2017/9/13.
 */

public class GaodeGeographicalFenceService extends Service {

    public static final String TAG = "GaodeFence";

    //定义接收广播的action字符串
    public static final String GEOFENCE_BROADCAST_ACTION = "com.location.apis.geofencedemo.broadcast";
    GeoFenceClient mGeoFenceClient = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TheTang.getSingleInstance().startForeground(this,getResources().getString(R.string.geo_fence_service),"EMM",7);

        mGeoFenceClient = new GeoFenceClient( getApplicationContext() );

        mGeoFenceClient.setActivateAction( GEOFENCE_IN | GEOFENCE_OUT | GEOFENCE_STAYED );

        DPoint centerPoint = new DPoint();

        String longitude = PreferencesManager.getSingleInstance().getFenceData( Common.longitude );
        String latitude = PreferencesManager.getSingleInstance().getFenceData( Common.latitude );
        String rad = PreferencesManager.getSingleInstance().getFenceData( Common.radius );

        if (TextUtils.isEmpty( longitude ) || TextUtils.isEmpty( latitude ) || TextUtils.isEmpty( rad )) {
            LogUtil.writeToFile( TAG, " longitude = " + longitude + "//" + " latitude = " + latitude + "//" + " rad = " + rad + "//" );
            stopSelf();
            return;
        }

        double lng = Double.valueOf( longitude);
        double lat = Double.valueOf( latitude );
        float radius = Float.valueOf( rad );
        double[] gps = TheTang.getSingleInstance().bd09_To_Gcj02( lat, lng );
        //设置中心点纬度
        centerPoint.setLatitude( gps[0] );
        //设置中心点经度
        centerPoint.setLongitude( gps[1] );
        mGeoFenceClient.addGeoFence( centerPoint, radius, "EMM_geo" );
        //创建围栏回调
        mGeoFenceClient.setGeoFenceListener( fenceListenter );
        //创建并设置PendingIntent
        mGeoFenceClient.createPendingIntent( GEOFENCE_BROADCAST_ACTION );

        IntentFilter filter = new IntentFilter( ConnectivityManager.CONNECTIVITY_ACTION );
        filter.addAction( GEOFENCE_BROADCAST_ACTION );
        registerReceiver( mGeoFenceReceiver, filter );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*PendingIntent pendingIntent = PendingIntent.getService( this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        Notification notification = new Notification.Builder( this )
                .setContentTitle( "EMM" )
                .setContentText( "地理围栏正在运行!" )
                .setSmallIcon( R.mipmap.emm )
                .setContentIntent( pendingIntent )
                .build();

        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        //让该service前台运行，避免手机休眠时系统自动杀掉该服务
        //如果 id 为 0 ，那么状态栏的 notification 将不会显示。
        startForeground( 2, notification );*/
        //TheTang.getSingleInstance().startForeground(this, intent,"地理围栏服务正在运行!","EMM",7);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGeoFenceClient.removeGeoFence(); //清除围栏
        unregisterReceiver( mGeoFenceReceiver );
        TheTang.getSingleInstance().cancelNotification(7);
    }

    //创建回调监听
    GeoFenceListener fenceListenter = new GeoFenceListener() {

        @Override
        public void onGeoFenceCreateFinished(List<GeoFence> list, int errorCode, String s) {
            if (errorCode == GeoFence.ADDGEOFENCE_SUCCESS) {//判断围栏是否创建成功
                Log.w( TAG, "添加围栏成功!!" );
                //geoFenceList就是已经添加的围栏列表，可据此查看创建的围栏
            } else {
                //geoFenceList就是已经添加的围栏列表
                Log.w( TAG, "添加围栏失败!!" );
            }
        }
    };


    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
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
                        Log.w( TAG, "excuteGeographicalFence" );
                        LogUtil.writeToFile( TAG, "excuteGeographicalFence" );
                        //用于判断是否在围栏内
                        PreferencesManager.getSingleInstance().setFenceData( "whether_in_goe_fence", "true" );
                        excuteGeographicalFence();

                        break;
                    case GEOFENCE_OUT:

                        Log.w( TAG, "contrastExcuteGeographicalFence" );
                        LogUtil.writeToFile( TAG, "contrastExcuteGeographicalFence" );
                        PreferencesManager.getSingleInstance().removeFenceData( "whether_in_goe_fence" );
                        contrastExcuteGeographicalFence();

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
    private void excuteGeographicalFence() {
        TheTang.getSingleInstance().getThreadPoolObject().submit( new Runnable() {
            @Override
            public void run() {
                FenceExcute.excuteGeographicalFence( true, false );
            }
        } );
    }

    /**
     * 反向执行围栏策略
     */
    private void contrastExcuteGeographicalFence() {
        TheTang.getSingleInstance().getThreadPoolObject().submit( new Runnable() {
            @Override
            public void run() {
                FenceExcute.excuteGeographicalFence( false, false );
            }
        } );
    }

}
