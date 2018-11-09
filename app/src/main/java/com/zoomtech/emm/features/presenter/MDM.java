package com.zoomtech.emm.features.presenter;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.zoomtech.emm.R;
import com.zoomtech.emm.base.BaseApplication;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.definition.OrderConfig;
import com.zoomtech.emm.features.db.DatabaseOperate;
import com.zoomtech.emm.features.download.DownLoadManager;
import com.zoomtech.emm.features.event.APKEvent;
import com.zoomtech.emm.features.event.CompleteEvent;
import com.zoomtech.emm.features.event.NotifyEvent;
import com.zoomtech.emm.features.event.SettingEvent;
import com.zoomtech.emm.features.event.WhiteListEvent;
import com.zoomtech.emm.features.excute.XiaomiMDMController;

import com.zoomtech.emm.features.impl.SendMessageManager;
import com.zoomtech.emm.features.impl.TelephoneWhiteListImpl;
import com.zoomtech.emm.features.impl.WebclipImageImpl;
import com.zoomtech.emm.features.location.LocationService;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.manager.ShortCutManager;
import com.zoomtech.emm.features.policy.compliance.ExcuteCompliance;
import com.zoomtech.emm.features.policy.compliance.machinecard.MachineCardBindingService;
import com.zoomtech.emm.features.policy.device.ExcuteLimitPolicy;
import com.zoomtech.emm.features.policy.device.ShortcutUtils;
import com.zoomtech.emm.features.silent.AppTask;
import com.zoomtech.emm.model.APPInfo;
import com.zoomtech.emm.model.AppBlackWhiteData;
import com.zoomtech.emm.model.DeleteAppData;
import com.zoomtech.emm.model.DownLoadEntity;
import com.zoomtech.emm.model.ExceptionLogData;
import com.zoomtech.emm.model.MessageSendData;
import com.zoomtech.emm.model.SafetyLimitData;
import com.zoomtech.emm.model.SecurityChromeData;
import com.zoomtech.emm.model.SettingAboutData;
import com.zoomtech.emm.model.TelephoyWhiteUser;
import com.zoomtech.emm.utils.AppUtils;
import com.zoomtech.emm.utils.ConvertUtils;
import com.zoomtech.emm.utils.DeviceUtils;
import com.zoomtech.emm.utils.FileUtils;
import com.zoomtech.emm.utils.HttpHelper;
import com.zoomtech.emm.utils.JsonGenerateUtil;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.utils.PhoneUtils;
import com.zoomtech.emm.utils.TimeUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Control功能类，接收命令与实现命令的中间层
 * Created by Administrator on 2017/7/10.
 */

public class MDM {

    public static final String TAG = "MDM";
//    public static HuaweiMDMController mHuaweiMDMController;
    private static XiaomiMDMController mMDMController;
    public static TheTang mTheTang;
    static Context mContext;

    static Bitmap bitmap = null;

    /**********************************************
     * 位置服务相关接口
     ********************************/

    static MDM.GpsLocationReceiver gpsLocationReceiver = null;

    static PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

    //单例
    private volatile static MDM mMDM;

    private MDM() {
    }

    /**
     * 单例
     *
     * @return
     */
    public static MDM getSingleInstance() {
        if (null == mMDM) {
            synchronized (MDM.class) {
                if (null == mMDM) {
                    mMDM = new MDM();
                }
            }
        }
        return mMDM;
    }

    /***********************************
     * 初始化
     *****************************************************/

    public MDM init(Context context) {
        mContext = context;

//        mHuaweiMDMController = HuaweiMDMController.getSingleInstance();
        mMDMController = XiaomiMDMController.getSingleInstance();

       /* PackageManager pm = mContext.getPackageManager();
        ApplicationInfo ai = null;
        try {
            ai = pm.getApplicationInfo("com.android.browser", PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (ai != null) {
            int uid = ai.uid;
            String str1 = " -A " + uid + " -m string --string hao123 --algo bm -j REJECT";
            String str2 = " -A " + uid + " -j ACCEPT";
            mMDMController.executeShellToSetIptables(str1);
            mMDMController.executeShellToSetIptables(str2);
        }*/

        ////
        mTheTang = TheTang.getSingleInstance();
        LogUtil.writeToFile(TAG, "MDM init!");
        return getSingleInstance();
    }

    /***********************************
     * 系统切换
     ***************************************************/

    public static void toLifeContainer() {

        LogUtil.writeToFile(TAG, "isInSecureContainer = " + mMDMController.isInSecureContainer());
        if (mMDMController.isInFgContainer()) {
            LogUtil.writeToFile(TAG, "switch to life Container!");

            preferencesManager.setOtherData("switchByOrder", "true");//标识是否为自动切换域
            switchContainer();
        } else {
            LogUtil.writeToFile(TAG, "current is life Container!");
        }
    }

    public static void toSecurityContainer() {

        LogUtil.writeToFile(TAG, "isInSecureContainer = " + mMDMController.isInSecureContainer());
        if (!mMDMController.isInFgContainer()) {
            LogUtil.writeToFile(TAG, "switch to security Container!");
            preferencesManager.setOtherData("switchByOrder", "true");//标识是否为自动切换域
            switchContainer();
        } else {
            LogUtil.writeToFile(TAG, "current is security Container!");
        }
    }

    /**
     * 域切换
     */
    private static void switchContainer() {
        LogUtil.writeToFile(TAG, "switchContainer start");
        mMDMController.switchContainer();
        LogUtil.writeToFile(TAG, "switchContainer end");
    }

    public static void disableSwitching() {
        mMDMController.disableSwitching();
    }

    public static void enableSwitching() {
        mMDMController.enableSwitching();
    }

    public static void isInSecureContainer(String code) {
        // mTheTang.feedBackAll( code, mMDMController.isInSecureContainer() );
    }

    public static void isInFgContainer(String code) {
        //mTheTang.feedBackAll( code, mMDMController.isInFgContainer() );
    }

    /***********************************
     * 应用管理
     ***************************************************/
    /**
     * 静默安装
     *
     * @param path
     * @return
     */
    public static void silentInstall(final String path) {
        mMDMController.installApplication(path, null);
    }

    /**
     * 静默卸载
     *
     * @param packageName
     * @return
     */
    public static void silentUninstall(String packageName) {
        mMDMController.uninstallApplication(packageName);
    }

    /*public static void queryPkgNameFromUninstallList(String code, String packageName) {
        mTheTang.feedBackAll( code, mMDMController.queryPkgNameFromUninstallList( packageName ) );
    }

    public static void addPkgNameToUninstallList(String code, String packageName) {
        mTheTang.feedBackAll( code, mMDMController.addPkgNameToUninstallList( packageName ) );
    }

    public static void deletePkgNameFromUninstallList(String code, String packageName) {
        mTheTang.feedBackAll( code, mMDMController.deletePkgNameFromUninstallList( packageName ) );
    }

    public static void queryPkgNameFromInstallList(String code, String packageName) {
        mTheTang.feedBackAll( code, mMDMController.queryPkgNameFromInstallList( packageName ) );
    }

    public static void addPkgNameToInstallList(String code, String packageName) {
        mTheTang.feedBackAll( code, mMDMController.addPkgNameToInstallList( packageName ) );
    }

    public static void deletePkgNameFromInstallList(String code, String packageName) {
        mTheTang.feedBackAll( code, mMDMController.deletePkgNameFromInstallList( packageName ) );
    }*/

    /***********************************
     * 外设相关
     ***************************************************/

    public static void enableBluetooth(boolean enable) {
        mMDMController.enableBluetooth(enable);
    }

    public static boolean isBluetoothEnabled() {
        return mMDMController.isBluetoothEnabled();
    }

    /*public static void queryMacFromBTSocketList(String code, String deviceMac) {
        mTheTang.feedBackAll( code, mMDMController.queryMacFromBTSocketList( deviceMac ) );
    }

    public static void addMacToBTSocketList(String code, String deviceMac) {
        mTheTang.feedBackAll( code, mMDMController.addMacToBTSocketList( deviceMac ) );
    }

    public static void deleteMacFromBTSocketList(String code, String deviceMac) {
        mTheTang.feedBackAll( code, mMDMController.deleteMacFromBTSocketList( deviceMac ) );
    }*/

    public static void enableWifi(boolean enable) {
        mMDMController.enableWifi(enable);
    }

    public static boolean isWifiEnabled() {
        return mMDMController.isWifiEnabled();
    }

    public static void openWifiOnBG(String code, boolean open) {
        mMDMController.openWifiOnBG(open);
    }

   /* public static void isWifiOpened(String code) {
        mTheTang.feedBackAll( code, mMDMController.isWifiOpened() );
    }

    public static void queryMacFromWifiList(String code, String deviceMac) {
        mTheTang.feedBackAll( code, mMDMController.queryMacFromWifiList( deviceMac ) );
    }

    public static void addMacToWifiList(String code, String deviceMac) {
        mTheTang.feedBackAll( code, mMDMController.addMacToWifiList( deviceMac ) );
    }

    public static void deleteMacFromWifiList(String code, String deviceMac) {
        mTheTang.feedBackAll( code, mMDMController.deleteMacFromWifiList( deviceMac ) );
    }*/

    public static void enableWifiAP(/*String code,*/ boolean enable) {
        mMDMController.enableWifiAP(enable);
    }

  /*  public static void isWifiAPEnabled(String code) {
        mTheTang.feedBackAll( code, mMDMController.isWifiAPEnabled() );
    }*/

    public static boolean isWifiAPEnabled() {
        return mMDMController.isWifiAPEnabled();
    }

   /* public static void queryMacFromWifiAPList(String code, String deviceMac) {
        mTheTang.feedBackAll( code, mMDMController.queryMacFromWifiAPList( deviceMac ) );
    }

    public static void addMacToWifiAPList(String code, String deviceMac) {
        mTheTang.feedBackAll( code, mMDMController.addMacToWifiAPList( deviceMac ) );
    }

    public static void deleteMacFromWifiAPList(String code, String deviceMac) {
        mTheTang.feedBackAll( code, mMDMController.deleteMacFromWifiAPList( deviceMac ) );
    }*/

    //WifiConfiguration配置
   /* public static void setWifiApOpened(String code, WifiConfiguration wifiConfig, boolean opened) {
        mTheTang.feedBackAll( code, mMDMController.setWifiApOpened( wifiConfig, opened ) );
    }

    //getWifiApState方法的返回值为Int类型
    public static void getWifiApState(String code) {
        mTheTang.feedBack( code, mMDMController.getWifiApState() + "" );
    }

    public static void isWifiAPOpened(String code) {
        mTheTang.feedBackAll( code, mMDMController.isWifiAPOpened() );
    }*/

    public static void openDataConnectivity(boolean isOpen) {
        mMDMController.openDataConnectivity(isOpen);
    }

    public static boolean isDataConnectivityOpen() {
        return mMDMController.isDataConnectivityOpen();
    }

    //WifiConfiguration
    public static void enableUsb(boolean enable) {
        mMDMController.enableUsb(enable);
    }

   /* public static void enableUsb(String code, boolean enable) {
        mTheTang.feedBackAll( code, mMDMController.enableUsb( enable ) );
    }*/

    public static boolean isUsbEnabled() {
        return mMDMController.isUsbEnabled();
    }

    public static void enableCamera(boolean enable) {
        mMDMController.enableCamera(enable);
    }

    public static void enableCamera(String code, boolean enable) {
        // mTheTang.feedBackAll( code, mMDMController.enableCamera( enable ) );
    }

    public static boolean isCameraEnabled() {
        return mMDMController.isCameraEnabled();
    }

    public static void enableSoundRecording(boolean enable) {
        mMDMController.enableSoundRecording(enable);
    }

    /*public static void enableSoundRecording(String code, boolean enable) {
        mTheTang.feedBackAll( code, mMDMController.enableSoundRecording( enable ) );
    }*/

    public static boolean isSoundRecordingEnabled(/*String code*/) {
        return mMDMController.isSoundRecordingEnabled();
    }

    public static void enableNfc(String code) {
        mMDMController.enableNfc();
    }

    public static void disableNfc(String code) {
        mMDMController.disableNfc();
    }

    public static boolean enableScreenShot() {
        return mMDMController.enableScreenShot();
    }

    public static boolean disableScreenShot() {
        return mMDMController.disableScreenShot();
    }

    public static boolean isScreenShotEnabled() {
        return mMDMController.isScreenShotEnabled();
    }

    public static boolean enableDropdown() {
        return mMDMController.enableDropdown();
    }

    public static boolean disableDropdown() {
        return mMDMController.disableDropdown();
    }

    public static boolean isDropdownEnabled() {
        return mMDMController.isDropdownEnabled();
    }

    public static void enableReset() {
        mMDMController.enableReset();
    }

    public static void disableReset() {
        mMDMController.disableReset();
    }

    public static boolean isResetEnabled() {
        return mMDMController.isResetEnabled();
    }

    public static void enableModifySystemtime() {
         mMDMController.enableModifySystemTime();
    }

    public static void disableModifySystemtime() {
         mMDMController.disableModifySystemTime();
    }

    public static boolean isModifySystemtimeEnabled() {
        return mMDMController.isModifySystemTimeEnabled();
    }

    public static boolean isNfcEnabled(String code) {
        return mMDMController.isNfcEnabled();
    }

    /*public static void openNfc(String code) {
        mTheTang.feedBackAll( code, mMDMController.openNfc() );
    }

    public static void closeNfc(String code) {
        mTheTang.feedBackAll( code, mMDMController.closeNfc() );
    }*/

    public static void enableSD(/*String code*/) {
        mMDMController.enableSD();
    }

    public static void disableSD(/*String code*/) {
        mMDMController.disableSD();
    }

    public static boolean createApn(/*String code,*/ ContentValues values) {
        return mMDMController.createApn(values);
    }

    public static void getApnList(String code) {
    }

    /*public static void getApn(String code, int id) {
        mTheTang.feedBack( code, DataParseUtil.parseAPNContentValues( mMDMController.getApn( id ) ) );
    }

    public static void getCurrentApn(String code) {
        mTheTang.feedBack( code, mMDMController.getCurrentApn() );
    }

    public static void setCurrentApn(String code, int id) {
        mTheTang.feedBackAll( code, mMDMController.setCurrentApn( id ) );
    }*/

    public static void deleteApn(String code, String name) {
        mMDMController.deleteApn(name);
    }

    public static void enableLocationService(String code, boolean enable) {
        gpsLocationReceiver = new MDM.GpsLocationReceiver(code);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.location.PROVIDERS_CHANGED");
        mContext.registerReceiver(gpsLocationReceiver, filter);
        LogUtil.writeToFile(TAG, "start to enable Location Service!");
        mMDMController.enableLocationService(enable);
    }

    public static void enableLocationService(boolean enable) {
        mMDMController.enableLocationService(enable);
    }

    public static boolean isLocationServiceEnabled() {
        return mMDMController.isLocationServiceEnabled();
    }

    public static void openGps(boolean isSetOpen) {
        if (mMDMController instanceof XiaomiMDMController) {
            mMDMController.openGps(isSetOpen);
        }
    }

    /*public static void openGpsOnBGSlient(String code) {
        mTheTang.feedBackAll( code, mMDMController.openGpsOnBGSlient() );
    }

    public static void isGpsOpenedOnBGSlient(String code) {
        mTheTang.feedBackAll( code, mMDMController.isGpsOpenedOnBGSlient() );
    }*/

    /**
     * 获取定位数据
     *
     * @param code
     */
    public static void getLocationData(String code) {
        Log.w(TAG, "getLocationData!");
        //需先强制关闭forceLocationService，然后打开能实现强制定位功能
        closeForceLocation();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        forceLocationService();

//        mTheTang.getLocation();
        startLocationService(mContext);


    }

    /**
     * 获取定位
     */
    public static void startLocationService(Context context) {//todo baii util ???
        Intent service_intent = new Intent(context, LocationService.class);
        context.startService(service_intent);
    }

    /**
     * 强制定位服务
     */
    public static void forceLocationService() {
        mMDMController.forceLocationService(true);
    }

    /**
     * 取消强制定位服务
     */
    public static void closeForceLocation() {
        mMDMController.forceLocationService(false);
    }

    /**********************************************
     * 系统安全管理
     ************************************/
    //设置锁屏
    public static void setScreenLock(String pwd) {
        /*int result = 0;
        try {
            result = mMDMController.setPassword( pwd );
            PasswordImpl passwordImpl = new PasswordImpl( mContext );
            passwordImpl.feedbackPassword( pwd );
        } catch (Exception e) {

        }
        //密码的起效，在灭屏之后
        if (result == 0) {
            LogUtil.writeToFile( TAG, "start to set screen lock!" );
            mMDMController.setScreenLock();
        }*/
        mMDMController.setPassword(pwd);
    }

    //判断是否锁屏
   /* public static void isScreenOn(String code) {
        PowerManager pm = (PowerManager) mContext.getSystemService( Context.POWER_SERVICE );
        LogUtil.writeToFile( TAG, "feedback screen lock result!" );
        mTheTang.feedBackAll( code, !pm.isScreenOn() );
    }*/

    //强制锁屏
    public static void forceLockScreen(String lockType, String pwd) {

        mTheTang.storyLockType(lockType);

        if (TextUtils.isEmpty(pwd)) {
            pwd = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        }

//        setPasswordNone();

        setScreenLock(pwd);

/*        PasswordImpl passwordImpl = new PasswordImpl(mContext);
        passwordImpl.feedbackPassword(pwd);*/
        //todo impl bai 88888888888888888888888
        feedbackPassword(pwd);
    }

    private static void feedbackPassword(String password) {
        JSONObject json = new JSONObject();
        try {
            json.put( "alias", PreferencesManager.getSingleInstance().getData( "alias"));
            json.put( "password", password );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MessageSendData data = new MessageSendData(Common.password_impl, json.toString(), true);
        SendMessageManager manager = new SendMessageManager(mContext);
        manager.sendMessage(data);
    }

    //解锁，清除密码
    public static void releaseLockScreen() {
        setPasswordNone();
        Log.w(TAG, "屏幕解锁状态==" + mMDMController.setPasswordNone());
    }

    //设置虚拟Recent按键是否可见(多任务见)
    public static void setRecentKeyVisible(boolean key) {
        mMDMController.setRecentKeyVisible(key);
    }

    public static void setHomeKeyVisible(boolean key) {
        mMDMController.setHomeKeyVisible(key);
    }

    public static void enableFingerNavigation(boolean enable) {
        mMDMController.enableFingerNavigation(enable);
    }

    public static void setKeyVisible(boolean key) {
        mMDMController.setKeyVisible(key);
    }

    /*public static void setPassword(String code, String pwd) {
        mTheTang.feedBack( code, mMDMController.setPassword( pwd ) + "" );
    }*/

    public static void takeScreenShot() {
        mMDMController.takeScreenShot();
    }

    /**
     * 关机
     */
    public static void setShutDown() {
        mMDMController.setShutDown();
    }

    /**
     * 重启
     *
     * @param orderCode
     */
    public static void setReboot(String orderCode) {
        mMDMController.setReboot();
    }

    /**
     * 恢复出厂设置
     */
    public static void setFactoryReset() {
        enableSwitching();//将域切换设置为允许
        ExcuteLimitPolicy.limitDefaultPolicy(); //先将系统恢复到默认状态再恢复出厂设置
        //modify by duanxin for bug189 on 2017.09.22
        mMDMController.setFactoryReset(true);
    }

    /**
     * 根据进程名杀掉进程
     *
     * @param processName
     */
    public static void killProcess(String processName) {
        mMDMController.killProcess(processName);
    }

    /**
     * 用户数据擦除
     */
    public static void wipeData() {
        //modify by duanxin for bug200 on 2017.09.22
        setFactoryReset();
    }

    /***********************************************
     * 电话服务管理
     ************************************/

    public static void enableSms(boolean enable) {
        mMDMController.enableSms(enable);
    }

    public static boolean isSmsEnabled() {
        return mMDMController.isSmsEnabled();
    }

    public static void enableTelePhone(boolean enable) {
        //mTheTang.feedBackAll( code, mMDMController.enableTelephone( enable ) );
        mMDMController.enableTelephone(enable);
    }

    public static boolean isTelephoneEnabled() {
        //mTheTang.feedBackAll( code, mMDMController.isTelephoneEnabled() );
        return mMDMController.isTelephoneEnabled();
    }

   /* public static void getAllContactInfo(String code) {
        /*
        ContactInfo返回值的数据格式：
        buf.append("id=" + id);
        如果通过id查询数据库：Uri.parse("content://com.android.contacts/contacts/" + id + "/data");
        如果有，则添加
            buf.append(",name=" + data);
            buf.append(",phone=" + data);
            buf.append(",email=" + data);
            buf.append(",address=" + data);
            buf.append(",organization=" + data);
        结束
        buf.append("\n");
         */
    //    String allContactInfo = mMDMController.getAllContactInfo();
    //    mTheTang.feedBack( code, DataParseUtil.parseContactInfo( allContactInfo ) );
    //}

    /**********************************************
     * 查询硬件及系统信息
     ******************************/

    public static List<String> getDeviceInfo() {
        return mMDMController.getDeviceInfo();
    }

    /***********************************
     * 电话白名单
     *************************************************/
    //添加白名单
    public static void addTelephonyWhiteList(List<TelephoyWhiteUser> listUser) {

        //insert telephony white data
        for (TelephoyWhiteUser mTelephoyWhiteUser : listUser) {
            mMDMController.insertContact(mTelephoyWhiteUser.getUserName(), mTelephoyWhiteUser.getTelephonyNumber());
        }

        DatabaseOperate.getSingleInstance().addTelephonyWhiteList(listUser);

        EventBus.getDefault().post(new WhiteListEvent());

    }

    //判断白名单是否添加成功，并反馈消息
    private static void isAddSuccess(List<TelephoyWhiteUser> listUser, String id) {
        for (TelephoyWhiteUser user : listUser) {
            if (!isTelephonyWhiteList(user.getUserId())) {
                LogUtil.writeToFile(TAG, "false add Telephony Number = " + user.getTelephonyNumber());
                //EventBus.getDefault().post( new CompleteEvent( String.valueOf( OrderConfig.AddTelephonyWhiteList ), "false", id ) );
                break;
            } else {
                EventBus.getDefault().post(new WhiteListEvent());
                LogUtil.writeToFile(TAG, "success add " + user.getTelephonyNumber());
                //EventBus.getDefault().post( new CompleteEvent( String.valueOf( OrderConfig.AddTelephonyWhiteList ), "true", id ) );
            }
        }
    }

    public static boolean isTelephonyWhiteList(String number) {
        return DatabaseOperate.getSingleInstance().isTelephonyWhite(number);
    }

   /* public static void isTelephonyWhiteListByNumber(String code, String number) {
        mTheTang.feedBackAll( code, DatabaseOperate.getSingleInstance().isTelephonyWhite( number ) );
    }*/

    /**
     * 查询白名单
     *
     * @param code
     */
   /* public static void queryTelephonyWhiteList(String code) {
        mTheTang.feedBack( code, DataParseUtil.parseWhiteListToString(
                DatabaseOperate.getSingleInstance().queryTelephonyWhite() ) );
    }*/

    /**
     * 删除白名单
     *
     * @param listUser
     */
    public static void deleteTelephonyWhiteList(List<TelephoyWhiteUser> listUser, String id) {
        DatabaseOperate.getSingleInstance().deleteTelephonyWhiteList(listUser);
        //delete telephony white data
        for (TelephoyWhiteUser mTelephoyWhiteUser : listUser) {
            mMDMController.deleteContact(mTelephoyWhiteUser.getUserName(), mTelephoyWhiteUser.getTelephonyNumber());
        }
    }

    /**
     * 判断白名单是否删除成功，并反馈消息
     *
     * @param listUser
     */
    private static void isDeleteSuccess(List<TelephoyWhiteUser> listUser, String id) {

        for (TelephoyWhiteUser user : listUser) {
            if (isTelephonyWhiteList(user.getUserId())) {
                LogUtil.writeToFile(TAG, "false delete " + user.getUserId());
                //EventBus.getDefault().post( new CompleteEvent( String.valueOf( OrderConfig.DeleteTelephonyWhiteList ), "false", id ) );
                break;
            } else {
                EventBus.getDefault().post(new WhiteListEvent());
                LogUtil.writeToFile(TAG, "success delete " + user.getUserId());
                //EventBus.getDefault().post( new CompleteEvent( String.valueOf( OrderConfig.DeleteTelephonyWhiteList ), "true", id ) );
            }
        }
    }

    /**
     * 更新电话白名单
     */
    public static void updateTelepfohonyWhiteList() {
        TelephoneWhiteListImpl mTelephoneWhiteListImpl = new TelephoneWhiteListImpl(mContext);
        mTelephoneWhiteListImpl.getTelephoneWhiteList();
    }

    /**
     * 文件下载处理
     *
     * @param fileList
     */
    public static void downloadFile(List<DownLoadEntity> fileList) {

        if (fileList.isEmpty()) {
            return;
        }

        for (int i = 0; i < fileList.size(); i++) {
            //如果为文件或图片下载，跳过app版本检测
            if ("1".equals(fileList.get(i).type) || "2".equals(fileList.get(i).type)) {
                continue;
            }

            //判断是否已安装相同或低版本的应用
            String version = judgmentAppHadInstall(fileList.get(i).packageName);

            if (version != null) {

                if (!isAppNewVersion(version, fileList.get(i).version)) {

                    mTheTang.showToastByRunnable(mContext, fileList.get(i).packageName + "："
                            + "该应用已安装高版本！", Toast.LENGTH_SHORT);

                    LogUtil.writeToFile(TAG, "packageName = " + fileList.get(i).packageName + "," + " version = " + fileList.get(i).version);

                    storageAppInfo(fileList.get(i));

                    if (!Common.packageName.equals(fileList.get(i).packageName)) {
                        //允许上网
                        if ((fileList.get(i).internet).equals("0")) {
                            forbiddenAppNetwork(fileList.get(i).packageName);
                        } else {
                            cancelForbiddenAppNetwork(fileList.get(i).packageName);
                        }

                        //添加到防卸载白名单
                        if ((fileList.get(i).uninstall).equals("0")) {
                            addAppTONoUninstallList(fileList.get(i).packageName);
                        } else {
                            deleteAppFromUninstallList(fileList.get(i).packageName);
                        }
                    }

                    DatabaseOperate.getSingleInstance().updateCompleteResultAndTime(fileList.get(i).code,
                            "true", String.valueOf(System.currentTimeMillis()), fileList.get(i).sendId);
                    EventBus.getDefault().post(new CompleteEvent(fileList.get(i).code, "true", fileList.get(i).sendId));
                    fileList.remove(i);
                } else {
                    deleteAppFromUninstallList(fileList.get(i).packageName);
                }
            }
        }
        DownLoadManager.getInstance().download(fileList);
    }


    public static boolean isAppNewVersion(String localVersion, String onlineVersion) {

        if (localVersion.equals(onlineVersion)) {
            return false;
        }

        String[] localArray = localVersion.split("\\.");
        String[] onlineArray = onlineVersion.split("\\.");

        int localLength = localArray.length;
        int onlineLength = onlineArray.length;

        for (int i = 0; i < (localLength < onlineLength ? localLength : onlineLength); i++) {

            if (Integer.parseInt(onlineArray[i]) > Integer.parseInt(localArray[i])) {
                return true;
            } else if (Integer.parseInt(onlineArray[i]) < Integer.parseInt(localArray[i])) {
                return false;
            }
            // 相等 比较下一组值
        }

        if (localLength > onlineLength) {
            return false;
        }

        return true;
    }

    /**
     * 当下发的应用已经安装（版本大于下发版本），需要将该应用纳入应用商店管理
     *
     * @param downLoadEntity
     */
    private static void storageAppInfo(DownLoadEntity downLoadEntity) {
        //如果该app已经存在或为EMM，不存入数据库
        if (Common.packageName.equals(downLoadEntity.packageName)) {
            return;
        }

        APPInfo installAPPInfo = new APPInfo();
        List<APPInfo> installAPPInfoLists = new ArrayList<>();
        installAPPInfo.setAppId(downLoadEntity.app_id);

        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(downLoadEntity.packageName, 0);

            installAPPInfo.setAppName((String) packageManager.getApplicationLabel(packageInfo.applicationInfo));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }

        installAPPInfo.setPackageName(downLoadEntity.packageName);

        installAPPInfoLists.add(installAPPInfo);
        DatabaseOperate.getSingleInstance().addInstallAppInfo(installAPPInfoLists);

        mTheTang.addMessage(OrderConfig.SilentInstallAppication + "",
                (String) packageManager.getApplicationLabel(packageInfo.applicationInfo) + "--"
                        + mTheTang.getNetworkInfo(downLoadEntity.internet) + ","
                        + mTheTang.getUninstallInfo(downLoadEntity.uninstall));

        EventBus.getDefault().post(new NotifyEvent());
    }

    /**
     * 判断该应用是否安装
     *
     * @param packageName
     * @return
     */
    public static String judgmentAppHadInstall(String packageName) {
        PackageManager packageManager = mContext.getPackageManager();
        String version = null;
        try {
            PackageInfo info = packageManager.getPackageInfo(packageName, 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 删除文件
     */
    public static void deleteIssuedFile(List<DownLoadEntity> fileList) {
        if (fileList.isEmpty()) {
            return;
        }

        for (DownLoadEntity downLoadEntity : fileList) {

            String fileName = DatabaseOperate.getSingleInstance().queryFile(downLoadEntity);
            File file = new File(BaseApplication.baseFilesPath + File.separator + fileName);
            if (file.exists()) {
                file.delete();
            }

            //added by duanxin for bug227 on 2017/09/29
            if (fileName == null) {
                fileName = "该文件不存在！";
            }

            mTheTang.addMessage(String.valueOf(OrderConfig.DeleteIssuedFile), fileName);
            DatabaseOperate.getSingleInstance().deleteFile(downLoadEntity);
        }
        EventBus.getDefault().post(new NotifyEvent());
    }


    /**
     * 静默卸载处理
     *
     * @param appList
     */
    public static void silentUninstallApp(List<DownLoadEntity> appList) {
        for (DownLoadEntity downLoadEntity : appList) {
            APKEvent apkEvent = new APKEvent(downLoadEntity, OrderConfig.SilentUninstallAppication);
            AppTask appTask = new AppTask();
            appTask.onSilentExcutor(apkEvent);
        }
    }

    /**
     * 清除密码
     */
    public static void setPasswordNone() {
        boolean result = mMDMController.setPasswordNone();

        //解锁
/*        PasswordImpl passwordImpl = new PasswordImpl(mContext);
        passwordImpl.feedbackPassword("");*/
        //todo impl bai 88888888888888888888888
        feedbackPassword("");
        LogUtil.writeToFile(TAG, " set Password None " + result);
    }
    /***********************************应用静默安装／卸载***************************************/

    /**
     * 播放铃声
     */
    public static void playRingtones() {
        //判断设备是否处于安全区域，否则跳转到安全区域
        if (!mMDMController.isInFgContainer()) {
            mMDMController.switchContainer();
            mTheTang.getThreadPoolObject().submit(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (mMDMController.isInFgContainer()) {
                            break;
                        }
                    }
                    playMusic();
                }
            });
        } else {
            playMusic();
        }
    }

    /**
     * 播放10秒歌曲
     */

    static MediaPlayer mediaPlayer = null;

    public static void playMusic() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(mContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
            mediaPlayer.prepare();
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }

            mTheTang.getThreadPoolObject().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(10000);
                        releasePlayer(mediaPlayer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        releasePlayer(mediaPlayer);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            releasePlayer(mediaPlayer);
        }
    }

    public static void releasePlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();

            //关键语句
            mediaPlayer.reset();

            mediaPlayer.release();

            //mediaPlayer = null;
        }
    }

    /**
     * 显示系统Dialog
     *
     * @param mediaPlayer
     */
    private static void showDialog(final MediaPlayer mediaPlayer) {

        final WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        Display display = windowManager.getDefaultDisplay(); // 获取屏幕宽、高

        layoutParams.height = (int) (display.getHeight() * 0.25);
        layoutParams.width = (int) (display.getWidth() * 0.95);
        layoutParams.format = 1;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_BLUR_BEHIND/*| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN*/;
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;

    }

    /**
     * 应用黑名单
     *
     * @param appBlackWhiteData
     */
    public static void appBlackWhiteList(AppBlackWhiteData appBlackWhiteData) {

        //删除黑白名单
        if (appBlackWhiteData == null) {

            String app_compliance_name = preferencesManager.getComplianceData(Common.app_compliance_name);


            if (app_compliance_name == null) {
                return;
            }

            if ("0".equals(preferencesManager.getOtherData(Common.appManagerType))) {
                mTheTang.addMessage(OrderConfig.delete_black_list + "", app_compliance_name);

                mTheTang.deleteStrategeInfo(OrderConfig.send_black_list + "");

            } else if ("1".equals(preferencesManager.getOtherData(Common.appManagerType))) {
                mTheTang.addMessage(OrderConfig.delete_White_list + "", app_compliance_name);

                mTheTang.deleteStrategeInfo(OrderConfig.send_White_list + "");
            }

            ExcuteCompliance.deleteAppCompliance();

            return;
        }

        //删除应用违规
        boolean app = DatabaseOperate.getSingleInstance().queryStrageInfo(OrderConfig.send_app_strategy + "");

        if (app) {
            DatabaseOperate.getSingleInstance().deleteSimpleStrategeInfo(OrderConfig.send_app_strategy + "");
        }

        mTheTang.storageBlackWhiteList(appBlackWhiteData);

        if ("0".equals(appBlackWhiteData.type)) {
            excuteBlackList(appBlackWhiteData.appList);
            mTheTang.addMessage(OrderConfig.send_black_list + "", appBlackWhiteData.name);
            mTheTang.deleteStrategeInfo(OrderConfig.send_White_list + "");
            mTheTang.addStratege(OrderConfig.send_black_list + "", appBlackWhiteData.name, System.currentTimeMillis() + "");
        } else {
            excuteWhiteList(appBlackWhiteData.appList);

            mTheTang.addMessage(OrderConfig.send_White_list + "", appBlackWhiteData.name);
            mTheTang.deleteStrategeInfo(OrderConfig.send_black_list + "");
            mTheTang.addStratege(OrderConfig.send_White_list + "", appBlackWhiteData.name, System.currentTimeMillis() + "");
        }

    }

    //用于黑白名单删除应用
    static Handler appHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };

    /**
     * 执行黑名单
     *
     * @param app_list
     */
    private static void excuteBlackList(List<String> app_list) {
        //将EMM与商城下发的应用清除出黑名单
        app_list.remove(mTheTang.getContext().getPackageName());

        List<APPInfo> appInfos = DatabaseOperate.getSingleInstance().queryInstallAppInfo();

        if (appInfos != null) {
            for (APPInfo appInfo : appInfos) {
                app_list.remove(appInfo.getPackageName());
            }
        }

        List<PackageInfo> packageInfos = AppUtils.getNoSystemApp(mContext);
        for (PackageInfo packageInfo : packageInfos) {
            if (app_list.contains(packageInfo.packageName)) {
                mMDMController.uninstallApplication(packageInfo.packageName);
            }
        }
    }

    /**
     * 执行白名单
     *
     * @param app_list
     */
    private static void excuteWhiteList(List<String> app_list) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //将EMM与商城下发的应用添加到应用白名单
                app_list.add(mTheTang.getContext().getPackageName());
                app_list.add("com.xiaomi.emm");

                List<APPInfo> appInfos = DatabaseOperate.getSingleInstance().queryInstallAppInfo();

                if (appInfos != null) {
                    for (APPInfo appInfo : appInfos) {
                        app_list.add(appInfo.getPackageName());
                    }
                }

                List<PackageInfo> packageInfos = AppUtils.getNoSystemApp(mContext);

                for (PackageInfo packageInfo : packageInfos) {
//                    Log.d("baii", "package " + packageInfo.packageName);
                    if (!app_list.contains(packageInfo.packageName)) {
//                        Log.d("baii", "uninstall " + packageInfo.packageName);
                        mMDMController.uninstallApplication(packageInfo.packageName);
                    }
                }
            }
        }).start();

    }

    /**
     * 执行机卡绑定
     *
     * @param isCancel
     */
    public static void excuteMachineCard(boolean isCancel) {
        //用于停止上次运行的service
        if (isCancel) {
            mContext.stopService(new Intent(mContext, MachineCardBindingService.class));
            return;
        }

        machineCard(preferencesManager);
        mTheTang.startService(new Intent(mContext, MachineCardBindingService.class));
    }

    public static void machineCard(PreferencesManager preferencesManager) {

        //绑定一张卡
        /*String card_iccid = preferencesManager.getComplianceData(Common.iccid_card);

        if (TextUtils.isEmpty(card_iccid)) {
            TelephonyManager telephonyManager = (TelephonyManager) TheTang.getSingleInstance().getContext().getSystemService(Service.TELEPHONY_SERVICE);
            if (telephonyManager.getSubscriberId() != null) {
                preferencesManager.setComplianceData(Common.iccid_card, telephonyManager.getSimSerialNumber());
            }
            return;
        }

        String iccid = mTheTang.getIccid();

        String iccid1 = mTheTang.getIccid1();

        if (!TextUtils.isEmpty(iccid) && TextUtils.isEmpty(iccid1)) {


            if (!iccid.equals(card_iccid)) {
                //违规
                sendMachineCardCompliance();
            } else {
                //合规
                sendMachinCardUnCompliance();
            }
        } else if (TextUtils.isEmpty(iccid) && !TextUtils.isEmpty(iccid1)) {

            if (!iccid1.equals(card_iccid)) {
                sendMachineCardCompliance();
            } else {
                sendMachinCardUnCompliance();
            }

        } else if (!TextUtils.isEmpty(iccid) && !TextUtils.isEmpty(iccid1)) {
            sendMachineCardCompliance();
        } else if (TextUtils.isEmpty(iccid) && TextUtils.isEmpty(iccid1)) {
            sendMachineCardCompliance();
        }*/

        //绑定两张卡
        String iccid_card = preferencesManager.getComplianceData(Common.iccid_card);
        String iccid_card1 = preferencesManager.getComplianceData(Common.iccid_card1);

        //卡槽一与卡槽二
        String iccid = PhoneUtils.getIccid(mContext, 0);
        String iccid1 = PhoneUtils.getIccid(mContext, 1);

        //卡槽一有卡，卡槽二无卡
        if (!TextUtils.isEmpty(iccid) && TextUtils.isEmpty(iccid1)) {

            //如果card_iccid无数据，则添加
            if (TextUtils.isEmpty(iccid_card)) {
                preferencesManager.setComplianceData(Common.iccid_card, iccid);
                return;
            }

            //card_iccid1数据为空
            if (TextUtils.isEmpty(iccid_card1)) {
                if (!iccid.equals(iccid_card)) {
                    //违规
                    //sendMachineCardCompliance();
                    preferencesManager.setComplianceData(Common.iccid_card1, iccid);
                }
            } else {
                if (!iccid.equals(iccid_card) && !iccid.equals(iccid_card1)) {
                    //违规
                    sendMachineCardCompliance();
                }
            }

            sendMachinCardUnCompliance();
        //卡槽一无卡，卡槽二有卡
        } else if (TextUtils.isEmpty(iccid) && !TextUtils.isEmpty(iccid1)) {

            if (TextUtils.isEmpty(iccid_card1)) {
                preferencesManager.setComplianceData(Common.iccid_card1, iccid1);
                return;
            }

            //card_iccid数据为空
            if (TextUtils.isEmpty(iccid_card)) {
                if (!iccid1.equals(iccid_card1)) {
                    //违规
                    //sendMachineCardCompliance();
                    preferencesManager.setComplianceData(Common.iccid_card, iccid1);
                }
            } else {
                if (!iccid1.equals(iccid_card) && !iccid1.equals(iccid_card1)) {
                    //违规
                    sendMachineCardCompliance();
                }
            }

            sendMachinCardUnCompliance();
        //卡槽一有卡，卡槽二有卡
        } else if (!TextUtils.isEmpty(iccid) && !TextUtils.isEmpty(iccid1)) {

            //card_iccid与card_iccid1为空
            if (TextUtils.isEmpty(iccid_card) && TextUtils.isEmpty(iccid_card1)) {

                preferencesManager.setComplianceData(Common.iccid_card, iccid);
                preferencesManager.setComplianceData(Common.iccid_card1, iccid1);
                return;

            } else if (TextUtils.isEmpty(iccid_card) && !TextUtils.isEmpty(iccid_card1)) {

                if (iccid_card1.equals(iccid)) {
                    preferencesManager.setComplianceData(Common.iccid_card, iccid1);
                } else if (iccid_card1.equals(iccid1)) {
                    preferencesManager.setComplianceData(Common.iccid_card, iccid);
                } else {
                    //如果两个都不符合，保存卡槽一，并上传违规
                    preferencesManager.setComplianceData(Common.iccid_card, iccid);
                    sendMachineCardCompliance();
                }

            } else if (!TextUtils.isEmpty(iccid_card) && TextUtils.isEmpty(iccid_card1)) {

                if (iccid_card.equals(iccid)) {
                    preferencesManager.setComplianceData(Common.iccid_card1, iccid1);
                } else if (iccid_card.equals(iccid1)) {
                    preferencesManager.setComplianceData(Common.iccid_card1, iccid);
                } else {
                    //如果两个都不符合，保存卡槽一，并上传违规
                    preferencesManager.setComplianceData(Common.iccid_card1, iccid);
                    sendMachineCardCompliance();
                }

            } else if (!TextUtils.isEmpty(iccid_card) && !TextUtils.isEmpty(iccid_card1)) {

                if ((iccid_card.equals(iccid) && iccid_card.equals(iccid)) || (iccid_card.equals(iccid1) && iccid_card1.equals(iccid))) {
                    sendMachinCardUnCompliance();
                } else {
                    sendMachineCardCompliance();
                }

            }

            sendMachinCardUnCompliance();
        //卡槽一无卡，卡槽二无卡
        } else if (TextUtils.isEmpty(iccid) && TextUtils.isEmpty(iccid1)) {
            //如果有存储数据，则表示拔卡，违规
            if (!TextUtils.isEmpty(iccid_card) || !TextUtils.isEmpty(iccid_card1)) {
                sendMachineCardCompliance();
            }
        }
    }

    /**
     * 机卡绑定违规
     */
    private static void sendMachineCardCompliance() {
/*        SystemImpl systemImpl_ready = new SystemImpl(mContext);
        systemImpl_ready.sendSystemCompliance(Common.machine_card, "0", "1");*/
//todo baii impl bbbbbbbbbbbbbbbbb
        LogUtil.writeToFile( TAG, "systemCompliance!" );
        MDM.excuteSystemCompliance();
        sendMachineCardComplianceInfo("0", "1");
    }

    /**
     * 机卡绑定合规
     */
    private static void sendMachinCardUnCompliance() {
/*        SystemImpl systemImpl_ready = new SystemImpl(mContext);
        systemImpl_ready.sendSystemCompliance(Common.machine_card, "1", "1");*/
        //todo baii impl bbbbbbbbbbbbbbbbb
        sendMachineCardComplianceInfo("1", "1");

    }

    private static void sendMachineCardComplianceInfo(String type, String state) {
        String alias = PreferencesManager.getSingleInstance().getData( Common.alias);
        String systemComplianceId = PreferencesManager.getSingleInstance().getComplianceData(Common.system_compliance_id);
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("alias", alias);
            jsonObject.put("systemComplianceId", systemComplianceId);
            jsonObject.put("state", state);
            jsonObject.put("type", type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MessageSendData data = new MessageSendData(Common.machine_card, jsonObject.toString(), true);
        SendMessageManager manager = new SendMessageManager(mContext);
        manager.sendMessage(data);
    }

    /**
     * 执行系统违规
     */
    public static void excuteSystemCompliance() {
        String pwd = PreferencesManager.getSingleInstance().getComplianceData(Common.system_compliance_pwd);
        if (TextUtils.isEmpty(pwd)) {
            setFactoryReset();
        } else {
            forceLockScreen(Common.lockTypes[2], pwd);
        }
    }

    /**
     * 通过NetworkManager的setFirewallEnabled方法禁止手机上网，需要系统权限
     */
    public static void forbiddenNetWork(boolean enable) {
        //设置系统防火墙
        Method method = null;
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            if (null != method) {
                method.setAccessible(true);
                IBinder binder = (IBinder) method.invoke(null, new Object[]{"network_management"});
                if (binder != null) {
                    INetworkManagementService networkManagementService = INetworkManagementService.Stub.asInterface(binder);
                    networkManagementService.setFirewallEnabled(enable);
                }
            }
        } catch (Exception e) {
            String str = e.getCause().toString();
            LogUtil.writeToFile(TAG, str);
        }
    }

    /**
     * 禁止数据流量，需要系统权限MODIFY_PHONE_STATE
     *
     * @param enable
     */
    public static void enableMobileData(boolean enable) {
        //设置系统防火墙
        Method method = null;
        try {
            TelephonyManager mTelephonyManager = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
            method = mTelephonyManager.getClass().getMethod("setDataEnabled", boolean.class);
            if (null != method) {
                method.setAccessible(true);
                method.invoke(mTelephonyManager, enable);
            }
        } catch (Exception e) {
            String str = e.getCause().toString();
            LogUtil.writeToFile(TAG, str);
        }
    }

    /**
     * 设置app是否可以使用网络
     *
     * @param aPkgName
     * @param aAllow
     * @return
     */
    public static boolean setAppAccessNetwork(String aPkgName, boolean aAllow) {

        try {
            //int pkgUid = mContext.getPackageManager().getPackageUid(aPkgName, mContext.getUserId());

            PackageManager pm = mContext.getPackageManager();

            ApplicationInfo applicationInfo = pm.getApplicationInfo(aPkgName, PackageManager.GET_META_DATA);

            int pkgUid = applicationInfo.uid;

            //Class ownerClass = mNetworkService.getClass();

           /* if (Build.VERSION.SDK_INT < ApiHelper.VERSION_CODES.M) {
//                mNetworkService.setPackageUidRule(pkgUid, aAllow);
                Method method = ownerClass.getDeclaredMethod("setPackageUidRule", new Class[]{int.class, boolean.class});
                method.setAccessible(true);
                method.invoke(mNetworkService, new Object[]{pkgUid, aAllow});
            } else {*/
//                mNetworkService.setFirewallUidRule(FIREWALL_CHAIN_NONE, pkgUid, aAllow ? FIREWALL_RULE_ALLOW : FIREWALL_RULE_DENY);
               /* Method method = ownerClass.getDeclaredMethod("setFirewallUidRule", new Class[]{int.class, int.class, int.class});
                method.setAccessible(true);
                method.invoke(mNetworkService, new Object[]{FIREWALL_CHAIN_NONE, pkgUid, aAllow ? FIREWALL_RULE_ALLOW : FIREWALL_RULE_DENY});*/
            //}
            return true;
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            return false;
        }
    }

    /**
     * 删除账户
     */
    public static void deleteAccount() {

        /*cleanDatabase();
        cleanSharePreference();
        cleanInternalCache();
        cleanExternalCache();
        cleanFiles();
        cleanCustomFiles();
        mTheTang.feedBack( code, "true" );
        loginOut();*/
        //修改为重置

        //mTheTang.feedBack( code, "true" );
        //mMDMController.setFactoryReset( true );//用户级恢复出厂设置，不清楚内置SD卡数据
        setFactoryReset();
    }

    /**
     * 清除数据库（/data/data/com.xxx.xxx/databases）
     */
    private static void cleanDatabase() {
        mContext.getFilesDir().getPath();
        deleteFilesByDirectory(new File(mContext.getDataDir().getPath() + "/databases"));
    }

    /**
     * 清除SharePreference（/data/data/com.xxx.xxx/shared_prefs）
     */
    private static void cleanSharePreference() {
        deleteFilesByDirectory(new File(mContext.getDataDir().getPath()  + "/shared_prefs"));
    }

    /**
     * 清除内部缓存（/data/data/com.xxx.xxx/cache）
     */
    private static void cleanInternalCache() {
        deleteFilesByDirectory(mContext.getCacheDir());
    }

    /**
     * 清除外部缓存（/mnt/sdcard/android/data/com.xxx.xxx/cache）
     */
    private static void cleanExternalCache() {
        deleteFilesByDirectory(mContext.getExternalCacheDir());
    }

    /**
     * 清除（/data/data/com.xxx.xxx/files）下文件
     */
    private static void cleanFiles() {
        deleteFilesByDirectory(mContext.getFilesDir());
    }

    /*********************删除用户*********************************/

    /**
     * 清除自定义文件
     */
    private static void cleanCustomFiles() {
        deleteFilesByDirectory(mContext.getExternalFilesDir(null));
    }

    /**
     * 退出应用
     */
    private static void loginOut() {
        Intent intent = new Intent();
        intent.setAction("login_out");
        mContext.sendBroadcast(intent);
    }

    /**
     * * 删除方法 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理 *
     *
     * @param directory
     */
    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            scanFile(directory);
        }
    }

    /**
     * 扫描并删除文件夹
     *
     * @param file
     */
    private static void scanFile(File file) {

        for (File subFile : file.listFiles()) {
            if (subFile.isDirectory()) {
                scanFile(subFile);
            } else {
                deleteFile(subFile);
            }
        }
    }

    /**
     * 删除文件
     *
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 获得所有设备信息
     */
    public static void sendAllDeviceInfo() {
/*        DeviceImpl deviceImpl = new DeviceImpl(mContext);
        deviceImpl.sendDeviceInfo(deviceUtil.getDeviceInfo());*/
//todo impl bai 444444444444
        MessageSendData data = new MessageSendData(Common.device_impl, JsonGenerateUtil.getDeviceInfoString(mContext), false);
        Log.d("baii", "json " + data.getJsonContent());
        SendMessageManager manager = new SendMessageManager(mContext);
        manager.sendMessage(data);
    }

    
    public boolean enableBluetoothOpp(boolean enable) {
        return false;
    }

    public boolean isBluetoothOppEnabled() {
        return false;
    }

    //WifiConfiguration
    public WifiConfiguration getWifiApConfiguration(String code) {
        return mMDMController.getWifiApConfiguration();
    }

    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
        return mMDMController.setWifiApConfiguration(wifiConfig);
    }

    public Uri getActualDefaultRingtoneUri(Context context, int type) {
        return null;
    }

    /*********************
     * 删除用户
     *********************************/

    public static class GpsLocationReceiver extends BroadcastReceiver {
        String code;

        public GpsLocationReceiver(String code) {
            this.code = code;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("android.location.PROVIDERS_CHANGED".equals(intent.getAction())) {

                if (code.equals(String.valueOf(OrderConfig.GetLocationData))) {
                    LogUtil.writeToFile(TAG, "Enable Location service success!");
//                    mTheTang.getLocation();
                    startLocationService(context);
                } else if (code.equals(String.valueOf(OrderConfig.EnableLocationService))) {
                    LogUtil.writeToFile(TAG, "Location service had Enabled!");
                }
            }
            mContext.unregisterReceiver(gpsLocationReceiver);
        }
    }


    /**
     * 添加应用防卸载
     *
     * @param packageName
     */
    public static void addAppTONoUninstallList(String packageName) {
        mMDMController.enableUninstallWhiteListFunction(true);
        mMDMController.addPkgNameToUninstallList(packageName);
    }

    /**
     * 从防卸载名单中去除
     *
     * @param packageName
     */
    public static void deleteAppFromUninstallList(String packageName) {
        if (mMDMController.queryPkgNameFromUninstallList(packageName)) {
            mMDMController.deletePkgNameFromUninstallList(packageName);
        }
    }

    /**
     * 上传异常log
     */
    public static void uploadDebugLog(ExceptionLogData exceptionLogData) {

        //判断是否在wifi环境下上传
        if ("1".equals(exceptionLogData.isWifiUpload)) {

            int state = PhoneUtils.getNetWorkState(mContext);

            if (state != 1) {
                preferencesManager.setLogData("logId", exceptionLogData.logId);
                preferencesManager.setLogData("isWifiUpload", exceptionLogData.isWifiUpload);
                preferencesManager.setLogData("date", exceptionLogData.date);
                return;
            }
        }

        uploadLog(exceptionLogData.logId, exceptionLogData.date);
    }

    /**
     * 上传Log
     */
    public static void uploadLog(String id, String date) {
/*        LogUploadImpl mLogUploadImpl = new LogUploadImpl(mContext);
        mLogUploadImpl.logUpload(id, date);*/
//todo impl bai 7777777777777777
        String version = android.os.Build.VERSION.RELEASE; //系统版本号
        String model = android.os.Build.MODEL; //系统型号
        String alias = PreferencesManager.getSingleInstance().getData(Common.alias); //alias

        File logFile = FileUtils.generateLogZip(TimeUtils.getDates(date, 7), BaseApplication.baseLogsPath + "/crash/crash");
        JSONObject json = new JSONObject();
        try {
            json.put("version", version);
            json.put("model", model);
            json.put("alias", alias);
            json.put("id", Integer.parseInt(id));
            json.put("filePath", logFile.getPath());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MessageSendData data = new MessageSendData(Common.log_upload_impl, json.toString(), false);
        SendMessageManager manager = new SendMessageManager(mContext);
        manager.setSendListener(new SendMessageManager.SendListener() {
            @Override
            public void onSuccess() {
                PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
                preferencesManager.removeLogData( "logId" );
                preferencesManager.removeLogData( "isWifiUpload" );
                preferencesManager.removeLogData( "date" );
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

    /**
     * 设备更新
     */
    public static void deviceUpdate(List<DownLoadEntity> list) {

        //mTheTang.showToastByRunnable( mContext, "EMM正在更新，请勿关机！", Toast.LENGTH_SHORT );
        mTheTang.addMessage(OrderConfig.device_update + "", "v" + list.get(0).version);
        downloadFile(list);
    }

    /**
     * 开启电话白名单
     */
    public static void startPhoneWhite() {
        preferencesManager.setOtherData(Common.white_phone, "true");
        if (!mMDMController.isCallWhiteListOpen()) {
            mMDMController.setCallWhiteList(true);
        }
        mTheTang.deleteStrategeInfo(OrderConfig.stop_phone_white + "");
        mTheTang.addStratege(OrderConfig.start_phone_white + "", null, System.currentTimeMillis() + "");

/*        WhiteTelephoneImpl mWhiteTelephoneImpl = new WhiteTelephoneImpl(mContext);
        mWhiteTelephoneImpl.sendWhiteTelephoneStatus("1");*/
        //todo baii impl dddddddddddddddddd
        sendPhoneWhiteListStatus("1");
    }

    /**
     * 关闭电话白名单
     */
    public static void stopPhoneWhite() {
        preferencesManager.setOtherData(Common.white_phone, "false");
        if (mMDMController.isCallWhiteListOpen()) {
            mMDMController.setCallWhiteList(false);
        }
        mTheTang.deleteStrategeInfo(OrderConfig.start_phone_white + "");
        mTheTang.addStratege(OrderConfig.stop_phone_white + "", null, System.currentTimeMillis() + "");

/*        WhiteTelephoneImpl mWhiteTelephoneImpl = new WhiteTelephoneImpl(mContext);
        mWhiteTelephoneImpl.sendWhiteTelephoneStatus("0");*/
        //todo baii impl dddddddddddddddddd
        sendPhoneWhiteListStatus("0");
    }

    private static void  sendPhoneWhiteListStatus(String status) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("status", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MessageSendData data = new MessageSendData(Common.white_telephone_status, jsonObject.toString(), true);
        SendMessageManager manager = new SendMessageManager(mContext);
        manager.sendMessage(data);
    }

    /**
     * 禁止某个应用上网
     *
     * @param packageName
     */
    public static void forbiddenAppNetwork(String packageName) {
        int uid = AppUtils.getAppUid(mContext, packageName);
        MDM.mMDMController.executeShellToSetIptables("-N " + uid);
        MDM.mMDMController.executeShellToSetIptables("-A OUTPUT -m owner --uid-owner " + uid + " -j DROP");
    }

    /**
     * 取消App上网禁止
     *
     * @param packageName
     */
    public static void cancelForbiddenAppNetwork(String packageName) {
        int uid = AppUtils.getAppUid(mContext, packageName);
        MDM.mMDMController.executeShellToSetIptables(" -D OUTPUT -m owner --uid-owner " + uid + " -j DROP ");
    }

    /**
     * 浏览器白名单
     */
    public static void excuteIptables(String packageName, List<String> hosts) {

        if (hosts == null || hosts.size() == 0) {
            return;
        }

        int uid = AppUtils.getAppUid(mContext, packageName);

        MDM.mMDMController.executeShellToSetIptables("-N " + uid);
        MDM.mMDMController.executeShellToSetIptables("-A OUTPUT -m owner --uid-owner " + uid + " -j " + uid);
        MDM.mMDMController.executeShellToSetIptables("-F " + uid);
        MDM.mMDMController.executeShellToSetIptables("-A " + uid + " -p tcp -m string --string Host: --algo bm -j MARK --set-mark 1");

        for (String host : hosts) {
            MDM.mMDMController.executeShellToSetIptables("-A " + uid + " -p tcp -m mark --mark 1 -m string --string " + host + " --algo bm -j ACCEPT");
        }

        MDM.mMDMController.executeShellToSetIptables("-A " + uid + " -p tcp -m mark --mark 1 -j REJECT");
    }

    /**
     * 取消浏览器白名单
     *
     * @param packageName
     */
    public static void cancelIPtable(String packageName) {
        int uid = AppUtils.getAppUid(mContext, packageName);
        MDM.mMDMController.executeShellToSetIptables("-F " + uid);
    }

    /**
     * 安全浏览器处理
     *
     * @param code
     * @param securityChromeData
     */
    public static void excuteSecurityChrome(String code, SecurityChromeData securityChromeData) {

        if (securityChromeData == null) {

            String securityChrome_name = preferencesManager.getComplianceData(Common.securityChrome_name);

            if (securityChrome_name == null) {
                return;
            }

            mTheTang.addMessage(code, securityChrome_name);
            mTheTang.deleteStrategeInfo(OrderConfig.security_chrome + "");

            deleteSecurityChrome();
            EventBus.getDefault().post(new NotifyEvent());  //通知前台更新数据

            return;
        }

        String allowChrome = preferencesManager.getFenceData(Common.allowChrome);
        String insideAndOutside = preferencesManager.getFenceData(Common.insideAndOutside);

        mTheTang.addMessage(OrderConfig.security_chrome + "",
                securityChromeData.sec_name);

        mTheTang.addStratege(code, securityChromeData.sec_name, System.currentTimeMillis() + "");

        //如果是重复下发，先清除再添加
        if (!TextUtils.isEmpty(preferencesManager.getComplianceData(Common.securityChrome))) {
            deleteSecurityChrome();
        }

        mTheTang.storageSecurityChrome(securityChromeData);

        //判断当前是否有围栏策略，然后判断当前是否设置安全浏览器，最后判断是否在围栏内
        if (!TextUtils.isEmpty(preferencesManager.getFenceData(Common.geographical_fence))) {
            if (allowChrome != null && !"null".equals(allowChrome)) {
                if ("true".equals(allowChrome)) {
                    if (insideAndOutside == null) {
                        return;
                    }
                } else {
                    if (insideAndOutside != null) {
                        return;
                    }
                }
            }
        } else if (!TextUtils.isEmpty(preferencesManager.getFenceData(Common.timeFence_name))) {

            if (allowChrome != null && !"null".equals(allowChrome)) {
                if ("true".equals(allowChrome)) {
                    if (insideAndOutside == null) {
                        return;
                    }
                } else {
                    if (insideAndOutside != null) {
                        return;
                    }
                }
            }
        }
        showToDesk(securityChromeData.sec_white_list);
        excuteChrome(securityChromeData.sec_white_list);

    }

    /**
     * 执行安全浏览器
     *
     * @param sec_white_list
     */
    public static void excuteChrome(Map<String, String> sec_white_list) {

        if (sec_white_list == null) {
            return;
        }

        //网页限制,得到网页域名
        List<String> last_list = new ArrayList<>();

        Iterator<Map.Entry<String, String>> iterator = sec_white_list.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            last_list.add(HttpHelper.getHost(entry.getValue()));//获得url的域名
        }

        excuteIptables("com.android.browser", last_list);

        int uid = AppUtils.getAppUid(mContext, "com.android.browser");
        if (isProcessWork(uid)) {
            killProcess("com.android.browser");
        }
    }

    /**
     * @param uid
     * @return
     */
    public static boolean isProcessWork(int uid) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> myList = myAM.getRunningAppProcesses();
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            int mName = myList.get(i).uid;
            if (mName == uid) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    /**
     * webclip显示到桌面
     *
     * @param sec_white_list
     */
    public static void showToDesk(Map<String, String> sec_white_list) {

        if (sec_white_list == null)
            return;

        List<String> url_list = new ArrayList<>();
        List<String> name_list = new ArrayList<>();
        Iterator<Map.Entry<String, String>> iterator = sec_white_list.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            url_list.add(entry.getValue());//获得url的域名
            name_list.add(entry.getKey());
        }

        Log.w(TAG, "showToDesk " + url_list.size());
        //webclip
        for (int i = 0; i < url_list.size(); i++) {

            String url = HttpHelper.getUrlForWebClip(url_list.get(i));

            if (url == null) { //网页没有shortcut图片

                //bitmap回收，防止OOM
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }

                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
                ShortcutUtils.addShortcut(mTheTang.getContext(), ShortcutUtils.getShortCutIntent(url_list.get(i)), name_list.get(i), false, bitmap);
                //回收bitmap
                TheTang.gcBitmap(bitmap);

                EventBus.getDefault().post(new NotifyEvent());
            } else {
                final WebclipImageImpl webclipImage = new WebclipImageImpl(mTheTang.getContext());
                webclipImage.downloadPicFromNet(url, name_list.get(i), url_list.get(i), name_list.get(i));
            }
        }
        EventBus.getDefault().post(new NotifyEvent());  //通知前台更新数据
    }

    /**
     * 删除安全浏览器
     */
    public static void deleteSecurityChrome() {

        cancelIPtable("com.android.browser");

        int uid = AppUtils.getAppUid(mContext, "com.android.browser");
        if (isProcessWork(uid)) {
            killProcess("com.android.browser");
        }

        String list = preferencesManager.getComplianceData(Common.securityChrome_list);

        if (list == null) {
            return;
        }

        Map<String, String> sec_white_list = new HashMap<>();
        sec_white_list = ConvertUtils.jsonStringToMap(list);

        List<String> url_list = new ArrayList<>();
        List<String> name_list = new ArrayList<>();
        Iterator<Map.Entry<String, String>> iterator = sec_white_list.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            url_list.add(entry.getValue());//获得url的域名
            name_list.add(entry.getKey());
        }

        for (int i = 0; i < url_list.size(); i++) {
            ShortCutManager.deleteShortCut(url_list.get(i), name_list.get(i), name_list.get(i));
        }

        EventBus.getDefault().post(new NotifyEvent());
        mTheTang.deleteSecurityChrome();
    }

    /**
     * 取消安全浏览器
     */
    public static void cancelSecurityChrome() {
        cancelIPtable("com.android.browser");

        int uid = AppUtils.getAppUid(mContext, "com.android.browser");
        if (isProcessWork(uid)) {
            killProcess("com.android.browser");
        }

        String list = preferencesManager.getComplianceData(Common.securityChrome_list);

        if (list == null) {
            return;
        }

        Map<String, String> sec_white_list = new HashMap<>();
        sec_white_list = ConvertUtils.jsonStringToMap(list);

        List<String> url_list = new ArrayList<>();
        List<String> name_list = new ArrayList<>();
        Iterator<Map.Entry<String, String>> iterator = sec_white_list.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            url_list.add(entry.getValue());//获得url的域名
            name_list.add(entry.getKey());
        }

        for (int i = 0; i < url_list.size(); i++) {
            ShortCutManager.deleteShortCut(url_list.get(i), name_list.get(i), name_list.get(i));
        }

        EventBus.getDefault().post(new NotifyEvent());
    }

    /**
     * 删除
     *
     * @param deleteAppData
     */
    public static void deleteApp(DeleteAppData deleteAppData) {
        if (deleteAppData == null) {
            return;
        }
        String app_name = null;
        try {
            ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(deleteAppData.packageName, 0);
            app_name = (String) mContext.getPackageManager().getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mTheTang.addMessage(OrderConfig.delete_app + "", app_name);

        MDM.silentUninstall(deleteAppData.packageName);
    }

    /**
     * 修改设置相关数据
     *
     * @param settingAboutData
     */
    public static void excuteSettingAbout(SettingAboutData settingAboutData) {
        mTheTang.storageSettingAboutData(settingAboutData);
        EventBus.getDefault().post(new SettingEvent());
    }

    /**
     * 存储安全区域管理配置
     *
     * @param safetyLimitData
     */
    public static void storageSafetyLimitData(SafetyLimitData safetyLimitData) {

        preferencesManager.setSecurityData(Common.banCamera, safetyLimitData.banCamera);
        preferencesManager.setSecurityData(Common.banWifi, safetyLimitData.banWifi);
        preferencesManager.setSecurityData(Common.banMobileData, safetyLimitData.banMobileData);
        preferencesManager.setSecurityData(Common.banBluetooth, safetyLimitData.banBluetooth);
        preferencesManager.setSecurityData(Common.banLocation, safetyLimitData.banLocation);
        preferencesManager.setSecurityData(Common.banMtp, safetyLimitData.banMtp);
        preferencesManager.setSecurityData(Common.banSoundRecord, safetyLimitData.banSoundRecord);
        preferencesManager.setSecurityData(Common.banExitSecurityDomain, safetyLimitData.banExitSecurityDomain);
        //preferencesManager.setSecurityData(Common.machineCardBind, safetyLimitData.machineCardBind);
        preferencesManager.setSecurityData(Common.secureDesktop, safetyLimitData.secureDesktop);
        preferencesManager.setSecurityData(Common.safetyLimitDesktops, safetyLimitData.safetyLimitDesktops);

        preferencesManager.setSecurityData(Common.banScreenshot, safetyLimitData.banScreenshot);
        preferencesManager.setSecurityData(Common.allowDropdown, safetyLimitData.allowDropdown);
        preferencesManager.setSecurityData(Common.allowReset, safetyLimitData.allowReset);
        preferencesManager.setSecurityData(Common.allowNFC, safetyLimitData.allowNFC);
        preferencesManager.setSecurityData(Common.allowModifySystemtime, safetyLimitData.allowModifySystemtime);

        preferencesManager.setSecurityData(Common.banTelephone, safetyLimitData.banTelephone);
        preferencesManager.setSecurityData(Common.banTelephoneWhiteList, safetyLimitData.banTelephoneWhiteList);
        preferencesManager.setSecurityData(Common.banMobileHotspot, safetyLimitData.banMobileHotspot);
        preferencesManager.setSecurityData(Common.banShortMessage, safetyLimitData.banShortMessage);
    }

    /**
     * 挂载SDCard
     */
    public static synchronized void mountSDCard() {

        String sd = preferencesManager.getComplianceData(Common.system_sd_id);

        //判断是否为第一次挂载sd卡
        if (TextUtils.isEmpty(sd)) {
            preferencesManager.setComplianceData(Common.system_sd_id, DeviceUtils.getSDCardId(mContext));
            return;
        }

        String cid = DeviceUtils.getSDCardId(mContext);
        //String sd = preferencesManager.getComplianceData( Common.system_sd_id );

        if (!sd.equals(cid)) {
            //未违规的情况下做违规处理，多次违规不做重复操作
            //if (!"true".equals( mPreferencesManager.getComplianceData( Common.hadSystemCompliance ) )) {
            //mPreferencesManager.setComplianceData(Common.hadSystemCompliance, "true");
/*            SystemImpl systemImpl_ready = new SystemImpl(TheTang.getSingleInstance().getContext());
            systemImpl_ready.sendSystemCompliance(Common.sd_card, "0", "0");*/
            //todo baii impl bbbbbbbbbbbbbbbbb
            LogUtil.writeToFile( TAG, "systemCompliance!" );
            MDM.excuteSystemCompliance();
            sendMachineCardComplianceInfo("0", "0");
            //}
        } else {
            //已违规的情况下做合规处理
            //if ("true".equals( mPreferencesManager.getComplianceData( Common.hadSystemCompliance ) )) {
            //mPreferencesManager.setComplianceData( Common.hadSystemCompliance, "false" );
/*            SystemImpl systemImpl_ready = new SystemImpl(TheTang.getSingleInstance().getContext());
            systemImpl_ready.sendSystemCompliance(Common.sd_card, "1", "0");*/
            //todo baii impl bbbbbbbbbbbbbbbbb
            sendMachineCardComplianceInfo("1", "0");
            //}
        }
    }

    /**
     * 弹出SDCard
     */
    public static synchronized void ejectSDCard() {
        //未违规的情况下做违规处理，多次违规不做重复操作
        //if (!"true".equals( mPreferencesManager.getComplianceData( Common.hadSystemCompliance ) )) {
        // mPreferencesManager.setComplianceData( Common.hadSystemCompliance, "true" );
/*        SystemImpl systemImpl_ready = new SystemImpl(mContext);
        systemImpl_ready.sendSystemCompliance(Common.sd_card, "0", "0");*/
        //todo baii impl bbbbbbbbbbbbbbbbb
        LogUtil.writeToFile( TAG, "systemCompliance!" );
        MDM.excuteSystemCompliance();
        sendMachineCardComplianceInfo("0", "0");
        //}
    }
}
