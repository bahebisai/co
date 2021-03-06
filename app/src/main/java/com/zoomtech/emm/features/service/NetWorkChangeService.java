package com.zoomtech.emm.features.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.zoomtech.emm.R;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.download.DownLoadManager;
import com.zoomtech.emm.features.impl.SendMessageManager;
import com.zoomtech.emm.features.policy.device.ConfigurationPolicy;
import com.zoomtech.emm.features.resend.MessageResendManager;
import com.zoomtech.emm.model.MessageSendData;
import com.zoomtech.emm.utils.AppUtils;
import com.zoomtech.emm.utils.JsonGenerateUtil;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.utils.PhoneUtils;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.utils.WifiHelper;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by Administrator on 2017/8/28.
 */

public class NetWorkChangeService extends IntentService {

    final static String TAG = "NetWorkChangeService";

    public static final int NETWORK_WIFI = 1;

    public static final int NETWORK_MOBILE = 0;

    public static final int NETWORK_NONE = -1;

    public NetWorkChangeService() {
        super( "NetWorkChangeService" );
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TheTang.getSingleInstance().startForeground(this, getResources().getString(R.string.net_change), "EMM", 12);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtil.writeToFile( TAG, "onHandleIntent!" );

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

        /**
         * 防止空指针
         */
        String baseUrl = preferencesManager.getData( "baseUrl" );
        if (baseUrl == null) {
            return;
        }

        /*try {
            Thread.sleep( 1000 );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        //enableConnectivityData();
        sendAppVersion();
        inquireFileDownLoad();
        sendCrashLog();
        sendSwitchLog( getApplicationContext() );
        sendComingNumberLog();
        sendFeedBackFalie();
        // totalMoible();

        /**
         * 在wifi情况下上传，如果在非wifi情况下获得上传命令
         */
        //if (preferencesManager.getLogData( "isWifiUpload" ) != null) {
        //    MDM.uploadLog( preferencesManager.getLogData( "logId" ), preferencesManager.getLogData( "date" ) );
        //}
    }


    //用于保持4G连接
    private void enableConnectivityData() {
        if (!MDM.getSingleInstance().isDataConnectivityOpen()) {
            MDM.getSingleInstance().openDataConnectivity(true);
        }
    }

    /**
     * 查询是否有版本更新，有即上传
     */
    private void sendAppVersion() {
        String oldVersion = PreferencesManager.getSingleInstance().getData( Common.appVersion );
        String newVersion = AppUtils.getAppVersionName(this, getPackageName());
        if (TextUtils.isEmpty( oldVersion ) || !oldVersion.equals( newVersion )) {
  /*          UpdateAPPVersionImpl mUpdateAPPVersionImpl = new UpdateAPPVersionImpl( TheTang.getSingleInstance().getContext() );
            mUpdateAPPVersionImpl.sendUpdateAppVersion();*/
            //todo baii impl ccccccccccccccccc
            sendUpdateVersion();
        }
    }

    private void sendUpdateVersion() {
        String alias = PreferencesManager.getSingleInstance().getData(Common.alias);
        String newVersion = AppUtils.getAppVersionName(this, Common.packageName);
        Map<String, String> map = new ArrayMap<>();
        map.put("alias", alias);
        map.put("appVersion", newVersion);
        JSONObject jsonObject = new JSONObject(map);
        MessageSendData data = new MessageSendData(Common.APP_VERSION_UPDATE, jsonObject.toString(), false);
        SendMessageManager manager = new SendMessageManager();
        manager.sendMessage(data);
    }

    /**
     * 上传失败返回的结果
     */
    public static void sendFeedBackFalie() {
        TheTang.getSingleInstance().getThreadPoolObject().submit( new MessageResendManager() );
    }

    private void totalMoible() {
       /* long mobileTxBytes = TrafficStats.getMobileTxBytes();//获取手机3g/2g网络上传的总流量
        long mobileRxBytes = TrafficStats.getMobileRxBytes();//手机2g/3g下载的总流量

        long l = mobileTxBytes + mobileRxBytes;
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();



        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String last = format.format(ca.getTime());
       // l=l+preferencesManager.getTraffictotal("mobileTotal");
        preferencesManager.setTraffictotal("mobileTotals",l);
        Log.w(TAG,"保存移动流量=="+l);
        getMobileDataState(TheTang.getSingleInstance().getContext(),null);
         EventBus.getDefault().post(new NotifyEvent());
         */

    }

    public static boolean getMobileDataState(Context pContext, Object[] arg) {

        try {

            ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext.getSystemService( Context.CONNECTIVITY_SERVICE );

            Class ownerClass = mConnectivityManager.getClass();

            Class[] argsClass = null;
            if (arg != null) {
                argsClass = new Class[1];
                argsClass[0] = arg.getClass();
            }

            Method method = ownerClass.getMethod( "getMobileDataEnabled", argsClass );

            Boolean isOpen = (Boolean) method.invoke( mConnectivityManager, arg );
            Log.w( TAG, "得到移动数据状态==" + isOpen );
            return isOpen;

        } catch (Exception e) {
            // TODO: handle exception

            Log.w( TAG, "得到移动数据状态出错" + e.toString());
            return false;
        }

    }

    /**
     * 保证crashlog能够上传成功
     */
    private void sendCrashLog() {
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        String logId = preferencesManager.getLogData( "logId" );

        if (TextUtils.isEmpty( logId ))
            return;

        if ("1".equals( preferencesManager.getLogData( "isWifiUpload" ) )) {

            int state = PhoneUtils.getNetWorkState(this);
            if (state != 1) {
                return;
            }
        }

        MDM.getSingleInstance().uploadLog( logId, preferencesManager.getLogData( "date" ) );
    }

    private void sendSwitchLog(Context context) {
        String switchLog = PreferencesManager.getSingleInstance().getLogData( "switchLog" );
        if (switchLog != null) {
           /* SwitchLogImpl mSwitchLogImpl = new SwitchLogImpl( context );
            mSwitchLogImpl.sendSwitchLog( switchLog );*/
//todo baii impl aaaaaaaaaaaaaaaaaaaaaaa
            String logJsonString =  JsonGenerateUtil.jsonSwitchLog(switchLog);
            MessageSendData data = new MessageSendData(Common.switch_log_impl, logJsonString, false);
            SendMessageManager manager = new SendMessageManager(context);
            manager.setSendListener(new SendMessageManager.SendListener() {
                @Override
                public void onSuccess() {
                    PreferencesManager.getSingleInstance().removeLogData("switchLog");//清除数据
                }

                @Override
                public void onFailure() {

                }

                @Override
                public void onError() {

                }
            });
            manager.sendMessage(data);
        }
    }

    /**
     * 发送ComingNumber log并清除log
     */
    private void sendComingNumberLog() {

        String comingNumberLog = PreferencesManager.getSingleInstance().getComingNumberLog( Common.ComingNumberLog );

        if (!TextUtils.isEmpty( comingNumberLog )) {
            Log.w( TAG, "存入sp有网就自动发给服务器=====" + comingNumberLog );
/*            ComingNumberLogImpl comingNumberLogImpl = new ComingNumberLogImpl( this );
            comingNumberLogImpl.sendComingNumberLog( comingNumberLog );*/
            //todo impl bai 33333333
            MessageSendData data = new MessageSendData(Common.coming_number_impl, comingNumberLog, false);
            SendMessageManager manager = new SendMessageManager(this);
            manager.setSendListener(new SendMessageManager.SendListener() {
                @Override
                public void onSuccess() {
                    PreferencesManager.getSingleInstance().clearComingNumberLog();//清除数据
                }

                @Override
                public void onFailure() {

                }

                @Override
                public void onError() {

                }
            });
            manager.sendMessage(data);
        }
    }

    private void inquireFileDownLoad() {

        /*TheTang.getSingleInstance().getThreadPoolObject().submit( new Runnable() {
            @Override
            public void run() {
                //DownLoadRequest.getSingleInstance().setWhetherAppDownloading( false );
                try {
                    //等待2分钟
                    Thread.sleep( 120000 );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                DownLoadManager.getInstance().download( null );
           /* }
        } );*/
    }

    /**
     * 初始化JPUSH
     *
     * @param context
     */
    public void initJPUSH(Context context) {
        JPushInterface.init( context );
    }


    /**
     * 时间围栏内如果有wifi则打开wifi的时候就连接
     *
     * @param state
     */
    private void connectWifi_insideFence(int state) {
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        switch (state) {
            case WifiManager.WIFI_STATE_DISABLED:

                break;
            case WifiManager.WIFI_STATE_DISABLING:

                break;
            case WifiManager.WIFI_STATE_ENABLED:

                Log.w( TAG, "WLANBroadcastReceiver --> onReceive--> WIFI_STATE_ENABLED WLAN已经打开" );

                //有围栏内有wifi设置的情况下配置
                if (WifiHelper.isWifiEnabled()) {

                    Log.w( TAG, "有围栏内有wifi设置的情况下配置" + WifiHelper.getSSID() );
                    if (!TextUtils.isEmpty( preferencesManager.getFenceData( Common.insideAndOutside ) ) &&
                            "true".equals( preferencesManager.getFenceData( Common.insideAndOutside ) )) {

                        if (!TextUtils.isEmpty( preferencesManager.getFenceData( Common.allowConfigureWifi ) ) &&
                                "1".equals( preferencesManager.getFenceData( Common.allowConfigureWifi ) ) &&
                                !TextUtils.isEmpty( preferencesManager.getFenceData( Common.configureWifi ) ) &&
                                preferencesManager.getFenceData( Common.wifi_password ) != null) {

                            if (!TextUtils.isEmpty( preferencesManager.getFenceData( "conect" ) )) {

                                Log.w( TAG, "有围栏内有wifi设置的配置----" + WifiHelper.isWifiEnabled() );
                                WifiConfiguration wifiConfiguration = WifiHelper.CreateWifiInfo( preferencesManager.getFenceData( Common.configureWifi ), preferencesManager.getFenceData( Common.wifi_password ), Integer.parseInt( preferencesManager.getFenceData( Common.safeType ) ) );

                                if (!TextUtils.isEmpty( WifiHelper.getSSID() ) && wifiConfiguration != null && !WifiHelper.getSSID().equals( wifiConfiguration.SSID )) {

                                    boolean network_connect = WifiHelper.addNetwork_Connect( wifiConfiguration );


                                    Log.w( TAG, wifiConfiguration.SSID + "network_connect===" + network_connect + "netid=" + preferencesManager.getFenceData( Common.wifi_password ) );
                                }
                                preferencesManager.removeFenceData( "conect" );
                            }


                        }

                    }

                    if (!TextUtils.isEmpty( preferencesManager.getConfiguration( "conect" ) ) && "true".equals( preferencesManager.getConfiguration( "conect" ) )) {
                        try {
                            Log.w( TAG, "下发配置策略时，wifi没打开，现在wifi打开无执行wifi策略" );
                            ConfigurationPolicy.excuteWifiConfiguration( preferencesManager );
                            preferencesManager.removeConfiguration( "conect" );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                break;
            case WifiManager.WIFI_STATE_ENABLING:

                Log.w( TAG, "WLANBroadcastReceiver --> onReceive--> WIFI_STATE_ENABLING WLAN正在打开" );

                break;
            case WifiManager.WIFI_STATE_UNKNOWN:

                Log.w( TAG, "WLANBroadcastReceiver --> onReceive--> WIFI_STATE_UNKNOWN  未知" );

                break;
        }
    }


}
