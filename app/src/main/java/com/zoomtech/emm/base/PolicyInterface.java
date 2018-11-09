package com.zoomtech.emm.base;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/7/10.
 */

public interface PolicyInterface {
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
    public boolean installApplication(String apkFilePath, boolean installOnSDCard, Handler handler);

    public boolean uninstallApplication(String packageName, boolean keepDataAndCache, Handler handler);

    public boolean queryPkgNameFromUninstallList(String packageName);

    public boolean addPkgNameToUninstallList(String packageName);

    public boolean deletePkgNameFromUninstallList(String packageName);

    public boolean queryPkgNameFromInstallList(String packageName);

    public boolean addPkgNameToInstallList(String packageName);

    public boolean deletePkgNameFromInstallList(String packageName);

    /**
     * Container相关接口
     * 1、切换系统
     * 2、禁止系统切换
     * 3、使能系统切换
     * 4、查询当前是否在安全系统
     * 5、查询当前所处系统是否为前台系统
     */

    public void switchContainer();

    public void disableSwitching();

    public void enableSwitching();

    public boolean isInSecureContainer();

    public boolean isInFgContainer();

    /**
     * 查询硬件及系统信息
     * 1、查询硬件及系统信息
     * 2、获取铃声
     * Created by Administrator on 2017/5/25.
     */
    public List<String> getDeviceInfo();

    /**
     * 位置服务相关接口
     * 1、设置是否允许使用地理位置服务
     * 2、查询是否使用地理位置服务
     * 3、后台静默打开GPS
     * 4、查询GPS是否被后台静默打开
     * Created by Administrator on 2017/5/25.
     */
    public void enableLocationService(boolean enable);

    public boolean isLocationServiceEnabled();

    public boolean openGpsOnBGSlient();

    public boolean isGpsOpenedOnBGSlient();

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
    public boolean enableBluetooth(boolean enable);

    public boolean isBluetoothEnabled();

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
    public boolean enableWifi(boolean enable);

    public boolean isWifiEnabled();

    /**
     * USB相关接口
     * 1、使能USB
     * 2、查询是否允许使用USB
     */
    public boolean enableUsb(boolean enable);

    public boolean isUsbEnabled();

    /**
     * Camera相关接口
     * 1、允许Camera
     * 2、查询是否允许使用Camera
     */
    public boolean enableCamera(boolean enable);

    public boolean isCameraEnabled();

    /**
     * 录音相关接口
     * 1、允许录音
     * 2、查询是否允许使用录音
     */
    public boolean enableSoundRecording(boolean enable);

    public boolean isSoundRecordingEnabled();

    /**
     * Nfc相关接口
     * 1、使能并开启Nfc
     * 2、去使能Nfc
     * 3、查询Nfc状态
     * 4、打开Nfc
     * 5、关闭Nfc
     */
    public boolean enableNfc();

    public boolean disableNfc();

    public boolean isNfcEnabled();

    /**
     * APN接口
     * 1、创建APN
     * 2、获取所有APN信息
     * 3、获得APN的详细信息
     * 4、查询当前使用的APN
     * 5、设置当前使用的APN
     */
    public boolean createApn(ContentValues values);

    public List<String> getApnList();

    public ContentValues getApn(int id);

    public String getCurrentApn();

    public boolean setCurrentApn(int id);

    /**
     * 系统安全管理
     */
    /**
     * 锁屏及截屏
     */
    public void setScreenLock();

    public int setPassword(String pwd);

    public void takeScreenShot();

    /**
     * 关机和重启
     */
    public void setShutDown();

    /**
     * 恢复出厂设置
     */
    public void setFactoryReset(boolean isWipeData);

    /**
     * 杀死指定进程
     */
    public boolean killProcess(String processName);

    /**
     * 擦除用户数据
     */
    public void wipeData(boolean wipeTwoSystem, int flags);

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

    public boolean enableSms(boolean enable);

    public boolean isSmsEnabled();

    public boolean enableTelePhone(boolean enable);

    public boolean isTelephoneEnabled();

    public boolean insertContact(String mName, String mNumber);

    public boolean deleteContactByName(String mName);

    public String getAllContactInfo();

    //联想独有
    public Uri getActualDefaultRingtoneUri(Context context, int type);

    public boolean queryMacFromBTSocketList(String deviceMac);

    public boolean addMacToBTSocketList(String deviceMac);

    public boolean deleteMacFromBTSocketList(String deviceMac);

    public boolean enableBluetoothOpp(boolean enable);

    public boolean isBluetoothOppEnabled();

    public boolean openWifiOnBG(boolean open);

    public boolean isWifiOpened();

    public boolean queryMacFromWifiList(String deviceMac);

    public boolean addMacToWifiList(String deviceMac);

    public boolean deleteMacFromWifiList(String deviceMac);

    public boolean enableWifiAP(boolean enable);

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
    public boolean isWifiAPEnabled();

    public boolean queryMacFromWifiAPList(String deviceMac);

    public boolean addMacToWifiAPList(String deviceMac);

    public boolean deleteMacFromWifiAPList(String deviceMac);

    public boolean setWifiApOpened(WifiConfiguration wifiConfig, boolean opened);

    public int getWifiApState();

    public boolean isWifiAPOpened();

    public WifiConfiguration getWifiApConfiguration();

    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig);

    public boolean openNfc();

    public boolean closeNfc();

    /**
     * SD相关接口
     * 1、使能SD，并挂载SD卡
     * 2、禁止外置SD挂载
     */
    public boolean enableSD();

    public boolean disableSD();

    //华为独有接口
    public boolean setAppInstallationPolicy(HashMap<String, String> policy);

    public HashMap<String, String> getAppInstallationPolicy();

    public void enableUninstallWhiteListFunction(boolean enable);

    public void enableInstallWhiteListFunction(boolean enable);

    boolean queryBTWhiteList(String BTDevice);

    List<String> queryAllBTWhiteList();

    boolean addDeviceToBTWhiteList(String BTDevice);

    boolean deleteDeviceToBTWhiteList(String BTDevice);

    public boolean openWifiOnBGSlient();

    public boolean isWifiOpenedOnBGSlient();

    public boolean forceLocationService(boolean isForceOpenLocation);

    /**
     * 第二卡槽控制
     * 1、使能/禁用第二卡槽
     * 2、查询第二卡槽是否使能
     */
    public boolean enableSecSimcard(boolean enable);

    public boolean isSecSimcardEnabled();

    /**
     * 虚拟按键管理
     * 1、设置虚拟按键是否可见
     * 2、设置虚拟Home按键是否可见
     * 3、设置虚拟Recent按键是否可见
     */
    public boolean setKeyVisible(boolean visible);

    public boolean setHomeKeyVisible(boolean visible);

    public boolean setRecentKeyVisible(boolean visible);

    /**
     * 数据开关控制
     * 1、打开/关闭数据连接开关
     * 2、查询数据连接开关是否被打开
     */
    public boolean openDataConnectivity(boolean isOpen);

    public boolean isDataConnectivityOpen();

    /**
     * 4G开关控制
     * 1、打开/关闭4G开关
     * 2、查询4G开关是否被打开
     */
    public boolean enable4G(int mode);

    public boolean is4GOpen();

    public boolean openGps(boolean isSetOpen);

    public boolean isGpsOpend();

    public boolean setLocationPolicy(int policy);

    public int getLocationPolicy();


    public boolean forceLockScreen();

    public Boolean releaseLockScreen();

    public boolean setPasswordWithPolicy(String pwd, int policy);

    public boolean setPasswordNone();

    /**
     * Iptables命令
     * 1、执行iptables命令
     */
    public String executeShellToSetIptables(String shellCommand);

    /**
     * IP白名单
     * 1、查询是否在IP白名单中
     * 2、查询IP白名单
     * 3、添加到IP白名单
     * 4、从IP白名单中删除
     */
    public boolean queryIACList(String iacItem);

    public List<String> queryAllIACList();

    public boolean addItemToIACList(String iacItem);

    public boolean deleteItemFromIACList(String iacItem);


    boolean deleteApn(String name);

    /**
     * 系统信息管理
     * 1、获取各个应用耗电信息
     * 2、查询正在运行的应用列表
     * 3、查询指定uid的流量信息
     */
    List<String[]> getAppPowerUsage();

    List<String[]> getRunningApplication();

    String[] getAppTrafficInfo(int uid);
}
