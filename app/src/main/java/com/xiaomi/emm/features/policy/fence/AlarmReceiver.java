package com.xiaomi.emm.features.policy.fence;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.gson.Gson;
import com.xiaomi.emm.model.TimeFenceData;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static android.content.Context.ALARM_SERVICE;


/**
 * Created by lenovo on 2017/8/16.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    //获取当前时间
    private SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            Log.w("testAppfront","到这里闹钟AlarmReceiver===intent.getAction()为空");
            return;
        }
        Log.w("testAppfront","到这里闹钟AlarmReceiver==="+intent.getAction()+"==此刻时间"+new Date().toString());

        /**1.首先获取sp相应的数据---时间围栏数据*/
        String tiemfence = PreferencesManager.getSingleInstance().getTimefenceData("tiemfence");
        if (tiemfence == null||tiemfence.isEmpty()){
            Log.w("testAppfront","到这里闹钟AlarmReceiver==="+intent.getAction()+"==此刻时间"+System.currentTimeMillis()+"---获取本地时间围栏数据tiemfence为空");

            return;
        }
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Gson gson = new Gson();
        TimeFenceData timeFenceData = gson.fromJson(tiemfence, TimeFenceData.class);
        TimeFenceData.PolicyBean timeFenceBean = timeFenceData.getPolicy().get(0);
        List<TimeFenceData.PolicyBean.TimeUnitBean> timeUnits = timeFenceBean.getTimeUnit();
        Intent intent_startTimeRage = new Intent(TheTang.getSingleInstance().getContext(), AlarmReceiver.class);

        if("startTimeRage".equals(intent.getAction())){
            /** 用gson把获取本地的时间围栏解析成ben对象*/

            /************************************************************************/
            for (TimeFenceData.PolicyBean.TimeUnitBean bean: timeUnits) {
                if ("1".equals(bean.getUnitType())){
                    try {
                        //每天闹钟
                        Log.w("testAppfront",bean.getStartTime()+"每天开始闹钟闹钟==="+bean.getEndTime()+"---"+intent.getAction());
                        /***************************每天开始闹钟闹钟********************/
                        Date date = sDateFormat.parse(bean.getStartTime());
                        Intent intent_start = new Intent(TheTang.getSingleInstance().getContext(), AlarmReceiver.class);
                        intent_start.setAction("everyDay_AlarmStart");//设置动作标志，
                        /***/
                        intent_start.putExtra("everyDay_AlarmStart",bean.getStartTime());
                        PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_start, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager am1 = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                        //如果设定的时间比当前时间还小则立即执行---设置开始时间的闹钟
                        am1.setExact(AlarmManager.RTC_WAKEUP, date.getTime(),pi);
                        /***************************每天结束闹钟********************/
                        date = sDateFormat.parse(bean.getEndTime());
                        intent_startTimeRage.setAction("everyDay_AlarmEnd");//设置动作标志，
                        intent_startTimeRage.putExtra("everyDay_AlarmEnd",bean.getEndTime());
                        PendingIntent pi2 = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
                        //如果设定的时间比当前时间还小则立即执行---设置开始时间的闹钟
                        //am.setRepeating(AlarmManager.RTC_WAKEUP, date.getCallTime(),  3600 * 24 * 1000, pi);//重复执行
                        am1.setExact(AlarmManager.RTC_WAKEUP, date.getTime(),pi2); //
                        Log.w("testAppfront",bean.getStartTime()+"每天结束闹钟==="+bean.getEndTime()+"---"+intent.getAction());
                    } catch (ParseException e) {
                        e.printStackTrace();
                        LogUtil.writeToFile(TAG,"时间围栏广播_每天的闹钟日期解析错误");
                    }
                }else if("2".equals(bean.getUnitType())){
                    //每周闹钟
                    intent_startTimeRage.setAction("everyWeek_AlarmStart");//设置动作标志，
                    Calendar c = Calendar.getInstance();

                    int year = c.get(Calendar.YEAR);
                    int  day = c.get(Calendar.DAY_OF_MONTH);
                    int month = c.get(Calendar.MONTH)+1;
                    try {
                        //获取今天的00:00的时间毫秒
                        long currentTime = format.parse(year + "-" + month + "-" + day).getTime();
                        /**获取一周内的第几天*/
                        int week ;
                        switch (c.get(Calendar.DAY_OF_WEEK)) {
                            case Calendar.SUNDAY:
                                week = 7;
                                break;
                            case Calendar.MONDAY:
                                week = 1;
                                break;
                            case Calendar.TUESDAY:
                                week = 2;
                                break;
                            case Calendar.WEDNESDAY:
                                week = 3;
                                break;
                            case Calendar.THURSDAY:
                                week = 4;
                                break;
                            case Calendar.FRIDAY:
                                week = 5;
                                break;
                            case Calendar.SATURDAY:
                                week = 6;
                                break;
                            default:
                                week=0;
                                break;
                        }
                        if (week-Integer.parseInt(bean.getTypeDate())>0){
                            //如果是当前的周几大于设置的周几，则设置下周的闹钟
                            //相差的时间天数
                            PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
                            /***************************每周开始闹钟闹钟********************/
                            long time = sDateFormat.parse(bean.getStartTime()).getTime();  //设置的时间
                            am.setExact(AlarmManager.RTC_WAKEUP,currentTime
                                    + 3600 * 24 * 1000*(7-(week-Integer.parseInt(bean.getTypeDate())))+time,pi);
                            /***************************每周结束闹钟********************/
                            intent_startTimeRage.setAction("everyWeek_AlarmEnd");//设置动作标志，
                            PendingIntent pi2 = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);

                            time = sDateFormat.parse(bean.getEndTime()).getTime();  //设置结束的时间
                            am.setExact(AlarmManager.RTC_WAKEUP, currentTime
                                    + 3600 * 24 * 1000*(7-(week-Integer.parseInt(bean.getTypeDate())))+time,pi2);
                            Log.w("testAppfront","每周="+week+"=的时间闹钟=1=="+intent.getAction());

                        }else if(week-Integer.parseInt(bean.getTypeDate())<0){
                            //如果是当前的周几小于设置的周几，则设置下周的闹钟
                            PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
                            long time = sDateFormat.parse(bean.getStartTime()).getTime();  //设置的时间
                            /***************************每周开始闹钟闹钟********************/

                            am.setExact(AlarmManager.RTC_WAKEUP, currentTime+
                                    3600 * 24 * 1000*(Integer.parseInt(bean.getTypeDate())-week)+time, pi);//重复执行3600 * 24 * 1000
                            /***************************每周结束闹钟********************/
                            intent_startTimeRage.setAction("everyWeek_AlarmEnd");
                            /**重新设置动作需重新设置 PendingIntent*/
                            PendingIntent pi2 = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
                            Date date = sDateFormat.parse(bean.getEndTime());  //设置的时间
                            am.setExact(AlarmManager.RTC_WAKEUP, currentTime+
                                    3600 * 24 * 1000*(Integer.parseInt(bean.getTypeDate())-week)+date.getTime(), pi2);//重复执行3600 * 24 * 1000
                            Log.w("testAppfront","每周="+week+"=的时间闹钟=2=="+intent.getAction());
                        }else {
                            //刚好是今天--设定时间

                            /***************************每周开始闹钟闹钟********************/
                            Date date = sDateFormat.parse(bean.getStartTime());  //设置的时间
                            intent_startTimeRage.putExtra("everyWeek_AlarmStart",bean.getStartTime());
                            PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);

                            am.setExact(AlarmManager.RTC_WAKEUP, currentTime+
                                    date.getTime(), pi);//重复执行3600 * 24 * 1000

                            /***************************每周结束闹钟********************/
                            intent_startTimeRage.setAction("everyWeek_AlarmEnd");
                            intent_startTimeRage.putExtra("everyWeek_AlarmEnd",bean.getEndTime());
                            PendingIntent pi2 = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
                            date = sDateFormat.parse(bean.getEndTime());  //设置的时间
                            am.setExact(AlarmManager.RTC_WAKEUP,currentTime+date.getTime(), pi2);
                            Log.w("testAppfront","每周="+week+"=的时间闹钟=3=="+intent.getAction());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        LogUtil.writeToFile(TAG,"时间围栏广播_每周的闹钟日期解析错误");
                    }

                }else if("3".equals(bean.getUnitType())){
                    try {
                        //工作日模式--这里还是以每天发的广播闹钟，等到接收闹钟这边再判断是否是工作日，如果是就执行策略，否则就不执行
                        /***************************开始闹钟闹钟********************/
                        Date date = sDateFormat.parse(bean.getStartTime());
                        intent_startTimeRage.setAction("workday_AlarmStart");//设置动作标志，
                        intent_startTimeRage.putExtra("workday_AlarmStart",bean.getStartTime());
                        PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);

                        //如果设定的时间比当前时间还小则立即执行---设置开始时间的闹钟
                        am.setExact(AlarmManager.RTC_WAKEUP, date.getTime(), pi);
                        /***************************结束闹钟********************/
                        date = sDateFormat.parse(bean.getEndTime());
                        intent_startTimeRage.setAction("workday_AlarmEnd");//设置动作标志，
                        intent_startTimeRage.putExtra("workday_AlarmEnd",bean.getEndTime());
                        PendingIntent pi2 = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
                        //如果设定的时间比当前时间还小则立即执行---设置开始时间的闹钟
                        am.setExact(AlarmManager.RTC_WAKEUP, date.getTime(), pi2);//重复执行
                        Log.w("testAppfront","工作日的时间闹钟==="+intent.getAction());
                    } catch (ParseException e) {
                        e.printStackTrace();
                        LogUtil.writeToFile(TAG,"时间围栏广播_工作日闹钟日期解析错误");
                    }

                }else if("4".equals(bean.getUnitType())){
                    //指定某个日期,执行一次
                    /***************************开始闹钟闹钟********************/
                    intent_startTimeRage.setAction("specific_AlarmStart");//设置动作标志，
                    PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
                    try {
                        //先获取那天的时间然后两个时间加起来，就是那天的时间
                        long currentTime = sDateFormat.parse(bean.getStartTime()).getTime(); //HH:mm的时和分钟
                        String typeDate = bean.getTypeDate();
                        if(typeDate.contains("T")) {
                            typeDate = typeDate.split("T")[0].trim();
                        }
                        Date date = format.parse(typeDate); //日期
                        //如果设定的时间比当前时间还小则立即执行---设置开始时间的闹钟
                        am.setExact(AlarmManager.RTC_WAKEUP, currentTime+date.getTime(), pi);//执行一次
                        /***************************结束闹钟********************/
                        date = sDateFormat.parse(bean.getEndTime());  //获取结束时间
                        intent_startTimeRage.setAction("specific_AlarmEnd");//设置动作标志，
                        PendingIntent pi2 = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
                        am.setExact(AlarmManager.RTC_WAKEUP, currentTime+date.getTime(), pi2);//执行一次
                        Log.w("testAppfront","特定的时间闹钟==="+intent.getAction());
                    } catch (ParseException e) {
                        e.printStackTrace();
                        LogUtil.writeToFile(TAG,"时间围栏广播_指定日期闹钟解析错误");
                    }
                }
            }
        }

        /**如果接收到时间范围的结束广播*/
        if("endTimeRage".equals(intent.getAction())){
            Log.w("testAppfront","结束的的时间闹钟==="+intent.getAction());
            /**结束所有的广播，同时取消策略(可以把本地的时间围栏数据删除掉)*/
            PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
            am.cancel(pi);
            /**清除本地的时间围栏数据*/
            ExcuteTimeFence.excute_TiemFence(false);

        }
        /****************开始时间的闹钟*****************************************************/

        switch(intent.getAction()){
            /**每天，特定,每周的开始的闹钟任务*/
            case "everyDay_AlarmStart":

                intent_startTimeRage.setAction("everyDay_AlarmStart");//设置动作标志，
                /*这里再发一次广播，形成定期的一个闹钟*/
                String everyDayAlarmStart = intent.getStringExtra("everyDay_AlarmStart");
                if (everyDayAlarmStart == null){
                    Log.w("testAppfront","everyDayAlarmStart为空===="+intent.getAction());
                    return;
                }
                intent_startTimeRage.putExtra("everyDay_AlarmStart",everyDayAlarmStart);
                PendingIntent pi1 = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
                try {
                    Log.w("testAppfront","everyDayAlarmStart==="+everyDayAlarmStart+"-----"+intent.getAction());
                    am.setExact(AlarmManager.RTC_WAKEUP,sDateFormat.parse(everyDayAlarmStart).getTime()+(3600 * 24 * 1000), pi1);//重复执行
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                excuteTiemFence(context, intent, tiemfence);
                break;
            case "specific_AlarmStart":
                excuteTiemFence(context, intent, tiemfence);
                break;
            case "everyWeek_AlarmStart":
                /*这里再发一次广播，形成定期的一个闹钟*/
                intent_startTimeRage.setAction("everyWeek_AlarmStart");//设置动作标志，
                String everyWeek_alarmStart = intent_startTimeRage.getStringExtra("everyWeek_AlarmStart");
                long everyWeek_alarmTime = 0;
                if (everyWeek_alarmStart != null && !everyWeek_alarmStart.isEmpty()){
                    try {
                        everyWeek_alarmTime = sDateFormat.parse(everyWeek_alarmStart).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }else {
                    everyWeek_alarmTime = System.currentTimeMillis();

                }
                PendingIntent pi3 = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
                am.setExact(AlarmManager.RTC_WAKEUP,everyWeek_alarmTime+(3600 * 24 * 1000*7), pi3);//重复执行
                excuteTiemFence(context, intent, tiemfence);
                break;
            case "workday_AlarmStart":
                /*这里再发一次广播，形成定期的一个闹钟*/
                intent_startTimeRage.setAction("workday_AlarmStart");//设置动作标志，
                String workday_alarmStart = intent.getStringExtra("workday_AlarmStart");
                PendingIntent pi4 = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
                try {
                    am.setExact(AlarmManager.RTC_WAKEUP,sDateFormat.parse(workday_alarmStart).getTime()+(3600 * 24 * 1000), pi4);//重复执行
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                excuteWorkDayAlarm(context, intent, tiemfence);
                break;
            /****************结束时间的闹钟*****************************************************/
            case "everyDay_AlarmEnd":
                intent_startTimeRage.setAction("everyDay_AlarmEnd");//设置动作标志，
                String everyDay_alarmEnd = intent.getStringExtra("everyDay_AlarmEnd");
                if (everyDay_alarmEnd != null) {

                    Log.w("testAppfront","everyDay_alarmEnd===="+everyDay_alarmEnd+"---"+intent.getAction());
                    intent_startTimeRage.putExtra("everyDay_AlarmEnd",everyDay_alarmEnd);
               /*这里再发一次广播，形成定期的一个闹钟*/
                    PendingIntent pi5 = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
                    try {
                        am.setExact(AlarmManager.RTC_WAKEUP,sDateFormat.parse(everyDay_alarmEnd).getTime()+(3600 * 24 * 1000), pi5);//重复执行
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    excute_EndAlarm(context, intent, tiemfence);
                }else {
                    Log.w("testAppfront","everyDay_alarmEnd====空");
                }
                break;
            case "specific_AlarmEnd":

                excute_EndAlarm(context, intent, tiemfence);
                break;
            case "everyWeek_AlarmEnd":
                /*这里再发一次广播，形成定期的一个闹钟*/
                intent_startTimeRage.setAction("everyWeek_AlarmEnd");//设置动作标志，
                String everyWeek_alarmEnd1 = intent.getStringExtra("everyWeek_AlarmEnd");
                PendingIntent pi6 = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);

                long everyWeek_alarmEnd = 0;
                if (everyWeek_alarmEnd1 != null && !everyWeek_alarmEnd1.isEmpty()){
                    try {
                        everyWeek_alarmEnd = sDateFormat.parse(everyWeek_alarmEnd1).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }else {
                    everyWeek_alarmEnd = System.currentTimeMillis();

                }

                am.setExact(AlarmManager.RTC_WAKEUP,everyWeek_alarmEnd+(3600 * 24 * 1000*7), pi6);//重复执行


                excute_EndAlarm(context, intent, tiemfence);

                break;
            case "workday_AlarmEnd":
                intent_startTimeRage.setAction("workday_AlarmEnd");//设置动作标志，
                String workday_alarmEnd = intent.getStringExtra("workday_AlarmEnd");
                if (workday_alarmEnd == null){
                    Log.w("testAppfront","workday_alarmEnd为空===="+intent.getAction());
                    return;
                }
                PendingIntent pi7 = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
                try {
                    am.setExact(AlarmManager.RTC_WAKEUP,sDateFormat.parse(workday_alarmEnd).getTime()+(3600 * 24 * 1000), pi7);//重复执行
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                excute_EndAlarm(context, intent, tiemfence);
                break;
        }
    }

    private void excute_EndAlarm(Context context, Intent intent, String tiemfence) {
        Log.w("testAppfront","所有单元时间结束闹钟任务都来这里===="+intent.getAction());
        if (!tiemfence.isEmpty()){
            /** 用gson把获取本地的时间围栏解析成ben对象*/
            Gson gson = new Gson();
            TimeFenceData timeFenceData = gson.fromJson(tiemfence, TimeFenceData.class);
            TimeFenceData.PolicyBean timeFenceBean = timeFenceData.getPolicy().get(0);
            List<TimeFenceData.PolicyBean.TimeUnitBean> timeUnits = timeFenceBean.getTimeUnit(); //获取存储的时间
            /**获取当前的系统时间--*/
            long time = System.currentTimeMillis();
            /**定义一个标志来作为排序的结果，默认为flase,排序过后得出的结果是是false则立马执行策略，如果是ture则不执行*/
            boolean flag = false;
            /**排序一个最晚的时间出来*/
            for (TimeFenceData.PolicyBean.TimeUnitBean bean :timeUnits) {
                //获取系统今天的周几，因为有可能今天是刚好设定的周几时间
                Calendar c = Calendar.getInstance();
                int day ;
                switch (c.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.SUNDAY:
                        day = 7;
                        break;
                    case Calendar.MONDAY:
                        day = 1;
                        break;
                    case Calendar.TUESDAY:
                        day = 2;
                        break;
                    case Calendar.WEDNESDAY:
                        day = 3;
                        break;
                    case Calendar.THURSDAY:
                        day = 4;
                        break;
                    case Calendar.FRIDAY:
                        day = 5;
                        break;
                    case Calendar.SATURDAY:
                        day = 6;
                        break;
                    default:
                        day = 0;
                        break;
                }
                try {
                    String unitType = bean.getUnitType();
                    if ("1".equals(unitType)){
                        long time1 = sDateFormat.parse(bean.getEndTime()).getTime();
                        if (time < time1){  //如果当前系统时间小于每天设定的结束时间--当前时间也是某个闹钟到时发过来的广播--发送过来的时间，所以可以拿这个比较跟其他时间比较，如果当前
                            flag = true;
                            time = time1;
                        }else {
                            flag = false;
                        }
                    }else if("2".equals(unitType)){
                        if (("" + day).equals(bean.getTypeDate())){
                            if (time < sDateFormat.parse(bean.getEndTime()).getTime()){
                                flag = true;
                                time = sDateFormat.parse(bean.getEndTime()).getTime();
                            }else {
                                flag = false;
                            }
                        }
                    }else if("3".equals(unitType)){
                        /**判断今天是不是工作日*/
                        if (day < 6){
                            if (time < sDateFormat.parse(bean.getEndTime()).getTime()){
                                flag = true;
                                time = sDateFormat.parse(bean.getEndTime()).getTime();
                            }else {
                                flag = false;
                            }
                        }
                    }else if("4".equals(unitType)){
                        /**判断今天是不是指定的日期*/
                        int year = c.get(Calendar.YEAR);
                        int monthday = c.get(Calendar.MONTH);
                        String typeDate = bean.getTypeDate();
                        if(typeDate.contains("T")) {
                            typeDate = typeDate.split("T")[0].trim();
                        }

                        if ((year+"-"+monthday+"-"+day).equals(typeDate)){
                            /**如果今天刚好就是设置的日期则就拿来比较*/
                            if (time < sDateFormat.parse(bean.getEndTime()).getTime()){
                                flag = true;
                                time = sDateFormat.parse(bean.getEndTime()).getTime();
                            }else {
                                flag = false;
                            }
                        }

                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }
            if (!flag){
                /**如果flag为flase,说明每天的时间都大于其他的结束时间，或者只有每天的结束时间，则执行策略*/
                Log.w("testAppfront","执行结束的闹钟的闹钟任务==="+intent.getAction());
                ExcuteTimeFence.excute_TiemFence(false);
            }

        }else {
            Log.w("testAppfront","单元结束闹钟的闹钟任务===如果本地数据为空则应该取消闹钟="+intent.getAction());
            /**如果本地数据为空则应该取消闹钟*/
            Intent intent_startTimeRage = new Intent(TheTang.getSingleInstance().getContext(), AlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            am.cancel(pi);
        }
    }

    /**工作日的闹钟，因为之前是按照每天发的闹钟过来，所以这里主要还要判断是不是周末跟节假日*/
    private void excuteWorkDayAlarm(Context context, Intent intent, String tiemfence) {
        Log.w("testAppfront","工作日的闹钟的闹钟任务==="+intent.getAction());
        Calendar calendar = Calendar.getInstance();
        /**获取今天是周几*/
        int week = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);//判断今天是周几
        if (week<6){
            /**执行时间围栏策略*/
            if (!tiemfence.isEmpty()){
                /** 用gson把获取本地的时间围栏解析成ben对象*/
                Gson gson = new Gson();
                TimeFenceData timeFenceData = gson.fromJson(tiemfence, TimeFenceData.class);
                TimeFenceData.PolicyBean policyBean = timeFenceData.getPolicy().get(0);
                /**执行策略*/
                ExcuteTimeFence.excute_TiemFence(true);
            }else {
                /**如果本地数据为空则应该取消闹钟*/
                Log.w("testAppfront","工作日的闹钟的闹钟任务===如果本地数据为空则应该取消闹钟="+intent.getAction());
                Intent intent_startTimeRage = new Intent(TheTang.getSingleInstance().getContext(), AlarmReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                am.cancel(pi);
            }

        }
    }

    /**执行时间围栏策略方法*/
    private void excuteTiemFence(Context context, Intent intent, String tiemfence) {
        if (!tiemfence.isEmpty()){
            /** 用gson把获取本地的时间围栏解析成ben对象*/
            Gson gson = new Gson();
            TimeFenceData timeFenceData = gson.fromJson(tiemfence, TimeFenceData.class);
            if (timeFenceData != null && timeFenceData.getPolicy().size() > 0){

                TimeFenceData.PolicyBean policyBean = timeFenceData.getPolicy().get(0);
                /**有可能用户发送过时刚好不过时间范围的策略，
                 * 所以在执行策略之前先获取当前时间跟时间围栏的所有结束判断是否当前的时间超过时间围栏策略结束时间，
                 * 如果已经超过就不执行*/
                long currentTimeMillis = System.currentTimeMillis();

                List<TimeFenceData.PolicyBean.TimeUnitBean> timeUnits= policyBean.getTimeUnit();
                if (timeUnits != null){
                    /**获取当前的系统时间--*/
                    long time = System.currentTimeMillis();
                    /**定义一个标志来作为排序的结果，默认为flase,排序过后得出的结果是是false则立马执行策略，如果是ture则不执行*/
                    boolean flag = false;
                    HashMap<String, String> hashMap = new HashMap<>();
                    /**排序一个最晚的时间出来*/
                    for (TimeFenceData.PolicyBean.TimeUnitBean bean :timeUnits) {
                        //获取系统今天的周几，因为有可能今天是刚好设定的周几时间
                        Calendar c = Calendar.getInstance();
                        int day ;
                        switch (c.get(Calendar.DAY_OF_WEEK)) {
                            case Calendar.SUNDAY:
                                day = 7;
                                break;
                            case Calendar.MONDAY:
                                day = 1;
                                break;
                            case Calendar.TUESDAY:
                                day = 2;
                                break;
                            case Calendar.WEDNESDAY:
                                day = 3;
                                break;
                            case Calendar.THURSDAY:
                                day = 4;
                                break;
                            case Calendar.FRIDAY:
                                day = 5;
                                break;
                            case Calendar.SATURDAY:
                                day = 6;
                                break;
                            default:
                                day = 0;
                                break;
                        }
                        try {
                            String unitType = bean.getUnitType();
                            if ("1".equals(unitType)){
                                long time1 = sDateFormat.parse(bean.getEndTime()).getTime();
                                if (time > time1){  //如果当前系统时间小于每天设定的结束时间--当前时间也是某个闹钟到时发过来的广播--发送过来的时间，所以可以拿这个比较跟其他时间比较，如果当前
                                    flag = true;
                                }else {

                                    flag=false;
                                }

                                hashMap.put(bean.getStartTime(),bean.getEndTime());

                            }else if("2".equals(unitType)){
                                if (("" + day).equals(bean.getTypeDate())){
                                    if (time > sDateFormat.parse(bean.getEndTime()).getTime()){
                                        flag=true;
                                    }else {
                                        flag=false;
                                    }
                                }
                                /******/
                                hashMap.put(bean.getStartTime(),bean.getEndTime());

                            }else if("3".equals(unitType)){
                                /**判断今天是不是工作日*/
                                if (day < 6){
                                    if (time > sDateFormat.parse(bean.getEndTime()).getTime()){
                                        flag = true;
                                    }else {
                                        flag = false;
                                    }
                                }
                                /********/
                                hashMap.put(bean.getStartTime(),bean.getEndTime());
                            }else if("4".equals(unitType)){
                                /**判断今天是不是指定的日期*/
                                int year = c.get(Calendar.YEAR);
                                int monthday = c.get(Calendar.MONTH);
                                String typeDate = bean.getTypeDate();
                                if(typeDate.contains("T")) {
                                    typeDate = typeDate.split("T")[0].trim();
                                }

                                if ((year+"-"+monthday+"-"+day).equals(typeDate)){
                                    /**如果今天刚好就是设置的日期则就拿来比较*/
                                    if (time < sDateFormat.parse(bean.getEndTime()).getTime()){
                                        flag = true;
                                    }else {
                                        flag = false;
                                    }
                                }

                            }
                            /******/
                            hashMap.put(bean.getStartTime(),bean.getEndTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                    }

                    HashMap<String, String> sort = sort(sDateFormat, hashMap);
                    Log.w("testAppfront","paxi，==1="+sort.toString()+"-------"+intent.getAction());
                    HashMap<String, String> sort1 = sort(sDateFormat, sort);
                    Log.w("testAppfront","paxi，==2="+sort.toString()+"-------"+intent.getAction());

                    /**如果当前时间没有超过所有制定今天的结束时间就执行策略**/
                    if (!flag){

                        /**执行策略*/
                        synchronized (this){
                            ExcuteTimeFence.excute_TiemFence(true);
                        }
                    }else {
                        Log.w("testAppfront","当前时间已经在结束时间外了，所以不执行开始闹钟的闹钟任务==="+intent.getAction());

                    }
                }
            }
        }else {
            Log.w("testAppfront","每天，特定,每周的开始的闹钟任务===如果本地数据为空则应该取消闹钟"+intent.getAction());
            /**如果本地数据为空则应该取消闹钟*/
            Intent intent_startTimeRage = new Intent(TheTang.getSingleInstance().getContext(), AlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            am.cancel(pi);
        }
    }

    private HashMap<String, String> sort(SimpleDateFormat sDateFormat, HashMap<String, String> hashMap) {
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> map3 = new HashMap<>();

        Set<String> strings = hashMap.keySet();
        ArrayList<String> timeUnits = new ArrayList<>();
        timeUnits.addAll(strings);
        if (timeUnits.size() > 2){
            try {
                String x,y;
                String source = timeUnits.get(0);
                String source1 = hashMap.get(timeUnits.get(0));
                x = source;
                y = source1;
                for (int i = 1; i < timeUnits.size(); i++) {
                    long time = sDateFormat.parse(source).getTime();//开始
                    long time1 = sDateFormat.parse(source1).getTime();//结束
                    long time2 = sDateFormat.parse(timeUnits.get(i)).getTime(); //开始
                    long time3 = sDateFormat.parse(hashMap.get(timeUnits.get(i))).getTime(); //结束
                    Log.w("textAppp"+i,source+"--1-="+source1+"--2-"+"---3--"+timeUnits.get(i)+"---4---"+hashMap.get(timeUnits.get(i)));
                    if (time <= time2 && time2 <= time1){
                        source = source;

                        //开始时间2大于开始时间1的情况且小于结束1的时间
                        if (time3 > time1){
                            source1 = hashMap.get(timeUnits.get(i));
                            Log.w("textAppp"+i,source+"---="+source1);
                        }else {
                            source1 = source1;
                            Log.w("textAppp"+i,source+"---="+source1);
                        }
                        map.put(source,source1);
                        Log.w("textAppp"+i,"map-1-"+map.toString());

                    }else if (time2 <= time&&time<=time3){

                        source = timeUnits.get(i);
                        if (time1 > time3){
                            source1 = source1;
                            Log.w("textAppp"+i,source+"---="+source1);
                        }else {
                            source1=hashMap.get(timeUnits.get(i));
                            Log.w("textAppp"+i,source+"-=--="+source1);
                        }
                        map.put(source,source1);
                        Log.w("textAppp"+i,"map-2-"+map.toString());
                    }else {
                        map3.put(timeUnits.get(i),hashMap.get(timeUnits.get(i))) ;
                        Log.w("textAppp"+i,"-yyyyyyyyyyyyyyyyyy--="+map3.toString());
                    }
                }
                //return map;
                if (x.equals(source)&&y.equals(source1)){
                    map3.put(x,y);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (map3.size() > 0 && timeUnits.size() > map3.size()){
                Log.w("textAppp","返回来的==11="+sort( sDateFormat,map3).toString());
                map.putAll(sort( sDateFormat,map3));
                Log.w("textAppp","返回来的==2="+map.toString());
            }else {
                Log.w("textAppp","--cccccccccccc-"+map3.toString());
                map.putAll(map3);
                Log.w("textAppp","cccc==2="+map.toString());
                return map;
            }

            Log.w("textAppp",map.toString()+"-------------------------0-----------");


            Log.w("textAppp",timeUnits.toString()+"-1--=");
            Log.w("textAppp",hashMap.toString()+"--2-=");
            Log.w("textAppp",map3.toString()+"--3-=");
            Log.w("textAppp",map.toString()+"--44444-=");
        }else {
            return hashMap;
        }
        return map;
    }
}








