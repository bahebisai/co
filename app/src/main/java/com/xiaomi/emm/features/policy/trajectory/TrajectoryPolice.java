package com.xiaomi.emm.features.policy.trajectory;

/*
 *  @项目名：  MDM 
 *  @包名：    com.vivo.emm.features.policy.trajectory
 *  @文件名:   TrajectoryPolice
 *  @创建者:   lenovo
 *  @创建时间:  2018/7/28 18:39
 *  @描述：    TODO
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaomi.emm.base.BaseApplication;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.OrderConfig;
import com.xiaomi.emm.features.event.NotifyEvent;
import com.xiaomi.emm.features.event.NotifySafedesk;
import com.xiaomi.emm.features.policy.fence.TimeFenceReceiver;
import com.xiaomi.emm.model.TimeFenceData;
import com.xiaomi.emm.model.TrajectoryData;
import com.xiaomi.emm.utils.DataParseUtil;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.features.presenter.MDM;
import com.xiaomi.emm.features.manager.PreferencesManager;
import com.xiaomi.emm.features.presenter.TheTang;
import com.xiaomi.emm.view.activity.SafeDeskActivity;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;

public class TrajectoryPolice {

    public static final String TAG = "TrajectoryPolice";
    private static TimeUtils timeUtils;

    public void execute() {

    }


    public static void storageTrajectory(TrajectoryData trajectoryData) {
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        //时间日期范围
        preferencesManager.setTrajectoryData(Common.startimeRage, trajectoryData.strategy.startDateRange);
        preferencesManager.setTrajectoryData(Common.endTimeRage, trajectoryData.strategy.endDateRange);
        preferencesManager.setTrajectoryData(Common.trajectoryID, trajectoryData.strategy.id);
        preferencesManager.setTrajectoryData(Common.trajectoryName, trajectoryData.strategy.name);
        preferencesManager.setTrajectoryData(Common.frequency, trajectoryData.strategy.frequency);
        Log.w(TAG, "trajectoryData---" + trajectoryData.toString());
        //时间单元
        if (trajectoryData.strategy.timeFenceUnits != null) {
            String json_timeFenceUnits = new Gson().toJson(trajectoryData.strategy.timeFenceUnits);
            preferencesManager.setTrajectoryData(Common.timeUnit, json_timeFenceUnits);
        } else {
            preferencesManager.setTrajectoryData(Common.timeUnit, null);
        }
        Log.w(TAG, "timeUnit---" + trajectoryData.strategy.timeFenceUnits.toString());

    }


    public static void excuteTrajectory(String extra) {

        if (TextUtils.isEmpty(extra)) {
            Log.w(TAG, "发下来的轨迹为空");
            return;
        }
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        TrajectoryData trajectoryData = DataParseUtil.jsonToData(TrajectoryData.class, extra);


        TheTang.getSingleInstance().addMessage(OrderConfig.send_trajectory_Strategy + "", trajectoryData.strategy.name);

        TheTang.getSingleInstance().addStratege(OrderConfig.send_trajectory_Strategy + "", trajectoryData.strategy.name, System.currentTimeMillis() + "");
        String timeUnit = preferencesManager.getTrajectoryData(Common.timeUnit);
        if (!TextUtils.isEmpty(timeUnit)) {

            Context context = TheTang.getSingleInstance().getContext();
            context.stopService(new Intent(context, TrajectoryFenceService.class));
            preferencesManager.clearTrajectoryData();
            if (timeUtils != null){
                timeUtils.cancelTimeReceive();
            }
        }

        storageTrajectory(trajectoryData);

        doTrajectoryPolice(preferencesManager);


    }

    public static void doTrajectoryPolice(PreferencesManager preferencesManager) {
        //强制定位打开
        MDM.forceLocationService();

        String timeUnit = preferencesManager.getTrajectoryData(Common.timeUnit);
        Log.w(TAG, "timeUnit---" + timeUnit);
        if (TextUtils.isEmpty(timeUnit)) {
            Log.w(TAG, "下发下来的轨迹时间为空不执行");

        } else {

            Type type = new TypeToken<ArrayList<TimeFenceData.PolicyBean.TimeUnitBean>>() {
            }.getType();
            ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> timeUnitBean = new Gson().fromJson(timeUnit, type);

            timeUtils = new TimeUtils();
            timeUtils.excuteTimeReceiver(timeUnitBean, preferencesManager.getTrajectoryData(Common.startimeRage), preferencesManager.getTrajectoryData(Common.endTimeRage), new TimeUtils.OnTimeUtilsListener() {
                @Override
                public void setBooleanFlag(boolean timeFlag) {
                    if (timeFlag) {
                        String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
                        String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
                        int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
                        Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers);

                        Log.w(TAG, "---timeFlag-开启轨迹服务-" + timeFlag);
                        Context context = TheTang.getSingleInstance().getContext();
                        context.stopService(new Intent(context, TrajectoryFenceService.class));
                        TheTang.getSingleInstance().startService(new Intent(context, TrajectoryFenceService.class));

                    } else {
                        String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
                        String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
                        int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
                        Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers);

                        Log.w(TAG, "---timeFlag-停止轨迹服务-");
                        if (preferencesManager.getAppFenceData(Common.appFenceRadius) == null || "0".equals(preferencesManager.getAppFenceData(Common.appFenceRadius)) || TextUtils.isEmpty(preferencesManager.getFenceData(Common.latitude))) {

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
                        Context context = TheTang.getSingleInstance().getContext();
                        context.stopService(new Intent(context, TrajectoryFenceService.class));
                    }
                }

                @Override
                public void createReceiverFail(int errorCode) {

                }

                @Override
                public void overtime_EndTimeRage() {
                    String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
                    String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
                    int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
                    Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers);

                    //时间日期已经超过了，所以清除所欲的数据
                    //如果没有应用围栏的定位服务，则关闭
                    if (preferencesManager.getAppFenceData(Common.appFenceRadius) == null || "0".equals(preferencesManager.getAppFenceData(Common.appFenceRadius)) || TextUtils.isEmpty(preferencesManager.getFenceData(Common.latitude))) {

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
                    Context context = TheTang.getSingleInstance().getContext();
                    context.stopService(new Intent(context, TrajectoryFenceService.class));
                    timeUtils.cancelTimeReceive();
                    timeUtils = null;

                }
            });




        }





    }


    public static void deleteTrajectory() {

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        String trajectoryName = preferencesManager.getTrajectoryData(Common.trajectoryName);

        if (TextUtils.isEmpty(trajectoryName)) {
            Log.w(TAG, "本地没有轨迹数据所以不执行");
            LogUtil.writeToFile(TAG, "本地没有轨迹数据所以不执行");
            return;
        }


        TheTang.getSingleInstance().addMessage(OrderConfig.delete_trajectory_Strategy + "", trajectoryName);

        TheTang.getSingleInstance().deleteStrategeInfo(OrderConfig.send_trajectory_Strategy+"" );

        if (timeUtils !=null){

            timeUtils.cancelTimeReceive();
            timeUtils = null;
        }

        Context context = TheTang.getSingleInstance().getContext();
        context.stopService(new Intent(context,TrajectoryFenceService.class));


        //如果没有应用围栏的定位服务，则关闭
        if (preferencesManager.getAppFenceData(Common.appFenceRadius) == null || "0".equals(preferencesManager.getAppFenceData(Common.appFenceRadius)) || TextUtils.isEmpty(preferencesManager.getFenceData(Common.latitude))) {

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



        //清除所有的数据
        preferencesManager.clearTrajectoryData();

    }


    /**
     * 取消时间广播
     */
    public static void cancelTimeRecevie() {
        Intent intent_CancleReceiver = new Intent(TheTang.getSingleInstance().getContext(), /*AlarmReceiver1.class*/TimeFenceReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 119, intent_CancleReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) TheTang.getSingleInstance().getContext().getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
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
            // String    date    =    sDateFormat.format(new    java.util.Date());
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
                    PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 119, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
                    //   AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                    am.cancel(pi);
                    //  preferencesManager.clearFenceData();
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
}
