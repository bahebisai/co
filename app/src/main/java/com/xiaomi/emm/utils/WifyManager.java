package com.xiaomi.emm.utils;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by lenovo on 2017/9/19.
 */

public class WifyManager {//todo baii util network
    private static final  String  TAG="WifyManager";
    private static WifiManager wifiManager = (WifiManager) TheTang.getSingleInstance().getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    // 扫描出的网络连接列表
    private static List<ScanResult> scanResults;
    // 网络连接列表
    private static List<WifiConfiguration> configuredNetworks;



    // 定义一个WifiLock
    WifiManager.WifiLock mWifiLock;




    //锁定WifiLock
    public void AcquireWifiLock()
    {
        mWifiLock.acquire();
    }
    //解锁WifiLock
    public void ReleaseWifiLock()
    {
        //判断时候锁定
        if (mWifiLock.isHeld())
        {
            mWifiLock.release();
        }
    }
    //创建一个WifiLock
    public void CreatWifiLock()
    {
        mWifiLock = wifiManager.createWifiLock("Test");
    }




    // 断开网络
    public static void disconnectWifi() {
        //wifiManager.disableNetwork(netId);
        wifiManager.disconnect();
    }




    // 断开指定ID的网络
    public static void disconnectWifi(int netId) {
        boolean disableNetwork = wifiManager.disableNetwork(netId);
        Log.w(TAG,"断开wifi配置=disableNetwork="+disableNetwork);
        wifiManager.disconnect();
    }


    /**
     * 创建一个wifi
     * @param SSID  账户
     * @param Password  密码
     * @param Type   类型
     * @return
     */
    public static  WifiConfiguration CreateWifiInfo(String SSID, String  MacAddress,String Password, int Type){

        WifiConfiguration wifiConfiguration = new WifiConfiguration();

        //wifi 配置

        //wifi名称
        wifiConfiguration.SSID = "\"" + SSID + "\"";//"thetang2.4"
        if (!TextUtils.isEmpty(MacAddress)) {

            //mac地址
            wifiConfiguration.BSSID= "\"" + MacAddress + "\"";
        }
        // wifiConfiguration.BSSID=  /*bean.getMacAddress()*/"50:fa:84:1e:95:12";  //50:fa:84:1e:95:13

            switch (Type) {
                case 1: //WEP
                    if (Password != null) {
                        wifiConfiguration.wepKeys[0] = "\"" + Password + "\"";
                        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                        wifiConfiguration.wepTxKeyIndex = 0;
                    }
                    break;
                case 2: //PSK
                    if (Password != null) {
                        wifiConfiguration.preSharedKey = "\"" + Password + "\"";
                        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                        wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
                    }
                    break;
                case 3: //EAP
                    //wifiConfiguration.allowedKeyManagement.set( WifiConfiguration.KeyMgmt.WPA_EAP );
                    break;
                default: //NONE
                    wifiConfiguration.wepKeys[0] = "";
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    wifiConfiguration.wepTxKeyIndex = 0;
                    break;
            }


        return  wifiConfiguration;

    }


    /**
     * 创建一个wifi
     * @param SSID  账户
     * @param Password  密码
     * @param Type   类型
     * @return
     */
    public static  WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type){

        WifiConfiguration wifiConfiguration = new WifiConfiguration();

        //wifi 配置

        //wifi名称
        wifiConfiguration.SSID = "\"" + SSID + "\"";//"thetang2.4"

        // wifiConfiguration.BSSID=  /*bean.getMacAddress()*/"50:fa:84:1e:95:12";  //50:fa:84:1e:95:13

        switch (Type) {
            case 1: //WEP
                if (Password != null) {
                    wifiConfiguration.wepKeys[0] = "\"" + Password + "\"";
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                    wifiConfiguration.wepTxKeyIndex = 0;
                }
                break;
            case 2: //PSK
                if (Password != null) {
                    wifiConfiguration.preSharedKey = "\"" + Password + "\"";
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                    wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
                }
                break;
            case 3: //EAP
                //wifiConfiguration.allowedKeyManagement.set( WifiConfiguration.KeyMgmt.WPA_EAP );
                break;
            default: //NONE
                wifiConfiguration.wepKeys[0] = "";
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfiguration.wepTxKeyIndex = 0;
                break;
        }


        return  wifiConfiguration;

    }


    public static boolean isWifiEnabled(){

        return wifiManager.isWifiEnabled();
    }

    // 打开WIFI
    public static void openWifi() {
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        Log.w(TAG,"wifiEnabled="+wifiEnabled);
        if (!wifiManager.isWifiEnabled()) {

            boolean b = wifiManager.setWifiEnabled(true);
            int wifiState = wifiManager.getWifiState();

            Log.w(TAG,b+"getWifiState"+wifiState);

        }
    }

    // 关闭WIFI
    public static void closeWifi() {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    // 检查当前WIFI状态
    public static int checkState() {
        return wifiManager.getWifiState();
    }

    public static void startScan() {
        wifiManager.startScan();
        // 得到扫描结果
        scanResults = wifiManager.getScanResults();
        // 得到配置好的网络连接
        configuredNetworks = wifiManager.getConfiguredNetworks();
    }


    // 得到网络列表
    public static  List<ScanResult> getWifiList() {
        return scanResults;
    }

    // 查看扫描结果
    public static StringBuilder lookUpScan() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < scanResults.size(); i++) {
            stringBuilder.append("Index_" + new Integer(i + 1).toString() + ":");
            // 将ScanResult信息转换成一个字符串包
            // 其中把包括：BSSID、SSID、capabilities、frequency、level
            stringBuilder.append((scanResults.get(i)).toString());
            stringBuilder.append("/n");
        }
        return stringBuilder;
    }


    // 得到MAC地址
    public static String getMacAddress() {
        // 定义WifiInfo对象
        WifiInfo        mWifiInfo = wifiManager.getConnectionInfo();

        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    // 得到接入点的BSSID
    public static String getBSSID() {
        // 定义WifiInfo对象
        WifiInfo        mWifiInfo = wifiManager.getConnectionInfo();
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    // 得到接入点的SSID
    public static String getSSID() {
        // 定义WifiInfo对象
        WifiInfo        mWifiInfo = wifiManager.getConnectionInfo();
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
    }


    // 得到IP地址
    public static  int getIPAddress() {
        // 定义WifiInfo对象
        WifiInfo        mWifiInfo = wifiManager.getConnectionInfo();
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    // 得到连接的ID
    public static int getNetworkId() {
        // 定义WifiInfo对象
        WifiInfo        mWifiInfo = wifiManager.getConnectionInfo();
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    /**
     * 得到WifiInfo的所有信息包
     * @return
     */
    public static String getWifiInfo() {
        // 定义WifiInfo对象
        WifiInfo        mWifiInfo = wifiManager.getConnectionInfo();
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }


    /**
     * 添加一个网络并连接
     * @param wcg
     */
    public static  boolean addNetwork_Connect(WifiConfiguration wcg) {
        if (wcg == null) {
            return false;
        }
        WifiConfiguration wifiConfiguration = IsExsits(wcg.SSID);
        boolean b;

        String ssid = getSSID();
        if (wifiConfiguration != null) {
            if (! TextUtils.isEmpty(ssid) && TextUtils.isEmpty(wifiConfiguration.SSID) && !ssid.equals(wifiConfiguration.SSID)){
                Log.w(TAG,"wifi不存在添加连接");
                return   wifiManager.enableNetwork(wifiConfiguration.networkId, true);
            }
        }else {

            int wcgID = wifiManager.addNetwork(wcg);
            Log.w(TAG,"wifi不存在添加==="+wcgID);

            /*if (wifiManager.enableNetwork(wcgID, true)){

                wifiConfiguration = IsExsits(wcg.SSID.toString());
                Log.w(TAG,"wifiConfiguration=="+wifiConfiguration);

            }*/
            return wifiManager.enableNetwork(wcgID, true);


        }

        return  false;

    }

    /**
     * 添加一个网络
     * @param wcg
     */
    public static  int addNetwork(WifiConfiguration wcg) {

        return  wifiManager.addNetwork(wcg);

    }


    /**
     * 根据mac地址和账户删除配置
     * @param SSID
     * @param BSSID
     */
    public  static  void  removeNetwork(String SSID,String BSSID){
        Log.w(TAG,BSSID+"---根据mac地址和账户删除配置===="+SSID);
        WifiConfiguration wifiConfiguration = WifyManager.IsExsits(SSID, BSSID);


        if (wifiConfiguration != null) {
            WifiInfo mWifiInfo = wifiManager.getConnectionInfo();
            //断开连接
            if (mWifiInfo.getSSID().equals(SSID)){

                //断开连接
                disconnectWifi(wifiConfiguration.networkId);
            }

            //删除配置
            boolean removeNetworkStatr = wifiManager.removeNetwork(wifiConfiguration.networkId);
            Log.w(TAG,"删除wifi配置=removeNetworkStatr="+removeNetworkStatr);
            wifiManager.saveConfiguration();
        }else {
            Log.w(TAG,"---wifiConfiguration为空===说明wifi没有保存该WiFi");

        }
    }

    /**
     * 根据mac地址删除配置
     * @param SSID
     *
     */
    public  static  void  removeNetwork(String SSID){
        WifiConfiguration wifiConfiguration = WifyManager.IsExsits(SSID);

        if (wifiConfiguration != null) {
            WifiInfo mWifiInfo = wifiManager.getConnectionInfo();
            if (mWifiInfo.getSSID().equals(SSID)){

                //断开连接
                 disconnectWifi(wifiConfiguration.networkId);
            }

            //删除配置
            wifiManager.removeNetwork( wifiConfiguration.networkId );
            wifiManager.saveConfiguration();
        }
    }


    /**
     * 判断当前wifi是否存在
     * @param SSID
     * @return
     */
    public static WifiConfiguration IsExsits(String SSID)
    {
        // 取得WifiManager对象
        // WifiManager mWifiManager = (WifiManager) TheTang.getSingleInstance().getContext() .getSystemService(Context.WIFI_SERVICE);
        // 取得WifiInfo对象
        WifiInfo mWifiInfo = wifiManager.getConnectionInfo();
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        if (existingConfigs == null) {
            Log.w(TAG,"existingConfigs为空==");
            return null;
        }
        for (WifiConfiguration existingConfig : existingConfigs)
        {

            Log.w(TAG,"existingConfig=="+existingConfig.SSID);


            if (existingConfig.SSID.equals("\""+SSID+"\"") || existingConfig.SSID.equals(SSID))
            {
                return existingConfig;
            }
        }
        return null;
    }


    /**
     * 根据wifiId 连接
     * @param wifiId
     * @return
     */
    public  static  boolean conectWifiBywifiId(int wifiId){
       return  wifiManager.enableNetwork(wifiId, true);
    }

    /**
     * 根据 账户和mac地址判断当前wifi是否存在
     * @param SSID
     * @param BSSID
     * @return
     */
    public static WifiConfiguration IsExsits(String SSID,String BSSID)
    {
        // 取得WifiManager对象
        // WifiManager mWifiManager = (WifiManager) TheTang.getSingleInstance().getContext() .getSystemService(Context.WIFI_SERVICE);
        // 取得WifiInfo对象
        WifiInfo mWifiInfo = wifiManager.getConnectionInfo();
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();

        if (existingConfigs == null) {
            return null;
        }
        for (WifiConfiguration existingConfig : existingConfigs)
        {

            Log.w(TAG,"existingConfig.SSID=="+existingConfig.SSID);
          //  Log.w(TAG,"---existingConfig.BSSID=="+existingConfig.BSSID);

            if (("\""+SSID+"\"").equals(existingConfig.SSID) )//(SSID.equals(existingConfig.SSID) && BSSID.equals(existingConfig.BSSID))//
            {
                if (existingConfig.SSID.equals("\"" + SSID + "\"") /*&& existingConfig.BSSID.equals("\""+BSSID+"\"")*/) {
                    return existingConfig;
                }
            }
        }
        return null;
    }


    public static  void open(){

        boolean wifiEnabled = wifiManager.isWifiEnabled();
        Log.w(TAG,"wifiEnabled="+wifiEnabled);


        try {
            Method addToBlacklist = wifiManager.getClass().getDeclaredMethod("addToBlacklist", String.class);
            addToBlacklist.setAccessible(true);
            addToBlacklist.invoke(wifiManager,"50:fa:84:1e:95:13");
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*if (!wifiManager.isWifiEnabled()) {

            try {
                Class  cs_WifiServiceImpl = Class.forName("com.android.server.wifi.WifiServiceImpl");

                Constructor c = cs_WifiServiceImpl.getConstructor(Context.class);
                c.setAccessible(true);//暴力反射,,,,需注意！！
                Object o = c.newInstance(TheTang.getSingleInstance().getContext());
                Method Method_setWifiEnabled = cs_WifiServiceImpl.getDeclaredMethod("setWifiEnabled", boolean.class);//getDeclaredMethod
                Method_setWifiEnabled.setAccessible(true);
                Method_setWifiEnabled.invoke(0, true);



            } catch (Exception e) {
                e.printStackTrace();

            }

        }*/
    }


}
