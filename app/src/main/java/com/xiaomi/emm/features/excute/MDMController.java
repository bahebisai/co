package com.xiaomi.emm.features.excute;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;

import com.miui.enterprise.sdk.APNConfig;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/7/11.
 */

public abstract class MDMController {

    /**
     * 应用管理相关接口
     * 1、静默安装应用
     * 2、静默卸载应用
     * 3、应用卸载白名单查询
     * 4、应用卸载白名单添加
     * 5、应用卸载白名单删除
     * 6、应用安装白名单查询
     * 7、应用安装白名单添加
     * 8、应用安装白名单删除
     */

    public abstract void installApplication(String apkFilePath, String installerPkg);

    public abstract void uninstallApplication(String packageName);

    public abstract boolean queryPkgNameFromUninstallList(String packageName);

    public abstract void addPkgNameToUninstallList(String packageName);

    public abstract boolean deletePkgNameFromUninstallList(String packageName);

    public abstract boolean queryPkgNameFromInstallList(String packageName);

    public abstract boolean addPkgNameToInstallList(String packageName);

    public abstract boolean deletePkgNameFromInstallList(String packageName);

    public abstract void disableAppAccessToNet(String packageName);

    public abstract void enableAppAccessToNet(String packageName);

    /**
     * Container相关接口
     * 1、切换系统
     * 2、禁止系统切换
     * 3、使能系统切换
     * 4、查询当前是否在安全系统
     * 5、查询当前所处系统是否为前台系统
     */

    public abstract void switchContainer();

    public abstract void disableSwitching();

    public abstract void enableSwitching();

    public abstract boolean isInSecureContainer();

    public abstract boolean isInFgContainer();

    /**
     * 查询硬件及系统信息
     * 1、查询硬件及系统信息
     * 2、获取铃声
     * Created by Administrator on 2017/5/25.
     */
    public abstract List<String> getDeviceInfo();

    /**
     * 位置服务相关接口
     * 1、设置是否允许使用地理位置服务
     * 2、查询是否使用地理位置服务
     * 3、后台静默打开GPS
     * 4、查询GPS是否被后台静默打开
     * Created by Administrator on 2017/5/25.
     */
    public abstract void enableLocationService(boolean enable);

    public abstract boolean isLocationServiceEnabled();

    public abstract boolean openGpsOnBGSlient();

    public abstract boolean isGpsOpenedOnBGSlient();

    /**
     * 蓝牙相关接口
     * 1、允许使用蓝牙
     * 2、查询是否允许使用蓝牙
     * 3、蓝牙连接白名单查询
     * 4、蓝牙连接白名单添加
     * 5、蓝牙连接白名单删除
     * 6、允许蓝牙传输文件
     * 7、查询是否允许蓝牙传输文件
     */
    public abstract void enableBluetooth(boolean enable);

    public abstract boolean isBluetoothEnabled();

    /**
     * Wifi相关接口
     * 1、使能、去使能Wifi
     * 2、查询是否允许使用Wifi
     * 3、后台开启/关闭Wifi
     * 4、查询Wifi状态
     * 5、Wifi 白名单查询
     * 6、Wifi 白名单添加
     * 7、Wifi 白名单删除
     */
    public abstract void enableWifi(boolean enable);

    public abstract boolean isWifiEnabled();

    /**
     * USB相关接口
     * 1、使能USB
     * 2、查询是否允许使用USB
     */
    public abstract void enableUsb(boolean enable);

    public abstract boolean isUsbEnabled();

    /**
     * Camera相关接口
     * 1、允许Camera
     * 2、查询是否允许使用Camera
     */
    public abstract void enableCamera(boolean enable);

    public abstract boolean isCameraEnabled();

    /**
     * 录音相关接口
     * 1、允许录音
     * 2、查询是否允许使用录音
     */
    public abstract void enableSoundRecording(boolean enable);

    public abstract boolean isSoundRecordingEnabled();

    /**
     * Nfc相关接口
     * 1、使能并开启Nfc
     * 2、去使能Nfc
     * 3、查询Nfc状态
     * 4、打开Nfc
     * 5、关闭Nfc
     */
    public abstract void enableNfc();

    public abstract void disableNfc();

    public abstract boolean isNfcEnabled();

    /**
     * APN接口
     * 1、创建APN
     * 2、获取所有APN信息
     * 3、获得APN的详细信息
     * 4、查询当前使用的APN
     * 5、设置当前使用的APN
     * 6、删除APN
     */
    public abstract boolean createApn(ContentValues values);

    public abstract void addApn(APNConfig config);

    public abstract boolean deleteApn(String name);

    public abstract List<String> getApnList();

    public abstract List<APNConfig> getAPNList();

    public abstract ContentValues getApn(int id);

    public abstract String getCurrentApn();

    public abstract boolean setCurrentApn(int id);


    /**
     * 系统安全管理
     */
    /**
     * 锁屏及截屏
     */
    public abstract void setScreenLock();

    public abstract void setPassword(String pwd);

    public abstract void takeScreenShot();

    /**
     * 关机和重启
     */
    public abstract void setShutDown();

    public abstract void setReboot();

    /**
     * 恢复出厂设置
     */
    public abstract void setFactoryReset(boolean isWipeData);

    /**
     * 杀死指定进程
     */
    public abstract void killProcess(String processName);

    /**
     * 擦除用户数据
     */
    public abstract void wipeData(boolean wipeTwoSystem, int flags);

    /**
     * 电话服务管理
     * 1、短信功能管控
     * 2、短信功能查询
     * 3、电话功能管控
     * 4、电话功能查询
     * 5、新增一条联系人
     * 6、删除一条联系人接口
     * 7、获取所有联系人信息
     * Created by Administrator on 2017/5/25.
     */

    public abstract void enableSms(boolean enable);

    public abstract boolean isSmsEnabled();

    public abstract void enableTelephone(boolean enable);

    public abstract boolean isTelephoneEnabled();

    public abstract boolean insertContact(String mName, String mNumber);

    public abstract boolean deleteContactByName(String mName);

    public abstract String getAllContactInfo();

    //联想独有
    public abstract Uri getActualDefaultRingtoneUri(Context context, int type);

    public abstract boolean queryMacFromBTSocketList(String deviceMac);

    public abstract boolean addMacToBTSocketList(String deviceMac);

    public abstract boolean deleteMacFromBTSocketList(String deviceMac);

    public abstract boolean enableBluetoothOpp(boolean enable);

    public abstract boolean isBluetoothOppEnabled();

    public abstract void openWifiOnBG(boolean open);

    public abstract boolean isWifiOpened();

    public abstract boolean queryMacFromWifiList(String deviceMac);

    public abstract boolean addMacToWifiList(String deviceMac);

    public abstract boolean deleteMacFromWifiList(String deviceMac);

    /**
     * Wifi热点相关接口
     * 1、允许/禁止Wifi热点功能
     * 2、查询是否允许使用Wifi AP
     * 3、Wifi AP白名单查询
     * 4、Wifi AP白名单添加
     * 5、Wifi AP白名单删除
     * 6、Wifi热点开关
     * 7、查询Wifi AP状态
     * 8、查询Wifi AP是否打开
     * 9、查询Wifi AP配置参数
     * 10、设置Wifi AP配置参数
     */
    public abstract boolean isWifiAPEnabled();

    public abstract void enableWifiAP(boolean enable);

    public abstract boolean queryMacFromWifiAPList(String deviceMac);

    public abstract boolean addMacToWifiAPList(String deviceMac);

    public abstract boolean deleteMacFromWifiAPList(String deviceMac);

    public abstract boolean setWifiApOpened(WifiConfiguration wifiConfig, boolean opened);

    public abstract int getWifiApState();

    public abstract boolean isWifiAPOpened();

    public abstract WifiConfiguration getWifiApConfiguration();

    public abstract boolean setWifiApConfiguration(WifiConfiguration wifiConfig);



    public abstract boolean openNfc();

    public abstract boolean closeNfc();

    public abstract boolean enableScreenShot();

    public abstract boolean disableScreenShot();

    public abstract boolean isScreenShotEnabled();

    public abstract boolean enableDropdown();

    public abstract boolean disableDropdown();

    public abstract boolean isDropdownEnabled();

    public abstract void enableReset();

    public abstract void disableReset();

    public abstract boolean isResetEnabled();

    public abstract void enableModifySystemTime();

    public abstract void disableModifySystemTime();

    public abstract boolean isModifySystemTimeEnabled();

    /**
     * SD相关接口
     * 1、使能SD，并挂载SD卡
     * 2、禁止外置SD挂载
     */
    public abstract boolean enableSD();

    public abstract boolean disableSD();

    //华为独有接口
    public abstract boolean setAppInstallationPolicy(HashMap<String, String> policy);

    public abstract HashMap<String, String> getAppInstallationPolicy();

    public abstract void enableUninstallWhiteListFunction(boolean enable);

    public abstract void enableInstallWhiteListFunction(boolean enable);

    public abstract boolean queryBTWhiteList(String BTDevice);

    public abstract List<String> queryAllBTWhiteList();

    public abstract boolean addDeviceToBTWhiteList(String BTDevice);

    public abstract boolean deleteDeviceToBTWhiteList(String BTDevice);

    public abstract boolean openWifiOnBGSlient();

    public abstract boolean isWifiOpenedOnBGSlient();

    //todo no impl in xiaomi
    public abstract boolean forceLocationService(boolean isForceOpenLocation);
    /**
     * 第二卡槽控制
     * 1、使能/禁用第二卡槽
     * 2、查询第二卡槽是否使能
     */
    public abstract boolean enableSecSimcard(boolean enable);

    public abstract boolean isSecSimcardEnabled();

    /**
     * 虚拟按键管理
     * 1、设置虚拟按键是否可见
     * 2、设置虚拟Home按键是否可见
     * 3、设置虚拟Recent按键是否可见
     */
    public abstract boolean setKeyVisible(boolean visible);

    public abstract boolean setHomeKeyVisible(boolean visible);

    public abstract boolean setRecentKeyVisible(boolean visible);

    public abstract boolean enableFingerNavigation( boolean enable);
    /**
     * 数据开关控制
     * 1、打开/关闭数据连接开关
     * 2、查询数据连接开关是否被打开
     */
    public abstract void openDataConnectivity(boolean isOpen);

    public abstract boolean isDataConnectivityOpen();

    /**
     * 4G开关控制
     * 1、打开/关闭4G开关
     * 2、查询4G开关是否被打开
     */
    public abstract boolean enable4G(int mode);

    public abstract boolean is4GOpen();

    public abstract void openGps(boolean isSetOpen);

    public abstract boolean isGpsOpend();

    public abstract boolean setLocationPolicy(int policy);

    public abstract int getLocationPolicy();


    public abstract boolean forceLockScreen();

    public abstract Boolean releaseLockScreen();

    public abstract boolean setPasswordWithPolicy(String pwd, int policy);

    public abstract boolean setPasswordNone();

    /**
     * Iptables命令
     * 1、执行iptables命令
     */
    public abstract String executeShellToSetIptables(String shellCommand);

    /**
     * IP白名单
     * 1、查询是否在IP白名单中
     * 2、查询IP白名单
     * 3、添加到IP白名单
     * 4、从IP白名单中删除
     */
    public abstract boolean queryIACList(String iacItem);

    public abstract List<String> queryAllIACList();

    public abstract boolean addItemToIACList(String iacItem);

    public abstract boolean deleteItemFromIACList(String iacItem);



    /**
     * 系统信息管理
     * 1、获取各个应用耗电信息
     * 2、查询正在运行的应用列表
     * 3、查询指定uid的流量信息
     */
    public abstract List<String[]> getAppPowerUsage();

    public abstract List<String[]> getRunningApplication();

    public abstract String[] getAppTrafficInfo(int uid);

    /**
     * 敏感词检测所需接口
     * 1、打开/关闭AccessibilityService
     * 2、查询组件是否AccessibilityService是否开启
     */
    public abstract void setAccessibilityService(ComponentName componentName, boolean isActive);

    public abstract boolean isAccessibilityServiceEnable(ComponentName componentName);

    /***********************************for xiaomi***************************************/
    /**
     * 设置桌面
     */
    public abstract void setHome(String pkgName);

    public abstract void setCallWhiteList(List<String> callWhiteList);

    public abstract boolean isCallWhiteListOpen();

    public abstract void setCallWhiteList(boolean open);

    public abstract void setCallAutoRecord(boolean open);

    public abstract void setCallAutoRecordDir(String path);

    public abstract boolean isCallAutoRecord();
}