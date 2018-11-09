package com.xiaomi.emm.definition;

/**
 * Created by Administrator on 2017/7/11.
 */

public class OrderConfig {
    //
    public static final int HadLink = 200;
    //Excuting
    public static final int AddTelephonyWhiteList = 0;//添加电话白名单
    public static final int modifyTelephonyWhiteList = 1000;//修改电话白名单
    public static final int DeleteTelephonyWhiteList = 1;//删除电话白名单
    public static final int IsTelephonyWhiteListByNumber = 2;
    public static final int QueryTelephonyWhiteList = 3;

    public static final int SwitchContainer = 4;
    public static final int DisableSwitching = 5;
    public static final int EnableSwitching = 6;
    public static final int IsInSecureContainer = 7;
    public static final int IsInFgContainer = 8;

    public static final int SilentInstallAppication = 9;//静默安装
    public static final int SilentUninstallAppication = 10;//静默卸载
    public static final int QueryPkgNameFromUninstallList = 11;
    public static final int AddPkgNameToUninstallList = 12;
    public static final int DeletePkgNameFromUninstallList = 13;
    public static final int QueryPkgNameFromInstallList = 14;
    public static final int AddPkgNameToInstallList = 15;
    public static final int DeletePkgNameFromInstallList = 16;

    public static final int EnableBluetooth = 17;
    public static final int IsBluetoothEnabled = 18;
    public static final int QueryMacFromBTSocketList = 19;
    public static final int AddMacToBTSocketList = 20;
    public static final int DeleteMacFromBTSocketList = 21;
    public static final int EnableBluetoothOpp = 22;
    public static final int IsBluetoothOppEnabled = 23;
    public static final int EnableWifi = 24;
    public static final int IsWifiEnabled = 25;
    public static final int OpenWifiOnBG = 26;//静默开启wifi
    public static final int IsWifiOpened = 27;

    public static final int EnableUsb = 41;
    public static final int IsUsbEnabled = 42;
    public static final int EnableCamera = 43;
    public static final int IsCameraEnabled = 44;
    public static final int EnableSoundRecording = 45;
    public static final int IsSoundRecordingEnabled = 46;
    public static final int EnableNfc = 47;
    public static final int DisableNfc = 48;
    public static final int IsNfcEnabled = 49;

    public static final int CreateApn = 54;
    public static final int GetApnList = 55;
    public static final int GetApn = 56;
    public static final int GetCurrentApn = 57;
    public static final int SetCurrentApn = 58;

    public static final int EnableLocationService = 59;//允许定位
    public static final int IsLocationServiceEnabled = 60;
    public static final int OpenGpsOnBGSlient = 61;
    public static final int IsGpsOpenedOnBGSlient = 62;

    public static final int SetScreenLock = 63;//锁屏
    public static final int SetPassword = 64;//设置密码
    public static final int TakeScreenShot = 65;//截屏
    public static final int SetShutDown = 66;//关机
    public static final int SetReboot = 67;//重启
    public static final int SetFactoryReset = 68;//恢复出厂设置
    public static final int KillProcess = 69;
    public static final int WipeData = 70;//淘汰

    public static final int EnableSms = 71;
    public static final int IsSmsEnabled = 72;
    public static final int EnableTelePhone = 73;
    public static final int IsTelephoneEnabled = 74;
    public static final int InsertContact = 75;
    public static final int DeleteContactByName = 76;
    public static final int GetAllContactInfo = 77;
    public static final int OpenWifiOnBGSlient = 83;
    public static final int MDMAPPUpdate = 114;

    //huawei独有
    public static final int SetAppInstallationPolicy = 78;
    public static final int GetAppInstallationPolicy = 79;
    public static final int EnableUninstallWhiteListFunction = 80;
    public static final int EnableInstallWhiteListFunction = 81;
    public static final int QueryAllBTWhiteList = 82;

    public static final int EnableSecSimcard = 84;
    public static final int IsSecSimcardEnabled = 85;
    public static final int SetKeyVisible = 86;
    public static final int SetHomeKeyVisible = 87;
    public static final int SetRecentKeyVisible = 88;
    public static final int OpenDataConnectivity = 89;
    public static final int IsDataConnectivityOpen = 90;
    public static final int Enable4G = 91;
    public static final int Is4GOpen = 92;

    public static final int DeleteApn = 93;
    public static final int OpenGps = 94;
    public static final int IsGpsOpend = 95;
    public static final int ForceLocationService = 96;
    public static final int IsLocationServiceForced = 97;
    public static final int SetLocationPolicy = 98;
    public static final int GetLocationPolicy = 99;
    public static final int ForceLockScreen = 100;//强制锁屏
    public static final int ReleaseLockScreen = 101;
    public static final int SetPasswordWithPolicy = 102;
    public static final int SetPasswordNone = 103;//清除密码
    public static final int ExecuteShellToSetIptables = 104;
    public static final int QueryIACList = 105;
    public static final int QueryAllIACList = 106;
    public static final int AddItemToIACList = 107;
    public static final int DeleteItemFromIACList = 108;
    public static final int GetAppPowerUsage = 109;
    public static final int GetRunningApplication = 110;
    public static final int GetAppTrafficInfo = 111;
    public static final int GetDeviceInfo = 112;

    //Lenovo独有
    public static final int QueryMacFromWifiList = 28;
    public static final int AddMacToWifiList = 29;
    public static final int DeleteMacFromWifiList = 30;
    public static final int EnableWifiAP = 31;
    public static final int IsWifiAPEnabled = 32;
    public static final int QueryMacFromWifiAPList = 33;
    public static final int AddMacToWifiAPList = 34;
    public static final int DeleteMacFromWifiAPList = 35;
    public static final int SetWifiApOpened = 36;
    public static final int GetWifiApState = 37;
    public static final int IsWifiAPOpened = 38;
    public static final int GetWifiApConfiguration = 39;
    public static final int SetWifiApConfiguration = 40;

    public static final int OpenNfc = 50;
    public static final int CloseNfc = 51;
    public static final int EnableSD = 52;
    public static final int DisableSD = 53;

    public static final int GetActualDefaultRingtoneUri = 113;

    //新增加
    public static final int GetLocationData = 115;//获取定位

    public static final int TOLifeContainer = 116;// 切换生活域
    public static final int TOSecurityContainer = 117;  //切换安全域
    public static final int PlayRingtones = 118;  //播放铃声
    public static final int IssuedFile = 119;
    public static final int DeleteIssuedFile = 120;

    public static final int EnableMobileHotspot = 121; //启用 移动热点

    public static final int send_system_strategy = 122; // 下发系统策略
    public static final int delete_system_strategy = 123; // 删除系统策略

    public static final int send_app_strategy = 124;// 下发应用策略
    public static final int delete_app_strategy = 125;// 删除应用策略

    public static final int send_loseCouplet_strategy = 126;// 下发失联策略
    public static final int delete_loseCouplet_strategy = 127;// 删除失联策略

    public static final int Send_Message = 128;//发送消息

    public static final int send_geographical_Fence = 131;//地理围栏
    public static final int delete_geographical_Fence = 132;//删除地理围栏

   // public static final int Category = 200;
    public static final int delete_limit_strategy = 133;// 删除限制策略
    public static final int send_limit_strategy = 134;// 下发限制策略

    public  static  final  int send_time_Frence = 136;//下发时间围栏
    public  static  final  int delete_time_Frence = 137;//删除时间围栏
    public  static  final  int revocation_time_Frence = 138;//撤销时间围栏

    public  static  final  int send_safe_desk = 139; //下发安全桌面
    public  static  final  int delete_safe_desk = 140; //删除安全桌面
    public  static  final  int revocation_safe_desk = 141; //撤销安全桌面

    public static final int send_black_White_list = 145;// 下发应用黑白名单
    public static final int delete_black_White_list = 146;// 删除黑白名单

    public static final int send_White_list = 1450;// 下发白名单
    public static final int delete_White_list = 1460;// 删除白名单

     public static final int send_black_list = 1451;// 下发黑名单
    public static final int delete_black_list = 1461;// 删除黑名单

    public static final int send_configure_Strategy = 147;// 下发配置策略
    public static final int delete_configure_Strategy = 148;// 删除配置策略

    public static final int machine_card_binding = 149;// 机卡绑定
    public static final int cancel_machine_card_binding = 150;// 取消机卡绑定

    public static final int get_device_info = 151; //获取设备信息

    public static final int login_out_and_delete_data = 152;//删除账户

    public static final int security_chrome = 142;// 安全浏览器

    public static final int delete_security_chrome = 143;// 删除安全浏览器

    public static final int upload_debug_log = 153;// 上传调试日志

    public static final int start_phone_white = 154;// 启动电话白名单

    public static final int stop_phone_white = 155;// 停用电话白名单

    public static final int device_update = 158;//设备更新

    public static final int flow_quota = 159;//设置流量

    public static final int delete_app = 160;//删除应用

    public static final int get_setting_about = 161;//获得设置相关：支持、帮助、许可协议

    public static final int enter_sercurity_stratege = 162; //进入安全域策略

    public static final int enter_life_stratege = 163; //进入生活域策略

    public static final int security_manager = 164; //安全区域管理

    public static final int download_avatar = 165; //下载用户头像

    public static final int put_down_application_fence = 166; //下发应用围栏

    public static final int put_down_application_fence_message = 1660; //下发应用围栏

    public static final int unstall_application_fence = 167; //删除应用围栏

    public static final int DELETE_SENSITIVE_WORD = 168; //删除敏感词策略
    public static final int SEND_SENSITIVE_WORD_POLICY = 169; //下发敏感词策略

    public static final int SEND_SMS_BACKUP_POLICY = 176; //下发短信备份策略
    public static final int DELETE_SMS_BACKUP_POLICY = 177; //删除短信备份策略

    public static final int SEND_CALL_RECORDER_BACKUP_POLICY = 178; //下发录音备份策略
    public static final int DELETE_CALL_RECORDER_BACKUP_POLICY = 179; //删除录音备份策略


    public static final int SEND_EntranceGuard_POLICY = 170; //下发门禁策略
    public static final int DELETE_EntranceGuard_POLICY = 171; //下发删除门禁策略

    public static final int SEND_ENTRANCE_GUARD_KEY = 172; //下发门禁KEY
    public static final int DELETE_ENTRANCE_GUARD_KEY = 173; //删除门禁KEY


    public static final int send_trajectory_Strategy = 174; //下发轨迹
    public static final int delete_trajectory_Strategy = 175; //删除轨迹

    public static final int SEND_WIFI_FENCE = 180; //下发WiFi围栏
    public static final int DELETE_WIFI_FENCE = 181; //删除WiFi围栏
}
