package com.xiaomi.emm.features.policy.compliance;

import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.OrderConfig;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.policy.app.ExcuteSafeDesktop;
import com.xiaomi.emm.model.AppBlackWhiteData;
import com.xiaomi.emm.model.ClearDeskData;
import com.xiaomi.emm.model.LostComplianceData;
import com.xiaomi.emm.model.SystemComplianceData;
import com.xiaomi.emm.utils.AppUtils;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.features.presenter.MDM;
import com.xiaomi.emm.features.manager.PreferencesManager;
import com.xiaomi.emm.features.presenter.TheTang;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @date Created by Administrator on 2017/8/9.
 */

public class ExcuteCompliance {
    private static final String TAG = "ExcuteCompliance";
    static PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

    /**
     * 失联合规
     *
     * @param lostComplianceData
     */
    public static void excuteLostCompliance(LostComplianceData lostComplianceData) {

        if (lostComplianceData == null) {

            String lost_name = preferencesManager.getComplianceData(Common.lost_name);

            if (lost_name == null) {
                return;
            }

            TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.delete_loseCouplet_strategy), lost_name);
            TheTang.getSingleInstance().deleteStrategeInfo(String.valueOf(OrderConfig.send_loseCouplet_strategy));
            deleteLostCompliance();
            TheTang.getSingleInstance().whetherCancelLock(5);
        } else {
            TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.send_loseCouplet_strategy), lostComplianceData.lost_name);
            TheTang.getSingleInstance().addStratege(String.valueOf(OrderConfig.send_loseCouplet_strategy), lostComplianceData.lost_name, System.currentTimeMillis() + "");

            storageLostCompliance(lostComplianceData);
        }
    }

    /**
     * 失联合规存储
     *
     * @param lostComplianceData
     */
    private static void storageLostCompliance(LostComplianceData lostComplianceData) {

        preferencesManager.setComplianceData(Common.missingId, String.valueOf(lostComplianceData.missingId));
        preferencesManager.setComplianceData(Common.lost_compliance, lostComplianceData.lost_compliance);
        preferencesManager.setComplianceData(Common.lost_name, lostComplianceData.lost_name);
        preferencesManager.setComplianceData(Common.lost_time, lostComplianceData.lost_time);
        preferencesManager.setComplianceData(Common.lost_password, lostComplianceData.lost_password);
    }

    /**
     * 失联合规删除
     */
    private static void deleteLostCompliance() {
        preferencesManager.removeComplianceData(Common.missingId);
        preferencesManager.removeComplianceData(Common.lost_compliance);
        preferencesManager.removeComplianceData(Common.lost_name);
        preferencesManager.removeComplianceData(Common.lost_time);
        preferencesManager.removeComplianceData(Common.lost_password);
    }

    /**
     * 系统合规
     */
    public static void excuteSystemCompliance(SystemComplianceData systemComplianceData) {

        Intent intent = new Intent(TheTang.getSingleInstance().getContext(), SystemComplianceService.class);

        //preferencesManager.setComplianceData(Common.hadSystemCompliance, "false");
        String system_sd = preferencesManager.getComplianceData(Common.system_sd);
        String system_sim = preferencesManager.getComplianceData(Common.system_sim);

        if (systemComplianceData == null) {

            String system_compliance_name = preferencesManager.getComplianceData(Common.system_compliance_name);

            if (system_compliance_name == null) {
                return;
            }

            TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.delete_system_strategy), system_compliance_name);

            TheTang.getSingleInstance().deleteStrategeInfo(String.valueOf(OrderConfig.send_system_strategy));

            if ("true".equals(system_sd)) {
                TheTang.getSingleInstance().getContext().stopService(intent);
            }

            if ("true".equals(system_sim)) {
                MDM.excuteMachineCard(true);
            }

            deleteSystemCompliance();

            //判断系统违规是否锁屏
            TheTang.getSingleInstance().whetherCancelLock(2);

        } else {

            TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.send_system_strategy), systemComplianceData.systemComplianceName);

            TheTang.getSingleInstance().addStratege(String.valueOf(OrderConfig.send_system_strategy), systemComplianceData.systemComplianceName, System.currentTimeMillis() + "");

            storageSystemCompliance(systemComplianceData);

            //执行SD违规
            if ("true".equals(systemComplianceData.systemSd)) {
                TheTang.getSingleInstance().startService(intent);
            } else {
                if ("true".equals(system_sd)) {
                    TheTang.getSingleInstance().getContext().stopService(intent);
                }
            }

            //执行SIM卡违规
            if ("true".equals(systemComplianceData.systemSim)) {
                MDM.excuteMachineCard(false);
            } else {
                if ("true".equals(system_sim)) {
                    MDM.excuteMachineCard(true);
                }
            }

        }
    }

    /**
     * 系统合规存储
     *
     * @param systemComplianceData
     */
    private static void storageSystemCompliance(SystemComplianceData systemComplianceData) {
        preferencesManager.setComplianceData(Common.system_compliance, systemComplianceData.systemCompliance);

        preferencesManager.setComplianceData(Common.system_compliance_id, systemComplianceData.systemComplianceId);
        preferencesManager.setComplianceData(Common.system_compliance_name, systemComplianceData.systemComplianceName);
        preferencesManager.setComplianceData(Common.system_compliance_pwd, systemComplianceData.lockPwd);

        preferencesManager.setComplianceData(Common.system_sd, systemComplianceData.systemSd);
        //如果手机中有sd卡，获得其cid并保存，用于sd变更判断
        //if (TextUtils.isEmpty(preferencesManager.getComplianceData(Common.system_sd_id))) {
            //preferencesManager.setComplianceData(Common.system_sd_id, TheTang.getSingleInstance().getSDCardId());
        //}

        preferencesManager.setComplianceData(Common.system_sim, systemComplianceData.systemSim);
        //if (TextUtils.isEmpty(preferencesManager.getComplianceData(Common.iccid_card))) {
           // TelephonyManager telephonyManager = (TelephonyManager) TheTang.getSingleInstance().getContext().getSystemService(Service.TELEPHONY_SERVICE);
            //判断是否设备是否有插入SIM卡
            //if (telephonyManager.getSubscriberId() != null) {
                //preferencesManager.setComplianceData(Common.iccid_card, telephonyManager.getSimSerialNumber());
            //}
        //}

    }

    /**
     * 删除系统合规数据
     */
    private static void deleteSystemCompliance() {
        preferencesManager.removeComplianceData(Common.system_compliance);
        preferencesManager.removeComplianceData(Common.system_compliance_id);
        preferencesManager.removeComplianceData(Common.system_compliance_name);

        preferencesManager.removeComplianceData(Common.system_compliance_pwd);
        preferencesManager.removeComplianceData(Common.system_sd);
        preferencesManager.removeComplianceData(Common.system_sim);

        //清除sd、sim信息
        preferencesManager.removeComplianceData(Common.system_sd_id);
        preferencesManager.removeComplianceData(Common.iccid_card);
        preferencesManager.removeComplianceData(Common.iccid_card1);
    }

    /**
     * 应用合规
     */
    public static void excuteAppCompliance(AppBlackWhiteData appBlackWhiteData, String id) {

        if (appBlackWhiteData == null) {

            String app_compliance_name = preferencesManager.getComplianceData(Common.app_compliance_name);

            if (app_compliance_name == null) {
                LogUtil.writeToFile(TAG, "app_compliance_name = " + app_compliance_name);
                return;
            }

            TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.delete_app_strategy), app_compliance_name);
            TheTang.getSingleInstance().deleteStrategeInfo(String.valueOf(OrderConfig.send_app_strategy));

            deleteAppCompliance();

            TheTang.getSingleInstance().whetherCancelLock(1);

        } else {

            TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.send_app_strategy), appBlackWhiteData.name);

            //删除黑白名单
            boolean black = DatabaseOperate.getSingleInstance().queryStrageInfo(String.valueOf(OrderConfig.send_black_list));
            boolean white = DatabaseOperate.getSingleInstance().queryStrageInfo(String.valueOf(OrderConfig.send_White_list));

            if (black) {
                DatabaseOperate.getSingleInstance().deleteSimpleStrategeInfo(String.valueOf(OrderConfig.send_black_list));
            }

            if (white) {
                DatabaseOperate.getSingleInstance().deleteSimpleStrategeInfo(String.valueOf(OrderConfig.send_White_list));
            }

            TheTang.getSingleInstance().addStratege(String.valueOf(OrderConfig.send_app_strategy),
                    appBlackWhiteData.name, System.currentTimeMillis() + "");


            storageAppCompliance(appBlackWhiteData);

            scanAppWhetherCompliance(appBlackWhiteData);

        }
    }

    /**
     * 存储应用合规数据
     *
     * @param appBlackWhiteData
     */
    public static void storageAppCompliance(AppBlackWhiteData appBlackWhiteData) {
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

        if (preferencesManager.getOtherData(Common.appManagerType) != null) {
            deleteAppCompliance();
        }

        preferencesManager.setOtherData(Common.appManagerType, "2");
        preferencesManager.setComplianceData(Common.appType, appBlackWhiteData.type + "");
        preferencesManager.setComplianceData(Common.app_compliance_name, appBlackWhiteData.name);
        preferencesManager.setComplianceData(Common.app_compliance_id, appBlackWhiteData.id);
        preferencesManager.setComplianceData(Common.app_compliance_pwd, appBlackWhiteData.lockPwd);
        DatabaseOperate.getSingleInstance().addAppWhiteList(appBlackWhiteData.appList);
    }

    /**
     * 删除应用合规数据
     */
    public static void deleteAppCompliance() {
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        preferencesManager.removeOtherData(Common.appManagerType);
        preferencesManager.removeComplianceData(Common.appType);
        preferencesManager.removeComplianceData(Common.app_compliance_name);
        preferencesManager.removeComplianceData(Common.app_compliance_id);
        preferencesManager.removeComplianceData(Common.excute_appCompliance);
        preferencesManager.removeComplianceData(Common.app_compliance_pwd);
        DatabaseOperate.getSingleInstance().deleteAllApp();
    }


    /**
     * 下发安全桌面策略
     *
     * @param extra
     */
    public static void excuteSafe_Desk(String extra) {
        if (TextUtils.isEmpty(extra)) {
            Log.w(TAG, "下发安全桌面策略为空");
            LogUtil.writeToFile(TAG, "下发安全桌面策略为空");
            return;
        }

        storageSafeDesktop(extra);
        //回调给服务

        //如果没有安全策略区域
        if (TextUtils.isEmpty(preferencesManager.getSecurityData(Common.safetyTosecureFlag))) {

            if (TextUtils.isEmpty(preferencesManager.getFenceData(Common.setToSecureDesktop)) ||
                    "2".equals(preferencesManager.getFenceData(Common.setToSecureDesktop)) ||
                    TextUtils.isEmpty(preferencesManager.getFenceData(Common.insideAndOutside)) ||
                    "false".equals(preferencesManager.getFenceData(Common.insideAndOutside))) {

                //如果没有时间围栏策略，或者当前处于时间围栏外，则可以执行安全桌面策略
                ExcuteSafeDesktop.excute_SafeDesktop();

            }
        }
    }

    /**
     * 删除安全桌面策略
     */
    public static void deleteSafeDesktop(String code) {
        if (TextUtils.isEmpty(preferencesManager.getSafedesktopData(Common.CODE))) {
            Log.w(TAG, "下发删除安全桌面策略 由于本地没有安全桌面策略数据所以不执行");
            LogUtil.writeToFile(TAG, "下发删除安全桌面策略 由于本地没有安全桌面策略数据所以不执行");
            return;
        }


        TheTang.getSingleInstance().addMessage(OrderConfig.delete_safe_desk + "",
                preferencesManager.getSafedesktopData("safeDesk_name"));

        TheTang.getSingleInstance().deleteStrategeInfo(OrderConfig.send_safe_desk + "");


        preferencesManager.setSafedesktopData(Common.CODE, code);

        //回调给服务

        String id = preferencesManager.getSafedesktopData(Common.ID);
        String code1 = preferencesManager.getSafedesktopData(Common.CODE) + "";
        String alias = preferencesManager.getSafedesktopData(Common.alias);

        //先删除安全桌面策略数据，再执行
        preferencesManager.clearSafedesktopData();
        if (TextUtils.isEmpty(preferencesManager.getSecurityData(Common.safetyTosecureFlag))) {

            if (TextUtils.isEmpty(preferencesManager.getFenceData(Common.setToSecureDesktop)) || TextUtils.isEmpty(preferencesManager.getFenceData(Common.insideAndOutside)) ||
                    "false".equals(preferencesManager.getFenceData(Common.insideAndOutside)) || "2".equals(preferencesManager.getFenceData(Common.setToSecureDesktop))) {

                Log.w(TAG, "如果没有时间围栏策略，或者当前处于时间围栏外，则可以执行删除安全桌面策略");
                //如果没有时间围栏策略，或者当前处于时间围栏外，则可以执行安全桌面策略
                ExcuteSafeDesktop.excute_IntentWorkFragment();

            }
        }
    }

    private static void storageSafeDesktop(String extra) {
        try {


            ClearDeskData deskData = new Gson().fromJson(extra, ClearDeskData.class);
            ClearDeskData.PolicyBean policyBean = deskData.getPolicy().get(0);

            //添加到策列表
            TheTang.getSingleInstance().addStratege(OrderConfig.send_safe_desk + "",
                    policyBean.getName(), System.currentTimeMillis() + "");
            //添加消息界面
            TheTang.getSingleInstance().addMessage(OrderConfig.send_safe_desk + "",
                    policyBean.getName());

            //添加信息
            preferencesManager.setSafedesktopData(Common.CODE, deskData.getCode());
            preferencesManager.setSafedesktopData(Common.ID, deskData.getId());

            preferencesManager.setSafedesktopData("allowNotice", policyBean.getAllowNotice());

            preferencesManager.setSafedesktopData("safeDesk_name", policyBean.getName());

            preferencesManager.setSafedesktopData("allowNotice", policyBean.getAllowNotice());
            preferencesManager.setSafedesktopData("defaultDesktop", policyBean.getDefaultDesktop());
            preferencesManager.setSafedesktopData("password", policyBean.getPassword());
            preferencesManager.setSafedesktopData("passwordOrNot", policyBean.getPasswordOrNot());
            //通
            preferencesManager.setSafedesktopData("displayCall", policyBean.getDisplayCall());
            preferencesManager.setSafedesktopData("displayContacts", policyBean.getDisplayContacts());
            preferencesManager.setSafedesktopData("displayMessage", policyBean.getDisplayMessage());

            if (policyBean.getApplicationProgram() != null && policyBean.getApplicationProgram().size() > 0) {
                preferencesManager.setSafedesktopData(Common.applicationProgram, new Gson().toJson(policyBean.getApplicationProgram()));
            } else {
                preferencesManager.setSafedesktopData(Common.applicationProgram, null);

            }
            Log.w(TAG, "存储安全桌面策略完毕");
            LogUtil.writeToFile(TAG, "存储安全桌面策略完毕");

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.writeToFile(TAG, "存储解析失败");
        }
    }

    /**
     * 应用合规处理
     *
     * @param appBlackWhiteData
     */
    private static void scanAppWhetherCompliance(AppBlackWhiteData appBlackWhiteData) {

        List<LauncherActivityInfo> launcherActivityInfoList = AppUtils.getLauncherNoSystemApp();

        List<String> apps = new ArrayList<>();

        for (LauncherActivityInfo launcherActivityInfo : launcherActivityInfoList) {
            apps.add(launcherActivityInfo.getApplicationInfo().packageName);
        }

        //黑名单
        if ("0".equals(appBlackWhiteData.type)) {

            TheTang.getSingleInstance().appBlackListCompliance(appBlackWhiteData.appList, apps);
            //白名单
        } else {

            TheTang.getSingleInstance().appWhiteListCompliance(appBlackWhiteData.appList, apps);

        }
    }

}
