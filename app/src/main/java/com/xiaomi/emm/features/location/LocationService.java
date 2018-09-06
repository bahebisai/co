package com.xiaomi.emm.features.location;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.xiaomi.emm.R;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.OrderConfig;
import com.xiaomi.emm.features.impl.LocationImpl;
import com.xiaomi.emm.utils.DataParseUtil;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.MDM;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

/**
 * Created by Administrator on 2017/7/26.
 */

public class LocationService extends Service {

    public static final String TAG = "LocationService";
    int repeart;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    //public AMapLocationListener mLocationListener = new AMapLocationListener();
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);
        //TheTang.getSingleInstance().startForeground(this, intent,"定位服务正在运行！","EMM",9);

        repeart = 0;
        //初始化定位
        mLocationClient = new AMapLocationClient( getApplicationContext() );
        //设置定位回调监听
        mLocationClient.setLocationListener( mAMapLocationListener );

        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();

        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode( AMapLocationClientOption.AMapLocationMode.Hight_Accuracy );

        //设置定位模式为AMapLocationMode.Device_Sensors，仅设备模式。
        //mLocationOption.setLocationMode( AMapLocationClientOption.AMapLocationMode.Device_Sensors);

        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        //mLocationOption.setInterval(1000);

        mLocationOption.setOnceLocation( true );

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest( true );

        mLocationOption.setLocationCacheEnable( false );//不保存缓存数据

        //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut( 20000 );

        mLocationClient.setLocationOption( mLocationOption );
        //启动定位
        mLocationClient.startLocation();

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TheTang.getSingleInstance().cancelNotification(9);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TheTang.getSingleInstance().startForeground(this,getResources().getString(R.string.location_service),"EMM",9);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //可以通过类implement方式实现AMapLocationListener接口，也可以通过创造接口类对象的方法实现
    AMapLocationListener mAMapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {

            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    feedBackLocation( amapLocation );
                } else {
                    Log.e("baii","location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                    if (repeart > 10) {
                        feedBackLocation( null );
                    } else {
                        repeart++;
                    }
                }
            } else {
                if (repeart > 10) {
                    feedBackLocation( null );
                } else {
                    repeart++;
                }
            }
        }
    };

    private synchronized void feedBackLocation(AMapLocation location) {

        LogUtil.writeToFile( TAG, location.getLongitude() + "," + location.getLatitude() );

        //坐标转换
        Log.w( TAG, location.getLatitude() + "," + location.getLongitude() );
        double[] gps = TheTang.getSingleInstance().gcj02_To_Bd09( location.getLatitude(), location.getLongitude() );

        String locationData = null;

        if (location == null) {
            locationData = DataParseUtil.jsonLocation( null + "," + null );
        } else {
            locationData = DataParseUtil.jsonLocation( gps[1] + "," + gps[0] );
        }

        //定位返回
        LocationImpl locationImpl = new LocationImpl( this );
        locationImpl.sendLocation( String.valueOf( OrderConfig.GetLocationData ), locationData);

        Log.w( TAG, "PreferencesManager.getSingleInstance().getFenceData( Common.geographical_fence ) ==" + PreferencesManager.getSingleInstance().getFenceData( Common.geographical_fence ) );

        //停止定位后，本地定位服务并不会被销毁
        mLocationClient.stopLocation();
        //销毁定位客户端，同时销毁本地定位服务。
        mLocationClient.onDestroy();

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

        //如果不存在应用围栏的地理围栏
        if (preferencesManager.getAppFenceData( Common.appFenceRadius ) == null ||
                "0".equals( preferencesManager.getAppFenceData( Common.appFenceRadius ) )) {
            //如果有地理围栏，则不关闭定位服务
            if (preferencesManager.getFenceData( Common.geographical_fence ) == null) {

                MDM.closeForceLocation();

                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) {
                    if ("0".equals( preferencesManager.getPolicyData( Common.middle_allowLocation ) )) {
                        MDM.enableLocationService( false );
                    } else {
                        MDM.enableLocationService( true );
                    }
                } else {
                    if ("0".equals( preferencesManager.getPolicyData( Common.default_allowLocation ) )) {
                        MDM.enableLocationService( false );
                    } else {
                        MDM.enableLocationService( true );
                    }
                }
            }
        }
        stopSelf();
    }
}
