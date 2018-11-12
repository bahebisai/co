package com.zoomtech.emm.features.policy.fence;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.zoomtech.emm.base.BaseApplication;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.event.NotifyEvent;
import com.zoomtech.emm.features.event.NotifySafedesk;
import com.zoomtech.emm.features.lockscreen.Lock2Activity;
import com.zoomtech.emm.features.lockscreen.NewsLifecycleHandler;
import com.zoomtech.emm.features.policy.app.ExcuteSafeDesktop;
import com.zoomtech.emm.features.service.NetWorkChangeService;
import com.zoomtech.emm.utils.ActivityCollector;
import com.zoomtech.emm.utils.ConvertUtils;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.utils.WifiHelper;
import com.zoomtech.emm.view.activity.MainActivity;
import com.zoomtech.emm.view.activity.SafeDeskActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/18.
 */

public class FenceExcute {
    private static final String TAG = "FenceExcute";

    /**
     * 执行围栏策略
     */
    public static void excuteGeographicalFence(boolean insideAndOutside, boolean isCancel) {

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

        excuteDeviceConfiguration( preferencesManager, insideAndOutside, isCancel );
        excuteSecurityChrome( preferencesManager, insideAndOutside );
        excuteCustomDesktop( preferencesManager, insideAndOutside );
        //excuteDoubleDomain( preferencesManager, insideAndOutside );
    }

    /**
     * 设备配置执行
     *
     * @param preferencesManager
     * @param insideAndOutside
     */
    public static void excuteDeviceConfiguration(PreferencesManager preferencesManager, boolean insideAndOutside, boolean isCancel) {

        //解决地理围栏被删除时，定位服务状态的无法还原的问题
        if ( TextUtils.isEmpty( preferencesManager.getFenceData( Common.radius ) ) &&
                (TextUtils.isEmpty( preferencesManager.getAppFenceData( Common.appFenceRadius ) ) ||
                "0".equals( preferencesManager.getAppFenceData( Common.appFenceRadius ) ) ) ) {
            //允许定位
            if (preferencesManager.getPolicyData( Common.middle_policy ) != null) { //判断是否下发了限制策略
                if ("1".equals( preferencesManager.getPolicyData( Common.middle_allowLocation ) )) {
                    MDM.getSingleInstance().enableLocationService( true );
                } else {
                    MDM.getSingleInstance().enableLocationService( false );
                }
            } else { //没有则使用默认策略
                if ("1".equals( preferencesManager.getPolicyData( Common.default_allowLocation ) )) {
                    MDM.getSingleInstance().enableLocationService( true );
                } else {
                    MDM.getSingleInstance().enableLocationService( false );
                }
            }
        }

        if ("false".equals( preferencesManager.getFenceData( Common.allowDevice ) ) ||
                (!TextUtils.isEmpty( preferencesManager.getFenceData( Common.lockScreen ) ) && "2".equals( preferencesManager.getFenceData( Common.lockScreen ) ))) {
            Log.w( TAG, "所有设备配置都为null,所以都不执行" );
            return;
        }

        Log.w( TAG, "执行设备配置--insideAndOutside" + insideAndOutside );

        if (insideAndOutside) {

            /************************围栏内********************************/
            if (preferencesManager.getFenceData(Common.twoDomainControl) != null && "1".equals(preferencesManager.getFenceData(Common.twoDomainControl))) {
                //如果"双域配置执行"有直接返回就不执行"域切换"，因为双域配置执行执行优先级高

                if (MDM.getSingleInstance().isInFgContainer()) {
                    MDM.getSingleInstance().disableSwitching();
                } else {
                    try {
                        MDM.getSingleInstance().enableSwitching();
                        Thread.sleep(1000);
                        MDM.getSingleInstance().toSecurityContainer();
                        Thread.sleep(1100);
                        MDM.getSingleInstance().disableSwitching();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                threadSecurity();
            }

            if (preferencesManager.getFenceData(Common.allowContainSwitching) != null && "1".equals(preferencesManager.getFenceData(Common.allowContainSwitching))) {
                //用于完成域的切换与禁止
                if (MDM.getSingleInstance().isInFgContainer()) {
                    MDM.getSingleInstance().disableSwitching();
                } else {
                    try {
                        MDM.getSingleInstance().enableSwitching();
                        Thread.sleep(1000);
                        MDM.getSingleInstance().toSecurityContainer();
                        Thread.sleep(1100);
                        MDM.getSingleInstance().disableSwitching();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                threadSecurity();
            }


            //强制锁屏

            if (preferencesManager.getFenceData( Common.lockScreen ) != null && "1".equals( preferencesManager.getFenceData( Common.lockScreen ) )) {
                String geographical_fence = preferencesManager.getFenceData( Common.geographical_fence );
                String pwd = preferencesManager.getFenceData( Common.lockPwd );
                //判断是处于地理围栏还是时间围栏
                if (!TextUtils.isEmpty(geographical_fence)) {
                    MDM.getSingleInstance().forceLockScreen(Common.lockTypes[4], pwd);
                } else {
                    MDM.getSingleInstance().forceLockScreen(Common.lockTypes[3], pwd);
                }
            }

            //不允许数据流量
            if (preferencesManager.getFenceData( Common.allowMobileData ) != null && "1".equals( preferencesManager.getFenceData( Common.allowMobileData ) )) {
                //禁止流量
                Log.w( TAG, "设备配置执行---禁止流量" );
                MDM.getSingleInstance().openDataConnectivity( false );
            }

            //不允许wifi
            if (preferencesManager.getFenceData( Common.allowCloseWifi ) != null && "1".equals( preferencesManager.getFenceData( Common.allowCloseWifi ) )) {
                Log.w( TAG, "设备配置执行---不允许wifi" );
                MDM.getSingleInstance().enableWifi( false );  //false
            }

            //不允许截屏
            if (preferencesManager.getFenceData( Common.banScreenshot ) != null && "1".equals( preferencesManager.getFenceData( Common.banScreenshot ) )) {
                Log.w( TAG, "设备配置执行---不允许截屏" );
                MDM.getSingleInstance().disableScreenShot();  //false
            }

            //不允许下拉栏
            if (preferencesManager.getFenceData( Common.allowDropdown ) != null && "1".equals( preferencesManager.getFenceData( Common.allowDropdown ) )) {
                Log.w( TAG, "设备配置执行---不允许下拉栏" );
                MDM.getSingleInstance().disableDropdown();  //false
            }

            //不允许复位
            if (preferencesManager.getFenceData( Common.allowReset ) != null && "1".equals( preferencesManager.getFenceData( Common.allowReset ) )) {
                Log.w( TAG, "设备配置执行---不允许复位" );
                MDM.getSingleInstance().disableReset(); //false
            }

            //不允许NFC
            if (preferencesManager.getFenceData( Common.allowNFC ) != null && "1".equals( preferencesManager.getFenceData( Common.allowNFC ) )) {
                Log.w( TAG, "设备配置执行---不允许NFC" );
                MDM.getSingleInstance().disableNfc( null );  //false
            }

            //不允许修改日期
            if (preferencesManager.getFenceData( Common.allowModifySystemtime ) != null && "1".equals( preferencesManager.getFenceData( Common.allowModifySystemtime ) )) {
                Log.w( TAG, "设备配置执行---不允许修改日期" );
                MDM.getSingleInstance().disableModifySystemtime();  //false

            }

            // 允许wifi
            String data = preferencesManager.getFenceData( Common.configureWifi );

            Log.w( TAG, preferencesManager.getFenceData( Common.configureWifi ) + "configureWifi===" + data );
            if (preferencesManager.getFenceData( Common.allowOpenWifi ) != null && "1".equals( preferencesManager.getFenceData( Common.allowOpenWifi ) )) {
                Log.w( TAG, "设备配置执行---允许wifi" );
                WifiConfiguration wifiConfiguration = new WifiConfiguration();
                WifiManager wifiManager = (WifiManager) TheTang.getSingleInstance().getContext().getApplicationContext().getSystemService( Context.WIFI_SERVICE );
                int wifiId = -1; //表示失败
                boolean wifiEnabled = wifiManager.isWifiEnabled();
                Log.w( TAG, "wifiEnabled---允许wifi==" + wifiEnabled );
                MDM.getSingleInstance().enableWifi( true );
                // WifiHelper.openWifi();
                //  WifiHelper.open();
                //  MDM.openWifiOnBG( OrderConfig.OpenWifiOnBG + "", true );
                //mMDMController.openWifiOnBGSlient
                wifiEnabled = wifiManager.isWifiEnabled();
                Log.w( TAG, "wifiEnabled---允许wifi===" + wifiEnabled );
                if (!wifiEnabled) {
                    preferencesManager.setFenceData( "conect", "true" );
                }
                //wifi 配置
                if (preferencesManager.getFenceData( Common.allowConfigureWifi ) != null && "1".equals( preferencesManager.getFenceData( Common.allowConfigureWifi ) )) { //判断是否允许配置

                    if (data != null) {
                        //如果原先有这个wifi先删除
                        WifiConfiguration isExsits = WifiHelper.IsExsits( data );
                        if (isExsits != null) {
                            if (!TextUtils.isEmpty( WifiHelper.getSSID() ) && WifiHelper.getSSID().equals( isExsits.SSID )) {

                            }
                            preferencesManager.setFenceData( "wifiId", isExsits.networkId + "" );

                            boolean b1 = wifiManager.enableNetwork( isExsits.networkId, true );

                            Log.w( TAG, b1 + "==如果原先有这个wifi连接成功==" + isExsits.networkId + "===" + isExsits.SSID );
                            wifiConfiguration = isExsits;

                        } else {
                            Log.w( TAG, "==该wifi不存在--" );
                        }

                        wifiConfiguration.SSID = "\"" + data + "\"";

                        if (preferencesManager.getFenceData( Common.hiddenNetwork ) != null && "1".equals( preferencesManager.getFenceData( Common.hiddenNetwork ) )) {
                            wifiConfiguration.hiddenSSID = true;
                        }

                        if (preferencesManager.getFenceData( Common.safeType ) != null) {
                            int typeId = Integer.parseInt( preferencesManager.getFenceData( Common.safeType ) );
                            String Password = preferencesManager.getFenceData( Common.wifi_password );
                            Log.w( TAG, Common.wifi_password + "=====" + Password );
                            switch (typeId) {
                                case 1: //WEP
                                    if (Password != null) {
                                        wifiConfiguration.wepKeys[0] = "\"" + Password + "\"";
                                        wifiConfiguration.allowedKeyManagement.set( WifiConfiguration.KeyMgmt.NONE );
                                        wifiConfiguration.allowedAuthAlgorithms.set( WifiConfiguration.AuthAlgorithm.OPEN );
                                        wifiConfiguration.allowedAuthAlgorithms.set( WifiConfiguration.AuthAlgorithm.SHARED );
                                        wifiConfiguration.allowedGroupCiphers.set( WifiConfiguration.GroupCipher.CCMP );
                                        wifiConfiguration.allowedGroupCiphers.set( WifiConfiguration.GroupCipher.TKIP );
                                        wifiConfiguration.allowedGroupCiphers.set( WifiConfiguration.GroupCipher.WEP40 );
                                        wifiConfiguration.allowedGroupCiphers.set( WifiConfiguration.GroupCipher.WEP104 );
                                        wifiConfiguration.wepTxKeyIndex = 0;
                                    }
                                    break;
                                case 2: //PSK
                                    if (Password != null) {
                                        wifiConfiguration.preSharedKey = "\"" + Password + "\"";
                                        wifiConfiguration.allowedKeyManagement.set( WifiConfiguration.KeyMgmt.WPA_PSK );
                                        wifiConfiguration.allowedAuthAlgorithms.set( WifiConfiguration.AuthAlgorithm.OPEN );
                                        wifiConfiguration.allowedGroupCiphers.set( WifiConfiguration.GroupCipher.TKIP );
                                        wifiConfiguration.allowedPairwiseCiphers.set( WifiConfiguration.PairwiseCipher.TKIP );
                                        wifiConfiguration.allowedGroupCiphers.set( WifiConfiguration.GroupCipher.CCMP );
                                        wifiConfiguration.allowedPairwiseCiphers.set( WifiConfiguration.PairwiseCipher.CCMP );
                                        wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
                                    }
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

                        if (isExsits != null) {

                            wifiId = wifiManager.updateNetwork( wifiConfiguration );
                            Log.w( TAG, "添加wifi配置到网络==" + wifiConfiguration.SSID );
                        } else {
                            //添加wifi配置到网络
                            wifiId = wifiManager.addNetwork( wifiConfiguration );
                            Log.w( TAG, "添加wifi配置到网络==" + wifiId );
                        }

                        if (preferencesManager.getFenceData( Common.allowAutomaticJoin ) != null && "1".equals( preferencesManager.getFenceData( Common.allowAutomaticJoin ) )) {

                            if (wifiId != -1) {
                                //不等于-1说明创建wifi成功
                                boolean b = wifiManager.enableNetwork( wifiId, true );//连接wifi
                                Log.w( TAG, b + "添加wifi配置到网络==" + wifiId );
                                preferencesManager.setFenceData( "wifiId", wifiId + "" );
                                Log.w( TAG, b + "==这个wifi连接=====" + wifiConfiguration.SSID );
                            }
                        }
                    }
                }
            }

            //允许照相机
            if (preferencesManager.getFenceData( Common.allowCamera ) != null && "1".equals( preferencesManager.getFenceData( Common.allowCamera ) )) {
                Log.w( TAG, "设备配置执行---不允许照相机" );
                MDM.getSingleInstance().enableCamera( false );
            }

            //允许蓝牙
            if (preferencesManager.getFenceData( Common.allowBluetooth ) != null && "1".equals( preferencesManager.getFenceData( Common.allowBluetooth ) )) {
                Log.w( TAG, "设备配置执行---不允许蓝牙" );
                MDM.getSingleInstance().enableBluetooth( false );
            }

            if (preferencesManager.getFenceData( Common.mobileHotspot ) != null && "1".equals( preferencesManager.getFenceData( Common.mobileHotspot ) )) {
                Log.w( TAG, "设备配置执行---不允许热点" );
                MDM.getSingleInstance().enableWifiAP( false );
            }

            //时间围栏需要修改地理定位服务
            if (preferencesManager.getFenceData( Common.locationService ) != null && "1".equals( preferencesManager.getFenceData( Common.locationService ) )) {
                Log.w( TAG, "设备配置执行---不允许定位" );
                if  (TextUtils.isEmpty( preferencesManager.getAppFenceData( Common.appFenceRadius ) ) ||
                        "0".equals( preferencesManager.getAppFenceData( Common.appFenceRadius ) ) ){
                    MDM.getSingleInstance().enableLocationService( false );
                }
            }

            if (preferencesManager.getFenceData( Common.matTransmission ) != null && "1".equals( preferencesManager.getFenceData( Common.matTransmission ) )) {
                Log.w( TAG, "设备配置执行---不允许USB" );
                MDM.getSingleInstance().enableUsb( false );
            }

            if (preferencesManager.getFenceData( Common.shortMessage ) != null && "1".equals( preferencesManager.getFenceData( Common.shortMessage ) )) {
                Log.w( TAG, "设备配置执行---不允许短信" );
                MDM.getSingleInstance().enableSms( false );
            }

            if (preferencesManager.getFenceData( Common.soundRecording ) != null && "1".equals( preferencesManager.getFenceData( Common.soundRecording ) )) {
                Log.w( TAG, "设备配置执行---不允许录音" );
                MDM.getSingleInstance().enableSoundRecording( false );
            }

            if (preferencesManager.getFenceData( Common.geo_telephone ) != null && "1".equals( preferencesManager.getFenceData( Common.geo_telephone ) )) {
                MDM.getSingleInstance().enableTelePhone(false);
            }

            if (preferencesManager.getFenceData( Common.geo_telephoneWhiteList ) != null && "1".equals( preferencesManager.getFenceData( Common.geo_telephoneWhiteList ) )) {
                MDM.getSingleInstance().startPhoneWhite();
            }


        } else { /************************围栏外********************************/
            //解锁
            if (preferencesManager.getFenceData( Common.lockScreen ) != null && "1".equals( preferencesManager.getFenceData( Common.lockScreen ) )) {

                String geographical_fence = preferencesManager.getFenceData( Common.geographical_fence );
                //判断是处于地理围栏还是时间围栏
                if (!TextUtils.isEmpty(geographical_fence)) {
                    TheTang.getSingleInstance().whetherCancelLock(4);
                } else {
                    TheTang.getSingleInstance().whetherCancelLock(3);
                }
                //MDM.releaseLockScreen();//取消强制锁定屏幕，手机回到“强制锁定”之前的状态
            }

            //不允许数据流量
            if (preferencesManager.getFenceData( Common.allowMobileData ) != null && "1".equals( preferencesManager.getFenceData( Common.allowMobileData ) )) {

                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) { //判断是否下发了限制策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.middle_allowMobileData ) )) {
                        MDM.getSingleInstance().openDataConnectivity( true ); //应该为禁用移动数据，需要开发
                    } else {
                        MDM.getSingleInstance().openDataConnectivity( false );
                    }
                } else { //没有则使用默认策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.default_allowMobileData ) )) {
                        MDM.getSingleInstance().openDataConnectivity( true ); //应该为禁用移动数据，需要开发
                    } else {
                        MDM.getSingleInstance().openDataConnectivity( false );
                    }
                }
            }

            //不允许wifi
            if (preferencesManager.getFenceData( Common.allowCloseWifi ) != null && "1".equals( preferencesManager.getFenceData( Common.allowCloseWifi ) )) {

                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) { //判断是否下发了限制策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.middle_allowWifi ) )) {
                        MDM.getSingleInstance().enableWifi( true );
                    } else {
                        MDM.getSingleInstance().enableWifi( false );
                    }
                } else { //没有则使用默认策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.default_allowWifi ) )) {
                        MDM.getSingleInstance().enableWifi( true );
                    } else {
                        MDM.getSingleInstance().enableWifi( false );
                    }
                }
            }

            // 允许wifi
            if (preferencesManager.getFenceData( Common.allowOpenWifi ) != null && "1".equals( preferencesManager.getFenceData( Common.allowOpenWifi ) )) {
                Log.w( TAG, "设备配置执行---删除wifi配置" );
                WifiManager wifiManager = (WifiManager) TheTang.getSingleInstance().getContext().getApplicationContext().getSystemService( Context.WIFI_SERVICE );

                //删除配置
                if ("1".equals( preferencesManager.getFenceData( Common.allowConfigureWifi ) )) { //判断是否允许配置
                    if (preferencesManager.getFenceData( Common.configureWifi ) != null) {
                        //先断开
                        String wifiId1 = preferencesManager.getFenceData( "wifiId" );
                        if (!TextUtils.isEmpty( wifiId1 )) {

                            wifiManager.disableNetwork( Integer.valueOf( wifiId1 ) );
                            boolean wifiId = wifiManager.removeNetwork( Integer.valueOf( preferencesManager.getFenceData( "wifiId" ) ) );//可能有问题

                            Log.w( TAG, preferencesManager.getFenceData( "wifiId" ) + "===删除wifi" + wifiId );
                            boolean wifiIds = wifiManager.saveConfiguration();
                            Log.w( TAG, "删除wifi--wifiIds保存-" + wifiIds );
                        }

                    }
                }

                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) { //判断是否下发了限制策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.middle_allowWifi ) )) {
                        MDM.getSingleInstance().enableWifi( true );
                    } else {
                        MDM.getSingleInstance().enableWifi( false );
                    }
                } else { //没有则使用默认策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.default_allowWifi ) )) {
                        MDM.getSingleInstance().enableWifi( true );
                    } else {
                        MDM.getSingleInstance().enableWifi( false );
                    }
                }
            }

            //允许照相机
            if (preferencesManager.getFenceData( Common.allowCamera ) != null && "1".equals( preferencesManager.getFenceData( Common.allowCamera ) )) {
                Log.w( TAG, "设备配置执行---不允许照相机" );

                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) { //判断是否下发了限制策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.middle_allowCamera ) )) {
                        MDM.getSingleInstance().enableCamera( true );
                    } else {
                        MDM.getSingleInstance().enableCamera( false );
                    }
                } else { //没有则使用默认策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.default_allowCamera ) )) {
                        MDM.getSingleInstance().enableCamera( true );
                    } else {
                        MDM.getSingleInstance().enableCamera( false );
                    }
                }
            }

            //允许蓝牙
            if (preferencesManager.getFenceData( Common.allowBluetooth ) != null && "1".equals( preferencesManager.getFenceData( Common.allowBluetooth ) )) {
                Log.w( TAG, "设备配置执行---不允许蓝牙" );

                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) { //判断是否下发了限制策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.middle_allowBluetooth ) )) {
                        MDM.getSingleInstance().enableBluetooth( true );
                    } else {
                        MDM.getSingleInstance().enableBluetooth( false );
                    }
                } else { //没有则使用默认策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.default_allowBluetooth ) )) {
                        MDM.getSingleInstance().enableBluetooth( true );
                    } else {
                        MDM.getSingleInstance().enableBluetooth( false );
                    }
                }
            }

            //允许热点
            if (preferencesManager.getFenceData( Common.mobileHotspot ) != null && "1".equals( preferencesManager.getFenceData( Common.mobileHotspot ) )) {
                Log.w( TAG, "设备配置执行---不允许热点" );

                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) { //判断是否下发了限制策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.middle_allowMobileHotspot ) )) {
                        MDM.getSingleInstance().enableWifiAP( true );
                    } else {
                        MDM.getSingleInstance().enableWifiAP( false );
                    }
                } else { //没有则使用默认策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.default_allowWifiAP ) )) {
                        MDM.getSingleInstance().enableWifiAP( true );
                    } else {
                        MDM.getSingleInstance().enableWifiAP( false );
                    }
                }
            }

            //允许定位
            if (preferencesManager.getFenceData( Common.locationService ) != null && "1".equals( preferencesManager.getFenceData( Common.locationService ) )) {
                Log.w( TAG, "设备配置执行---不允许定位" );

                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) { //判断是否下发了限制策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.middle_allowLocation ) )) {
                        MDM.getSingleInstance().enableLocationService( true );
                    } else {
                        MDM.getSingleInstance().enableLocationService( false );
                    }
                } else { //没有则使用默认策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.default_allowLocation ) )) {
                        MDM.getSingleInstance().enableLocationService( true );
                    } else {
                        MDM.getSingleInstance().enableLocationService( false );
                    }
                }
            }

            //允许USB
            if (preferencesManager.getFenceData( Common.matTransmission ) != null && "1".equals( preferencesManager.getFenceData( Common.matTransmission ) )) {
                Log.w( TAG, "设备配置执行---不允许USB" );

                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) { //判断是否下发了限制策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.middle_allowUsb ) )) {
                        MDM.getSingleInstance().enableUsb( true );
                    } else {
                        MDM.getSingleInstance().enableUsb( false );
                    }
                } else { //没有则使用默认策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.default_allowUsb ) )) {
                        MDM.getSingleInstance().enableUsb( true );
                    } else {
                        MDM.getSingleInstance().enableUsb( false );
                    }
                }
            }

            //允许SMS
            if (preferencesManager.getFenceData( Common.shortMessage ) != null && "1".equals( preferencesManager.getFenceData( Common.shortMessage ) )) {
                Log.w( TAG, "设备配置执行---不允许蓝牙" );

                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) { //判断是否下发了限制策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.middle_allowMessage ) )) {
                        MDM.getSingleInstance().enableSms( true );
                    } else {
                        MDM.getSingleInstance().enableSms( false );
                    }
                } else { //没有则使用默认策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.default_allowMessage ) )) {
                        MDM.getSingleInstance().enableSms( true );
                    } else {
                        MDM.getSingleInstance().enableSms( false );
                    }
                }
            }

            //允许录音
            if (preferencesManager.getFenceData( Common.soundRecording ) != null && "1".equals( preferencesManager.getFenceData( Common.soundRecording ) )) {
                Log.w( TAG, "设备配置执行---不允许蓝牙" );

                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) { //判断是否下发了限制策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.middle_allowSoundRecording ) )) {
                        MDM.getSingleInstance().enableSoundRecording( true );
                    } else {
                        MDM.getSingleInstance().enableSoundRecording( false );
                    }
                } else { //没有则使用默认策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.default_allowSoundRecording ) )) {
                        MDM.getSingleInstance().enableSoundRecording( true );
                    } else {
                        MDM.getSingleInstance().enableSoundRecording( false );
                    }
                }
            }

            if (preferencesManager.getFenceData( Common.banScreenshot ) != null && "1".equals( preferencesManager.getFenceData( Common.banScreenshot ) )) {
                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) {
                    if ("1".equals( preferencesManager.getPolicyData( Common.middle_allowScreenshot ) )) {
                        MDM.getSingleInstance().enableScreenShot( );
                    } else {
                        MDM.getSingleInstance().disableScreenShot();
                    }
                } else { //没有则使用默认策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.default_allowScreenshot ) )) {
                        MDM.getSingleInstance().enableScreenShot();
                    } else {
                        MDM.getSingleInstance().disableScreenShot();
                    }
                }
            }

            //不允许下拉栏
            if (preferencesManager.getFenceData( Common.allowDropdown ) != null && "1".equals( preferencesManager.getFenceData( Common.allowDropdown ) )) {
                if (TextUtils.isEmpty( preferencesManager.getSafedesktopData("code") )) {
                    if (preferencesManager.getPolicyData(Common.middle_policy) != null) {
                        if ("1".equals(preferencesManager.getPolicyData(Common.middle_allowDropdown))) {
                            MDM.getSingleInstance().enableDropdown();
                        } else {
                            MDM.getSingleInstance().disableDropdown();
                        }
                    } else { //没有则使用默认策略
                        if ("1".equals(preferencesManager.getPolicyData(Common.default_allowDropdown))) {
                            MDM.getSingleInstance().enableDropdown();
                        } else {
                            MDM.getSingleInstance().disableDropdown();
                        }
                    }
                }
            }

            //不允许复位
            if (preferencesManager.getFenceData( Common.allowReset ) != null && "1".equals( preferencesManager.getFenceData( Common.allowReset ) )) {

                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) {
                    if ("1".equals( preferencesManager.getPolicyData( Common.middle_allowReset ) )) {
                        MDM.getSingleInstance().enableReset( );
                    } else {
                        MDM.getSingleInstance().disableReset();
                    }
                } else { //没有则使用默认策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.default_allowReset ) )) {
                        MDM.getSingleInstance().enableReset();
                    } else {
                        MDM.getSingleInstance().disableReset();
                    }
                }
            }

            //不允许NFC
            if (preferencesManager.getFenceData( Common.allowNFC ) != null && "1".equals( preferencesManager.getFenceData( Common.allowNFC ) )) {
                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) {
                    if ("1".equals( preferencesManager.getPolicyData( Common.middle_allowNFC ) )) {
                        MDM.getSingleInstance().enableNfc( null);
                    } else {
                        MDM.getSingleInstance().disableNfc(null);
                    }
                } else { //没有则使用默认策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.default_allowNFC ) )) {
                        MDM.getSingleInstance().enableNfc(null);
                    } else {
                        MDM.getSingleInstance().disableNfc(null);
                    }
                }
            }

            //不允许修改日期
            if (preferencesManager.getFenceData( Common.allowModifySystemtime ) != null && "1".equals( preferencesManager.getFenceData( Common.allowModifySystemtime ) )) {

                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) {
                    if ("1".equals( preferencesManager.getPolicyData( Common.middle_allowModifySystemtime ) )) {
                        MDM.getSingleInstance().enableModifySystemtime();
                    } else {
                        MDM.getSingleInstance().disableModifySystemtime();
                    }
                } else { //没有则使用默认策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.default_allowModifySystemtime ) )) {
                        MDM.getSingleInstance().enableModifySystemtime();
                    } else {
                        MDM.getSingleInstance().disableModifySystemtime();
                    }
                }
            }


            if (preferencesManager.getFenceData( Common.geo_telephone ) != null && "1".equals( preferencesManager.getFenceData( Common.geo_telephone ) )) {

                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) {
                    if ("1".equals( preferencesManager.getPolicyData( Common.middle_telephone ) )) {
                        MDM.getSingleInstance().enableTelePhone(true);
                    } else {
                        MDM.getSingleInstance().enableTelePhone(false);
                    }
                } else { //没有则使用默认策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.default_allowTelephone ) )) {
                        MDM.getSingleInstance().enableTelePhone(true);
                    } else {
                        MDM.getSingleInstance().enableTelePhone(false);
                    }
                }
            }

            if (preferencesManager.getFenceData( Common.geo_telephoneWhiteList ) != null && "1".equals( preferencesManager.getFenceData( Common.geo_telephoneWhiteList ) )) {
                if (preferencesManager.getPolicyData( Common.middle_policy ) != null) {
                    if ("1".equals( preferencesManager.getPolicyData( Common.middle_telephoneWhiteList ) )) {
                        MDM.getSingleInstance().startPhoneWhite();
                    } else {
                        MDM.getSingleInstance().stopPhoneWhite();
                    }
                } else { //没有则使用默认策略
                    if ("1".equals( preferencesManager.getPolicyData( Common.default_allowTelephoneWhiteList ) )) {
                        MDM.getSingleInstance().startPhoneWhite();
                    } else {
                        MDM.getSingleInstance().stopPhoneWhite();
                    }
                }
            }


            //域切换到安全域后 不允许切换到生活域
            if (preferencesManager.getFenceData(Common.twoDomainControl) != null && "1".equals(preferencesManager.getFenceData(Common.twoDomainControl))) {
                if (isCancel) {
                    MDM.getSingleInstance().enableSwitching();
                } else {
                    try {
                        MDM.getSingleInstance().enableSwitching();
                        Thread.sleep(1000);
                        MDM.getSingleInstance().toLifeContainer();
                        Thread.sleep(1100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (preferencesManager.getFenceData(Common.allowContainSwitching) != null && "1".equals(preferencesManager.getFenceData(Common.allowContainSwitching))) {

                if (isCancel) {
                    MDM.getSingleInstance().enableSwitching();
                } else {
                    if (!MDM.getSingleInstance().isInFgContainer()) {
                        MDM.getSingleInstance().disableSwitching();
                    } else {
                        try {
                            MDM.getSingleInstance().enableSwitching();
                            Thread.sleep(1000);
                            MDM.getSingleInstance().toLifeContainer();
                            Thread.sleep(1100);
                            MDM.getSingleInstance().disableSwitching();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                threadLife();
            }
        }
    }

    /**
     * 浏览器配置执行
     *
     * @param preferencesManager
     * @param insideAndOutside
     */
    private static void excuteSecurityChrome(PreferencesManager preferencesManager, boolean insideAndOutside) {

        if (preferencesManager.getFenceData( Common.allowChrome ) == null || "2".equals( preferencesManager.getFenceData( Common.webPageList ) ) || "null".equals( preferencesManager.getFenceData( Common.allowChrome ) )) {
            Log.w( TAG, " 浏览器配置 为空不执行浏览器配置执行" );
            return;
        }

        if (preferencesManager.getFenceData( Common.allowChrome ) != null && "true".equals( preferencesManager.getFenceData( Common.allowChrome ) )) {
            Log.w( TAG, "allowChrome true" );
            excuteChrome( preferencesManager, insideAndOutside );
        } else if ("false".equals( preferencesManager.getFenceData( Common.allowChrome ) )) {
            Log.w( TAG, "allowChrome false" );
            excuteChrome( preferencesManager, !insideAndOutside );
        }
    }

    private static void excuteChrome(PreferencesManager preferencesManager, boolean insideAndOutside) {

        if (insideAndOutside) {
            Log.w( TAG, "insideAndOutside true" );
            String list = preferencesManager.getComplianceData( Common.securityChrome_list );

            if (list == null) {
                return;
            }

            Map<String, String> sec_white_list = new HashMap<>();
            sec_white_list = ConvertUtils.jsonStringToMap( list );
            MDM.getSingleInstance().excuteChrome( sec_white_list );
            MDM.getSingleInstance().showToDesk( sec_white_list );
        } else {
            Log.w( TAG, "insideAndOutside false" );
            MDM.getSingleInstance().cancelSecurityChrome();
        }
    }

    /**
     * 安装桌面配置执行
     *
     * @param preferencesManager
     * @param insideAndOutside
     */
    private static void excuteCustomDesktop(PreferencesManager preferencesManager, boolean insideAndOutside) {

        //存储围栏状态存储下来,以便安全桌面策略那边做处理
        preferencesManager.setFenceData( Common.insideAndOutside, insideAndOutside + "" );

        if (TextUtils.isEmpty( preferencesManager.getFenceData( Common.setToSecureDesktop ) ) || "2".equals( preferencesManager.getFenceData( Common.setToSecureDesktop ) ) || "fasle".equals( preferencesManager.getFenceData( Common.allowDesktop ) )) {

            Log.w( TAG, "安装桌面都配置都为null，所以都不执行" );
            return;
        }


        Log.w( TAG, preferencesManager.getFenceData( Common.allowDesktop ) + "执行安装桌面都配置,定制桌面Common.setToSecureDesktop=" + preferencesManager.getFenceData( Common.setToSecureDesktop ) );
        ArrayList<String> list = new ArrayList<>();
        if (insideAndOutside) {

            //如果时间围栏之前已经有安全桌面策略，则应该先清除安全桌面策略

            if (preferencesManager.getFenceData( Common.displayContacts ) != null
                    && "1".equals( preferencesManager.getFenceData( Common.displayContacts ) )) {

                preferencesManager.setTimefenceData( Common.displayContacts, "com.android.contacts" );

            } else {

                preferencesManager.setTimefenceData( Common.displayContacts, null );

            }

            if (preferencesManager.getFenceData( Common.displayMessage ) != null
                    && "1".equals( preferencesManager.getFenceData( Common.displayMessage ) )) {

                preferencesManager.setTimefenceData( "displayMessage", "com.android.mms" );
            } else {

                preferencesManager.setTimefenceData( Common.displayMessage, null );
            }

            if (preferencesManager.getFenceData( Common.displayCall ) != null
                    && "1".equals( preferencesManager.getFenceData( Common.displayCall ) )) {

                preferencesManager.setTimefenceData( Common.displayCall, "com.android.phone" );
            } else {

                preferencesManager.setTimefenceData( Common.displayCall, null );
            }

            //把应用程序添加
            preferencesManager.setTimefenceData( Common.applicationProgram, preferencesManager.getFenceData( Common.applicationProgram ) );


            if (preferencesManager.getFenceData( Common.setToSecureDesktop ) != null &&
                    "1".equals( preferencesManager.getFenceData( Common.setToSecureDesktop ) )) {
                Log.w( TAG, "切换到定制安装桌面隐藏虚拟键" + preferencesManager.getFenceData( Common.setToSecureDesktop ) );
                if (!NewsLifecycleHandler.isApplicationInForeground()) {

                    Log.w( "testAppfront", "setFlag=====后台" );
                    //不锁屏的标志
                    PreferencesManager.getSingleInstance().setLockFlag( "unLockScreen", true );
                    Intent intent = new Intent( Intent.ACTION_MAIN );
                    intent.addCategory( Intent.CATEGORY_LAUNCHER );
                    String packageName = TheTang.getSingleInstance().getContext().getPackageName();//"com.zoomtech.emm";
                    String className = SafeDeskActivity.class.getName();//"com.zoomtech.emm.view.activity.SafeDeskActivity";//InitActivity SafeDeskActivity
                    ComponentName cn = new ComponentName( packageName, className );
                    intent.setComponent( cn );
                    TheTang.getSingleInstance().getContext().startActivity( intent );
                    ActivityCollector.removeAllLock2Activity();

                }

                if (BaseApplication.getNewsLifecycleHandler().isSameClassName( Lock2Activity.class.getSimpleName() )) {
                    Log.w( TAG, "切换到到安全桌面不隐藏虚拟键setToSecureDesktop--如果当前界面为锁屏界面则不跳转有锁屏界面跳转" );
                    MDM.getSingleInstance().enableFingerNavigation(false);
                    MDM.getSingleInstance().setKeyVisible( true );
                    MDM.getSingleInstance().setRecentKeyVisible( false );
                    MDM.getSingleInstance().setHomeKeyVisible( false );


                } else {
                    if (BaseApplication.getNewsLifecycleHandler().isSameClassName( SafeDeskActivity.class.getSimpleName() )) {
                        EventBus.getDefault().post( new NotifySafedesk( Common.safeActicivty_flush ) );

                    } else {

                        Intent intent = new Intent( Intent.ACTION_MAIN );
                        intent.addCategory( Intent.CATEGORY_LAUNCHER );
                        String packageName = TheTang.getSingleInstance().getContext().getPackageName();//"com.zoomtech.emm";
                        String className = SafeDeskActivity.class.getName();//"com.zoomtech.emm.view.activity.SafeDeskActivity";
                        ComponentName cn = new ComponentName( packageName, className );
                        intent.setComponent( cn );
                        TheTang.getSingleInstance().getContext().startActivity( intent );
                        ActivityCollector.removeAllLock2Activity();

                    }

                }


            } else if (preferencesManager.getFenceData( Common.setToSecureDesktop ) != null &&
                    "0".equals( preferencesManager.getFenceData( Common.setToSecureDesktop ) )) {
                if (!NewsLifecycleHandler.isApplicationInForeground()) {

                    Log.w( "testAppfront", "setFlag=====后台" );
                    //不锁屏的标志
                    PreferencesManager.getSingleInstance().setLockFlag( "unLockScreen", true );
                    Intent intent = new Intent( Intent.ACTION_MAIN );
                    intent.addCategory( Intent.CATEGORY_LAUNCHER );
                    String packageName = TheTang.getSingleInstance().getContext().getPackageName();//"com.zoomtech.emm";
                    String className = SafeDeskActivity.class.getName();//"com.zoomtech.emm.view.activity.SafeDeskActivity";//InitActivity SafeDeskActivity
                    ComponentName cn = new ComponentName( packageName, className );
                    intent.setComponent( cn );
                    TheTang.getSingleInstance().getContext().startActivity( intent );
                    ActivityCollector.removeAllLock2Activity();

                }
                Log.w( TAG, "切换到到安全桌面不隐藏虚拟键setToSecureDesktop--" + preferencesManager.getFenceData( Common.setToSecureDesktop ) );
                if (BaseApplication.getNewsLifecycleHandler().isSameClassName( Lock2Activity.class.getSimpleName() )) {
                    Log.w( TAG, "切换到到安全桌面不隐藏虚拟键setToSecureDesktop--如果当前界面为锁屏界面则不跳转有锁屏界面跳转" );
                    MDM.getSingleInstance().enableFingerNavigation(true);
                    MDM.getSingleInstance().setRecentKeyVisible( true );
                    MDM.getSingleInstance().setHomeKeyVisible( true );


                } else {

                    if (BaseApplication.getNewsLifecycleHandler().isSameClassName( SafeDeskActivity.class.getSimpleName() )) {

                        EventBus.getDefault().post( new NotifySafedesk( Common.safeActicivty_flush ) );


                    } else {

                        Intent intent = new Intent( Intent.ACTION_MAIN );
                        intent.addCategory( Intent.CATEGORY_LAUNCHER );
                        String packageName = Common.packageName;
                        String className = SafeDeskActivity.class.getName();//"com.zoomtech.emm.view.activity.SafeDeskActivity";
                        ComponentName cn = new ComponentName( packageName, className );
                        intent.setComponent( cn );
                        TheTang.getSingleInstance().getContext().startActivity( intent );
                        ActivityCollector.removeAllLock2Activity();
                    }
                }

            }
        } else {

            if (preferencesManager.getFenceData( Common.displayContacts ) != null &&
                    "1".equals( preferencesManager.getFenceData( Common.displayContacts ) )) {

                preferencesManager.setTimefenceData( Common.displayContacts, null );
            }

            if (preferencesManager.getFenceData( Common.displayMessage ) != null &&
                    "1".equals( preferencesManager.getFenceData( Common.displayMessage ) )) {

                preferencesManager.setTimefenceData( Common.displayMessage, null );
            }

            if (preferencesManager.getFenceData( Common.displayCall ) != null &&
                    "1".equals( preferencesManager.getFenceData( Common.displayCall ) )) {

                preferencesManager.setTimefenceData( Common.displayCall, null );
            }

            preferencesManager.setTimefenceData( Common.applicationProgram, null );

            if (BaseApplication.getNewsLifecycleHandler().isSameClassName( Lock2Activity.class.getSimpleName() )) {
                Log.w( TAG, "切换到到安全桌面不隐藏虚拟键setToSecureDesktop--如果当前界面为锁屏界面则不跳转有锁屏界面跳转" );
                //如果当前是工作台则不用重新跳转工作台,结束safeActicivty
                EventBus.getDefault().post(new NotifySafedesk(Common.safeActicivty_finsh));

            } else {

                if (!TextUtils.isEmpty( preferencesManager.getFenceData( "desktop" ) ) &&
                        !"2".equals( preferencesManager.getFenceData( "desktop" ) ) &&
                        BaseApplication.getNewsLifecycleHandler().isSameClassName( SafeDeskActivity.class.getSimpleName() )) {

                    preferencesManager.removeFenceData( "desktop" );
                } else {

                    if (preferencesManager.getSafedesktopData( "code" ) != null) {
                        Log.w( TAG, "围栏外如果有安全桌面策略就执行" );
                        //如果安全桌面策略就执行
                        ExcuteSafeDesktop.excute_SafeDesktop();

                    } else {
                        Log.w( TAG, "围栏外如果没有安全桌面策略则就默认显示虚拟键，转到工作台" );
                        //如果没有则就默认显示虚拟键
                        MDM.getSingleInstance().enableFingerNavigation(true);
                        MDM.getSingleInstance().setRecentKeyVisible( true );
                        MDM.getSingleInstance().setHomeKeyVisible( true );
                        if (BaseApplication.getNewsLifecycleHandler().isSameClassName( MainActivity.class.getSimpleName() )) {
                            //如果当前是工作台则不用重新跳转工作台,关闭定制界面
                            EventBus.getDefault().post( new NotifySafedesk( Common.safeActicivty_finsh ) );
                            EventBus.getDefault().post( new NotifyEvent() );

                        } else {

                            Context context = TheTang.getSingleInstance().getContext();
                            context.startActivity( new Intent( context, MainActivity.class ) );
                            EventBus.getDefault().post( new NotifySafedesk( Common.safeActicivty_finsh ) );
                        }
                    }
                }
            }
        }
    }

    /**
     * 双域配置执行
     *
     * @param preferencesManager
     * @param insideAndOutside
     */
    private static void excuteDoubleDomain(PreferencesManager preferencesManager, boolean insideAndOutside) {

        String allowDoubleDomain = preferencesManager.getFenceData( Common.allowDoubleDomain );
        if (TextUtils.isEmpty( allowDoubleDomain ) || "false".equals( allowDoubleDomain )) {
            Log.w( TAG, "双域配置执行为null所以都不执行" );
            return;
        }

        String twoDomainControl = preferencesManager.getFenceData( Common.twoDomainControl );
        if (TextUtils.isEmpty( twoDomainControl ) || "2".equals( twoDomainControl )) {
            Log.w( TAG, "双域配置执行为null所以都不执行" );
            return;
        }
        Log.w( TAG, "双域配置执行" );
        if ("1".equals( twoDomainControl )) {
            if (insideAndOutside) {
                MDM.getSingleInstance().toSecurityContainer();
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
                } );
            } else {
                MDM.getSingleInstance().enableSwitching();
            }
        }
    }

    private static void threadSecurity() {
        //解决跳转失败的问题
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    //用于完成安全域的切换与禁止
                    try {
                        Thread.sleep(5000);
                        NetWorkChangeService.sendFeedBackFalie();

                        if (!MDM.getSingleInstance().isInFgContainer()) {
                            MDM.getSingleInstance().enableSwitching();
                            Thread.sleep(2000);
                            MDM.getSingleInstance().toSecurityContainer();
                            Thread.sleep(1100);
                            MDM.getSingleInstance().disableSwitching();
                            NetWorkChangeService.sendFeedBackFalie();
                        } else {
                            break;
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                LogUtil.writeToFile(TAG, "threadSecurity..." + System.currentTimeMillis());
            }
        }.start();
    }

    private static void threadLife() {
        //解决跳转失败的问题
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    //用于完成安全域的切换与禁止
                    try {
                        Thread.sleep(5000);
                        NetWorkChangeService.sendFeedBackFalie();

                        if (MDM.getSingleInstance().isInFgContainer()) {
                            MDM.getSingleInstance().enableSwitching();
                            Thread.sleep(2000);
                            MDM.getSingleInstance().toLifeContainer();
                            Thread.sleep(1100);
                            MDM.getSingleInstance().disableSwitching();
                            NetWorkChangeService.sendFeedBackFalie();
                        } else {
                            break;
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                LogUtil.writeToFile(TAG, "threadLife..." + System.currentTimeMillis());
            }
        }.start();
    }
}
