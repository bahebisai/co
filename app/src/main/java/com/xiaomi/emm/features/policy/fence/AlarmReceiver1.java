package com.xiaomi.emm.features.policy.fence;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaomi.emm.model.TimeFenceData;
import com.xiaomi.emm.model.TimeFenceData.PolicyBean.TimeUnitBean;
import com.xiaomi.emm.features.manager.PreferencesManager;
import com.xiaomi.emm.features.presenter.TheTang;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import static android.content.Context.ALARM_SERVICE;


/**
 * Created by lenovo on 2017/8/16.
 */

public class AlarmReceiver1 extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver1";
    //获取当前时间
    private SimpleDateFormat sDateFormat    =   new SimpleDateFormat("HH:mm");
    private SimpleDateFormat mm = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    AlarmManager am = (AlarmManager) TheTang.getSingleInstance().getContext().getSystemService(ALARM_SERVICE);

    @Override
    public void onReceive(Context context, Intent intent) {

        Calendar c = Calendar.getInstance();
        String dates= c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);

        Log.w("testAppfront", "到这里闹钟AlarmReceiver1===intent.getAction()为空");
        if (intent.getAction() == null) {
            Log.w("testAppfront", "到这里闹钟AlarmReceiver1===intent.getAction()为空");
            return;
        }

        Log.w("testAppfront", "到这里闹钟AlarmReceiver1===" + intent.getAction() + "==此刻时间" + new Date().toString());
        String tiemfence = PreferencesManager.getSingleInstance().getTimefenceData("tiemfence");
        if (tiemfence==null||tiemfence.isEmpty()){
            Log.w("testAppfront","到这里闹钟AlarmReceiver1==="+intent.getAction()+"==此刻时间"+System.currentTimeMillis()+"---获取本地时间围栏数据tiemfence为空");

            return;
        }

        if("startTimeRage".equals(intent.getAction())) {
            /**日期范围的开始的第一天*/

            Gson gson = new Gson();
            TimeFenceData timeFenceData = gson.fromJson(tiemfence, TimeFenceData.class);
            TimeFenceData.PolicyBean timeFenceBean = timeFenceData.getPolicy()
                    .get(0);
            List<TimeUnitBean> timeUnits = timeFenceBean.getTimeUnit();
            if (timeUnits == null) {
                Log.w("testAppfront","timeUnits为空==="+intent.getAction());
                return;
            }
        /*初始化选择执行时间**/
            selcetTimeExcute(timeUnits);
        }else  if("endTimeRage".equals(intent.getAction())){
            /**如果接收到时间范围的结束广播*/
            Log.w("testAppfront","结束的的时间闹钟==="+intent.getAction());
            /**结束所有的广播，同时取消策略(可以把本地的时间围栏数据删除掉)*/
            Intent intent_startTimeRage = new Intent(TheTang.getSingleInstance().getContext(), AlarmReceiver1.class);
            PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);
            am.cancel(pi);
            /**清除本地的时间围栏数据*/
            PreferencesManager.getSingleInstance().clearTimefenceData();

        }else if("alarm_start".equals(intent.getAction())){

            ExcuteTimeFence.excute_TiemFence(true);
            String extr = PreferencesManager.getSingleInstance()
                    .getTimefenceData("key");
            if (extr!=null){

                String i = extr.split("_")[0];
                String data = extr.split("_")[1];
                int    parseInt = Integer.parseInt(i);
                Type type = new TypeToken<List<TimeUnitBean>>() {}.getType();
                List<TimeUnitBean> list = new Gson().fromJson(data, type);

                Intent intents = new Intent(TheTang.getSingleInstance().getContext(), AlarmReceiver1.class);
                intents.setAction("alarm_end");
                PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intents, PendingIntent.FLAG_UPDATE_CURRENT);
                /*定结束闹钟*/
                try {
                    am.setExact(AlarmManager.RTC_WAKEUP, mm.parse(dates + " "+list.get(parseInt).getEndTime()).getTime(), pi);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }else if("alarm_end".equals(intent.getAction())){
            /*执行围栏外的策略*/
            ExcuteTimeFence.excute_TiemFence(false);
            String extr = PreferencesManager.getSingleInstance()
                    .getTimefenceData("key");
            String i = extr.split("_")[0];
            String data = extr.split("_")[1];
            int    parseInt = Integer.parseInt(i);
            Type type = new TypeToken<List<TimeUnitBean>>() {}.getType();
            List<TimeUnitBean> list = new Gson().fromJson(data, type);
            if (parseInt<list.size()-1){

                Intent intents = new Intent(TheTang.getSingleInstance().getContext(), AlarmReceiver1.class);
                intents.setAction("alarm_start");
                PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intents, PendingIntent.FLAG_UPDATE_CURRENT);
                //定下个开始闹钟
                try {
                    am.setExact(AlarmManager.RTC_WAKEUP, mm.parse(dates + " "+list.get(parseInt+1).getStartTime())
                            .getTime(), pi);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                PreferencesManager.getSingleInstance().setTimefenceData("key",(parseInt+1)+"_"+extr);
            }else {
                //1.最后一个结束闹钟，则定明天的闹钟
                //2.添加符合第二天的时间，排序，然后存储起来发明天开始的广播
                nextSelectExcuteTime(list);
                Log.w("testAppfront","最后一个结束闹钟，则定明天的闹钟"+intent.getAction());
            }


        }



    }


    /**
     * 初始化选择执行时间
     * @param list
     */
    private void selcetTimeExcute(List<TimeUnitBean> list) {

        /**选择今天符合的条件时间段出来*/
        list= selectTodayTime(list);

        String json = new Gson().toJson(list);
        try {

            Calendar c = Calendar.getInstance();
            String dates = c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
            long   millis = System.currentTimeMillis();
            Date date = new Date(millis);
            String v = list.get(list.size() - 1).getStartTime();
            String s = list.get(list.size() - 1).getEndTime();
            long x = mm.parse(dates + " " +s).getTime();
            long y = mm.parse(dates + " " +v).getTime();
            Log.w("testAppfront", dates+"现在此刻的时间--"+date.toString());
            if (millis >= x){
                //1.执行围栏外的策略
                ExcuteTimeFence.excute_TiemFence(false);
                //2.添加符合第二天的时间，排序，然后存储起来发明天开始的广播
                nextSelectExcuteTime(list);
                Log.w("testAppfront", "--"+list.size()+"--"+"执行围栏外的策略,添加符合第二天的时间，排序，然后存储起来发明天开始的广播");
                Log.w("testAppfront","-2-ExcuteTimeFence.excute_TiemFence(false)");

                return;

            }else if (list.size() == 1){
                Intent intent = new Intent(TheTang.getSingleInstance().getContext(), AlarmReceiver1.class);
                if (millis<y){
                    //在第一个时间围栏之前
                    Log.w("testAppfront", "--"+0+"--"+"在第一个时间围栏之前="+v+"--发送"+v+"-de闹钟");
                    Log.w("testAppfront","-2-"+date.toString());

                    intent.setAction("alarm_start");
                    PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    am.setExact(AlarmManager.RTC_WAKEUP, y, pi);
                    /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                    PreferencesManager.getSingleInstance().setTimefenceData("key",0+"_"+json);

                }else if (y<=millis&&millis<x) {
                    //在某个时间围栏内
                    Log.w("testAppfront", "--" + 0 + "--" + "在某个时间围栏内" + v + "==" + s);
                    Log.w("testAppfront", "-2-在某个时间围栏内命令-ExcuteTimeFence.excute_TiemFence(true)");
                    //1.执行时间围栏内的策略
                    ExcuteTimeFence.excute_TiemFence(true);
                    intent.setAction("alarm_end");
                    PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    //2定结束闹钟
                    am.setExact(AlarmManager.RTC_WAKEUP, x, pi);
                    /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                    PreferencesManager.getSingleInstance().setTimefenceData("key", 0 + "_" + json);

                }


            }else {

                for (int i = 0; i < list.size()-1 ; i++) {

                    try {
                        Date parse = mm.parse(dates + " " + list.get(i).getStartTime());
                        long start = mm.parse(dates + " " +list.get(i).getStartTime()).getTime();
                        long end = mm.parse(dates + " " +list.get(i).getEndTime()) .getTime();
                        Intent intent = new Intent(TheTang.getSingleInstance().getContext(), AlarmReceiver1.class);
                        Log.w("testAppfront", "--"+i+"--"+"时间围--parse--"+parse+"=="+parse.getTime());

                        Log.w("testAppfront", "--"+i+"--"+sDateFormat.parse(list.get(i).getStartTime()).getTime()+"时间围="+list.get(i).getStartTime()+"==="+start);
                        Log.w("testAppfront", "--"+i+"--"+sDateFormat.parse(list.get(i).getEndTime()) .getTime()+"时间围="+list.get(i).getEndTime()+"=="+end);
                        Log.w("testAppfront", "--"+i+"--"+"时间围此刻时间="+millis+"==="+date.toString());

                        if (millis < start){
                            //在第一个时间围栏之前
                            Log.w("testAppfront", "--"+i+"--"+"在第一个时间围栏之前="+list.get(i).getStartTime());
                            Log.w("testAppfront","-2-"+date.toString());

                            intent.setAction("alarm_start");
                            PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            am.setExact(AlarmManager.RTC_WAKEUP, start, pi);//定闹钟
                            /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                            PreferencesManager.getSingleInstance().setTimefenceData("key",i+"_"+json);
                            break;
                        }else if (start <= millis && millis < end){
                            //在某个时间围栏内
                            Log.w("testAppfront", "--"+i+"--"+"在某个时间围栏内"+list.get(i).getStartTime()+"=="+list.get(i).getEndTime());
                            Log.w("testAppfront","-2-在某个时间围栏内命令-ExcuteTimeFence.excute_TiemFence(true)");
                            //1.执行时间围栏内的策略
                            ExcuteTimeFence.excute_TiemFence(true);
                            intent.setAction("alarm_end");
                            PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            //2定结束闹钟
                            am.setExact(AlarmManager.RTC_WAKEUP, end, pi);//定闹钟
                            /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                            PreferencesManager.getSingleInstance().setTimefenceData("key",i+"_"+json);
                            break;
                        }else if (millis >= end&&millis < mm.parse(dates + " " +list.get(i+1).getStartTime())
                                .getTime()){
                            //在某个时间围栏外，下一个时间围栏之前
                            Log.w("testAppfront", "--"+i+"--"+"在某个时间围栏外="+list.get(i).getEndTime()+"，下一个时间围栏之前="+list.get(i+1).getStartTime());
                            Log.w("testAppfront","-2-在某个时间围栏内命令-ExcuteTimeFence.excute_TiemFence(false)");
                            //1.执行围栏外的策略
                            ExcuteTimeFence.excute_TiemFence(false);
                            intent.setAction("alarm_start");
                            PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            //2定下个开始闹钟
                            am.setExact(AlarmManager.RTC_WAKEUP, mm.parse(dates + " " +list.get(i+1).getStartTime())
                                    .getTime(), pi);//定闹钟

                            /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                            PreferencesManager.getSingleInstance().setTimefenceData("key",(i+1)+"_"+json);
                            break;
                        }else {
                            Log.w("testAppfront",i+"--到这里时"+list.get(i).getStartTime()+",="+list.get(i).getEndTime());
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

    private ArrayList<TimeUnitBean> selectTodayTime(List<TimeUnitBean> timeUnits) {

        ArrayList<TimeUnitBean> lists = new ArrayList<>();
        for (TimeUnitBean bean: timeUnits) {
            if ("1".equals(bean.getUnitType())){
                lists.add(bean);
            }else  if("2".equals(bean.getUnitType())){
                int week = getDay_Week();
                //今天的周几跟设置是否一致
                if ((week + "").equals(bean.getTypeDate())){
                    lists.add(bean);
                }

            }else if("3".equals(bean.getUnitType())){
                //工作日
                int week = getDay_Week();

                if(week < 6){
                    lists.add(bean);
                }
            }else if("4".equals(bean.getUnitType())) {
                //指定某个日期,执行一次
                Calendar c = Calendar.getInstance();
                String date = c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
                String typeDate = bean.getTypeDate();
                if(typeDate.contains("T")) {
                    typeDate = typeDate.split("T")[0].trim();
                }
                if (date.equals(typeDate)) {
                    lists.add(bean);
                }
            }
        }
        /*排出各个时间段出来*/
        ArrayList<TimeUnitBean> arrayList = sortTime(lists);

        return arrayList;
    }

    /**
     * 下一天的执行时间
     * @param timeUnits
     */
    private void nextSelectExcuteTime(List<TimeUnitBean> timeUnits){
        ArrayList<TimeUnitBean> lists = new ArrayList<>();
        for (TimeUnitBean bean: timeUnits) {
            if ("1".equals(bean.getUnitType())){
                lists.add(bean);
            }else  if("2".equals(bean.getUnitType())){

                int week = getDay_Week();
                if (week == 7){
                    week = 1;
                }else {
                    week = week + 1;
                }
                //下一天的周几跟设置是否一致
                if ((week+"").equals(bean.getTypeDate())){
                    lists.add(bean);
                }

            }else if("3".equals(bean.getUnitType())){
                //工作日
                int week = getDay_Week();
                if (week == 7){
                    week = 1;
                }else {
                    week = week + 1;
                }
                if(week < 6){
                    lists.add(bean);
                }
            }else if("4".equals(bean.getUnitType())) {
                //指定某个日期,执行一次
                Calendar c = Calendar.getInstance();
                c.add(java.util.Calendar.DAY_OF_MONTH, 1);
                String date= c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
                String typeDate = bean.getTypeDate();
                if(typeDate.contains("T")) {
                    typeDate = typeDate.split("T")[0].trim();
                }
                if (date.equals(typeDate)) {
                    lists.add(bean);
                }
            }

        }
        /*排出各个时间段出来*/
        ArrayList<TimeUnitBean> arrayList = sortTime(lists);
        /*把下一天分好的时间段转换成时间格式*/
        String json = new Gson().toJson(arrayList);
        /**选择第一个开始时间闹钟*/
        Intent intent = new Intent(TheTang.getSingleInstance().getContext(), AlarmReceiver1.class);

        PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        intent.setAction("alarm_start");
        if (arrayList.size() > 0){
            TimeUnitBean unitBean = arrayList.get(0);
            try {

                Calendar c = Calendar.getInstance();
                c.add(java.util.Calendar.DAY_OF_MONTH, 1);
                String date= c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
                long time =  mm.parse(date + " "+unitBean.getStartTime()).getTime();//第一个开始时间第二天的时间

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
    private ArrayList<TimeUnitBean> sortTime(List<TimeUnitBean> list) {

        Collections.sort(list, new Comparator<TimeUnitBean>() {
            @Override
            public int compare(TimeUnitBean o1, TimeUnitBean o2) {

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

        for (int i = 0; i < list.size(); i++) {

            Log.w("testAppfront", "--"+i+"-按时间排序好-"+list.get(i).toString());

        }

        for (int i = 0; i < list.size()-1; i++) {
            for (int j=i+1;j<list.size();j++){
                if (list.get(i)!=null){

                    try {
                        TimeUnitBean unitBean  = list.get(i);
                        Date         date_End       = sDateFormat.parse(unitBean
                                .getEndTime());
                        Date         nextDate_End   = sDateFormat.parse(list.get(j).getEndTime());
                        Date         nextDate_Start = sDateFormat.parse(list.get(j).getStartTime());

                        if (nextDate_Start.before(date_End)&&nextDate_End.before(date_End)){

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
            }
        }
        for (int i = 0; i < unitBeen.size(); i++) {
            Log.w("testAppfront", "--"+i+"-分好的时间段-"+unitBeen.get(i).toString());
        }

        return unitBeen;

    }

    public  String formatData(String dataFormat, long timeStamp) {
        if (timeStamp == 0) {
            Log.d("testAppfront","-----------result为空" );
            return "";
        }
        timeStamp = timeStamp * 1000;
        String result = "";
        SimpleDateFormat format = new SimpleDateFormat(dataFormat);
        result = format.format(new Date(timeStamp));
        Log.d("testAppfront","-----------"+ result);
        return result;
    }

}
