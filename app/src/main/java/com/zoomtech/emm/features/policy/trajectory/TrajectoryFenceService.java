package com.zoomtech.emm.features.policy.trajectory;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.CoordinateConverter;
import com.amap.api.location.DPoint;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.impl.SendMessageManager;
import com.zoomtech.emm.model.MessageSendData;
import com.zoomtech.emm.utils.CoordinateUtils;
import com.zoomtech.emm.utils.DataParseUtil;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TrajectoryFenceService extends Service {

    public static final String TAG = "TrajectoryFenceService";

    //定义接收广播的action字符串
    public static final String GEOFENCE_BROADCAST_ACTION = "com.location.apis.geofencedemo.broadcast";

    int repeart;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    //public AMapLocationListener mLocationListener = new AMapLocationListener();
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    public boolean inside = false;

    CoordinateConverter converter = new CoordinateConverter(this);

    double lng = 0;
    double lat = 0;
    double rad = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG,"onStartCommand");
        super.onStartCommand(intent, flags, startId);
        TheTang.getSingleInstance().startForeground(this, "轨迹服务正在运行!", "EMM", 20);

        repeart = 0;
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mAMapLocationListener);

        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();

        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。

        //生活域支持网络定位，安全域支持gps定位
        /*if ("true".equals(intent.getStringExtra("Hight_Accuracy"))) {
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        } else {
            if (MDM.mMDMController.isInFgContainer()) {
                mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
            } else {
                mLocationOption.setLocationMode( AMapLocationClientOption.AMapLocationMode.Hight_Accuracy );
            }
        }*/

        //生活域与安全域都支持网络定位
      //  mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        String frequency = PreferencesManager.getSingleInstance().getTrajectoryData(Common.frequency);
        //安全域支持GPS定位
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        if (TextUtils.isEmpty( frequency )){
            LogUtil.writeToFile(TAG,"下发下来的间隔频率为空");
            Log.w(TAG,"下发下来的间隔频率为空");
            mLocationOption.setInterval(1000);
        }else {
            int i = Integer.parseInt(frequency);
            if (i!= 0){

                mLocationOption.setInterval(i*60*1000);
            }else {
                mLocationOption.setInterval(1000);
            }
        }

        //mLocationOption.setOnceLocation( false );

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
       // mLocationOption.setOnceLocationLatest( true );

        mLocationOption.setLocationCacheEnable(false);//不保存缓存数据

        if (TextUtils.isEmpty( frequency )) {
            //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
            mLocationOption.setHttpTimeOut(20000);
        }else {
            int i = Integer.parseInt(frequency);
            if (i!= 0){
                mLocationOption.setHttpTimeOut(i*60*1000*2);
            }else {

                mLocationOption.setHttpTimeOut(20000);
            }
        }

        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TheTang.getSingleInstance().startForeground(this, "轨迹服务正在运行!", "EMM", 20);


    }

    //可以通过类implement方式实现AMapLocationListener接口，也可以通过创造接口类对象的方法实现
    AMapLocationListener mAMapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {

            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    feedBackLocation( amapLocation );
                } else {

                   // feedBackLocation( null );
                }
            } else {

               // feedBackLocation( null );
            }
        }

    };

    /**
     * 距离计算
     *
     * @param currentLng
     * @param currentLat
     * @return
     */
    private float excuteDistance(double currentLng, double currentLat) {

        double[] gps = CoordinateUtils.bd09_To_Gcj02(lat, lng);
        DPoint centerPoint = new DPoint();
        centerPoint.setLatitude(gps[0]);
        centerPoint.setLongitude(gps[1]);

        DPoint currentPoint = new DPoint();
        currentPoint.setLatitude(currentLat);
        currentPoint.setLongitude(currentLng);

        return converter.calculateLineDistance(centerPoint, currentPoint);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private synchronized void feedBackLocation(AMapLocation location) {


        String locationData = null;
        Map<String, String> map = new HashMap<>();
        if (location == null) {
            locationData = DataParseUtil.jsonLocation(null + "," + null);

        } else {
            LogUtil.writeToFile(TAG, location.getLongitude() + "," + location.getLatitude());

            //坐标转换
            Log.w(TAG, location.getLatitude() + "," + location.getLongitude());
            double[] gps = CoordinateUtils.gcj02_To_Bd09(location.getLatitude(), location.getLongitude());
            locationData = DataParseUtil.jsonLocation(gps[1] + "," + gps[0]);

             map.put("longitude",gps[1]+"");
             map.put("latitude",gps[0]+"");

        }

        map.put("strategyId",PreferencesManager.getSingleInstance().getTrajectoryData( Common.trajectoryID));
      //  map.put("feedback_code",String.valueOf(OrderConfig.GetLocationData));
      //  map.put( "alias", PreferencesManager.getSingleInstance().getData( Common.alias ) );
        //定位返回
/*        TrajectoryImpl trajectoryImpl = new TrajectoryImpl(this);
        trajectoryImpl.sendTrajectoryData( map);*/

        //todo baii impl bbbbbbbbbbbbbbbbb
        JSONObject json = new JSONObject(map);
        MessageSendData data = new MessageSendData(Common.USER_TRACK, json.toString(), false);
        SendMessageManager manager = new SendMessageManager(this);
        manager.sendMessage(data);
    }
}
