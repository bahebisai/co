package com.zoomtech.emm.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.event.NotifyEvent;
import com.zoomtech.emm.features.policy.device.ConfigurationPolicy;
import com.zoomtech.emm.features.service.NetWorkChangeService;
import com.zoomtech.emm.model.ConfigureStrategyData;
import com.zoomtech.emm.socket.bean.UserMgr;
import com.zoomtech.emm.socket.service.ConnTask;
import com.zoomtech.emm.socket.service.TVBoxService;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.utils.PhoneUtils;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.utils.WifiHelper;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/7/26.
 */

public class NetWorkReceiver extends BroadcastReceiver {

    public static final String TAG = "NetWorkReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.w( TAG, intent.getAction() );
        LogUtil.writeToFile( TAG, "onReceive, action " + intent.getAction());

        if (PhoneUtils.isNetworkAvailable(context)) {
            // LogUtil.writeToFile( TAG, "android.net.conn.CONNECTIVITY_CHANGE" );
            int networkState = PhoneUtils.getNetWorkState(context);
            if (networkState == NetWorkChangeService.NETWORK_WIFI) {
                //连接长连接
                connectTcp(context);
                PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
                //如果是wifi的打开动作则是执行围栏内的wifi 连接
                connectWifi_insideFence(intent.getIntExtra("wifi_state",1));
                Log.w( TAG, intent.getAction()+"---------"+intent.getIntExtra("wifi_state",1) );
                //  connectWifi_insideFence();
                limitNetwork_WIFI( networkState, preferencesManager );

            } else if (networkState == NetWorkChangeService.NETWORK_MOBILE) {

                //刷新流量弹出框
                EventBus.getDefault().post(new NotifyEvent("flow_flag"));
                //连接长连接
                connectTcp(context);
            }

            //如果收到在这个"android.net.wifi.WIFI_STATE_CHANGED" 动作 说明 是wifi  的  动作  打开wifi
            int state = intent.getIntExtra("wifi_state", 1);
            Intent intentService = new Intent( context, NetWorkChangeService.class );
            intentService.putExtra("wifi_state",state);
            context.startService( intentService );

        } else {
            //连接失败，
            TheTang.getSingleInstance().isLostCompliance(false);
        }
    }




    private void limitNetwork_WIFI(int networkState, PreferencesManager preferencesManager) {
        // Log.w(TAG,TextUtils.isEmpty(preferencesManager.getConfiguration("isWifi") )+"如果不在此策略的wifi就断开"+preferencesManager.getFenceData(Common.allowConfigureWifi));
            /*if (! TextUtils.isEmpty(preferencesManager.getFenceData(Common.allowConfigureWifi))  &&
                    !"1".equals(preferencesManager.getFenceData(Common.allowConfigureWifi))) { */
        //判断是否允许配置

        //如果有配置策略没有没有围栏则以位置策略的weifi为主，如果是在围栏内，则以围栏wifi为主

        //wifi
        if (!TextUtils.isEmpty( preferencesManager.getConfiguration( "isWifi" ) ) && "1".equals( preferencesManager.getConfiguration( "isWifi" ) )) {
            if (!TextUtils.isEmpty( preferencesManager.getConfiguration( "IsAllowWifiConfig" ) ) && "1".equals( preferencesManager.getConfiguration( "IsAllowWifiConfig" ) )) {
                String extra = preferencesManager.getConfiguration( "WifiConfig" );
                if (!TextUtils.isEmpty( extra )) {
                    boolean flag = false;
                    boolean isExit = false;
                    Type listType = new TypeToken<ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WifiListBean>>() {
                    }.getType();
                    ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WifiListBean> wifiConfigureData = new Gson().fromJson( extra, listType );

                    if (wifiConfigureData != null) {
                        for (ConfigureStrategyData.ConfigureStrategyBean.WifiListBean bean : wifiConfigureData) {

                            WifiConfiguration configuration = WifiHelper.IsExsits( bean.getSsid(), bean.getMacAddress() );
                            if (configuration != null) {
                                isExit = true;
                                Log.w( TAG, "WifiHelper.getSSID()=" + WifiHelper.getSSID() + "configuration.SSID)" + configuration.SSID );
                                //  Log.w(TAG," WifiHelper.getBSSID()="+WifiHelper.getBSSID()+"configuration.SSID)"+configuration.BSSID);
                                //&& WifiHelper.getBSSID().equals(configuration.BSSID)
                                if (WifiHelper.getSSID().equals( configuration.SSID )) {
                                    flag = true;
                                    break;
                                } else {
                                    isExit = false;
                                }
                            }
                        }

                        if (!TextUtils.isEmpty( preferencesManager.getFenceData( Common.configureWifi ) )) {

                            WifiConfiguration configuration = WifiHelper.IsExsits( preferencesManager.getFenceData( Common.configureWifi ) );
                            if (configuration != null) {
                                isExit = true;
                                Log.w( TAG, "getFenceData--WifiHelper.getSSID()=" + WifiHelper.getSSID() + "configuration.SSID)" + configuration.SSID );
                                //&& WifiHelper.getBSSID().equals(configuration.BSSID)
                                if (WifiHelper.getSSID().equals( configuration.SSID )) {
                                    flag = true;
                                } else {
                                    isExit = false;
                                }
                            }
                        }

                        //如果不在此策略的wifi就断开  || 还有一种可能就是没有配置成功(所以wifi配置列表没有该wifi存在)
                        if (!flag /*&& isExit*/) {
                            Log.w( TAG, "如果不在此策略的wifi就断开====" + WifiHelper.getSSID() );
                            WifiHelper.disconnectWifi( WifiHelper.getNetworkId() );
                            flag = false;
                        } else {
                            Log.w( TAG, flag + "如果在此策略的wifi就不断开====" + WifiHelper.getSSID() + " ,isExit=" + isExit );
                            downloadPic();
                        }
                    }
                }
            }
        }
    }

    private void downloadPic() {

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        if (!TextUtils.isEmpty( preferencesManager.getConfiguration( "downloadPic" ) ) &&
                "true".equals( preferencesManager.getConfiguration( "downloadPic" ) )) {
            try {
                ConfigurationPolicy.createDeskShortCut( preferencesManager );
                preferencesManager.removeConfiguration( "downloadPic" );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    /**
     * 时间围栏内如果有wifi则打开wifi的时候就连接
     * @param state
     */
    private void connectWifi_insideFence(int state) {
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

        switch (state) {
            case WifiManager.WIFI_STATE_DISABLED:

                // Log.w(TAG, "WLANBroadcastReceiver --> onReceive--> WIFI_STATE_DISABLED--   WLAN已经关闭");
                //totalMoible();

                break;
            case WifiManager.WIFI_STATE_DISABLING:

                //    Log.w(TAG, "WLANBroadcastReceiver --> onReceive--> WIFI_STATE_DISABLING WLAN正在关闭");

                break;
            case WifiManager.WIFI_STATE_ENABLED:

                Log.w(TAG, "WLANBroadcastReceiver --> onReceive--> WIFI_STATE_ENABLED WLAN已经打开");
                //  totalMoible();



                //有围栏内有wifi设置的情况下配置
                if (WifiHelper.isWifiEnabled()){

                    Log.w(TAG,"有围栏内有wifi设置的情况下配置"+ WifiHelper.getSSID());
                    if (! TextUtils.isEmpty(preferencesManager.getFenceData( Common.insideAndOutside)) &&
                            "true".equals(preferencesManager.getFenceData( Common.insideAndOutside)) ){

                        if (! TextUtils .isEmpty(preferencesManager.getFenceData(Common.allowConfigureWifi))  &&
                                "1".equals( preferencesManager.getFenceData(Common.allowConfigureWifi))&&
                                ! TextUtils.isEmpty( preferencesManager.getFenceData(Common.configureWifi)) &&
                                preferencesManager.getFenceData(Common.wifi_password) !=null){

                            if ( !TextUtils.isEmpty(preferencesManager.getFenceData("conect")) ){
                                //!TextUtils.isEmpty(preferencesManager.getFenceData(Common.passWord)) &&
                                //    if(!TextUtils.isEmpty(preferencesManager.getFenceData(Common.safeType))){
                                Log.w(TAG,"有围栏内有wifi设置的配置----"+ WifiHelper.isWifiEnabled());
                                WifiConfiguration wifiConfiguration = WifiHelper.CreateWifiInfo(preferencesManager.getFenceData(Common.configureWifi), preferencesManager.getFenceData(Common.wifi_password), Integer.parseInt(preferencesManager.getFenceData(Common.safeType)));

                                if (!TextUtils.isEmpty(WifiHelper.getSSID()) && wifiConfiguration != null && !WifiHelper.getSSID().equals(wifiConfiguration.SSID)) {

                                    boolean network_connect = WifiHelper.addNetwork_Connect(wifiConfiguration);


                                    Log.w(TAG,wifiConfiguration.SSID+"network_connect==="+network_connect+"netid="+preferencesManager.getFenceData(Common.wifi_password));
                                }
                                preferencesManager.removeFenceData("conect");
                                //  }
                            }



                        }

                    }

                    if ( !TextUtils.isEmpty( preferencesManager.getConfiguration("conect")) && "true".equals( preferencesManager.getConfiguration("conect"))) {
                        try {
                            Log.w(TAG,"下发配置策略时，wifi没打开，现在wifi打开无执行wifi策略");
                            ConfigurationPolicy.excuteWifiConfiguration(preferencesManager);
                            preferencesManager.removeConfiguration("conect");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                }





                break;
            case WifiManager.WIFI_STATE_ENABLING:

                Log.w(TAG, "WLANBroadcastReceiver --> onReceive--> WIFI_STATE_ENABLING WLAN正在打开");

                break;
            case WifiManager.WIFI_STATE_UNKNOWN:

                Log.w(TAG, "WLANBroadcastReceiver --> onReceive--> WIFI_STATE_UNKNOWN  未知");


                break;
        }
    }

    private void connectTcp(Context context) {
        //有网络的情况下判断长连接有没有连接上，没有连接则去连接
        // Log.w( TAG, "网络波动获取当前的网络状态:0：没有网络 1：WIFI网络 2：WAP网络 3：NET网络==tcp=" + TheTang.getSingleInstance().getNetworkType() );

        boolean whetherLogin = false;
        whetherLogin = PreferencesManager.getSingleInstance().getData( Common.token ) != null && PreferencesManager.getSingleInstance().getData( Common.alias ) != null;

        if ( ! whetherLogin ){
            Log.w( TAG, "网络波动的情况下 ,用户还没登录 ");
            LogUtil.writeToFile( TAG, "网络波动的情况下 ,用户还没登录 ");
            return;
        }

        boolean isNetworkConnect = PhoneUtils.isNetworkAvailable(context);

        if (!isNetworkConnect){
            Log.w( TAG, "网络波动的情况下,网络不通 = ");
            LogUtil.writeToFile( TAG, "网络波动的情况下,网络不通 = ");
            return;
        }
        boolean networkAvaliable = PhoneUtils.isNetworkAvailable(context);
        boolean isNetworkConnected = PhoneUtils.isNetworkConnected(context);

        Log.w( TAG, "networkAvaliable = " + networkAvaliable+" ,isNetworkConnected= "+isNetworkConnected +" ,isNetworkConnect="+isNetworkConnect );
        LogUtil.writeToFile( TAG, "networkAvaliable = "+networkAvaliable+" ,isNetworkConnected= "+isNetworkConnected +" ,isNetworkConnect="+isNetworkConnect );

        ConnTask connTask = ConnTask.getInstance();
        if (connTask!=null  ){
            if (!UserMgr.isLogon() && !connTask.IsConnecting()){
                Log.w(TAG , "网络波动，如果长连接没有登陆UserMgr.isLogon()=而且没有正在通信连接"+UserMgr.isLogon());
                LogUtil.writeToFile(TAG , "网络波动，如果长连接没有登陆UserMgr.isLogon()="+UserMgr.isLogon());
                connTask.stop();
                connTask.start();

            } else if ( UserMgr.isLogon() ) {

                LogUtil.writeToFile( TAG,"TCP-----已经登录检测心跳---------");
                Log.w( TAG,"TCP-----已经登录检测心跳---------");
                connTask.startCheckHeartBeat( true );

            }

            LogUtil.writeToFile( TAG,"TCP-----11111---------");
                /*
                *
                * else {
                    connTask = new ConnTask(TVBoxService.getInstance());
                    if (!connTask.isRunning() && !connTask.IsConnecting()){
                        //如果长连接已经stop了就start
                        Log.w(TAG , "网络波动，如果长连接已经connTask"+connTask.isRunning());
                        LogUtil.writeToFile(TAG , "网络波动，如果长连接已经connTask"+connTask.isRunning());
                        connTask.stopReconnOpt();
                        connTask.start();
                    }
                    LogUtil.writeToFile(TAG,"TVBoxService--connTask==null"+(connTask==null)+"  ,UserMgr.isLogon()="+UserMgr.isLogon()+"  ,UserMgr.getUserMgr()="+(UserMgr.getUserMgr()==null));
                }
                * */

        }else {
            // LogUtil.writeToFile(TAG,"TVBoxService.getInstance()==null");
            //初始化本地长连接
            if (TVBoxService.getInstance() == null){
                LogUtil.writeToFile(TAG,"网络波动，"+TVBoxService.getInstance()+" 初始化TVBoxService");
                Log.w(TAG,"网络波动，"+TVBoxService.getInstance()+" 初始化TVBoxService");
                UserMgr.createInstance(TheTang.getSingleInstance().getContext());
                Intent integer = new Intent(TheTang.getSingleInstance().getContext(), TVBoxService.class);
                TheTang.getSingleInstance().startService(integer);
            }else {
                ConnTask connTask1 = TVBoxService.getInstance().getConnTask();
                if (connTask1 !=null){
                    LogUtil.writeToFile( TAG,"TCP-----2222---------");
                }
                LogUtil.writeToFile( TAG,"TCP-----333333---------");

            }
        }
        LogUtil.writeToFile( TAG,"TCP--------------");
    }
}
