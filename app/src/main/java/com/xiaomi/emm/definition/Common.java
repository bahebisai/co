package com.xiaomi.emm.definition;

import com.xiaomi.emm.R;
import com.xiaomi.emm.features.presenter.TheTang;

/**
 * Created by Administrator on 2017/6/6.
 */

public class Common {

    public static String packageName = TheTang.getSingleInstance().getContext().getPackageName();

    public static long SIM_STATE_CHANGED = 0;
    public static long ACTION_MEDIA_MOUNTED = 0;

    //Device Info
    public static final String[] deviceInfo = {"imei","meid","run_memory","phone_storage","resolution",
            "manufacturers","model","android_version","system_version","safe_version","patch_level","sim1_iccid",
            "sim2_iccid","sim1_ismi","sim2_ismi"};

    public static final String[] deviceInfo1 = {"main_imei","vice_imei","ram","phone_storage","resolution",
            "device_manufacturer","device_model","android_version","current_system_version","safe_version","patch_level","main_iccid",
            "vice_iccid","main_imsi","vice_imsi"};

    public static final String[] mobileDatas = {"main_mobile_flow_day", "main_mobile_flow_week", "main_mobile_flow_month",
            "vice_mobile_flow_day", "vice_mobile_flow_week", "vice_mobile_flow_month"};

    public final static Object[][] FileIcon_Table = {
            {".aep", R.mipmap.aep},
            {".ai", R.mipmap.ai},
            {".avi", R.mipmap.avi},
            {".cdr", R.mipmap.cdr},
            {".css", R.mipmap.css},
            {".doc", R.mipmap.doc},
            {".eps", R.mipmap.eps},
            {".gif", R.mipmap.gif},
            {".jpeg", R.mipmap.jpeg},
            {".html", R.mipmap.html},
            {".mov", R.mipmap.mov},
            {".mp3", R.mipmap.mp3},
            {".ne", R.mipmap.ne},
            {".pdf", R.mipmap.pdf},
            {".php", R.mipmap.php},
            {".png", R.mipmap.png},
            {".ppt", R.mipmap.ppt},
            {".psd", R.mipmap.psd},
            {".rar", R.mipmap.rar},
            {".ttf", R.mipmap.ttf},
            {".txt", R.mipmap.txt},
            {".url", R.mipmap.url},
            {".xls", R.mipmap.xls},
            {".zip", R.mipmap.zip},
    };

    //message
    public final static Object[][] message_info = {
            {OrderConfig.SilentUninstallAppication,R.string.uninstall_app},
            {OrderConfig.SilentInstallAppication,R.string.install_app},
            {OrderConfig.IssuedFile,R.string.download_file},
            {OrderConfig.DeleteIssuedFile,R.string.delete_file},
            {OrderConfig.GetLocationData,R.string.location_info},
            {OrderConfig.PlayRingtones,R.string.play_ringtones},
            {OrderConfig.TOLifeContainer,R.string.switch_life},
            {OrderConfig.TOSecurityContainer,R.string.switch_security},
            {OrderConfig.SetReboot,R.string.device_reboot},
            {OrderConfig.SetShutDown,R.string.device_shut},
            {OrderConfig.SetScreenLock,R.string.device_lock},
            {OrderConfig.SetPasswordNone,R.string.device_unlock},
            {OrderConfig.Send_Message,R.string.message_notification},
            {OrderConfig.AddTelephonyWhiteList,R.string.add_telephone_number},
            {OrderConfig.DeleteTelephonyWhiteList,R.string.delete_telephone_number},
            {OrderConfig.get_device_info,R.string.get_device_info},
            {OrderConfig.send_system_strategy,R.string.send_system_strategy_msg},
            {OrderConfig.delete_system_strategy,R.string.delete_system_strategy},
            {OrderConfig.send_app_strategy,R.string.send_app_strategy_msg },
            {OrderConfig.delete_app_strategy,R.string.delete_app_strategy},
            {OrderConfig.send_loseCouplet_strategy,R.string.send_loseCouplet_strategy_msg},
            {OrderConfig.delete_loseCouplet_strategy,R.string.delete_loseCouplet_strategy},
            {OrderConfig.send_geographical_Fence,R.string.send_geographical_Fence_msg},
            {OrderConfig.delete_geographical_Fence,R.string.delete_geographical_Fence},
            {OrderConfig.send_black_White_list,R.string.send_black_White_list_msg},
            {OrderConfig.delete_black_White_list,R.string.delete_black_White_list},
            {OrderConfig.send_limit_strategy,R.string.send_limit_strategy_msg},
            {OrderConfig.delete_limit_strategy,R.string.delete_limit_strategy},
            {OrderConfig.send_configure_Strategy,R.string.send_configure_Strategy_msg},
            {OrderConfig.delete_configure_Strategy,R.string.delete_configure_Strategy},
            {OrderConfig.machine_card_binding,R.string.machine_card_binding},
            {OrderConfig.cancel_machine_card_binding,R.string.delete_machine_card_binding},
            {OrderConfig.send_safe_desk,R.string.send_safe_desk_msg},
            {OrderConfig.delete_safe_desk,R.string.delete_safe_desk},
            {OrderConfig.revocation_safe_desk,R.string.revocation_safe_desk},
            {OrderConfig.send_time_Frence,R.string.send_time_Frence_msg},
            {OrderConfig.delete_time_Frence,R.string.delete_time_Frence},
            {OrderConfig.revocation_time_Frence,R.string.revocation_time_Frence},
            {OrderConfig.start_phone_white,R.string.start_phone_white},
            {OrderConfig.stop_phone_white,R.string.stop_phone_white},
            {OrderConfig.device_update,R.string.device_update},
            {OrderConfig.upload_debug_log,R.string.upload_debug_log},
            {OrderConfig.SetFactoryReset,R.string.SetFactoryReset},
            {OrderConfig.WipeData,R.string.wipe_data},
            {OrderConfig.security_chrome,R.string.send_security_chrome_msg},
            {OrderConfig.delete_security_chrome,R.string.delete_security_chrome},
            {OrderConfig.flow_quota,R.string.flow_quota},
            {OrderConfig.delete_app,R.string.delete_app},
            {OrderConfig.send_White_list,R.string.send_White_list_msg},
            {OrderConfig.delete_White_list,R.string.delete_White_list},
            {OrderConfig.send_black_list,R.string.send_black_list_msg},
            {OrderConfig.delete_black_list,R.string.delete_black_list},
            {OrderConfig.security_manager,R.string.security_manager},
            {OrderConfig.enter_sercurity_stratege,R.string.enter_sercurity_stratege},
            {OrderConfig.enter_life_stratege,R.string.enter_life_stratege},
            {OrderConfig.download_avatar,R.string.download_avatar},
            {OrderConfig.put_down_application_fence,R.string.put_down_application_fence_msg},
            {OrderConfig.put_down_application_fence_message,R.string.put_down_application_fence_msg_content},
            {OrderConfig.unstall_application_fence,R.string.unstall_application_fence},
            {OrderConfig.login_out_and_delete_data,R.string.login_out_and_delete_data},
            {OrderConfig.modifyTelephonyWhiteList,R.string.modifyTelephonyWhiteList},
            {OrderConfig.SEND_SENSITIVE_WORD_POLICY,R.string.send_sensitive_word_policy},
            {OrderConfig.DELETE_SENSITIVE_WORD,R.string.delete_sensitive_word},
            {OrderConfig.SEND_SMS_BACKUP_POLICY,R.string.send_sms_backup_policy},
            {OrderConfig.DELETE_SMS_BACKUP_POLICY,R.string.delete_sms_backup_policy},
            {OrderConfig.SEND_CALL_RECORDER_BACKUP_POLICY,R.string.send_call_recorder_policy},
            {OrderConfig.DELETE_CALL_RECORDER_BACKUP_POLICY,R.string.delete_call_recorder_policy},
            {OrderConfig.SEND_EntranceGuard_POLICY,R.string.send_Entrance_guard},
            {OrderConfig.DELETE_EntranceGuard_POLICY,R.string.delete_Entrance_guard},
            {OrderConfig.SEND_ENTRANCE_GUARD_KEY,R.string.send_Entrance_guard_key},
            {OrderConfig.DELETE_ENTRANCE_GUARD_KEY,R.string.delete_Entrance_guard_key},
            {OrderConfig.send_trajectory_Strategy,R.string.send_trajectory_Strategy},
            {OrderConfig.delete_trajectory_Strategy,R.string.delete_trajectory_Strategy},
            {OrderConfig.SEND_WIFI_FENCE,R.string.send_wifi_fence},
            {OrderConfig.DELETE_WIFI_FENCE,R.string.delete_wifi_fence},
    };

    /***************************Json****************************/
    //login
    public final static String keepAliveHost = "keepAliveHost";
    public final static String keepAlivePort = "keepAlivePort";
    public final static String token = "token";
    public final static String alias = "alias";
    public final static String userName = "userName";
    public final static String passWord = "passWord";

    public final static String appVersion = "appVersion";

    //public static final String local_iccid = "local_iccid";
    public static final String local_imei = "local_imei";
    public static final String system_sd_id = "system_sd_id";
    //password
    public final static String app_passWord = "app_passWord";

    //phone
    public final static String white_phone = "white_phone";
    //policy
    public final static Object[][] stratege_info = {
            {OrderConfig.send_system_strategy,R.string.send_system_strategy},
            {OrderConfig.send_app_strategy,R.string.send_app_strategy},
            {OrderConfig.send_loseCouplet_strategy,R.string.send_loseCouplet_strategy},
            {OrderConfig.send_geographical_Fence,R.string.send_geographical_Fence},
            {OrderConfig.send_black_White_list,R.string.send_black_White_list},
            {OrderConfig.send_White_list,R.string.send_White_list},
            {OrderConfig.send_black_list,R.string.send_black_list},
            {OrderConfig.send_limit_strategy,R.string.send_limit_strategy},
            {OrderConfig.send_configure_Strategy,R.string.send_configure_Strategy},
            {OrderConfig.machine_card_binding,R.string.machine_card_binding},
            {OrderConfig.send_safe_desk,R.string.send_safe_desk},
            {OrderConfig.delete_safe_desk,R.string.delete_safe_desk},
            {OrderConfig.revocation_safe_desk,R.string.revocation_safe_desk},
            {OrderConfig.send_time_Frence,R.string.send_time_Frence},
            {OrderConfig.delete_time_Frence,R.string.delete_time_Frence},
            {OrderConfig.revocation_time_Frence,R.string.revocation_time_Frence},
            {OrderConfig.security_chrome,R.string.send_security_chrome},
            {OrderConfig.start_phone_white,R.string.start_phone_white},
            {OrderConfig.stop_phone_white,R.string.stop_phone_white},
            {OrderConfig.put_down_application_fence,R.string.put_down_application_fence},
            {OrderConfig.SEND_SENSITIVE_WORD_POLICY,R.string.send_sensitive_word_policy},
            {OrderConfig.DELETE_SENSITIVE_WORD,R.string.delete_sensitive_word},
            {OrderConfig.SEND_SMS_BACKUP_POLICY,R.string.send_sms_backup_policy},
            {OrderConfig.DELETE_SMS_BACKUP_POLICY,R.string.delete_sms_backup_policy},
            {OrderConfig.SEND_CALL_RECORDER_BACKUP_POLICY,R.string.send_call_recorder_policy},
            {OrderConfig.DELETE_CALL_RECORDER_BACKUP_POLICY,R.string.delete_call_recorder_policy},
            {OrderConfig.SEND_EntranceGuard_POLICY,R.string.send_Entrance_guard},
            {OrderConfig.DELETE_EntranceGuard_POLICY,R.string.delete_Entrance_guard},
            {OrderConfig.SEND_ENTRANCE_GUARD_KEY,R.string.send_Entrance_guard_key},
            {OrderConfig.DELETE_ENTRANCE_GUARD_KEY,R.string.delete_Entrance_guard_key},
            {OrderConfig.send_trajectory_Strategy,R.string.send_trajectory_Strategy},
            {OrderConfig.delete_trajectory_Strategy,R.string.delete_trajectory_Strategy},
            {OrderConfig.SEND_WIFI_FENCE,R.string.send_wifi_fence},
            {OrderConfig.DELETE_WIFI_FENCE,R.string.delete_wifi_fence},
    };

    //default policy
    public static final String default_policy = "default_policy";
    public static final String default_allowCamera = "default_allowCamera";
    public static final String default_allowWifi = "default_allowWifi";
    public static final String default_allowWifiAP = "default_allowWifiAP";
    public static final String default_allowMobileData = "default_allowMobileData";
    public static final String default_allowBluetooth = "default_allowBluetooth";
    public static final String default_allowLocation = "default_allowLocation";
    public static final String default_allowUsb = "default_allowUsb";
    public static final String default_allowSoundRecording = "default_allowSoundRecording";
    public static final String default_allowScreenshot = "default_allowScreenshot";
    public static final String default_allowMessage = "default_allowMessage";

    public static final String default_allowDropdown = "default_allowDropdown";
    public static final String default_allowReset = "default_allowReset";
    public static final String default_allowNFC = "default_allowNFC";
    public static final String default_allowModifySystemtime = "default_allowModifySystemtime";
    public static final String default_allowTelephone = "default_allowTelephone";
    public static final String default_allowTelephoneWhiteList = "default_allowTelephoneWhiteList";

    public static final String wifi_password = "wifiPassword";
    public static final String wifi_ssid = "wifi_ssid";

    //middle policy
    public static final String limit_name = "limit_name";
    public static final String limit_id = "limit_id";
    public static final String middle_policy = "policy";
    public static final String middle_allowCamera = "allowCamera";
    public static final String middle_allowSdCard = "allowSdCard";
    public static final String middle_allowWifi = "allowWifi";
    public static final String middle_allowMobileData = "allowMobileData";
    public static final String middle_allowBluetooth = "allowBluetooth";
    public static final String middle_allowMobileHotspot = "allowMobileHotspot";
    public static final String middle_allowLocation = "allowOpenGps";
    public static final String middle_allowUpdateTime = "allowUpdateTime";
    public static final String middle_allowUsb = "allowUsb";
    public static final String middle_allowSoundRecording = "allowSoundRecording";
    public static final String middle_allowScreenshot = "banScreenshot";
    public static final String middle_allowMessage = "allowMessage";

    public static final String middle_allowDropdown = "allowDropdown";
    public static final String middle_allowReset = "allowReset";
    public static final String middle_allowNFC = "allowNFC";
    public static final String middle_allowModifySystemtime = "banModifySystemtime";

    public static final String middle_telephone = "allowTelephone";
    public static final String middle_telephoneWhiteList = "openTelephoneWhiteList";

    //lost compliance
    public static final String lost_compliance = "lost";
    public static final String lost_name = "lost_name";
    public static final String lost_time = "loseCoupletTime";
    public static final String lost_password = "lost_password";
    public static final String lost_time_frame = "lost_time_frame";
    public static final String missingId = "missingId";

    //system compliance
    //public static final String hadSystemCompliance = "hadSystemCompliance";
    public static final String system_compliance = "systemCompliance";
    public static final String system_compliance_id = "system_compliance_id";
    public static final String system_compliance_delay = "delayedHour";
    public static final String system_compliance_name = "system_compliance_name";
    public static final String system_compliance_pwd = "system_compliance_pwd";

    public static final String system_version = "system_version_sdk";
    public static final String system_sd = "system_sd";
    public static final String system_sim = "system_sim";
    public static final String system_encryption = "system_encryption";
    public static final String system_root = "system_root";
    public static final String system_statistics = "system_statistics";

    public final static String[] system_info = {"system_version_sdk","system_sd","system_sim",
            "system_encryption","system_root","system_statistics"};

    //appControlType
    public static final String appControlType = "appControlType"; //用于标识黑白名单及应用合规：0、1、2

    //app compliance
    public static final String appType = "violationStrategyType";
    public static final String appList = "list";
    public static final String app_compliance_id = "app_compliance_id";
    public static final String app_compliance_name = "app_compliance_name";
    public static final String excute_appCompliance = "excute_appCompliance";
    public static final String app_compliance_pwd = "app_compliance_pwd";
    public static final String app_compliance_sendId = "sendId";
    //setting
    public static final String app_auto_install = "app_auto_install";
    public static final String setting_help = "messageForHelp";
    public static final String setting_stand_by = "supportContent";
    public static final String setting_agreement = "agreementLicense";
    public static final String setting_clientManagement = "clientManagement";

    //geography fence
    public static final String geographical_fence = "geographical_fence";
    public static final String geographical_fence_name = "name";
    //round
    public static final String coordinate = "coordinate";
    public static final String longitude = "longitude";
    public static final String latitude = "latitude";
    public static final String radius = "radius";
    public static final String geo_id = "geo_id";

    //fence device
    public static final String allowDevice = "allowDevice";
    public static final String lockScreen = "lockScreen";
    public static final String lockPwd = "lockPwd";
    public static final String allowMobileData = "allowMobileData";
    public static final String allowOpenWifi = "allowOpenWifi";
    public static final String allowCloseWifi = "allowCloseWifi";
    public static final String allowConfigureWifi = "allowConfigureWifi";
    public static final String configureWifi = "configureWifi";
    public static final String allowAutomaticJoin = "allowAutomaticJoin";
    public static final String hiddenNetwork = "hiddenNetwork";
    public static final String allowCamera = "allowCamera";
    public static final String allowBluetooth = "allowBluetooth";
    public static final String allowContainSwitching = "allowDomainSwitching";
    public static final String safeType = "safeType";

    public static final String mobileHotspot = "mobileHotspot";
    public static final String locationService = "locationService";
    public static final String matTransmission = "matTransmission";
    public static final String shortMessage = "shortMessage";
    public static final String soundRecording = "soundRecording";

    public static final String geo_telephone = "telephone";
    public static final String geo_telephoneWhiteList = "telephoneWhiteList";
    //fence chrome
    public static final String allowChrome = "allowChrome";
    public static final String webPageList = "webpageList";
    //fence desktop
    public static final String allowDesktop = "allowDesktop";
    public static final String displayCall = "displayCall";
    public static final String displayContacts = "displayContacts";
    public static final String displayMessage = "displayMessage";
    public static final String applicationProgram = "applicationProgram";
    public static final String appPageName = "appPageName";
    public static final String appName = "appName";
    public static final String setToSecureDesktop = "setToSecureDesktop";
    public static final String insideAndOutside = "insideAndOutside";


    //double domain
    public static final String allowDoubleDomain = "allowDoubleDomain";
    public static final String twoDomainControl = "twoDomainControl";

    public static final String appManagerType = "appManagerType";

    //machine card binding
    public static final String machineCard = "machineCard";
    public static final String imei_phone = "imei";
    public static final String iccid_card = "iccid";
    public static final String iccid_card1 = "iccid1";
    public static final String imei_phone_card = "imei_phone_card";
    //tiemFence
    public static final String timeUnit = "timeUnit";
    public static final String startimeRage = "startimeRage";
    public static final String endTimeRage = "endTimeRage";
    public static String timeFence_name="timeFence_name";
    //modifyed by duanxin for bug107 on 2017/09/04
    public static boolean isReplace = false;

    //ComingNumberLog
    public static  String ComingNumberLog = "ComingNumberLog";
    public static  String ID = "id";
    public static  String CODE = "code";

    //Security chrome
    public static final String securityChrome = "securityChrome";
    public static final String securityChrome_name = "securityChrome_name";
    public static final String securityChrome_id = "securityChrome_id";
    public static final String securityChrome_list = "securityChrome_list";

    //sensitive word strategy
    public static final String SENSITIVE_WORD = "sensitiveStrategy";
    public static final String SENSITIVE_STRATEGY_ID = "id";
    public static final String SENSITIVE_STRATEGY_NAME = "name";

    //Network && Uninstall
    public final static Object[] net_info = {
            R.string.forbidden_net,
            R.string.allow_net
    };

    public final static Object[] uninstall_info = {
            R.string.forbidden_un,
            R.string.allow_un
    };

    //用于失败反馈的处理
    public static final int feedback = 0;

    public static final int app_impl = 1;
    public static final int coming_number_impl = 2;
    public static final int location_upload = 3;

    public static final int device_impl = 4;
    public static final int device_update = 5;
    public static final int download_impl = 6;
    public static final int iccid_impl = 7;
    public static final int log_upload_impl = 8;
    public static final int login = 9;
    public static final int password_impl = 10;
    public static final int switch_log_impl = 11;
    public static final int sync_data_impl = 12;
    public static final int sd_card = 13;
    public static final int webclip_image_impl = 14;
    public static final int excute_complete_impl = 15;
    public static final int send_complete_result = 16;
    public static final int get_telephone_white = 17;
    public static final int machine_card = 18;
    public static final int white_telephone_status = 19;
    public static final int SMS_BACKUP = 20;
    public static final int CALL_RECORDER_BACKUP = 21;
    public static final int USER_TRACK = 22;
    public static final int USER_AVATAR_UPLOAD = 23;
    public static final int SETTING_DATA = 24;
    public static final int APP_VERSION_UPDATE = 25;

    //security stratege data
    public static final String banSecurity = "safetyLimit";
    //whether in security container
    public static final String securityContainer = "securityContainer";
    public static final String banCamera = "banCamera";
    public static final String banWifi = "banWifi";
    public static final String banMobileData = "banMobileData";
    public static final String banBluetooth = "banBluetooth";
    public static final String banLocation = "banLocation";
    public static final String banMtp = "banMtp";
    public static final String banSoundRecord = "banSoundRecord";
    public static final String banExitSecurityDomain = "banExitSecurityDomain";
    public static final String machineCardBind = "machineCardBind";
    public static final String secureDesktop = "secureDesktop";
    public static final String safetyLimitDesktops = "safetyLimitDesktops";
    public static final String safetyTosecureFlag = "safetyTosecureFlag";
    public static final String secureDesktopFlag = "secureDesktopFlag";
    public static final String banScreenshot = "banScreenshot";
    public static final String allowDropdown = "allowDropdown";
    public static final String allowReset = "allowReset";
    public static final String allowNFC  = "allowNFC";
    public static final String allowModifySystemtime  = "banModifySystemtime";

    public static final String banTelephone  = "banTelephone";
    public static final String banTelephoneWhiteList  = "banTelephoneList";
    public static final String banMobileHotspot  = "banMobileHotspot";
    public static final String banShortMessage  = "banShortMessage";

    public static final String callPackageName = "com.android.phone";
    public static final String contactsPackageName = "com.android.contacts";
    public static final String messagePackageName = "com.android.mms";

    public static final String safeActicivty_finsh = "finsh";
    public static final String safeActicivty_flush= "fulsh";

    //app fence
    public static final String appFenceId = "id";
    public static final String appFencePolicy = "policy";
    public static final String applicationFence = "applicationFence";
    public static final String appFenceName = "name";
    public static final String appFenceCoordinate = "coordinate";
    public static final String appFenceRadius = "radius";
    public static final String appFenceStartDateRange = "startDateRange";
    public static final String appFenceEndDateRange = "endDateRange";
    public static final String appFenceNoticeMessage = "noticeMessage";
    public static final String appFenceNoticeBell = "noticeBell";
    public static final String appFenceLimitType = "limitType";
    public static final String appFenceMessageContent = "messageContent";

    public static final String appFenceTimeFenceUnit = "timeFenceUnit";
    public static final String appFenceTypeDate= "typeDate";
    public static final String appFenceUnitType = "unitType";
    public static final String appFenceEndTime = "endTime";
    public static final String appFenceStartTime = "startTime";

    public static final String appFenceApplicationPrograms = "applicationPrograms";
    public static final String appFenceAppPageName = "packageName";

    public static final String nomal_lock = "nomal_lock";
    public static final String app_lock = "app_lock";
    public static final String system_lock = "system_lock";
    public static final String fence_time_lock = "fence_time_lock";
    public static final String fence_geo_lock = "fence_geo_lock";
    public static final String lost_lock = "lost_lock";

    public static String[] lockTypes = {Common.nomal_lock, Common.app_lock, Common.system_lock, Common.fence_time_lock, Common.fence_geo_lock, Common.lost_lock};

    public static final String trajectoryName = "name";
    public static final String frequency = "frequency";
    public static final String trajectoryID = "id";

}
