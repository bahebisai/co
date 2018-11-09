package com.zoomtech.emm.features.policy.fence;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.zoomtech.emm.base.BaseApplication;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.definition.OrderConfig;
import com.zoomtech.emm.features.db.DatabaseOperate;
import com.zoomtech.emm.features.event.NotifyEvent;
import com.zoomtech.emm.features.event.NotifySafedesk;
import com.zoomtech.emm.features.lockscreen.TimeFenceService;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.model.GeographicalFenceData;
import com.zoomtech.emm.model.TimeFenceData;
import com.zoomtech.emm.utils.DataParseUtil;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.view.activity.SafeDeskActivity;

import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Administrator on 2017/8/9.
 */

public class FenceManager {
    private static final String TAG = "FenceManager";

    /**
     * 地理围栏
     */
    public static void geographicalFence(GeographicalFenceData geographicalFenceData) {
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        Intent intent = new Intent(TheTang.getSingleInstance().getContext(), GaodeGeographicalFenceService.class);
        if (geographicalFenceData == null) {
            LogUtil.writeToFile(TAG, "geographicalFence delete!");
            Log.w(TAG, "geographicalFence delete!");
            //如果没有应用围栏的定位服务，则关闭
            if (preferencesManager.getAppFenceData(Common.appFenceRadius) == null ||
                    "0".equals(preferencesManager.getAppFenceData(Common.appFenceRadius))) {
                MDM.closeForceLocation();
                if (preferencesManager.getPolicyData(Common.middle_policy) != null) {
                    if ("0".equals(preferencesManager.getPolicyData(Common.middle_allowLocation))) {
                        MDM.enableLocationService(false);
                    } else {
                        MDM.enableLocationService(true);
                    }
                } else {
                    if ("0".equals(preferencesManager.getPolicyData(Common.default_allowLocation))) {
                        MDM.enableLocationService(false);
                    } else {
                        MDM.enableLocationService(true);
                    }
                }
            }
            String geographical_fence_name = preferencesManager.getFenceData(Common.geographical_fence_name);
            if (geographical_fence_name == null) {
                return;
            }
            TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.delete_geographical_Fence), geographical_fence_name);
            TheTang.getSingleInstance().deleteStrategeInfo(String.valueOf(OrderConfig.send_geographical_Fence));
            TheTang.getSingleInstance().getContext().stopService(intent);
            preferencesManager.setFenceData("newContainSwitching", "1");
            if (TextUtils.isEmpty(preferencesManager.getSecurityData(Common.safetyTosecureFlag))) {
                FenceExcute.excuteGeographicalFence(false, true);
            }
            preferencesManager.clearFenceData();
            //TheTang.getSingleInstance().whetherCancelLock(4);
        } else {
            if (TextUtils.isEmpty(preferencesManager.getSecurityData(Common.safetyTosecureFlag))) {
                LogUtil.writeToFile(TAG, "geographicalFence add!");
                Log.w(TAG, "geographicalFence add!");
                /**
                 * 替换时间围栏，去除时间围栏数据
                 */
                String endTimeRage = preferencesManager.getFenceData(Common.endTimeRage);
                if (!TextUtils.isEmpty(endTimeRage)) {
                    //停止服务
                    //停止时间围栏的服务
                    Intent intentTime = new Intent(TheTang.getSingleInstance().getContext(), TimeFenceService.class);
                    TheTang.getSingleInstance().getContext().stopService(intentTime);
                    //取消时间围栏的广播
                    cancelTimeRecevie();
                    SimpleDateFormat formats = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    if (preferencesManager.getFenceData(Common.endTimeRage).contains("T")) {
                        endTimeRage = endTimeRage.split("T")[0].trim();
                    }
                    try {
                        Date parse1 = formats.parse(endTimeRage + " 23:59");
                        if (System.currentTimeMillis() < parse1.getTime()) {
                            preferencesManager.setFenceData("newContainSwitching", "1");
                            preferencesManager.setFenceData("desktop", Common.setToSecureDesktop);
                            String securityContainer = preferencesManager.getSecurityData(Common.securityContainer);

                            if (TextUtils.isEmpty(securityContainer) || "false".equals(securityContainer)) {
                                FenceExcute.excuteGeographicalFence(false, true);
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    backToDefault(geographicalFenceData, preferencesManager);
                    preferencesManager.clearTimefenceData();
                }
                //判断是否已经存在地理围栏
                if (preferencesManager.getFenceData(Common.geographical_fence_name) != null) {
                    TheTang.getSingleInstance().getContext().stopService(intent);
                    backToDefault(geographicalFenceData, preferencesManager);
                }
            }

            storageGeographicalFence(geographicalFenceData);
            TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.send_geographical_Fence),
                    geographicalFenceData.geographical_fence_name);
            TheTang.getSingleInstance().addStratege(String.valueOf(OrderConfig.send_geographical_Fence),
                    geographicalFenceData.geographical_fence_name, System.currentTimeMillis() + "");
            if (TextUtils.isEmpty(preferencesManager.getSecurityData(Common.safetyTosecureFlag))) {
                MDM.closeForceLocation();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MDM.forceLocationService();
                TheTang.getSingleInstance().startService(intent);
            }
        }
    }

    private static void backToDefault(GeographicalFenceData geographicalFenceData, PreferencesManager preferencesManager) {
        preferencesManager.setFenceData("newContainSwitching", "1");
        preferencesManager.setFenceData("desktop", geographicalFenceData.setToSecureDesktop);
        if (TextUtils.isEmpty(preferencesManager.getSecurityData(Common.safetyTosecureFlag))) {
            String securityContainer = preferencesManager.getSecurityData(Common.securityContainer);
            if (TextUtils.isEmpty(securityContainer) || "false".equals(securityContainer)) {
                FenceExcute.excuteGeographicalFence(false, true);
            }
        }
        preferencesManager.clearFenceData();
    }

    /**
     * 地理围栏缓存
     *
     * @param geographicalFenceData
     */
    private static void storageGeographicalFence(GeographicalFenceData geographicalFenceData) {

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        preferencesManager.setFenceData(Common.geographical_fence, geographicalFenceData.geographical_fence);
        preferencesManager.setFenceData(Common.geographical_fence_name, geographicalFenceData.geographical_fence_name);
        preferencesManager.setFenceData(Common.longitude, geographicalFenceData.fence_longitude);
        preferencesManager.setFenceData(Common.latitude, geographicalFenceData.fence_latitude);
        preferencesManager.setFenceData(Common.radius, geographicalFenceData.radius);
        preferencesManager.setFenceData(Common.geo_id, geographicalFenceData.geo_id);

        //device
        preferencesManager.setFenceData(Common.allowDevice, geographicalFenceData.allowDevice);
        preferencesManager.setFenceData(Common.hiddenNetwork, geographicalFenceData.hiddenNetwork);
        preferencesManager.setFenceData(Common.allowBluetooth, geographicalFenceData.allowBluetooth);
        preferencesManager.setFenceData(Common.configureWifi, geographicalFenceData.configureWifi);
        preferencesManager.setFenceData(Common.allowAutomaticJoin, geographicalFenceData.allowAutomaticJoin);
        preferencesManager.setFenceData(Common.allowConfigureWifi, geographicalFenceData.allowConfigureWifi);
        preferencesManager.setFenceData(Common.lockScreen, geographicalFenceData.lockScreen);
        preferencesManager.setFenceData(Common.lockPwd, geographicalFenceData.lockPassword);

        preferencesManager.setFenceData(Common.allowMobileData, geographicalFenceData.allowMobileData);
        preferencesManager.setFenceData(Common.allowContainSwitching, geographicalFenceData.allowContainSwitching);
        preferencesManager.setFenceData(Common.safeType, geographicalFenceData.safeType);
        preferencesManager.setFenceData(Common.allowCamera, geographicalFenceData.allowCamera);
        preferencesManager.setFenceData(Common.allowCloseWifi, geographicalFenceData.allowCloseWifi);
        preferencesManager.setFenceData(Common.allowOpenWifi, geographicalFenceData.allowOpenWifi);

        preferencesManager.setFenceData(Common.mobileHotspot, geographicalFenceData.mobileHotspot);
        //preferencesManager.setFenceData( Common.locationService, geographicalFenceData.locationService);
        preferencesManager.setFenceData(Common.matTransmission, geographicalFenceData.matTransmission);
        preferencesManager.setFenceData(Common.shortMessage, geographicalFenceData.shortMessage);
        preferencesManager.setFenceData(Common.soundRecording, geographicalFenceData.soundRecording);

        preferencesManager.setFenceData(Common.wifi_ssid, geographicalFenceData.ssid);
        preferencesManager.setFenceData(Common.wifi_password, geographicalFenceData.wifiPassword);

        preferencesManager.setFenceData(Common.banScreenshot, geographicalFenceData.banScreenshot);
        preferencesManager.setFenceData(Common.allowDropdown, geographicalFenceData.allowDropdown);
        preferencesManager.setFenceData(Common.allowReset, geographicalFenceData.allowReset);
        preferencesManager.setFenceData(Common.allowNFC, geographicalFenceData.allowNFC);
        preferencesManager.setFenceData(Common.allowModifySystemtime, geographicalFenceData.allowModifySystemtime);

        preferencesManager.setFenceData(Common.geo_telephone, geographicalFenceData.telephone);
        preferencesManager.setFenceData(Common.geo_telephoneWhiteList, geographicalFenceData.telephoneWhiteList);

        preferencesManager.setFenceData(Common.allowChrome, geographicalFenceData.allowChrome);

        //desktop
        preferencesManager.setFenceData(Common.allowDesktop, geographicalFenceData.allowDesktop);
        preferencesManager.setFenceData(Common.displayContacts, geographicalFenceData.displayContacts);
        preferencesManager.setFenceData(Common.displayMessage, geographicalFenceData.displayMessage);
        preferencesManager.setFenceData(Common.displayCall, geographicalFenceData.displayCall);
        preferencesManager.setFenceData(Common.setToSecureDesktop, geographicalFenceData.setToSecureDesktop);

        if (geographicalFenceData.json_Apploication != null && !"null".equals(geographicalFenceData.json_Apploication)) {
            preferencesManager.setFenceData(Common.applicationProgram, geographicalFenceData.json_Apploication);
        } else {
            preferencesManager.setFenceData(Common.applicationProgram, null);
        }

        //domain
        preferencesManager.setFenceData(Common.allowDoubleDomain, geographicalFenceData.allowDoubleDomain);
        preferencesManager.setFenceData(Common.twoDomainControl, geographicalFenceData.twoDomainControl);
    }


    /**
     * 时间围栏缓存
     *
     * @param timeFenceData
     */
    public static void storageTimeFenceData(TimeFenceData timeFenceData) {

        Log.w(TAG, "zhi  时间围栏 存储完11");
        TimeFenceData.PolicyBean bean = timeFenceData.getPolicy().get(0);

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

        //添加到策列表
        TheTang.getSingleInstance().addStratege(OrderConfig.send_time_Frence + "",
                bean.getName(), System.currentTimeMillis() + "");

        TheTang.getSingleInstance().addMessage(OrderConfig.send_time_Frence + "",
                bean.getName());

        //回调给服务的信息
        preferencesManager.setFenceData(Common.timeFence_name, bean.getName());
        preferencesManager.setFenceData(Common.alias, timeFenceData.getAlias());
        preferencesManager.setFenceData(Common.ID, timeFenceData.getId());
        preferencesManager.setFenceData(Common.CODE, timeFenceData.getCode());

        //device
        if ("2".equals(bean.getHiddenNetwork())) {
            preferencesManager.setFenceData(Common.allowDevice, "false");
        } else {
            preferencesManager.setFenceData(Common.allowDevice, "true");
        }

        preferencesManager.setFenceData(Common.hiddenNetwork, bean.getHiddenNetwork());
        preferencesManager.setFenceData(Common.allowBluetooth, bean.getAllowBluetooth());
        preferencesManager.setFenceData(Common.configureWifi, bean.getConfigureWifi());
        preferencesManager.setFenceData(Common.allowAutomaticJoin, bean.getAllowAutomaticJoin());
        preferencesManager.setFenceData(Common.lockScreen, bean.getLockScreen());
        preferencesManager.setFenceData(Common.lockPwd, bean.getLockPwd());

        preferencesManager.setFenceData(Common.allowMobileData, bean.getAllowMobileData());
        preferencesManager.setFenceData(Common.allowContainSwitching, bean.getAllowDomainSwitching());
        preferencesManager.setFenceData(Common.safeType, bean.getSafeType());
        preferencesManager.setFenceData(Common.allowCamera, bean.getAllowCamera());
        preferencesManager.setFenceData(Common.allowCloseWifi, bean.getAllowCloseWifi());
        preferencesManager.setFenceData(Common.allowOpenWifi, bean.getAllowOpenWifi());
        preferencesManager.setFenceData(Common.wifi_ssid, bean.getSsid());
        preferencesManager.setFenceData(Common.wifi_password, bean.getWifiPassword());
        preferencesManager.setFenceData(Common.allowConfigureWifi, bean.getAllowConfigureWifi());

        //
        preferencesManager.setFenceData(Common.mobileHotspot, bean.getMobileHotspot());
        preferencesManager.setFenceData(Common.locationService, bean.getLocationService());
        preferencesManager.setFenceData(Common.matTransmission, bean.getMatTransmission());
        preferencesManager.setFenceData(Common.shortMessage, bean.getShortMessage());
        preferencesManager.setFenceData(Common.soundRecording, bean.getSoundRecording());

        preferencesManager.setFenceData(Common.banScreenshot, bean.getBanScreenshot());
        preferencesManager.setFenceData(Common.allowDropdown, bean.getAllowDropdown());
        preferencesManager.setFenceData(Common.allowReset, bean.getAllowReset());
        preferencesManager.setFenceData(Common.allowNFC, bean.getAllowNFC());
        preferencesManager.setFenceData(Common.allowModifySystemtime, bean.getAllowModifySystemtime());

        preferencesManager.setFenceData(Common.geo_telephone, bean.getTelephone());
        preferencesManager.setFenceData(Common.geo_telephoneWhiteList, bean.getTelephoneWhiteList());

        //时间日期范围
        preferencesManager.setFenceData(Common.startimeRage, bean.getStartTimeRage());
        preferencesManager.setFenceData(Common.endTimeRage, bean.getEndTimeRage());
        //时间单元
        if (bean.getTimeUnit() != null) {
            String json = new Gson().toJson(bean.getTimeUnit());
            preferencesManager.setFenceData(Common.timeUnit, json);
        } else {
            preferencesManager.setFenceData(Common.timeUnit, null);
        }

        //chrome
        if (TextUtils.isEmpty(bean.getWebpageList()) || "2".equals(bean.getWebpageList())) {
            preferencesManager.setFenceData(Common.allowChrome, null);
        } else {
            if (!TextUtils.isEmpty(bean.getWebpageList()) && "0".equals(bean.getWebpageList())) {
                preferencesManager.setFenceData(Common.allowChrome, "false");
            } else if (!TextUtils.isEmpty(bean.getWebpageList()) && "1".equals(bean.getWebpageList())) {
                preferencesManager.setFenceData(Common.allowChrome, "true");
            }
        }
        preferencesManager.setFenceData(Common.webPageList, bean.getWebpageList());

        //desktop   setToSecureDesktop
        if (TextUtils.isEmpty(bean.getSetToSecureDesktop()) || "2".equals(bean.getSetToSecureDesktop())) {
            preferencesManager.setFenceData(Common.allowDesktop, "false");
        } else {
            preferencesManager.setFenceData(Common.allowDesktop, "true");
        }

        preferencesManager.setFenceData(Common.displayContacts, bean.getDisplayContacts());
        preferencesManager.setFenceData(Common.displayMessage, bean.getDisplayMessage());
        preferencesManager.setFenceData(Common.displayCall, bean.getDisplayCall());
        preferencesManager.setFenceData(Common.setToSecureDesktop, bean.getSetToSecureDesktop());

        //先删除数据库添加到白名单
        DatabaseOperate.getSingleInstance().deleteAllApp();
        if (bean.getApplicationProgram() != null && bean.getApplicationProgram().size() > 0) {
            preferencesManager.setFenceData(Common.applicationProgram, new Gson().toJson(bean.getApplicationProgram()));
        } else {
            preferencesManager.setFenceData(Common.applicationProgram, null);
        }

        //domain
        if (!TextUtils.isEmpty(bean.getTwoDomainControl()) && "2".equals(bean.getTwoDomainControl())) {
            preferencesManager.setFenceData(Common.allowDoubleDomain, "false");
        } else {
            preferencesManager.setFenceData(Common.allowDoubleDomain, "true");
        }
        preferencesManager.setFenceData(Common.twoDomainControl, bean.getTwoDomainControl());
        Log.w(TAG, "zhi  时间围栏 存储完");
    }

    /**
     * 时间围栏
     *
     * @param extra
     */
    public static void excuteTimeFence(String extra) {
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        try {
            /**如果有地理围栏则先把地理围栏清除掉*/
            //停止地理围栏的服务
            Intent intent = new Intent(TheTang.getSingleInstance().getContext(), GaodeGeographicalFenceService.class);
            TheTang.getSingleInstance().getContext().stopService(intent);
            Log.w(TAG, "----------时间围栏策略==" + extra);
            if (TextUtils.isEmpty(extra)) {
                Log.w(TAG, "----------时间围栏策略=extra=为空");
                return;
            }
            TimeFenceData timeFenceData = DataParseUtil.jsonToData(TimeFenceData.class, extra);
            if (TextUtils.isEmpty(preferencesManager.getSecurityData(Common.safetyTosecureFlag))) {
                //如果有地理围栏的数据，则清除掉
                if (!TextUtils.isEmpty(preferencesManager.getFenceData(Common.latitude))) {
                    //如果有应用围栏的定位服务
                    if (preferencesManager.getAppFenceData(Common.appFenceRadius) == null ||
                            "0".equals(preferencesManager.getAppFenceData(Common.appFenceRadius))) {
                        MDM.closeForceLocation();
                        if (preferencesManager.getPolicyData(Common.middle_policy) != null) {
                            if ("0".equals(preferencesManager.getPolicyData(Common.middle_allowLocation))) {
                                MDM.enableLocationService(false);
                            } else {
                                MDM.enableLocationService(true);
                            }
                        } else {
                            if ("0".equals(preferencesManager.getPolicyData(Common.default_allowLocation))) {
                                MDM.enableLocationService(false);
                            } else {
                                MDM.enableLocationService(true);
                            }
                        }
                    }
                    preferencesManager.setFenceData("newContainSwitching", "1");
                    preferencesManager.setFenceData("desktop", timeFenceData.getPolicy().get(0).getSetToSecureDesktop());
                    //初始化之前的一般状态
                    FenceExcute.excuteGeographicalFence(false, true);
                    //清除数据
                    preferencesManager.clearFenceData();
                    preferencesManager.clearTimefenceData();
                }
                //获取时间日期范围
                String endTimeRage = preferencesManager.getFenceData(Common.endTimeRage);
                if (!TextUtils.isEmpty(endTimeRage)) {
                    Log.w(TAG, "如果之前有时间围栏则初始化之前的一般状态");
                    SimpleDateFormat formats = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    if (endTimeRage.contains("T")) {
                        endTimeRage = endTimeRage.split("T")[0].trim();
                    }
                    Date parse1 = formats.parse(endTimeRage + " 23:59");
                    //如果结束日期不超过当前日期说明时间没过期
                    if (System.currentTimeMillis() < parse1.getTime()) {
                        preferencesManager.setFenceData("newContainSwitching", "1");
                        preferencesManager.setFenceData("desktop", timeFenceData.getPolicy().get(0).getSetToSecureDesktop());
                        //初始化之前的一般状态
                        FenceExcute.excuteGeographicalFence(false, true);
                    }
                    preferencesManager.clearFenceData();
                    preferencesManager.clearTimefenceData();
                }
            }
            //把时间策略存储到sp里
            storageTimeFenceData(timeFenceData);
            if (TextUtils.isEmpty(preferencesManager.getSecurityData(Common.safetyTosecureFlag))) {
                //发送广播
                doSendBroadcast();
            }
        } catch (Exception e) {

        }
    }

    /**
     * 发送广播
     */
    public static void doSendBroadcast() {
        /**r如果之前有过时间围栏的闹钟应该先取消闹钟---防止之前下发过时间围栏策略又重新发过一次*/
        cancelTimeRecevie();
        /*********************/
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        //获取时间日期范围
        String startTimeRage = preferencesManager.getFenceData(Common.startimeRage);
        String endTimeRage = preferencesManager.getFenceData(Common.endTimeRage);

        if (startTimeRage != null && endTimeRage != null) {
            /*************************/
            SimpleDateFormat formats = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                if (startTimeRage.contains("T")) {
                    startTimeRage = startTimeRage.split("T")[0].trim();
                }
                if (endTimeRage.contains("T")) {
                    endTimeRage = endTimeRage.split("T")[0].trim();
                }

                Date parse = formats.parse(startTimeRage + " 00:00");
                Date parse1 = formats.parse(endTimeRage + " 23:59");
                AlarmManager am = (AlarmManager) TheTang.getSingleInstance().getContext().getSystemService(ALARM_SERVICE);
                /**如果当前时间已经超过时间范围则返回*/
                if (System.currentTimeMillis() > parse1.getTime()) {
                    Log.w(TAG, "如果当前时间已经超过时间围栏设置的结束时间范围，不知执行闹钟，同时取消策略(可以把本地的时间围栏数据删除掉)");
                    LogUtil.writeToFile(TAG, "当前时间已经超过时间围栏设置的结束时间范围，不知执行闹钟--结束所有的广播，同时取消策略(可以把本地的时间围栏数据删除掉)");
                    /**结束所有的广播，同时取消策略(可以把本地的时间围栏数据删除掉)*/
                    Intent intent_startTimeRage = new Intent(TheTang.getSingleInstance().getContext(), TimeFenceReceiver.class);
                    PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
                    if (am != null) {
                        am.cancel(pi);
                    }
                    preferencesManager.clearTimefenceData();
                    if (BaseApplication.getNewsLifecycleHandler().isSameClassName(SafeDeskActivity.class.getSimpleName())) {
                        EventBus.getDefault().post(new NotifySafedesk(Common.safeActicivty_finsh)); //关闭安全桌面
                        EventBus.getDefault().post(new NotifyEvent());//刷新工作台界面
                    }
                    return;
                }
                Log.w(TAG, "发送广播");
                Intent intent_startTimeRage = new Intent(TheTang.getSingleInstance().getContext(), /*AlarmReceiver1.class*/TimeFenceReceiver.class);
                intent_startTimeRage.setAction("startTimeRage");

                //第二个参数用于识别AlarmManager
                PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);

                //如果设定的时间比当前时间还小则立即执行---设置开始时间的闹钟
                am.setExact(AlarmManager.RTC_WAKEUP, parse.getTime(), pi); //执行一次

                /******************************************/
                Intent intent_endTimeRage = new Intent(TheTang.getSingleInstance().getContext(), /*AlarmReceiver1.class*/TimeFenceReceiver.class);
                intent_endTimeRage.setAction("endTimeRage");
                PendingIntent pi2 = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_endTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);

                //如果设定的时间比当前时间还小则立即执行---设置结束时间的闹钟
                Log.w(TAG, startTimeRage + " 00:00==" + parse.getTime() + "==size==" + endTimeRage + " 23:59===" + parse1.getTime());
                am.setExact(AlarmManager.RTC_WAKEUP, parse1.getTime(), pi2); //执行一次

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }


    public static void excute_deleteTimeFenceData(String code) {
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        /**结束所有的广播，同时取消策略(可以把本地的时间围栏数据删除掉)*/
        cancelTimeRecevie();
        //结束服务
        TheTang.getSingleInstance().getContext().stopService(new Intent(TheTang.getSingleInstance().getContext(), TimeFenceService.class));

        try {
            //获取时间日期范围
            String endTimeRage = preferencesManager.getFenceData(Common.endTimeRage);

            if (endTimeRage == null || endTimeRage.isEmpty()) {
                Log.w(TAG, "时间围栏本地数据为空");
                //防止还有数据再清空
                preferencesManager.clearFenceData();
                return;
            }

            if (!TextUtils.isEmpty(preferencesManager.getFenceData(Common.timeFence_name))) {
                TheTang.getSingleInstance().addMessage(OrderConfig.delete_time_Frence + "",
                        preferencesManager.getFenceData(Common.timeFence_name));
            }
            TheTang.getSingleInstance().deleteStrategeInfo(OrderConfig.send_time_Frence + "");

            if (TextUtils.isEmpty(preferencesManager.getSecurityData(Common.safetyTosecureFlag))) {

                FenceExcute.excuteGeographicalFence(false, true);
            }
            deleteTimeFenceData();
            //回复时间围栏之前的状态
            //TheTang.getSingleInstance().whetherCancelLock(3);
        } catch (Exception e) {

        }
    }

    /**
     * 取消时间广播
     */
    public static void cancelTimeRecevie() {
        Intent intent_CancleReceiver = new Intent(TheTang.getSingleInstance().getContext(), /*AlarmReceiver1.class*/TimeFenceReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_CancleReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) TheTang.getSingleInstance().getContext().getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public static void deleteTimeFenceData() {
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        preferencesManager.removeFenceData(Common.allowDevice);
        preferencesManager.removeFenceData(Common.timeFence_name);

        //device
        preferencesManager.removeFenceData(Common.hiddenNetwork);
        preferencesManager.removeFenceData(Common.allowBluetooth);
        preferencesManager.removeFenceData(Common.configureWifi);
        preferencesManager.removeFenceData(Common.allowAutomaticJoin);
        preferencesManager.removeFenceData(Common.lockScreen);
        preferencesManager.removeFenceData(Common.allowMobileData);
        preferencesManager.removeFenceData(Common.allowContainSwitching);
        preferencesManager.removeFenceData(Common.safeType);
        preferencesManager.removeFenceData(Common.allowCamera);
        preferencesManager.removeFenceData(Common.geo_telephone);
        preferencesManager.removeFenceData(Common.geo_telephoneWhiteList);
        //时间日期范围
        preferencesManager.removeFenceData(Common.startimeRage);
        preferencesManager.removeFenceData(Common.endTimeRage);
        //时间单元

        preferencesManager.removeFenceData(Common.timeUnit);
        preferencesManager.removeFenceData(Common.allowChrome);
        preferencesManager.removeFenceData(Common.webPageList);

        //desktop
        preferencesManager.removeFenceData(Common.allowDesktop);
        preferencesManager.removeFenceData(Common.displayContacts);
        preferencesManager.removeFenceData(Common.displayMessage);
        preferencesManager.removeFenceData(Common.displayCall);
        preferencesManager.removeFenceData(Common.setToSecureDesktop);

        preferencesManager.removeFenceData(Common.applicationProgram);

        //domain
        preferencesManager.removeFenceData(Common.allowDoubleDomain);
        preferencesManager.removeFenceData(Common.twoDomainControl);

        //insideAndOutside
        preferencesManager.removeFenceData(Common.insideAndOutside);

        //防止还有数据再清空
        preferencesManager.clearFenceData();
    }
}
