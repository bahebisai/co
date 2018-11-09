package com.zoomtech.emm.features.excute;

import android.content.Context;

import com.zoomtech.emm.definition.OrderConfig;
import com.zoomtech.emm.features.manager.CompleteMessageManager;
import com.zoomtech.emm.utils.DataParseUtil;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.manager.PreferencesManager;

/**
 * Created by lenovo on 2018/1/2.
 */

public class MDMOrderMessageBackImpl {

    boolean result = true;
    public static final String TAG = "MDMOrderMessageImpl";
    public PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

    static Context mContext;
    String orderCode = null;
    String id = null;

    CompleteMessageManager mCompleteMessageManager = new CompleteMessageManager();

    public /*synchronized*/ boolean feedbackOrderMessage(Context context, String extra) {
        mContext = context;

        orderCode = DataParseUtil.jSonCode( extra );

        if (orderCode == null) {
            LogUtil.writeToFile( TAG, "code is null!" );
            return false;
        }

        id = DataParseUtil.jSon( "sendId", extra );

        if (id == null) {
            LogUtil.writeToFile( TAG, "sendId is null!" );
            return false;
        }

        CompleteMessageManager.checkWhetherHadOrder(1);

        switch (Integer.parseInt( orderCode )) {
            /***********************************命令***********************************************/
            case OrderConfig.SetScreenLock:
            case OrderConfig.SetPassword:
            case OrderConfig.TakeScreenShot:
            case OrderConfig.SetShutDown:
            case OrderConfig.SetFactoryReset:
            case OrderConfig.WipeData:
            case OrderConfig.HadLink:
            case OrderConfig.GetLocationData:
            case OrderConfig.SetPasswordNone:
            case OrderConfig.TOLifeContainer:
            case OrderConfig.TOSecurityContainer:
            case OrderConfig.SetReboot:
            case OrderConfig.PlayRingtones:
            case OrderConfig.Send_Message:
            case OrderConfig.flow_quota:  //流量定额
            case OrderConfig.login_out_and_delete_data: //删除
            case OrderConfig.get_device_info:
            case OrderConfig.start_phone_white:
            case OrderConfig.stop_phone_white:
            case OrderConfig.upload_debug_log:
            case OrderConfig.ForceLockScreen:
            case OrderConfig.delete_app:
            case OrderConfig.enter_sercurity_stratege:
            case OrderConfig.enter_life_stratege:
            //case OrderConfig.get_setting_about:
            /***********************************策略***********************************************/
            case OrderConfig.SilentUninstallAppication:
            case OrderConfig.AddTelephonyWhiteList:  //添加电话白名单
            //case OrderConfig.DeleteTelephonyWhiteList:  //删除电话白名单
            case OrderConfig.DeleteIssuedFile:
            case OrderConfig.send_limit_strategy:  //下发限制策略
            case OrderConfig.delete_limit_strategy: //删除限制策略
            case OrderConfig.send_loseCouplet_strategy:
            case OrderConfig.delete_loseCouplet_strategy:
            case OrderConfig.send_system_strategy:
            case OrderConfig.delete_system_strategy:
            case OrderConfig.send_geographical_Fence:
            case OrderConfig.delete_geographical_Fence:  //删除地理围栏
            case OrderConfig.send_time_Frence:  //时间围栏
            case OrderConfig.delete_time_Frence: //删除时间围栏
            case OrderConfig.revocation_time_Frence: //卸载时间围栏
            case OrderConfig.send_app_strategy:
            case OrderConfig.delete_app_strategy:
            case OrderConfig.send_black_White_list:  //黑白名单
            case OrderConfig.delete_black_White_list:  //删除黑白名单
            case OrderConfig.send_safe_desk:  //下发安全桌面
            case OrderConfig.delete_safe_desk:  //删除安全桌面
            case OrderConfig.revocation_safe_desk:  //撤销安全桌面
            // case OrderConfig.machine_card_binding:
            //case OrderConfig.cancel_machine_card_binding:
            case OrderConfig.security_chrome:
            case OrderConfig.delete_security_chrome:
            case OrderConfig.send_configure_Strategy:  // 下发配置策略
            case OrderConfig.delete_configure_Strategy: // 删除配置策略
            case OrderConfig.put_down_application_fence:
            case OrderConfig.unstall_application_fence:
            case OrderConfig.security_manager:
            case OrderConfig.SEND_SENSITIVE_WORD_POLICY: //下发敏感词策略
            case OrderConfig.DELETE_SENSITIVE_WORD: //删除敏感词策略
            case OrderConfig.SEND_SMS_BACKUP_POLICY:
            case OrderConfig.DELETE_SMS_BACKUP_POLICY:
            case OrderConfig.SEND_CALL_RECORDER_BACKUP_POLICY:
            case OrderConfig.DELETE_CALL_RECORDER_BACKUP_POLICY:
            case OrderConfig.SEND_EntranceGuard_POLICY:  //下发门禁策略
            case OrderConfig.DELETE_EntranceGuard_POLICY:  //下发删除门禁策略
            case OrderConfig.SEND_ENTRANCE_GUARD_KEY:     //下发门禁KEY
            case OrderConfig.DELETE_ENTRANCE_GUARD_KEY:    //删除门禁KEY
            case OrderConfig.send_trajectory_Strategy:   //下发轨迹策略
            case OrderConfig.delete_trajectory_Strategy:  //删除下发轨迹
            case OrderConfig.SEND_WIFI_FENCE:
            case OrderConfig.DELETE_WIFI_FENCE:
                mCompleteMessageManager.addMessageResult( orderCode, "false", String.valueOf( System.currentTimeMillis() ), id );
            break;
            /***********************************下载***********************************************/
            case OrderConfig.SilentInstallAppication:  //应用静默安装
            case OrderConfig.IssuedFile:
            case OrderConfig.download_avatar:
            case OrderConfig.device_update:
                mCompleteMessageManager.addMessageResult( orderCode, "false", "-1", id );
                break;
            default:
                result = false;
                break;
        }
        return result;
    }

    //判断命令定时闹钟是否开启，
    /*private void checkWhetherHadOrder() {

        boolean time_out = false;

        List<CompleteMessageData> mList = DatabaseOperate.getSingleInstance().queryAllCompleteResultSql();

        //用于判断闹钟是否存在
        if (mList != null && mList.size() > 0) {
            for (CompleteMessageData mCompleteMessageData : mList) {
                if (mCompleteMessageData.time != "-1" && ( System.currentTimeMillis() - Double.valueOf(mCompleteMessageData.time)) >= 4 * 60 * 1000) {
                    time_out = true;
                }
            }
        }

        if (!time_out)
            return;

        AlarmManager alarmManager = (AlarmManager) TheTang.getSingleInstance().getSystemService( Context.ALARM_SERVICE );
        Intent intent1 = new Intent( );
        intent1.setAction( "timer_task" );
        //第二个参数用于识别AlarmManager
        PendingIntent pendingIntent = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(), 4, intent1, PendingIntent.FLAG_UPDATE_CURRENT );
        alarmManager.setExact( AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2 * 60 * 1000, pendingIntent );
        LogUtil.writeToFile(TAG, "alarmManager.setExact!");
    }*/
}
