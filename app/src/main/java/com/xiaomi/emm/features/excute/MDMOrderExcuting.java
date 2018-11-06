package com.xiaomi.emm.features.excute;


import android.content.Context;
import android.util.Log;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.OrderConfig;
import com.xiaomi.emm.features.event.CompleteEvent;
import com.xiaomi.emm.features.event.NotifyEvent;
import com.xiaomi.emm.features.policy.appFence.AppFenceExcute;
import com.xiaomi.emm.features.policy.compliance.ExcuteCompliance;
import com.xiaomi.emm.features.policy.device.ExcuteLimitPolicy;
import com.xiaomi.emm.features.policy.fence.FenceManager;
import com.xiaomi.emm.features.policy.phoneCall.CallRecorderManager;
import com.xiaomi.emm.features.policy.sensitiveWords.SensiWordManager;
import com.xiaomi.emm.features.policy.sms.SmsManager;
import com.xiaomi.emm.features.policy.trajectory.TrajectoryPolice;
import com.xiaomi.emm.model.AppBlackWhiteData;
import com.xiaomi.emm.model.AppFenceData;
import com.xiaomi.emm.model.DeleteAppData;
import com.xiaomi.emm.model.DownLoadEntity;
import com.xiaomi.emm.model.ExceptionLogData;
import com.xiaomi.emm.model.GeographicalFenceData;
import com.xiaomi.emm.model.LostComplianceData;
import com.xiaomi.emm.model.PolicyData;
import com.xiaomi.emm.model.SafetyLimitData;
import com.xiaomi.emm.model.SecurityChromeData;
import com.xiaomi.emm.model.SensitiveStrategyInfo;
import com.xiaomi.emm.model.SystemComplianceData;
import com.xiaomi.emm.utils.DataParseUtil;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.features.presenter.MDM;
import com.xiaomi.emm.features.manager.PreferencesManager;
import com.xiaomi.emm.features.presenter.TheTang;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by Administrator on 2017/5/31.
 */

public class MDMOrderExcuting {
    public static final String TAG = "MDMOrderExcuting";
    private static TheTang mTheTang = TheTang.getSingleInstance();
    public static PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
    private static boolean order = false;
    static Context mContext;
    private String orderCode = null;
    String id = null;

    public void processOrderMessage(Context context, String extra) {
        mContext = context;

        orderCode = DataParseUtil.jSonCode( extra );
        LogUtil.writeToFile( TAG, "orderCode = " + orderCode );

        if (orderCode == null) {
            LogUtil.writeToFile( TAG, "code is null!" );
            return;
        }

        id = DataParseUtil.jSon( "sendId", extra );
        LogUtil.writeToFile( TAG, "sendId = " + id );

        if (id == null) {
            LogUtil.writeToFile( TAG, "sendId is null!" );
            return;
        }
        mTheTang.addMessageInfo( orderCode );

        Log.d("baii", "json " + extra);

        switch (Integer.parseInt( orderCode )) {
            /***********************************电话白名单*********************************************/
            case OrderConfig.AddTelephonyWhiteList:  //**更新电话白名单
                TheTang.getSingleInstance().addMessage( OrderConfig.AddTelephonyWhiteList + "", "");
                MDM.updateTelepfohonyWhiteList();
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            /*case OrderConfig.DeleteTelephonyWhiteList:  //删除电话白名单
                //List<TelephoyWhiteUser> listDeleteUser = DataParseUtil.jSonWhiteList( extra );
                //MDM.deleteTelephonyWhiteList( listDeleteUser, id );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;*/
            /*case OrderConfig.IsTelephonyWhiteListByNumber:  //查询是否在电话白名单
                String telephoneNumber = DataParseUtil.jSonString("telephonyNumber",extra);
                MDM.isTelephonyWhiteListByNumber(orderCode,telephoneNumber);
                break;*/
            /*case OrderConfig.QueryTelephonyWhiteList:  //查询电话白名单
                MDM.queryTelephonyWhiteList(orderCode);
                break;*/
            /***********************************系统切换***********************************************/
            /*case OrderConfig.SwitchContainer:
                LogUtil.writeToFile(TAG, "switchContainer receive");;
                MDM.switchContainer();
                LogUtil.writeToFile(TAG, "switchContainer finish");
                break;
            case OrderConfig.DisableSwitching:
                MDM.disableSwitching();
                break;
            case OrderConfig.EnableSwitching:
                MDM.enableSwitching();
                break;
            case OrderConfig.IsInSecureContainer:
                MDM.isInSecureContainer(orderCode);
                break;
            case OrderConfig.IsInFgContainer:
                MDM.isInFgContainer(orderCode);
                break;*/
            /***********************************应用管理***********************************************/
            case OrderConfig.SilentInstallAppication:  //应用静默安装Name
                List<DownLoadEntity> entityList = DataParseUtil.jSonInstallApplicationList( orderCode, extra );
                MDM.downloadFile( entityList );
                break;
            case OrderConfig.SilentUninstallAppication:
                List<DownLoadEntity> entityList1 = DataParseUtil.jSonUninstallApplicationList( orderCode, extra );
                MDM.silentUninstallApp( entityList1 );
                break;
            /*case OrderConfig.QueryPkgNameFromUninstallList:
                String packageNameQueryUn = DataParseUtil.jSonString("package_name",extra);
                MDM.queryPkgNameFromUninstallList(orderCode,packageNameQueryUn);
                break;
            case OrderConfig.AddPkgNameToUninstallList:
                String packageNameAddUn = DataParseUtil.jSonString("package_name",extra);
                MDM.addPkgNameToUninstallList(orderCode,packageNameAddUn);
                break;
            case OrderConfig.DeletePkgNameFromUninstallList:
                String packageNameDeleteUn = DataParseUtil.jSonString("package_name",extra);
                MDM.deletePkgNameFromUninstallList(orderCode,packageNameDeleteUn);
                break;
            case OrderConfig.QueryPkgNameFromInstallList:
                String packageNameQueryIn = DataParseUtil.jSonString("package_name",extra);
                MDM.queryPkgNameFromInstallList(orderCode,packageNameQueryIn);
                break;
            case OrderConfig.AddPkgNameToInstallList:
                String packageNameAddIn = DataParseUtil.jSonString("package_name",extra);
                MDM.addPkgNameToInstallList(orderCode,packageNameAddIn);
                break;
            case OrderConfig.DeletePkgNameFromInstallList:
                String packageNameDeleteIn = DataParseUtil.jSonString("package_name",extra);
                MDM.deletePkgNameFromInstallList(orderCode,packageNameDeleteIn);
                break;*/
            /***********************************外设相关***********************************************/
            /*case OrderConfig.EnableBluetooth:
                order = DataParseUtil.jSonEnable(extra);
                MDM.enableBluetooth(orderCode,order);
                break;
            case OrderConfig.IsBluetoothEnabled:
                MDM.isBluetoothEnabled(orderCode);
                break;
            case OrderConfig.QueryMacFromBTSocketList:
                deviceBTMac = DataParseUtil.jSonString("BTMac",extra);
                MDM.queryMacFromBTSocketList(orderCode,deviceBTMac);
                break;
            case OrderConfig.AddMacToBTSocketList:
                deviceBTMac = DataParseUtil.jSonString("BTMac",extra);
                MDM.addMacToBTSocketList(orderCode,deviceBTMac);
                break;
            case OrderConfig.DeleteMacFromBTSocketList:
                deviceBTMac = DataParseUtil.jSonString("BTMac",extra);
                MDM.deleteMacFromBTSocketList(orderCode,deviceBTMac);
                break;
            case OrderConfig.EnableBluetoothOpp:
                //enableBluetoothOpp
                break;
            case OrderConfig.IsBluetoothOppEnabled:
                //isBluetoothOppEnabled
                break;
            case OrderConfig.EnableWifi:
                order = DataParseUtil.jSonEnable(extra);
                MDM.enableWifi(orderCode,order);
                break;
            case OrderConfig.IsWifiEnabled:
                MDM.isWifiEnabled(orderCode);
                break;
            case OrderConfig.OpenWifiOnBG:
                order = DataParseUtil.jSonEnable(extra);
                MDM.openWifiOnBG(orderCode,order);
                break;
            case OrderConfig.IsWifiOpened:
                MDM.isWifiOpened(orderCode);
                break;
            case OrderConfig.QueryMacFromWifiList:
                deviceWifiMac = DataParseUtil.jSonString("WIFIMac",extra);
                MDM.queryMacFromWifiList(orderCode,deviceWifiMac);
                break;
            case OrderConfig.AddMacToWifiList:
                deviceWifiMac = DataParseUtil.jSonString("WIFIMac",extra);
                MDM.addMacToWifiList(orderCode,deviceWifiMac);
                break;
            case OrderConfig.DeleteMacFromWifiList:
                deviceWifiMac = DataParseUtil.jSonString("WIFIMac",extra);
                MDM.deleteMacFromWifiList(orderCode,deviceWifiMac);
                break;
            case OrderConfig.EnableWifiAP:
                order = DataParseUtil.jSonEnable(extra);
                //MDM.enableWifiAP(orderCode,order);
                break;
            case OrderConfig.IsWifiAPEnabled:
                MDM.isWifiAPEnabled(orderCode);
                break;
            case OrderConfig.QueryMacFromWifiAPList:
                deviceWifiMac = DataParseUtil.jSonString("WIFIMac",extra);
                MDM.queryMacFromWifiAPList(orderCode,deviceWifiMac);
                break;
            case OrderConfig.AddMacToWifiAPList:
                deviceWifiMac = DataParseUtil.jSonString("WIFIMac",extra);
                MDM.addMacToWifiAPList(orderCode,deviceWifiMac);
                break;
            case OrderConfig.DeleteMacFromWifiAPList:
                deviceWifiMac = DataParseUtil.jSonString("WIFIMac",extra);
                MDM.deleteMacFromWifiAPList(orderCode,deviceWifiMac);
                break;
            case OrderConfig.SetWifiApOpened:
                //setWifiApOpened
                break;
            case OrderConfig.GetWifiApState:
                MDM.getWifiApState(orderCode);
                break;
            case OrderConfig.IsWifiAPOpened:
                MDM.isWifiAPOpened(orderCode);
                break;
            case OrderConfig.GetWifiApConfiguration:
                //getWifiApConfiguration
                break;
            case OrderConfig.SetWifiApConfiguration:
                //setWifiApConfiguration
                break;
            case OrderConfig.EnableUsb:
                order = DataParseUtil.jSonEnable(extra);
                MDM.enableUsb(orderCode,order);
                break;
            case OrderConfig.IsUsbEnabled:
                MDM.isUsbEnabled(orderCode);
                break;
            case OrderConfig.EnableCamera:
                order = DataParseUtil.jSonEnable(extra);
                MDM.enableCamera(orderCode,order);
                break;
            case OrderConfig.IsCameraEnabled:
                MDM.isCameraEnabled(orderCode);
                break;
            case OrderConfig.EnableSoundRecording:
                order = DataParseUtil.jSonEnable(extra);
                MDM.enableSoundRecording(orderCode,order);
                break;
            case OrderConfig.IsSoundRecordingEnabled:
                MDM.isSoundRecordingEnabled(orderCode);
                break;
            case OrderConfig.EnableNfc:
                MDM.enableNfc(orderCode);
                break;
            case OrderConfig.DisableNfc:
                MDM.disableNfc(orderCode);
                break;
            case OrderConfig.IsNfcEnabled:
                MDM.isNfcEnabled(orderCode);
                break;
            case OrderConfig.OpenNfc:
                MDM.openNfc(orderCode);
                break;
            case OrderConfig.CloseNfc:
                MDM.closeNfc(orderCode);
                break;
            case OrderConfig.EnableSD:
                MDM.enableSD(orderCode);
                break;
            case OrderConfig.DisableSD:
                MDM.disableSD(orderCode);
                break;
            case OrderConfig.CreateApn:
                ContentValues contentValues = DataParseUtil.jSonAPNContentValues(extra);
                MDM.createApn(orderCode,contentValues);
                break;
            case OrderConfig.GetApnList:
                MDM.getApnList(orderCode);
                break;
            case OrderConfig.GetApn:
                String apn_Id = DataParseUtil.jSonString("apn_id",extra);
                MDM.getApn(orderCode,Integer.parseInt(apn_Id));
                break;
            case OrderConfig.GetCurrentApn:
                MDM.getCurrentApn(orderCode);
                break;
            case OrderConfig.SetCurrentApn:
                String apnId = DataParseUtil.jSonString("apn_id",extra);
                MDM.setCurrentApn(orderCode,Integer.parseInt(apnId));
                break;*/
            /**********************************************位置服务相关接口****************************/
            /*case OrderConfig.EnableLocationService:
                order = DataParseUtil.jSonEnable(extra);
                MDM.enableLocationService(orderCode,order);
                break;
            case OrderConfig.IsLocationServiceEnabled:
                MDM.isLocationServiceEnabled(orderCode);
                break;
            case OrderConfig.OpenGpsOnBGSlient:
                MDM.openGpsOnBGSlient(orderCode);
                break;
            case OrderConfig.IsGpsOpenedOnBGSlient:
                MDM.isGpsOpenedOnBGSlient(orderCode);
                break;*/
            /**********************************************系统安全管理************************************/
            case OrderConfig.SetScreenLock:
                String pwd = DataParseUtil.jSonString( "pwd", extra );
                MDM.forceLockScreen(Common.lockTypes[0], pwd );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            /*case OrderConfig.SetPassword:
                String password = DataParseUtil.jSonString("password",extra);
                MDM.setPassword(orderCode,password);
                break;
            case OrderConfig.TakeScreenShot:
                MDM.takeScreenShot();
                break;*/
            case OrderConfig.SetShutDown:
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id) );
                break;
            case OrderConfig.SetFactoryReset:
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            /*case OrderConfig.KillProcess:
                String processName = DataParseUtil.jSonString("process_name",extra);
                MDM.killProcess(orderCode,processName);
                break;*/
            case OrderConfig.WipeData:
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            /**********************************************电话服务管理************************************/
            /*case OrderConfig.EnableSms:
                order = DataParseUtil.jSonEnable(extra);
                MDM.enableSms(orderCode,order);
                break;*/
            /*case OrderConfig.IsSmsEnabled:
                MDM.isSmsEnabled(orderCode);
                break;*/
            /*case OrderConfig.EnableTelePhone:
                order = DataParseUtil.jSonEnable(extra);
                MDM.enableTelephone(orderCode,order);
                break;
            case OrderConfig.IsTelephoneEnabled:
                MDM.isTelephoneEnabled(orderCode);
                break;
            case OrderConfig.InsertContact:
                String mName = DataParseUtil.jSonString("phone_name",extra);
                String mNumber = DataParseUtil.jSonString("phone_number",extra);
                MDM.insertContact(orderCode,mName,mNumber);
                break;
            case OrderConfig.DeleteContactByName:
                String mName1 = DataParseUtil.jSonString("phone_name",extra);
                MDM.deleteContactByName(orderCode,mName1);
                break;
            case OrderConfig.GetAllContactInfo:
                MDM.getAllContactInfo(orderCode);
                break;
            case OrderConfig.MDMAPPUpdate:
                String versionCode = DataParseUtil.jSonString("version_code",extra);
                List<String> version_code = new ArrayList<>();
                version_code.add(versionCode);
               // MDM.silentInstall(orderCode,version_code);
                break;*/
           /* case OrderConfig.HadLink:
                TheTang.getSingleInstance().startNetWorkService();
                break;*/
            case OrderConfig.GetLocationData:
                MDM.getLocationData( orderCode );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.SetPasswordNone:
                TheTang.clearLockState();
                MDM.setPasswordNone();
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.TOLifeContainer:
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.TOSecurityContainer:
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.SetReboot:
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.PlayRingtones:
                MDM.playRingtones( );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.IssuedFile:
                List<DownLoadEntity> fileList = DataParseUtil.jSonFileNameList( orderCode, extra );
                MDM.downloadFile( fileList );
                break;
            case OrderConfig.download_avatar:
                List<DownLoadEntity> pictureList = DataParseUtil.jSonPictureNameList( orderCode, extra );
                MDM.downloadFile( pictureList );
                break;
            /*case OrderConfig.System_Compliance:
            /*case OrderConfig.System_Compliance:
                String order = DataParseUtil.jSon("system_compliance",extra);
                //ExcutePolicyAndCompliance.systemCompliance(order);*/
            case OrderConfig.Send_Message:
                String message = DataParseUtil.jSon( "message", extra );
                mTheTang.addMessage( orderCode, message );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.flow_quota:  //流量定额
                String quota = DataParseUtil.jSon( "quota", extra );
                Log.w( TAG, "流量定额==" + quota );

                try {
                    PreferencesManager.getSingleInstance().setTraffictotal( "quota", (long) Double.parseDouble( quota.toString().trim() ) );
                    PreferencesManager.getSingleInstance().setTraffictotal( "quota_flag", 0 );
                    EventBus.getDefault().post( new NotifyEvent( "flow_flag" ) );  //通知前台更新数据
                } catch (Exception e) {
                    Log.w( TAG, e.toString() );
                    e.printStackTrace();
                }
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.login_out_and_delete_data: //删除
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.get_device_info:
                MDM.sendAllDeviceInfo();
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.start_phone_white:
                MDM.startPhoneWhite(  );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.stop_phone_white:
                MDM.stopPhoneWhite(  );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.upload_debug_log:
                ExceptionLogData exceptionLogData = DataParseUtil.jsonExceptionLog( extra );
                MDM.uploadDebugLog( exceptionLogData );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            /*case OrderConfig.ForceLockScreen:
                MDM.forceLockScreen(Common.lockTypes[0],null);
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;*/
            case OrderConfig.device_update:
                List<DownLoadEntity> entityListDevice = DataParseUtil.jSonDeviceUpdate( orderCode, extra );
                MDM.deviceUpdate( entityListDevice );
                break;
            case OrderConfig.delete_app:
                DeleteAppData deleteAppData = DataParseUtil.jsonDeleteAppData( orderCode, extra );
                MDM.deleteApp( deleteAppData );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            /*case OrderConfig.get_setting_about:
                SettingAboutData settingAboutData = DataParseUtil.jsonSettingAboutData( extra );
                MDM.excuteSettingAbout( settingAboutData );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;*/
            case OrderConfig.enter_sercurity_stratege:
                //preferencesManager.setSecurityData(Common.safetyTosecureFlag,"true");
                SafetyLimitData safetyLimitData = DataParseUtil.jsonSafetyLimitData( extra );
                MDM.storageSafetyLimitData( safetyLimitData );
                //ContainerStratege.excuteSecurityContainerStratege(safetyLimitData);
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.enter_life_stratege:  //安全局域配置
                //PreferencesManager.getSingleInstance().removeSecurityData( Common.safetyTosecureFlag );
                //PreferencesManager.getSingleInstance().removeSecurityData( Common.banTelephoneList );
                //ContainerStratege.excuteLifeContainerStratege();
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            //执行完成后需要返回的命令
            case OrderConfig.DeleteIssuedFile:
                List<DownLoadEntity> fileList1 = DataParseUtil.jSonFileNameList( orderCode, extra );
                MDM.deleteIssuedFile( fileList1 );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.send_limit_strategy:
                PolicyData policyData = DataParseUtil.jsonPolicyData( extra );
                ExcuteLimitPolicy.excuteLimitPolicy( policyData );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.delete_limit_strategy: //删除限制策略
                ExcuteLimitPolicy.excuteLimitPolicy( null );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.send_loseCouplet_strategy:
                LostComplianceData lost = DataParseUtil.jSonLostCompilance( extra );
                ExcuteCompliance.excuteLostCompliance( lost );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.delete_loseCouplet_strategy:
                ExcuteCompliance.excuteLostCompliance( null );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.send_system_strategy:
                SystemComplianceData systemComplianceData = DataParseUtil.jSonSystemCompilance( extra );
                ExcuteCompliance.excuteSystemCompliance( systemComplianceData );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.delete_system_strategy:
                ExcuteCompliance.excuteSystemCompliance( null );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.send_geographical_Fence:
                GeographicalFenceData mGeographicalFenceData = DataParseUtil.jSonGeographicalFence( extra );
                FenceManager.geographicalFence( mGeographicalFenceData );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.delete_geographical_Fence:  //删除地理围栏
                FenceManager.geographicalFence( null );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.send_time_Frence:  //时间围栏
                FenceManager.excuteTimeFence( extra );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.delete_time_Frence: //删除时间围栏
                FenceManager.excute_deleteTimeFenceData( OrderConfig.delete_time_Frence + "" );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.revocation_time_Frence: //卸载时间围栏
                FenceManager.excute_deleteTimeFenceData( OrderConfig.revocation_time_Frence + "" );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.send_app_strategy:
                AppBlackWhiteData appBlackWhiteData = DataParseUtil.jSonAppCompilance( extra );
                ExcuteCompliance.excuteAppCompliance( appBlackWhiteData, appBlackWhiteData.id );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.delete_app_strategy:
                String delete_id = DataParseUtil.jSon( "id", extra );
                ExcuteCompliance.excuteAppCompliance( null, delete_id );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.send_black_White_list:  //黑白名单
                AppBlackWhiteData appBlackWhiteData1 = DataParseUtil.jsonBlackWhiteList( extra );
                MDM.appBlackWhiteList( appBlackWhiteData1 );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.delete_black_White_list:  //删除黑白名单
                MDM.appBlackWhiteList( null );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.send_safe_desk:  //下发安全桌面
                ExcuteCompliance.excuteSafe_Desk( extra );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.delete_safe_desk:  //删除安全桌面
                ExcuteCompliance.deleteSafeDesktop( OrderConfig.delete_safe_desk + "" );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.revocation_safe_desk:  //撤销安全桌面
                ExcuteCompliance.deleteSafeDesktop( OrderConfig.revocation_safe_desk + "" );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            /*case OrderConfig.machine_card_binding:
                MachineCardInfo machineCardInfo = DataParseUtil.jsonMachineCard( extra );
                //MDM.excuteMachineCard( machineCardInfo );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.cancel_machine_card_binding:
                //MDM.excuteMachineCard( null );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;*/
            case OrderConfig.security_chrome:
                SecurityChromeData securityChromeData = DataParseUtil.jsonSecurityData( extra );
                MDM.excuteSecurityChrome( orderCode, securityChromeData );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.delete_security_chrome:
                MDM.excuteSecurityChrome( orderCode, null );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.send_configure_Strategy:  // 下发配置策略
                //这里主要是存储配置策略的数据后给后台返回再执行，因为先执行会导致wifi断开执行返回一直发送失败
                preferencesManager.setConfiguration("json_Configurat", extra);
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.delete_configure_Strategy: // 删除配置策略
                Log.w( TAG, "delete_configure_Strategy" );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            /*case OrderConfig.security_manager:
                SafetyLimitData safetyLimitData = DataParseUtil.jsonSafetyLimitData( extra );
                MDM.storageSafetyLimitData( safetyLimitData );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;*/
            case OrderConfig.put_down_application_fence:
                AppFenceData appFenceData = DataParseUtil.jsonAppFenceData(extra);
                AppFenceExcute.excuteAppFence( appFenceData );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.unstall_application_fence:
                AppFenceExcute.excuteAppFence( null );
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.DELETE_SENSITIVE_WORD:
                SensitiveStrategyInfo deleteInfo = DataParseUtil.getDeletedSensitiveStategyInfo(extra);
                SensiWordManager.newInstance().deleteSensitiveStrategy(context, deleteInfo);
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.SEND_SENSITIVE_WORD_POLICY:
                SensitiveStrategyInfo addInfo = DataParseUtil.getSensitiveStategyInfo(extra);
                SensiWordManager.newInstance().addSensitiveStrategy(context, addInfo);
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.SEND_SMS_BACKUP_POLICY:
                SmsManager.newInstance().executeSmsPolicy(extra, true);
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.DELETE_SMS_BACKUP_POLICY:
                SmsManager.newInstance().executeDeleteSmsPolicy(extra, false);
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.SEND_CALL_RECORDER_BACKUP_POLICY:
                CallRecorderManager.newInstance().executeCallRecorderPolicy(extra, true);
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.DELETE_CALL_RECORDER_BACKUP_POLICY:
                CallRecorderManager.newInstance().executeDeleteCallRecorderPolicy("", false);
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;

            case  OrderConfig.SEND_EntranceGuard_POLICY:
                /*String data = PreferencesManager.getSingleInstance().getData(Common.nfcContainerFlag);
                if (TextUtils.isEmpty( data )){

                   NfcManager.storage(extra);
                }else {
                    //现存一个临时变量
                    PreferencesManager.getSingleInstance().setData(Common.nfcContainer_temporary,extra);
                }*/
                Log.w(TAG,"--------orderCode--"+orderCode+"------id="+id);
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case  OrderConfig.DELETE_EntranceGuard_POLICY:
             /*   if (!TextUtils.isEmpty( PreferencesManager.getSingleInstance().getData(Common.NFCStrategy))){

                    NfcStratege.exitNfcStratege();

                }
                NfcManager.deleteNfc();*/
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case  OrderConfig.SEND_ENTRANCE_GUARD_KEY:  //下发门禁KEY
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.DELETE_ENTRANCE_GUARD_KEY://删除门禁KEY
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.send_trajectory_Strategy:

                TrajectoryPolice.excuteTrajectory(extra);
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            case OrderConfig.delete_trajectory_Strategy:
                TrajectoryPolice.deleteTrajectory();
                EventBus.getDefault().post( new CompleteEvent( orderCode, "true", id ) );
                break;
            default:
                break;
        }
    }
}
