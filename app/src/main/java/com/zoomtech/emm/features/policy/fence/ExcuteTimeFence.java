package com.zoomtech.emm.features.policy.fence;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.google.gson.Gson;
import com.zoomtech.emm.definition.OrderConfig;
import com.zoomtech.emm.features.event.NotifyEvent;
import com.zoomtech.emm.model.TimeFenceData;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by lenovo on 2017/8/17.
 */

public class ExcuteTimeFence {
    private static final String TAG="ExcuteTimeFence";

    public synchronized static void excute_TiemFence( boolean insideAndOutside) {
        String tiemfence = PreferencesManager.getSingleInstance().getTimefenceData("tiemfence");
        if (tiemfence == null || tiemfence.isEmpty()){
            Log.w("testAppfront","获取本地时间围栏数据tiemfence为空");

            return;
        }
        Gson gson = new Gson();
        TimeFenceData timeFenceData = gson.fromJson(tiemfence, TimeFenceData.class);
        TimeFenceData.PolicyBean bean = timeFenceData.getPolicy() .get(0);
        if (bean == null) {
            Log.w("testAppfront","获取本地时间围栏数据PolicyBean为空");
            return;
        }

        Log.w("testAppfront","执行时间围栏命令");
        excuteDeviceConfiguration(bean,insideAndOutside);
        excuteSecurityChrome(bean,insideAndOutside);
        excuteCustomDesktop(bean,insideAndOutside);
        excuteDoubleDomain(bean,insideAndOutside);
    }

    /**
     * 设备配置执行
     * @param policyBean
     * @param insideAndOutside
     */
    private static void excuteDeviceConfiguration(TimeFenceData.PolicyBean policyBean, boolean insideAndOutside) {

        Log.w("testApp","设备配置执行");
        //强制锁屏
        if (policyBean.getLockScreen()!=null&& "1".equals(policyBean.getLockScreen())) {
            if (insideAndOutside) {
                //MDM.forceLockScreen(null);
            } else {
                MDM.getSingleInstance().releaseLockScreen();
            }
        }

        //不允许数据流量
        if (policyBean.getAllowMobileData()!=null&& "1".equals(policyBean.getAllowMobileData())) {
            if (insideAndOutside) {
                //禁止流量
                Log.w("testApp","设备配置执行---禁止流量");
                MDM.getSingleInstance().openDataConnectivity(false);
            } else {
                //允许流量
                Log.w("testApp","设备配置执行---允许流量");
                MDM.getSingleInstance().openDataConnectivity(true);
            }
        }

        //不允许wifi
        if (policyBean.getAllowCloseWifi()!=null&& "1".equals(policyBean.getAllowCloseWifi())) {

            if (insideAndOutside) {
                Log.w("testApp","设备配置执行---不允许wifi");
                MDM.getSingleInstance().enableWifi( false );  //false
            } else {
                MDM.getSingleInstance().enableWifi( true );  //true
            }
        }

        // 允许wifi
        if (policyBean.getAllowOpenWifi()!=null&& "1".equals(policyBean.getAllowOpenWifi())) {
            Log.w("testApp","设备配置执行---允许wifi");
            WifiConfiguration wifiConfiguration = new WifiConfiguration();
            WifiManager wifiManager = (WifiManager) TheTang.getSingleInstance().getContext().getApplicationContext().getSystemService( Context.WIFI_SERVICE );
            int wifiId = -1; //表示失败

            if (insideAndOutside) {

                MDM.getSingleInstance().enableWifi( true );
                MDM.getSingleInstance().openWifiOnBG(OrderConfig.OpenWifiOnBG + "", true );

                //wifi 配置
                if (policyBean.getConfigureWifi()!=null&& "1".equals(policyBean.getConfigureWifi())) {

                    if (policyBean.getSsid() != null) {
                        wifiConfiguration.SSID = "\"" + policyBean.getSsid() + "\"";
                        Log.w("testApp", " wifiConfiguration.SSID="+wifiConfiguration.SSID);
                    }

                    if ("1".equals(policyBean.getHiddenNetwork())) {
                        wifiConfiguration.hiddenSSID = true;
                    }

                    if (policyBean.getSafeType() != null) {
                        String wifiPassword;
                        int typeId = Integer.parseInt( policyBean.getSafeType() );
                        if (policyBean.getWifiPassword()!=null&&!policyBean.getWifiPassword().isEmpty()){
                            wifiPassword= policyBean.getWifiPassword();

                            switch(typeId) {

                                case 1: //WEP

                                    wifiConfiguration.wepKeys[0] = "\"" + wifiPassword + "\"";
                                    wifiConfiguration.allowedKeyManagement.set( WifiConfiguration.KeyMgmt.NONE );
                                    wifiConfiguration.allowedAuthAlgorithms.set( WifiConfiguration.AuthAlgorithm.OPEN);
                                    wifiConfiguration.allowedAuthAlgorithms.set( WifiConfiguration.AuthAlgorithm.SHARED);
                                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                                    wifiConfiguration.wepTxKeyIndex = 0;

                                    break;
                                case 2: //PSK
                                    Log.w("testApp", " wifiPassword="+wifiPassword);
                                    wifiConfiguration.preSharedKey = "\"" + wifiPassword + "\"";
                                    wifiConfiguration.allowedKeyManagement.set( WifiConfiguration.KeyMgmt.WPA_PSK );
                                    wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                                    wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
                                    break;
                                case 3: //EAP
                                    break;
                                default: //NONE
                                    wifiConfiguration.wepKeys[0] = "";
                                    wifiConfiguration.allowedKeyManagement.set( WifiConfiguration.KeyMgmt.NONE );
                                    wifiConfiguration.wepTxKeyIndex = 0;
                                    break;
                            }
                        }
                    }else {
                        LogUtil.writeToFile(TAG,"设置的WiFi密码为空Password");

                    }
                    //添加wifi配置到网络
                    wifiId = wifiManager.addNetwork( wifiConfiguration );
                    Log.w("testApp","wifiId="+wifiId);
                    if (policyBean.getAllowAutomaticJoin()!=null&& "1".equals(policyBean.getAllowAutomaticJoin())) {
                        wifiManager.enableNetwork( wifiId, true ); //连接
                    }

                }

            } else {
                //删除配置
                if (policyBean.getConfigureWifi()!=null&& "0".equals(policyBean.getConfigureWifi())) {
                    if (policyBean.getSsid() != null) {
                        wifiManager.removeNetwork( wifiId );//可能有问题
                    }
                }
            }
        }

        //允许照相机
        if (policyBean.getAllowCamera()!=null&& "1".equals(policyBean.getAllowCamera())) {
            if (insideAndOutside) {
                MDM.getSingleInstance().enableCamera( false );
            } else {
                MDM.getSingleInstance().enableCamera( true );
            }
        }

        //允许蓝牙
        if (policyBean.getAllowBluetooth()!=null&& "1".equals(policyBean.getAllowBluetooth())) {
            if (insideAndOutside) {
                MDM.getSingleInstance().enableBluetooth( false );
            } else {
                MDM.getSingleInstance().enableBluetooth( true );
            }
        }

        //域切换 围栏内切换到工作域，围栏外切换到生活域
        if (policyBean.getTwoDomainControl()!=null&&"1".equals(policyBean.getTwoDomainControl())) {    //如果"域切换到安全域后 不允许切换到生活域"，因为双域的执行优先级高
                      return;
        }
        if (policyBean.getAllowDomainSwitching()!=null&& "1".equals(policyBean.getAllowDomainSwitching())) {   //Common.hiddenNetwork
            if (insideAndOutside) {
                MDM.getSingleInstance().toSecurityContainer();
            }
        } else {
                MDM.getSingleInstance().toLifeContainer( );
        }
    }

    /**
     * 浏览器配置执行
     * @param policyBean
     * @param insideAndOutside
     */
    private static void excuteSecurityChrome(TimeFenceData.PolicyBean policyBean, boolean insideAndOutside) {

        if (insideAndOutside) {

        } else {

        }
    }

    /**
     * 安装桌面配置执行
     * @param policyBean
     * @param insideAndOutside
     */
    private static void excuteCustomDesktop(TimeFenceData.PolicyBean policyBean, boolean insideAndOutside) {
        Log.w("testApp","安装桌面配置执行");
        /***
         * 显示通话
         显示联系人
         显示短信
         添加应用----应用名称
         */

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        if (insideAndOutside) {
            if (policyBean.getDisplayCall()!=null&&"1".equals(policyBean.getDisplayCall())){
                preferencesManager.setTimefenceData("displayCall","com.android.incallui");
            }

            if (policyBean.getDisplayContacts()!=null&&"1".equals(policyBean.getDisplayContacts())){
                preferencesManager.setTimefenceData("displayContacts","com.android.contacts");
            }

            if (policyBean.getDisplayMessage()!=null&&"1".equals(policyBean.getDisplayMessage())){
                preferencesManager.setTimefenceData("displayMessage","com.android.mms");
            }
            if (policyBean.getApplicationProgram()!=null&&policyBean.getApplicationProgram().size()>0){
                //应用名称
                preferencesManager.setTimefenceData("getApplicationProgram",new Gson().toJson(policyBean.getApplicationProgram()));
            }
            EventBus.getDefault().post(new NotifyEvent());
        } else {
            if (policyBean.getDisplayCall()!=null&&"1".equals(policyBean.getDisplayCall())){
                preferencesManager.setTimefenceData("displayCall",null);
                 }

            if (policyBean.getDisplayContacts()!=null&&"1".equals(policyBean.getDisplayContacts())){
                preferencesManager.setTimefenceData("displayContacts",null);
            }

            if (policyBean.getDisplayMessage()!=null&&"1".equals(policyBean.getDisplayMessage())){
                preferencesManager.setTimefenceData("displayMessage",null);
            }
            if (policyBean.getApplicationProgram()!=null&&policyBean.getApplicationProgram().size()>0){
                //应用名称
                preferencesManager.setTimefenceData("getApplicationProgram",null);
            }
            EventBus.getDefault().post(new NotifyEvent());

        }
    }

    /**
     * 双域配置执行
     * @param policyBean
     * @param insideAndOutside
     */
    private static void excuteDoubleDomain(TimeFenceData.PolicyBean policyBean, boolean insideAndOutside) {

        Log.w("testApp","双域配置执行");
        if (policyBean == null) {
            return;
        }
        if (policyBean.getTwoDomainControl()!=null&&"1".equals(policyBean.getTwoDomainControl())){
            if (insideAndOutside) {
                MDM.getSingleInstance().toSecurityContainer( );

                TheTang.getSingleInstance().getThreadPoolObject().submit( new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if (MDM.getSingleInstance().isInFgContainer()) {
                                MDM.getSingleInstance().disableSwitching();
                                break;
                            }
                        }
                    }
                });
            } else {
                MDM.getSingleInstance().enableSwitching();
            }
        }
    }
}
