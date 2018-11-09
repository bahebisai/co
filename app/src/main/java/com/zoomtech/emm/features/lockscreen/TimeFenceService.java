package com.zoomtech.emm.features.lockscreen;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zoomtech.emm.R;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.policy.fence.FenceExcute;
import com.zoomtech.emm.features.policy.fence.TimeFenceReceiver;
import com.zoomtech.emm.model.TimeFenceData;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by lenovo on 2017/8/11.
 */

public class TimeFenceService extends IntentService {
    //获取当前时间
    private SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat mm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private long gtm_add = 3600 * 8 * 1000;
    AlarmManager am = (AlarmManager) TheTang.getSingleInstance().getContext().getSystemService(ALARM_SERVICE);

    public static final String TAG = "TimeFenceService";

    public TimeFenceService() {
        super( "TimeFenceService" );
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onStartCommand" + new Date().toLocaleString());

        if (intent == null) {

            return;
        }

        final String tpye = intent.getStringExtra("TimeFenceReceiver");
        if (tpye == null || TextUtils.isEmpty( tpye )) {
            Log.w("testAppfront", "到这里闹钟TimeFenceService===tpye为空");
            return ;
        }
        //执行时间围栏
        doTimeFence(tpye);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        TheTang.getSingleInstance().startForeground(this,getResources().getString(R.string.time_fence_service),"EMM",6);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*Log.i(TAG, "onStartCommand" + new Date().toLocaleString());

        if (intent == null) {

            return super.onStartCommand(intent, flags, startId);
        }
        final String tpye = intent.getStringExtra("TimeFenceReceiver");
        if (tpye == null || tpye.isEmpty()) {
            Log.w("testAppfront", "到这里闹钟TimeFenceService===tpye为空");
            return super.onStartCommand(intent, flags, startId);
        }
        Log.w("testAppfront", "doTimeFence");
           // 耗时的工作可以放到线程里面执行
           new Thread() {
               public void run() {
                   doTimeFence(tpye);
               }


           }.start();*/
        //TheTang.getSingleInstance().startForeground(this, intent,"时间围栏正在运行！","EMM",6);

        return super.onStartCommand(intent, flags, startId);
    }




    private void  doTimeFence(String type) {
        // SimpleDateFormat formats = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        String dates = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
        /*long     times_Date = 0;//今天 00:00的毫秒时间
        try {
            times_Date = formats.parse(dates).getCallTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        // String tiemfence = PreferencesManager.getSingleInstance().getTimefenceData("tiemfence");
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        String startTimeRage = preferencesManager.getFenceData(Common.startimeRage);




       /* if (tiemfence == null || tiemfence.isEmpty()) {
            Log.w("testAppfront", "到这里TimeFenceService=====此刻时间" + System.currentTimeMillis() + "---获取本地时间围栏数据tiemfence为空");
            return ;
        }*/

        if ("startTimeRage".equals(type)) {
            /**日期范围的开始的第一天*/

            //   Gson gson = new Gson();
            //  TimeFenceData timeFenceData = gson.fromJson(tiemfence, TimeFenceData.class);
            //TimeFenceData.PolicyBean timeFenceBean = timeFenceData.getPolicy().get(0);
            // List<TimeFenceData.PolicyBean.TimeUnitBean> timeUnits = timeFenceBean.getTimeUnit();
            String timeUnit = preferencesManager.getFenceData(Common.timeUnit);
            Type listType = new TypeToken<ArrayList<TimeFenceData.PolicyBean.TimeUnitBean>>(){}.getType();
            ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> timeUnits = new Gson().fromJson(timeUnit, listType);
            if (timeUnits == null) {
                Log.w("testAppfront", "timeUnits为空===" + type);
                return ;
            }
          /*初始化选择执行时间**/
            selcetTimeExcute(timeUnits);
        } else if ("endTimeRage".equals(type)) {
            /**如果接收到时间范围的结束广播*/
            Log.w("testAppfront", "结束的的时间闹钟===" + type);
            /**结束所有的广播，同时取消策略(可以把本地的时间围栏数据删除掉)*/
            Intent intent_startTimeRage = new Intent(TheTang.getSingleInstance().getContext(), TimeFenceReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
            //   AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            am.cancel(pi);
            /**清除本地的时间围栏临时数据*/
            PreferencesManager.getSingleInstance().clearTimefenceData();
            //清除时间围栏本地数据
            // FenceManager.deleteTimeFenceData();
            /**执行时间范围外的操作*/
            //ExcuteTimeFence.excute_TiemFence(false); 应该没有这个


        } else if ("alarm_start".equals(type)) {

            //  ExcuteTimeFence.excute_TiemFence(true);
            FenceExcute.excuteGeographicalFence(true, false);
            String extr = PreferencesManager.getSingleInstance() .getTimefenceData("key");
            if (extr != null) {

                String i = extr.split("_")[0];
                String data = extr.split("_")[1];
                int parseInt = Integer.parseInt(i);
                //    Type type = new TypeToken<List<TimeUnitBean>>() {}.getType();
                Type listType = new TypeToken<List<TimeFenceData.PolicyBean.TimeUnitBean>>() {
                }.getType();
                //   Log.w("testAppfront", "timeUnits分段好的时间===" + data);
                //  Log.w("testAppfront", "timeUnits分段好的时间=extr==" + extr);
                List<TimeFenceData.PolicyBean.TimeUnitBean> list = new Gson().fromJson(data, listType);

                if (list==null){
                    Log.w("testAppfront", "timeUnits解析错误===");
                }
                Intent intents = new Intent(TheTang.getSingleInstance().getContext(), TimeFenceReceiver.class);
                intents.setAction("alarm_end");
                PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intents, PendingIntent.FLAG_UPDATE_CURRENT);
                /*定结束闹钟*/
                try {
                    am.setExact(AlarmManager.RTC_WAKEUP, mm.parse(dates + " " + list.get(parseInt).getEndTime()).getTime(), pi);//定闹钟
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        } else if ("alarm_end".equals(type)) {

            /*执行围栏外的策略*/
            //ExcuteTimeFence.excute_TiemFence(false);
            FenceExcute.excuteGeographicalFence(false, false);
            Log.w(TAG, "执行alarm_end" + type);

            String extr = PreferencesManager.getSingleInstance().getTimefenceData("key");
            if (TextUtils.isEmpty(extr)){
                return;
            }
            Log.w(TAG,extr);
            String i = extr.split("_")[0];
            String data = extr.split("_")[1];
            if (TextUtils.isEmpty(i) || TextUtils.isEmpty(data)) {
                Log.w(TAG, "i 或者 data 为空" );
                return;
            }

            int parseInt = Integer.parseInt(i);
            //    Type type = new TypeToken<List<TimeUnitBean>>() {}.getType();
            Log.w(TAG, "timeUnits分PreferencesManager段好的时间=extr==" + extr);
            Type listType = new TypeToken<List<TimeFenceData.PolicyBean.TimeUnitBean>>() {
            }.getType();
            List<TimeFenceData.PolicyBean.TimeUnitBean> list = new Gson().fromJson(data, listType);
            if (parseInt < list.size() - 1) {

                Intent intents = new Intent(TheTang.getSingleInstance().getContext(), TimeFenceReceiver.class);
                intents.setAction("alarm_start");
                PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intents, PendingIntent.FLAG_UPDATE_CURRENT);
                //定下个开始闹钟
                try {
                    am.setExact(AlarmManager.RTC_WAKEUP, mm.parse(dates + " " + list.get(parseInt + 1).getStartTime())
                            .getTime(), pi);//定闹钟
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                PreferencesManager.getSingleInstance().setTimefenceData("key", (parseInt + 1) + "_" + data);
            } else {
                //1.最后一个结束闹钟，则定明天的闹钟
                //2.添加符合第二天的时间，排序，然后存储起来发明天开始的广播
                String date= c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
                // String endTimeRage= preferencesManager.getFenceData(Common.endTimeRage);
                String endTimeRage = preferencesManager.getFenceData(Common.endTimeRage);
                if (!date.equals(endTimeRage)){

                    nextSelectExcuteTime(list);
                    Log.w(TAG, "最后一个结束闹钟，则定明天的闹钟" + type);
                }else {
                    Log.w(TAG, "最后一个结束闹钟，今天时最后一天的时间范围不定明天的闹钟了...." + type);
                }
            }

        }

    }






    /**
     * 初始化选择执行时间
     * @param list
     */
    private void selcetTimeExcute(List<TimeFenceData.PolicyBean.TimeUnitBean> list) {

        /**选择今天符合的条件时间段出来*/
        list= selectTodayTime(list);
        if (list==null ||list.size()<=0){
            Log.w(TAG, "选择今天符合的条件时间段出来为null或size为0这样的情况，那就选择下一天的闹钟时间，因为有可能开始时间小于今天的时间");

            FenceExcute.excuteGeographicalFence(false, false);
            //2.添加符合第二天的时间，排序，然后存储起来发明天开始的广播
            Log.w(TAG, "--"+list.size()+"--"+"执行围栏外的策略后,添加符合第二天的时间，排序，然后存储起来发明天开始的广播FenceExcute.excuteGeographicalFence(false)");
            nextSelectExcuteTime(list);
            return;
        }

        //   SimpleDateFormat format =   new SimpleDateFormat("HH:mm");
        //  SimpleDateFormat formats = new SimpleDateFormat("yyyy-MM-dd");
        String json        = new Gson().toJson(list);
        try {
            //  Date date = format.parse("01:40");
            // long   millis = date .getCallTime();
            Calendar c = Calendar.getInstance();
            String dates= c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
            //   long     times_Date = format.parse(dates).getCallTime();//今天 00:00的毫秒时间

            long   millis =System.currentTimeMillis();
            Date date = new Date(millis);
            String v      = list.get(list.size() - 1).getStartTime();
            String s      = list.get(list.size() - 1).getEndTime();
            long x   =  mm.parse(dates + " " +s).getTime();
            long y   =  mm.parse(dates + " " +v).getTime();
            Log.w(TAG, dates+"现在此刻的时间--"+date.toString());
            if (millis>=x){
                //1.执行围栏外的策略
                //  ExcuteTimeFence.excute_TiemFence(false);
                FenceExcute.excuteGeographicalFence(false, false);
                //2.添加符合第二天的时间，排序，然后存储起来发明天开始的广播
                Log.w(TAG, "--"+list.size()+"--"+"执行围栏外的策略后,添加符合第二天的时间，排序，然后存储起来发明天开始的广播");
                Log.w(TAG," FenceExcute.excuteGeographicalFence(false)");
                nextSelectExcuteTime(list);

                return;

            }else if (list.size() == 1){
                Intent intent = new Intent(TheTang.getSingleInstance().getContext(), TimeFenceReceiver.class);
                if (millis < y){
                    //在第一个时间围栏之前
                    Log.w(TAG, "--"+0+"--"+"在第一个时间围栏之前="+v+"--发送"+v+"-de闹钟");
                    Log.w(TAG,"-2-"+date.toString());

                    intent.setAction("alarm_start");
                    PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    am.setExact(AlarmManager.RTC_WAKEUP, y, pi);//定闹钟
                    /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                    PreferencesManager.getSingleInstance().setTimefenceData("key",0+"_"+json);
                    Log.w(TAG, "--"+0+"--"+"在第一个时间围栏之前闹钟==="+  PreferencesManager.getSingleInstance().getTimefenceData("key"));
                }else if (y<= millis && millis< x) {
                    //在某个时间围栏内
                    Log.w(TAG, "--" + 0 + "--" + "在某个时间围栏内" + v + "==" + s);
                    Log.w(TAG, "-2-在某个时间围栏内命令-ExcuteTimeFence.excute_TiemFence(true)");
                    //1.执行时间围栏内的策略
                    //  ExcuteTimeFence.excute_TiemFence(true);
                    FenceExcute.excuteGeographicalFence(true, false);
                    intent.setAction("alarm_end");
                    PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    //2定结束闹钟
                    am.setExact(AlarmManager.RTC_WAKEUP, x, pi);//定闹钟
                    /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                    PreferencesManager.getSingleInstance().setTimefenceData("key", 0 + "_" + json);

                }


            }else {

                for (int i = 0; i < list.size() ; i++) {

                    try {
                        Date parse = mm.parse(dates + " " + list.get(i).getStartTime());
                        long start =  mm.parse(dates + " " +list.get(i).getStartTime()).getTime();
                        long end =  mm.parse(dates + " " +list.get(i).getEndTime()) .getTime();
                        Intent intent = new Intent(TheTang.getSingleInstance().getContext(), TimeFenceReceiver.class);
                        Log.w(TAG, "--"+i+"--"+"时间围--parse--"+parse+"=="+parse.getTime());
                        // Log.w(TAG, "--"+i+"--"+"时间围--00:00--"+dates+"=="+times_Date);
                        Log.w(TAG, "--"+i+"--"+sDateFormat.parse(list.get(i).getStartTime()).getTime()+"时间围="+list.get(i).getStartTime()+"==="+start);
                        Log.w(TAG, "--"+i+"--"+sDateFormat.parse(list.get(i).getEndTime()) .getTime()+"时间围="+list.get(i).getEndTime()+"=="+end);
                        Log.w(TAG, "--"+i+"--"+"时间围此刻时间="+millis+"==="+date.toString());
                        //   String result = formatData("yyyy-MM-dd HH:mm", millis);

                        if (millis < start){
                            //在第一个时间围栏之前
                            Log.w(TAG, "--"+i+"--"+"在第一个时间围栏之前="+list.get(i).getStartTime());
                            Log.w(TAG,"-2-"+date.toString());

                            intent.setAction("alarm_start");
                            PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            am.setExact(AlarmManager.RTC_WAKEUP, start, pi);//定闹钟
                            /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                            PreferencesManager.getSingleInstance().setTimefenceData("key",i+"_"+json);
                            break;
                        }else if (start <= millis&&millis<end){
                            //在某个时间围栏内
                            Log.w(TAG, "--"+i+"--"+"在某个时间围栏内"+list.get(i).getStartTime()+"=="+list.get(i).getEndTime());
                            Log.w(TAG,"-2-在某个时间围栏内命令-ExcuteTimeFence.excute_TiemFence(true)");
                            //1.执行时间围栏内的策略
                            // ExcuteTimeFence.excute_TiemFence(true);
                            FenceExcute.excuteGeographicalFence(true, false);
                            intent.setAction("alarm_end");
                            PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            //2定结束闹钟
                            am.setExact(AlarmManager.RTC_WAKEUP, end, pi);//定闹钟
                            /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                            PreferencesManager.getSingleInstance().setTimefenceData("key",i+"_"+json);
                            break;
                        }else if (millis >= end){
                            if ((i+1) >= (list.size())) {
                                continue;
                            }else if(millis < mm.parse(dates + " " +list.get(i+1).getStartTime()).getTime()){

                                //在某个时间围栏外，下一个时间围栏之前
                                Log.w(TAG, "--"+i+"--"+"在某个时间围栏外="+list.get(i).getEndTime()+"，下一个时间围栏之前="+list.get(i+1).getStartTime());
                                Log.w(TAG,"-2-在某个时间围栏内命令-ExcuteTimeFence.excute_TiemFence(false)");
                                //1.执行围栏外的策略
                                // ExcuteTimeFence.excute_TiemFence(false);
                                FenceExcute.excuteGeographicalFence(false, false);
                                intent.setAction("alarm_start");
                                PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                //2定下个开始闹钟
                                am.setExact(AlarmManager.RTC_WAKEUP, mm.parse(dates + " " +list.get(i+1).getStartTime())
                                        .getTime(), pi);//定闹钟

                                /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                                PreferencesManager.getSingleInstance().setTimefenceData("key",(i+1)+"_"+json);
                                break;
                            }

                        }else {
                            Log.w(TAG,i+"--到这里时"+list.get(i).getStartTime()+",="+list.get(i).getEndTime());
                        }


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> selectTodayTime(List<TimeFenceData.PolicyBean.TimeUnitBean> timeUnits) {
        if (timeUnits==null||timeUnits.size()<=0){

            return null;
        }

        ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> lists = new ArrayList<>();
        for (TimeFenceData.PolicyBean.TimeUnitBean bean: timeUnits) {
            if ("1".equals(bean.getUnitType())){
                lists.add(bean);
            }else  if("2".equals(bean.getUnitType())){
                int week = getDay_Week();
                //今天的周几跟设置是否一致
                if ((week+"").equals(bean.getTypeDate())){
                    lists.add(bean);
                }

            }else if("3".equals(bean.getUnitType())){
                //工作日
                int week = getDay_Week();

                if(week<6){
                    lists.add(bean);
                }
            }else if("4".equals(bean.getUnitType())) {
                //指定某个日期,执行一次
                Calendar c = Calendar.getInstance();
                String date= c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
                String typeDate = bean.getTypeDate();
                if(typeDate.contains("T"))
                    typeDate= typeDate.split("T")[0].trim();
                if (date.equals(typeDate)) {
                    lists.add(bean);
                }
            }

        }
        /*排出各个时间段出来*/
        ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> arrayList = sortTime(lists);

        return arrayList;

    }

    /**
     * 下一天的执行时间
     * @param timeUnits
     */
    private void nextSelectExcuteTime(List<TimeFenceData.PolicyBean.TimeUnitBean> timeUnits){
        ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> lists = new ArrayList<>();
        for (TimeFenceData.PolicyBean.TimeUnitBean bean: timeUnits) {
            if ("1".equals(bean.getUnitType())){
                lists.add(bean);
            }else  if("2".equals(bean.getUnitType())){

                int week = getDay_Week();
                if (week==7){
                    week=1;
                }else {
                    week=week+1;
                }
                //下一天的周几跟设置是否一致
                if ((week+"").equals(bean.getTypeDate())){
                    lists.add(bean);
                }

            }else if("3".equals(bean.getUnitType())){
                //工作日
                int week = getDay_Week();
                if (week==7){
                    week=1;
                }else {
                    week=week+1;
                }
                if(week<6){
                    lists.add(bean);
                }
            }else if("4".equals(bean.getUnitType())) {
                //指定某个日期,执行一次
                Calendar c = Calendar.getInstance();
                c.add(java.util.Calendar.DAY_OF_MONTH, 1);
                String date= c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
                String typeDate = bean.getTypeDate();
                if(typeDate.contains("T"))
                    typeDate= typeDate.split("T")[0].trim();
                if (date.equals(typeDate)) {
                    lists.add(bean);
                }
            }

        }



        /*排出各个时间段出来*/
        ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> arrayList = sortTime(lists);

        if (arrayList==null || arrayList.size() <=0){
            SimpleDateFormat formats = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            //如果明天没有符合明天的时间，那就订明天00：00的闹钟,每天查询
            Calendar c = Calendar.getInstance();
            c.add(java.util.Calendar.DAY_OF_MONTH, 1);
            String date= c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
            try {
                Log.w(TAG,"如果明天没有符合明天的时间，那就订明天00：00的闹钟,每天查询");
                LogUtil.writeToFile(TAG,"如果明天没有符合明天的时间，那就订明天00：00的闹钟,每天查询");
                Date parse = formats.parse(date+" 00:00");
                Intent intent_startTimeRage = new Intent(TheTang.getSingleInstance().getContext(), /*AlarmReceiver1.class*/TimeFenceReceiver.class);
                intent_startTimeRage.setAction("startTimeRage");

                //第二个参数用于识别AlarmManager
                PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);

                am.setExact(AlarmManager.RTC_WAKEUP, parse.getTime(), pi); //执行一次
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return;
        }

        /*把下一天分好的时间段转换成时间格式*/
        String json        = new Gson().toJson(arrayList);
        /**选择第一个开始时间闹钟*/
        Intent intent = new Intent(TheTang.getSingleInstance().getContext(), TimeFenceReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        intent.setAction("alarm_start");
        if (arrayList.size()>0){
            TimeFenceData.PolicyBean.TimeUnitBean unitBean = arrayList.get(0);
            try {
                //long time = sDateFormat.parse(unitBean.getStartTime()) .getCallTime();
                Calendar c = Calendar.getInstance();
                c.add(java.util.Calendar.DAY_OF_MONTH, 1);
                String date= c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
                long time =  mm.parse(date + " "+unitBean.getStartTime()).getTime();//第一个开始时间第二天的时间
                // long     times_Date = format.parse(date).getCallTime();//第二天 00:00的毫秒时间
                am.setExact(AlarmManager.RTC_WAKEUP, time, pi);//执行
                /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                PreferencesManager.getSingleInstance().setTimefenceData("key",0+"_"+json);

            } catch (ParseException e) {
                e.printStackTrace();
            }


        }

    }

    private int getDay_Week() {
        Calendar c = Calendar.getInstance();
        int week;
        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                week = 7;
                //   Log.i("MainActivityFilter", "今天是周日");
                break;
            case Calendar.MONDAY:
                week = 1;
                //   Log.i("MainActivityFilter", "今天是周一");
                break;
            case Calendar.TUESDAY:
                week = 2;
                //   Log.i("MainActivityFilter", "今天是周二");
                break;
            case Calendar.WEDNESDAY:
                week = 3;
                //  Log.i("MainActivityFilter", "今天是周三");
                break;
            case Calendar.THURSDAY:
                week = 4;
                //   Log.i("MainActivityFilter", "今天是周四");
                break;
            case Calendar.FRIDAY:
                week = 5;
                //   Log.i("MainActivityFilter", "今天是周五");
                break;
            case Calendar.SATURDAY:
                week = 6;
                //  Log.i("MainActivityFilter", "今天是周六");
                break;
            default:
                week = 0;
                break;

        }
        return week;
    }


    /**
     * 分段时间出来
     * @param list
     * @return
     */
    private ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> sortTime(ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> list) {

        /*TimeUnitBean bean1 = new TimeUnitBean();
        bean1.setStartTime("02:30");
        bean1.setEndTime("03:58");
        list.add(bean1);
        TimeUnitBean bean2 = new TimeUnitBean();
        bean2.setStartTime("03:30");
        bean2.setEndTime("05:58");
        list.add(bean2);
        TimeUnitBean bean3 = new TimeUnitBean();
        bean3.setStartTime("18:30");
        bean3.setEndTime("20:58");
        list.add(bean3);
        TimeUnitBean bean4 = new TimeUnitBean();
        bean4.setStartTime("14:50");
        bean4.setEndTime("16:58");
        list.add(bean4);
        TimeUnitBean bean5 = new TimeUnitBean();
        bean5.setStartTime("06:50");
        bean5.setEndTime("9:08");
        list.add(bean5);
        TimeUnitBean bean6 = new TimeUnitBean();
        bean6.setStartTime("9:00");
        bean6.setEndTime("12:28");
        list.add(bean6);
        TimeUnitBean bean7 = new TimeUnitBean();
        bean7.setStartTime("13:00");
        bean7.setEndTime("14:28");
        list.add(bean7);
        TimeUnitBean bean8 = new TimeUnitBean();
        bean8.setStartTime("14:00");
        bean8.setEndTime("17:28");
        list.add(bean8);*/
        if (list==null){
            return null;
        }else  if (list.size()==1){
            return list;
        }



        Collections.sort(list, new Comparator<TimeFenceData.PolicyBean.TimeUnitBean>() {
            @Override
            public int compare(TimeFenceData.PolicyBean.TimeUnitBean o1, TimeFenceData.PolicyBean.TimeUnitBean o2) {

                int i = 0;
                try {
                    i = sDateFormat.parse(o1.getStartTime()).compareTo(sDateFormat.parse(o2.getStartTime()));
                    //  Log.w("ss",i+"---");

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return i;
            }
        });


        //  Log.w("ss", "--1--"+list.toString());
        for (int i = 0; i < list.size(); i++) {

            Log.w(TAG, "--"+i+"-按时间排序好-"+list.get(i).toString());

        }

        for (int i = 0; i < list.size()-1; i++) {
            for (int j=i+1;j<list.size();j++){
                if (list.get(i)!=null){

                    try {
                        TimeFenceData.PolicyBean.TimeUnitBean unitBean  = list.get(i);
                        Date         date_End       = sDateFormat.parse(unitBean.getEndTime());
                        Date         nextDate_End   = sDateFormat.parse(list.get(j).getEndTime());
                        Date         nextDate_Start = sDateFormat.parse(list.get(j).getStartTime());

                        if (nextDate_Start.before(date_End)&&nextDate_End.before(date_End)){
                            //     Log.w(TAG,nextDate_Start+"nextDate_Start.before(date_End)&&nextDate_End.before(date_End)");
                            list.set(j,null);

                        }else if(nextDate_Start.before(date_End)&&nextDate_End.after(date_End)){
                            unitBean.setEndTime(list.get(j).getEndTime());
                            list.set(i,unitBean);

                            list.set(j,null);

                        }else {
                            break;
                        }


                    } catch (ParseException e) {


                    }

                }else {
                    break;
                }
            }

        }

        ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> unitBeen = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {


            if (list.get(i)!=null){
                unitBeen.add(list.get(i));
                // Log.w(TAG, "--"+i+"ee--"+list.get(i).toString());
            }
        }
        for (int i = 0; i < unitBeen.size(); i++) {
            Log.w(TAG, "--"+i+"-分好的时间段-"+unitBeen.get(i).toString());
        }

        return unitBeen;

    }

    public  String formatData(String dataFormat, long timeStamp) {
        if (timeStamp == 0) {
            Log.d(TAG,"-----------result为空" );
            return "";
        }
        timeStamp = timeStamp * 1000;
        String result = "";
        SimpleDateFormat format = new SimpleDateFormat(dataFormat);
        result = format.format(new Date(timeStamp));
        Log.d(TAG,"-----------"+ result);
        return result;
    }


}
