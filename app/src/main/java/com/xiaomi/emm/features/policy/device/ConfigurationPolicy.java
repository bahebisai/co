package com.xiaomi.emm.features.policy.device;

import android.net.wifi.WifiConfiguration;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.miui.enterprise.sdk.APNConfig;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.OrderConfig;
import com.xiaomi.emm.features.event.NotifyEvent;
import com.xiaomi.emm.model.ConfigureStrategyData;
import com.xiaomi.emm.utils.DataParseUtil;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.MDM;
import com.xiaomi.emm.utils.PhoneUtils;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.ShortCutManager;
import com.xiaomi.emm.utils.TheTang;
import com.xiaomi.emm.utils.VpnUtilss;
import com.xiaomi.emm.utils.WifyManager;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.TELEPHONY_SERVICE;
import static com.xiaomi.emm.utils.MDM.mMDMController;

/**
 * Created by lenovo on 2017/8/29.
 * 配置策略
 */

public class ConfigurationPolicy {

    private static final String TAG = "ConfigurationPolicy";

    /**
     * 配置策略
     */
    public static void excuteConfigurationPolicy(String extra) {
        if (TextUtils.isEmpty( extra )) {
            Log.w( TAG, "传递过来的配置策略策略参数为空" );
            return;
        }
        ConfigureStrategyData data = DataParseUtil.jsonToData( ConfigureStrategyData.class, extra );

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

        //添加到策列表
        TheTang.getSingleInstance().addStratege( OrderConfig.send_configure_Strategy + "", data.getConfigureStrategy().getName(), System.currentTimeMillis() + "" );

        TheTang.getSingleInstance().addMessage( OrderConfig.send_configure_Strategy + "",
                data.getConfigureStrategy().getName() );

        String isWifi = preferencesManager.getConfiguration( "isWifi" );
        Log.w( TAG, "preferencesManager.getConfiguration(\"isWifi\")====" + isWifi );
        //如果有之前策略,先把之前配置删除
        if (!TextUtils.isEmpty( isWifi ) && !"null".equals( isWifi )) {
            Log.w( TAG, "如果有之前策略,先把之前配置删除" );
            try {

                tearDownShortCut( preferencesManager );
                deleteApn( preferencesManager );
                deleteVpn(preferencesManager);
                deleteWifiConfiguration( preferencesManager );
                //删除数据
                preferencesManager.clearConfiguration();
            } catch (Exception e) {
                Log.w( TAG, "如果有之前策略,先把之前配置删除" );
            }
        }

        storageConfigurationPolicy( data );
        doConfigurationPolicy();
    }


    public static void doConfigurationPolicy() {
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        createDeskShortCut( preferencesManager );
        addAPNConfigure( preferencesManager );
        addVPN( preferencesManager );
        excuteWifiConfiguration( preferencesManager );
    }

    public static void deleteConfigurationPolicy() {

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        if (TextUtils.isEmpty( preferencesManager.getConfiguration( "configuration_name" ) )) {
            Log.w( TAG, "配置策略为空，不执行" );
            return;
        }

        try {
            //添加到策列表
            TheTang.getSingleInstance().deleteStrategeInfo( OrderConfig.send_configure_Strategy + "" );

            TheTang.getSingleInstance().addMessage( OrderConfig.delete_configure_Strategy + "", preferencesManager.getConfiguration( "configuration_name" ) );

            tearDownShortCut( preferencesManager );
            deleteWifiConfiguration( preferencesManager );
            deleteApn( preferencesManager );
            deleteVpn(preferencesManager);

            //删除数据
            preferencesManager.clearConfiguration();

            Log.w( TAG, "isWifi = " + preferencesManager.getConfiguration( "isWifi" ) );
        } catch (Exception e) {
            //返给服务器删除成功信息
        }
    }

    /**
     * 缓存配置策略
     *
     * @param data
     */
    private static void storageConfigurationPolicy(ConfigureStrategyData data) {
        if (data == null) {
            return;
        }

        ConfigureStrategyData.ConfigureStrategyBean configureStrategy = data.getConfigureStrategy();
        if (configureStrategy == null) {
            return;
        }

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        //name
        preferencesManager.setConfiguration( "configuration_name", configureStrategy.getName() );
        //code
        preferencesManager.setConfiguration( Common.CODE, data.getCode() );
        preferencesManager.setConfiguration( Common.ID, data.getId() );
        preferencesManager.setConfiguration( Common.alias, preferencesManager.getData( Common.alias ) );
        //wifi
        preferencesManager.setConfiguration( "isWifi", configureStrategy.getIsWifi() );
        preferencesManager.setConfiguration( "IsAllowWifiConfig", configureStrategy.getIsAllowWifiConfig() );
        if (configureStrategy.getWifiList() != null && configureStrategy.getWifiList().size() > 0) {
            preferencesManager.setConfiguration( "WifiConfig", new Gson().toJson( configureStrategy.getWifiList() ) );
        } else {
            preferencesManager.setConfiguration( "WifiConfig", null );
        }

        //vpn
        preferencesManager.setConfiguration( "isVpn", configureStrategy.getIsVpn() );

        if (configureStrategy.getVpnList() != null && configureStrategy.getVpnList().size() > 0) {
            preferencesManager.setConfiguration( "VpnConfig", new Gson().toJson( configureStrategy.getVpnList() ) );
        } else {
            preferencesManager.setConfiguration( "VpnConfig", null );
        }
        //webclip
        preferencesManager.setConfiguration( "isWebclip", configureStrategy.getIsWebclip() );

        if (configureStrategy.getWebclipList() != null && configureStrategy.getWebclipList().size() > 0) {
            preferencesManager.setConfiguration( "WebclipConfig", new Gson().toJson( configureStrategy.getWebclipList() ) );
        } else {
            preferencesManager.setConfiguration( "WebclipConfig", null );
        }

        //apn
        preferencesManager.setConfiguration( "isApn", configureStrategy.getIsApn() );
        if (configureStrategy.getApnList() != null && configureStrategy.getApnList().size() > 0) {
            preferencesManager.setConfiguration( "ApnConfig", new Gson().toJson( configureStrategy.getApnList() ) );
        } else {
            preferencesManager.setConfiguration( "ApnConfig", null );
        }
    }

    /**
     * 创建webclip(快捷方式)
     *
     * @param preferencesManager
     */
    public static void createDeskShortCut(final PreferencesManager preferencesManager) {

        if (TextUtils.isEmpty( preferencesManager.getConfiguration( "isWebclip" ) ) || "0".equals( preferencesManager.getConfiguration( "isWebclip" ) )) {
            Log.w( TAG, "webclip 不执行" );
            return;
        }

        String webclipConfig = preferencesManager.getConfiguration( "WebclipConfig" );
        if (TextUtils.isEmpty( webclipConfig )) {
            Log.w( TAG, "webclip 没数据不 执行" );
            return;
        }
        Log.w( TAG, "创建webclip 执行" + webclipConfig );

        Type type = new TypeToken<ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean>>() {
        }.getType();

        ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean> lists = new Gson().fromJson( webclipConfig, type );
        Log.w( TAG, lists.size() + "webclipConfig====" + webclipConfig );
        for (ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean bean : lists) {
            ShortCutManager.doDeskShortCut( bean.getWebClipImgPath(), bean.getWebClipName(), bean.getWebClipUrl() );
        }
    }

    /**
     * 删除桌面快捷方式
     *
     * @param preferencesManager
     */
    public static void tearDownShortCut(PreferencesManager preferencesManager) throws Exception {
        if (preferencesManager.getConfiguration( "isWebclip" ) == null || preferencesManager.getConfiguration( "isWebclip" ).isEmpty()
                || "0".equals( preferencesManager.getConfiguration( "isWebclip" ) )) {
            Log.w( TAG, "webclip 不执行删除桌面快捷方式" );
            return;
        }

        String webclipConfig = preferencesManager.getConfiguration( "WebclipConfig" );
        if (webclipConfig == null || webclipConfig.isEmpty()) {
            Log.w( TAG, "webclip 没数据不 执行删除桌面快捷方式" );
            return;
        }

        Log.w( TAG, "删除桌面快捷" );

        Type type = new TypeToken<ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean>>() {
        }.getType();

        ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean> lists = new Gson().fromJson( webclipConfig, type );
        for (ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean bean : lists) {
            String webClipUrl = bean.getWebClipUrl();
            String webClipName = bean.getWebClipName();
            String webClipImgPath = bean.getWebClipImgPath();
            String picName = webClipImgPath.split( "\"" )[webClipImgPath.split( "\"" ).length - 1];
            ShortCutManager.deleteShortCut( webClipUrl, webClipName, picName );
        }
        EventBus.getDefault().post( new NotifyEvent() );
    }


    /**
     * 删除桌面快捷方式
     *
     * @param preferencesManager
     */
    public static void tearDownShortCut2(PreferencesManager preferencesManager) throws Exception {
        if (preferencesManager.getConfiguration( "isWebclip" ) == null || preferencesManager.getConfiguration( "isWebclip" ).isEmpty()
                || "0".equals( preferencesManager.getConfiguration( "isWebclip" ) )) {
            Log.w( TAG, "webclip 不执行删除桌面快捷方式" );
            return;
        }

        String webclipConfig = preferencesManager.getConfiguration( "WebclipConfig" );
        if (webclipConfig == null || webclipConfig.isEmpty()) {
            Log.w( TAG, "webclip 没数据不 执行删除桌面快捷方式" );
            return;
        }

        Log.w( TAG, "删除桌面快捷" );

        Type type = new TypeToken<ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean>>() {
        }.getType();

        ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean> lists = new Gson().fromJson( webclipConfig, type );
        String IN_PATH = "/MDM/Files/images/";
        String savePath = TheTang.getSingleInstance().getContext().getApplicationContext().getFilesDir().getAbsolutePath() + IN_PATH;
        for (ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean bean : lists) {
            ShortcutUtils.removeShortcut( TheTang.getSingleInstance().getContext(), ShortcutUtils.getShortCutIntent( bean.getWebClipUrl() ), bean.getWebClipName() );
            //删除图片
            String picName = bean.getWebClipImgPath().split( "\"" )[bean.getWebClipImgPath().split( "\"" ).length - 1];
            File file = new File( savePath + picName );
            if (file.exists()) {
                file.delete();
            }
        }
        EventBus.getDefault().post( new NotifyEvent() );
    }

    public static void excuteWifiConfiguration(PreferencesManager preferencesManager) {
        String isWifi = preferencesManager.getConfiguration( "isWifi" );
        String wifiConfig = preferencesManager.getConfiguration( "WifiConfig" );

        String isAllowWifiConfig = preferencesManager.getConfiguration( "IsAllowWifiConfig" );
        if (isWifi == null || "0".equals( isWifi ) || wifiConfig == null || wifiConfig.isEmpty() || isAllowWifiConfig == null || "0".equals( isAllowWifiConfig )) {
            Log.w( TAG, "wifi没有配置" );
            return;
        }

        if (!WifyManager.isWifiEnabled()) {
            preferencesManager.setConfiguration( "conect", "true" );
        }

        MDM.openWifiOnBG( OrderConfig.OpenWifiOnBG + "", true );

        Type type = new TypeToken<ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WifiListBean>>() {
        }.getType();
        List<ConfigureStrategyData.ConfigureStrategyBean.WifiListBean> list = new Gson().fromJson( wifiConfig, type );
        if (list == null || list.size() <= 0) {
            Log.w( TAG, "ConfigureStrategyBean.WifiListBean 数据为空" );
            return;
        }
        Log.w( TAG, "配置执行---wifi" );
        for (ConfigureStrategyData.ConfigureStrategyBean.WifiListBean bean : list) {
            performWifiConfiguration( bean );

        }

        //判断当前网络
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

                            WifiConfiguration configuration = WifyManager.IsExsits( bean.getSsid(), bean.getMacAddress() );
                            if (configuration != null) {
                                isExit = true;
                                Log.w( TAG, "WifyManager.getSSID()=" + WifyManager.getSSID() + "configuration.SSID)" + configuration.SSID );

                                if (WifyManager.getSSID().equals( configuration.SSID )) {
                                    flag = true;
                                    break;
                                } else {
                                    isExit = false;
                                }
                            }
                        }
                        if (!TextUtils.isEmpty( preferencesManager.getFenceData( Common.configureWifi ) )) {

                            WifiConfiguration configuration = WifyManager.IsExsits( preferencesManager.getFenceData( Common.configureWifi ) );
                            if (configuration != null) {
                                isExit = true;
                                Log.w( TAG, "getFenceData--WifyManager.getSSID()=" + WifyManager.getSSID() + "configuration.SSID)" + configuration.SSID );

                                if (WifyManager.getSSID().equals( configuration.SSID )) {
                                    flag = true;
                                } else {
                                    isExit = false;
                                }
                            }
                        }

                        //如果不在此策略的wifi就断开  || 还有一种可能就是没有配置成功(所以wifi配置列表没有该wifi存在)
                        if (!flag /*&& isExit*/) {
                            Log.w( TAG, "如果不在此策略的wifi就断开====" + WifyManager.getSSID() );
                            WifyManager.disconnectWifi( WifyManager.getNetworkId() );
                            flag = false;
                        } else {
                            Log.w( TAG, flag + "如果在此策略的wifi就不断开====" + WifyManager.getSSID() + " ,isExit=" + isExit );

                        }
                    }
                }
            }
        }
    }

    private static void performWifiConfiguration(ConfigureStrategyData.ConfigureStrategyBean.WifiListBean bean) {

        WifiConfiguration wifiConfiguration = new WifiConfiguration();

        if (bean.getSecurityType() != null && !bean.getSecurityType().isEmpty()) {
            int typeId = Integer.parseInt( bean.getSecurityType() );
            //创建一个wifi
            wifiConfiguration = WifyManager.CreateWifiInfo( bean.getSsid(), bean.getMacAddress(), bean.getPassword(), typeId );
        }

        //隐藏账户
        if ("1".equals( bean.getIsHiddenNetwork() )) {
            wifiConfiguration.hiddenSSID = true;
        }

        //添加wifi配置到网络
        int wifiId = -1; //表示失败

        WifiConfiguration wifiConfiguration1 = WifyManager.IsExsits( bean.getSsid(), bean.getMacAddress() );
        if (wifiConfiguration1 != null) {
            Log.w( TAG, "存在的wifiConfiguration1=" + wifiConfiguration1.SSID );
            //如果已经存在则先删除掉
            WifyManager.removeNetwork( wifiConfiguration1.SSID );
        }
        wifiId = WifyManager.addNetwork( wifiConfiguration );
        Log.w( TAG, wifiConfiguration.SSID + "添加wifi状态==" + wifiId );
        if ("1".equals( bean.getIsAutoJoin() )) {
        }
    }

    /**
     * 删除WiFi配置参数
     *
     * @param preferencesManager
     */
    private static void deleteWifiConfiguration(PreferencesManager preferencesManager) throws Exception {
        String isWifi = preferencesManager.getConfiguration( "isWifi" );

        if (TextUtils.isEmpty( isWifi ) || "0".equals( isWifi )) {
            Log.w( TAG, "wifi没有配置" );
            return;
        }

        String extra = preferencesManager.getConfiguration( "WifiConfig" );
        if (TextUtils.isEmpty( extra )) {
            Log.w( TAG, "获取存储的WiFi数据为空" );
            return;
        }
        Log.w( TAG, "执行删除WiFi配置" );

        Type listType = new TypeToken<ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WifiListBean>>() {
        }.getType();
        ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WifiListBean> wifiConfigureData = new Gson().fromJson( extra, listType );
        Log.w( TAG, "wifiConfigureData.size()===" + wifiConfigureData.size() );
        if (wifiConfigureData == null) {
            Log.w( TAG, "wifiConfigureData 解析失败" );
            return;
        }

        for (ConfigureStrategyData.ConfigureStrategyBean.WifiListBean bean : wifiConfigureData) {
            WifyManager.removeNetwork( bean.getSsid(), bean.getMacAddress() );
        }
    }

    /**
     * 添加Apn配置
     *
     * @param preferencesManager
     */
    public static void addAPNConfigure(PreferencesManager preferencesManager) {

        if (TextUtils.isEmpty( preferencesManager.getConfiguration( "isApn" ) ) || "0".equals( preferencesManager.getConfiguration( "isApn" ) )) {
            Log.w( TAG, "isApn为" + preferencesManager.getConfiguration( "isApn" ) + ",所以不执行" );
            return;
        }

        String apnConfig = preferencesManager.getConfiguration( "ApnConfig" );
        if (TextUtils.isEmpty( apnConfig )) {
            Log.w( TAG, "APN配置参数" + apnConfig + "，所以不执行" );
            return;
        }

        Log.w( TAG, "执行APN配置参数" );
        Type type = new TypeToken<ArrayList<ConfigureStrategyData.ConfigureStrategyBean.ApnListBean>>() {
        }.getType();

        ArrayList<ConfigureStrategyData.ConfigureStrategyBean.ApnListBean> list = new Gson().fromJson( apnConfig, type );
        Log.w( TAG, "执行APN配置参数" + list.toString() );
        for (ConfigureStrategyData.ConfigureStrategyBean.ApnListBean bean : list) {
            doAddAPN( bean );
        }

        //存储apn 的id 和 name

        List<APNConfig> apnList = mMDMController.getAPNList();
//        Log.w( TAG, "---" + mMDMController.getApn( Integer.parseInt( apnList.get( apnList.size() - 1 ).split( ":" )[0] ) ).toString() + "    ,mMDMController==" + apnList.toString() );

        Log.w(TAG,"getCurrentApn()="+mMDMController.getCurrentApn());
       /* if (apnList != null && apnList.size()>0 && !TextUtils.isEmpty(apnList.get(apnList.size()-1))){
            for (String s:apnList) {
                if ( !TextUtils.isEmpty(s)){
                    String apn_id = s.split(":")[0];
                    mMDMController.setCurrentApn(Integer.parseInt(apn_id));
                }
            }
        }*/

    }

    public static void doAddAPN(ConfigureStrategyData.ConfigureStrategyBean.ApnListBean bean) {
        /**
         * ContentValues values
         参数 values：APN的参数值，key说明如下
         "name"：APN描述（用于显示标题）
         "apn"：APN名称
         "type"：APN类型，如"default,supl"
         "numeric"：运营商网络码，一般通过getSimOperator获取
         "mcc"：MCC
         "mnc"：MNC
         "proxy"：代理
         "port"：端口
         "mmsproxy"：彩信代理
         "mmsport"：彩信端口
         "user"：用户名
         "server"：服务器
         "password"：密码
         "mmsc"：MMSC
         "visible"：是否可见
         */
        TelephonyManager telephonyManager = (TelephonyManager) TheTang.getSingleInstance().getContext().getSystemService( TELEPHONY_SERVICE );
        String networkOperator = telephonyManager.getNetworkOperator();
        String subscriberId = telephonyManager.getSubscriberId();
        //添加到所有的卡槽
        String[] imsis = PhoneUtils.getSubscriberId(TheTang.getSingleInstance().getContext());
        for (int i = 0; i < imsis.length; i++) {
            if (!TextUtils.isEmpty(imsis[i])){
/*                ContentValues values = new ContentValues();
                values.put( "name", bean.getApnName() );
                //imsis[i].substring(0, 3)
                values.put( "mcc", subscriberId.substring( 0, 3 ) );
                //imsis[i].substring(3, 5)
                values.put( "mnc", subscriberId.substring( 3, 5 ) );
                // imsis[i].substring(0, 5)
                values.put( "numeric", subscriberId.substring( 0, 5 ) );
                values.put( "type", "default" );
                // preferencesManager.getConfiguration("apnName")
                values.put( "apn", bean.getApn() );
                //  if (!TextUtils.isEmpty( bean.getApnPassword() )) {
                //preferencesManager.getConfiguration("apnPassword")
                values.put( "password", bean.getApnPassword() );
                //   }
                //  if (!TextUtils.isEmpty( bean.getApnServerAddress() )) {
                // preferencesManager.getConfiguration("apnServerAddress")
                values.put( "server", bean.getApnServerAddress() );
                //   }
                values.put( "user", bean.getApnUsername() );
                // if (!TextUtils.isEmpty( bean.getApnPort() )) {
                //preferencesManager.getConfiguration("apnPort")
                values.put( "port", bean.getApnPort() );
                //   }
                //  MDM.createApn( preferencesManager.getConfiguration("code"),values);
                //    Log.w(TAG,MDM.getApnList(preferencesManager.getConfiguration("code").toString()+"");
                //boolean apn = mMDMController.createApn(values);
                boolean apn = MDM.createApn( values );*/

                String name = bean.getApnName();
                String apn = bean.getApn();
                String user = bean.getApnUsername();
                String password = bean.getApnPassword();
                APNConfig apnConfig = new APNConfig(name, apn, user, password,-1, null, 0);
                apnConfig.mMcc = subscriberId.substring( 0, 3 );
                apnConfig.mMnc = subscriberId.substring( 3, 5 );
                apnConfig.mNumeric = subscriberId.substring( 0, 5 );
                apnConfig.mServer = bean.getApnServerAddress();
                apnConfig.mPort = bean.getApnPort();
                mMDMController.addApn(apnConfig);

                Log.w( TAG, networkOperator.substring( 0, 3 ) + "APN---连接状态==" + networkOperator + "  ,bean.getApnName()=" + bean.getApnName() );
                LogUtil.writeToFile(TAG, "apn = " + apnConfig.toString());
                //   ContentValues apn1 = mMDMController.getApn( 1834 );

                Log.w( TAG, "subscriberId =  " + subscriberId + "   ----, networkOperator.substring(0,3) =" + networkOperator.substring( 0, 3 ) + "APN---连接状态==" + networkOperator.substring( 3 ) + "+networkOperator.substring(3)  +" + bean.getApnName() + "---" + bean.getApnName() );
            }
        }
    }

    /**
     * 删除APN配置
     *
     * @param preferencesManager
     */
    private static void deleteApn(PreferencesManager preferencesManager) {
        if (TextUtils.isEmpty( preferencesManager.getConfiguration( "isApn" ) ) || "0".equals( preferencesManager.getConfiguration( "isApn" ) )) {
            Log.w( TAG, "isApn为" + preferencesManager.getConfiguration( "isApn" ) + ",所以不执行" );
            return;
        }

        String apnConfig = preferencesManager.getConfiguration( "ApnConfig" );
        if (apnConfig == null || apnConfig.isEmpty()) {
            Log.w( TAG, "APN配置参数" + apnConfig + "，所以不执行删除APN配置" );
            return;
        }

        Log.w( TAG, "删除APN配置" );
        Type type = new TypeToken<ArrayList<ConfigureStrategyData.ConfigureStrategyBean.ApnListBean>>() {
        }.getType();

        ArrayList<ConfigureStrategyData.ConfigureStrategyBean.ApnListBean> list = new Gson().fromJson( apnConfig, type );

        for (ConfigureStrategyData.ConfigureStrategyBean.ApnListBean bean : list) {
            //根据名称 bean.getApnName()
            MDM.deleteApn( preferencesManager.getConfiguration( "code" ), bean.getApnName() );
        }
    }

    private static void addVPN(PreferencesManager preferencesManager) {
        //vpn

        if (TextUtils.isEmpty( preferencesManager.getConfiguration( "isVpn" ) ) || "0".equals( preferencesManager.getConfiguration( "isVpn" ) )) {
            Log.w( TAG, "isVpn 为" + preferencesManager.getConfiguration( "isVpn" ) + "所以 不执行" );
            return;
        }

        String vpnConfig = preferencesManager.getConfiguration( "VpnConfig" );

        if (TextUtils.isEmpty( vpnConfig )) {
            Log.w( TAG, "vpn数据为=" + vpnConfig + "  所以不执行" );
            return;
        }
        Log.w( TAG, "vpn 执行" );
        Type type = new TypeToken<ArrayList<ConfigureStrategyData.ConfigureStrategyBean.VpnListBean>>() {
        }.getType();
        ArrayList<ConfigureStrategyData.ConfigureStrategyBean.VpnListBean> list = new Gson().fromJson( vpnConfig, type );

        for (ConfigureStrategyData.ConfigureStrategyBean.VpnListBean bean : list) {

            //doAddVPN( preferencesManager, bean );
            doAddVPN2( preferencesManager, bean );

        }
    }

    private static  void  doAddVPN2(PreferencesManager preferencesManager,ConfigureStrategyData.ConfigureStrategyBean.VpnListBean bean) {
        VpnUtilss.init(TheTang.getSingleInstance().getContext());

        Object vpnProfile = VpnUtilss.createVpnProfile(bean.getVpnConnectionName(), bean.getVpnServerAddress(), bean.getVpnAccount(), bean.getVpnPassword(), bean.getVpnConnectionType());
        String keyVpnProfiles = preferencesManager.getConfiguration("keyVpnProfile");

        if (TextUtils.isEmpty(keyVpnProfiles)){
            keyVpnProfiles="";
        }
        try {
            Field key_type = VpnUtilss.vpnProfileClz.getDeclaredField("key");
            keyVpnProfiles=keyVpnProfiles+key_type.get(vpnProfile)+",";
            preferencesManager.setConfiguration("keyVpnProfile",keyVpnProfiles);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean connect = VpnUtilss.connect(TheTang.getSingleInstance().getContext(), vpnProfile);

        if (connect){
            Log.w( TAG, "vpn  连接成功" );
        }else {
            Log.w( TAG, "vpn  连接失败" );
        }
    }

    /**
     * 删除VPN配置
     * @param preferencesManager
     */
    private static void deleteVpn(PreferencesManager preferencesManager) {

        String keyVpnProfiles = preferencesManager.getConfiguration("keyVpnProfile");

        if (TextUtils.isEmpty(keyVpnProfiles)){
            Log.w(TAG,"keyVpnProfiles数据为="+keyVpnProfiles+"  所以不执行删除");
            return;
        }
        VpnUtilss.init(TheTang.getSingleInstance().getContext());
        if (keyVpnProfiles.contains(",")){
            Log.w(TAG,"keyVpnProfiles数据为=1"+keyVpnProfiles+"  所以执行删除");
            String[] split = keyVpnProfiles.split(",");
            /**
             * 如果有","去掉最后一个","
             */
            for (int i = 0; i < split.length; i++) {
                VpnUtilss.delete(split[i]);
            }
        }else {
            VpnUtilss.delete(keyVpnProfiles);
        }
        preferencesManager.removeConfiguration("keyVpnProfile");
    }

    private static void doAddVPN(PreferencesManager preferencesManager, ConfigureStrategyData.ConfigureStrategyBean.VpnListBean bean) {
        if (preferencesManager == null || bean == null) {
            return;
        }
        //doVpn(preferencesManager);
          //  IVpnPolicy vpnPolicyService = new VpnPolicyService(TheTang.getSingleInstance().getContext());
          //  IBinder iBinder = vpnPolicyService.asBinder();
           // VpnPolicy vpnPolicy = new VpnPolicy(TheTang.getSingleInstance().getContext(), iBinder);
        }

/*
    private static Map<String , Class<?>> getMappedFields(){
        Map<String , Class<?>> fieldsAndTypes = new HashMap<String, Class<?>>();
        fieldsAndTypes.put("name", String.class);        // 0
        fieldsAndTypes.put("type" , int.class);   // 1
        fieldsAndTypes.put("server", String.class);        // 2
        fieldsAndTypes.put("username", String.class);
        fieldsAndTypes.put("password", String.class);
        fieldsAndTypes.put("dnsServers", String.class);
        fieldsAndTypes.put("searchDomains", String.class);
        fieldsAndTypes.put("routes", String.class);
        fieldsAndTypes.put("mppe", boolean.class);
        fieldsAndTypes.put("l2tpSecret", String.class);
        fieldsAndTypes.put("ipsecIdentifier", String.class);
        fieldsAndTypes.put("ipsecSecret", String.class);
        fieldsAndTypes.put("ipsecUserCert", String.class);
        fieldsAndTypes.put("ipsecCaCert", String.class);
        fieldsAndTypes.put("saveLogin", boolean.class);
        return fieldsAndTypes;
    }*/

    /**
     * 这个方法不行（不适用）
     *
     * @param preferencesManager
     */
    private static void doVpn(PreferencesManager preferencesManager) throws Exception {
        String command = "mtpd wlan0";
        /**
         *
         * 执行 mtpd
         输出
         mtpd interface 12tp <server> <port> <secret> pppd-arguments
         mtpd interface pptp <server> <port> pppd-arguments

         实例如下:
         mtpd wlan0  pptp a.ueuz.com 1723 name d11234 password 1234 linkname vpn refuse-eap  nodefaultroute idle 1800 mtu 1400 mru 1400 nomppe unit 100
         */

        String type = preferencesManager.getConfiguration( "connectionType" );// connectionType": 0,// vpn 连接类型 0 1  
        if (!TextUtils.isEmpty( type ) && "0".equals( type )) {
            command = command + " " + "l2tp";
        } else if (!TextUtils.isEmpty( type ) && "1".equals( type )) {
            command = command + " " + "pptp";
        }

        command = command + " " + preferencesManager.getConfiguration( "serverAddress" ); //serverAddress": "1",//服务器地址  
        command = command + " " + "name  " + preferencesManager.getConfiguration( "accounts" );//accounts": "",//vpn 账号
        command = command + " " + "password  " + preferencesManager.getConfiguration( "password" );//password": "1",// vpn 密码
        command = command + " " + "linkname  " + preferencesManager.getConfiguration( "connectionName" );// connectionName": "1",//vpn 连接名称
        command = command + " " + "refuse-eap  nodefaultroute";
        command = command + "  idle 1800";
        String level = preferencesManager.getConfiguration( "encryptionLevel" );
        if (level != null && "0".equals( level )) {

            command = command + "  " + "nomppe";
        } else if (level != null && "1".equals( level )) {
            command = command + "  " + "mppe";
        } else if (level != null && "2".equals( level )) {
            command = command + "  " + "mppe-128";
        }
        command = command + " " + "unit 100";

        //preferencesManager.setConfiguration("sharedKey",configureStrategy.getSharedKey());// sharedKey": "1",// 共享秘钥
        //preferencesManager.setConfiguration("isEncryption",configureStrategy.getEncryptionLevel());//: 0,// 是否加密 
        try {
            execCommand( command );
        } catch (IOException e) {
            e.printStackTrace();
            Log.w( TAG, "执行VPN命令异常=:" + e.toString() );
            LogUtil.writeToFile( TAG, "执行VPN命令异常=:" );
        }
    }

    private static void execCommand(String command) throws Exception {
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec( command );
        try {
            if (proc.waitFor() != 0) {
                //  System.err.println("exit value = " + proc.exitValue());
                Log.w( TAG, "exit value = " + proc.exitValue() );
            }
            BufferedReader in = new BufferedReader( new InputStreamReader(
                    proc.getInputStream() ) );
            StringBuffer stringBuffer = new StringBuffer();
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append( line + " " );
            }
            Log.w( TAG, "-----" + stringBuffer.toString() );

        } catch (InterruptedException e) {
            //   System.err.println(e);
            //  e.printStackTrace();
            //Log.w(TAG,);
            Log.w( TAG, " value = " + e.toString() );
        } finally {
            try {
                proc.destroy();
            } catch (Exception e2) {
            }
        }
    }
}



