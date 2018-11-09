package com.zoomtech.emm.features.policy.trajectory;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.zoomtech.emm.model.TimeFenceData;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;

/*
 *
 *  @项目名：  MDM
 *  @包名：    com.huawei_emm.emm.utils
 *  @文件名:   TimeUtils
 *  @author:   lenovo
 *  @date:  2018/6/20 11:58
 *  @描述：    TODO
 */

public class TimeUtils {


    public  final String TAG = this.getClass().getSimpleName();

    private SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat mm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private long gtm_add = 3600 * 8 * 1000;
    private AlarmManager am = (AlarmManager) TheTang.getSingleInstance().getContext().getSystemService(ALARM_SERVICE);
    private BaseTimeReceiver baseTimeReceiver;
    private List<TimeFenceData.PolicyBean.TimeUnitBean> timeUnitList;
    // private Map<Integer,List<TimeFenceData.PolicyBean.TimeUnitBean>> map =new HashMap<>();


    OnTimeUtilsListener onTimeUtilsListener;

    public static List<String> usingtimeReceivers = new ArrayList<>() ;
    public static List<String> alltimeReceivers = new ArrayList<>() ;

    private String mStartTimeRage;
    private String mEndTimeRage;
    private List<TimeFenceData.PolicyBean.TimeUnitBean> mListUnitTime;
    private String className;

    /**
     *回调标志
     */
    public interface OnTimeUtilsListener {

        //围栏内外标志(true:表示围栏内;false:表示围栏外)
        public void setBooleanFlag(boolean timeFlag);

        //创建接收广播失败
        public void createReceiverFail(int errorCode);
        //超出结束的日期时间范围
        public void overtime_EndTimeRage();
    }






    /**
     * 传过来的时间
     * @param timeUnits
     */
    public void excuteTimeReceiver(List<TimeFenceData.PolicyBean.TimeUnitBean> timeUnits, String startTimeRage,String endTimeRage, OnTimeUtilsListener onTimeUtilsListener) {
        this.onTimeUtilsListener = onTimeUtilsListener ;
        this.mListUnitTime = timeUnits;
        this.mStartTimeRage = startTimeRage ;
        this.mEndTimeRage = endTimeRage ;
        Calendar c = Calendar.getInstance();
        String dates = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);


        //传过来的时间为空,则不需要执行
        if (mListUnitTime == null || mListUnitTime.size() <=0 || TextUtils.isEmpty( startTimeRage ) || TextUtils.isEmpty( endTimeRage )) {
            Log.w(TAG, "传过来的时间为空,则不需要执行timeUnits为空==="  );
            onTimeUtilsListener.createReceiverFail(1002); //时间为空
            return ;
        }




        //获取调取函数所在类名称
        String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
        String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
        int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
        Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers);



        className = getunUseClassName();
        if (! TextUtils.isEmpty(className)){
            addReciver(className);
            baseTimeReceiver = getTimeReceiverObject(className);
            IntentFilter intentFilter = new IntentFilter( );
            intentFilter.addAction( baseTimeReceiver.getStartTimeRage());
            intentFilter.addAction( baseTimeReceiver.getEndTimeRage());
            intentFilter.addAction( baseTimeReceiver.getAlarm_start());
            intentFilter.addAction( baseTimeReceiver.getAlarm_end());

            TheTang.getSingleInstance().getContext().registerReceiver(baseTimeReceiver, intentFilter );

            //发送广播
            doSendBroadcast();
        }else{

            onTimeUtilsListener.createReceiverFail(1001);
        }


    }



    /**
     * 取消时间广播
     */
    public  void cancelTimeReceive() {
        if (baseTimeReceiver !=null){

            Intent intentCancelReceiver = new Intent( TheTang.getSingleInstance().getContext(), /*AlarmReceiver1.class*/baseTimeReceiver.getClass() );
            PendingIntent pendingIntent = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(), baseTimeReceiver.getPendingIntentID(), intentCancelReceiver, PendingIntent.FLAG_UPDATE_CURRENT );
            AlarmManager alarmManager = (AlarmManager) TheTang.getSingleInstance().getContext().getSystemService( ALARM_SERVICE );
            alarmManager.cancel( pendingIntent );
        }
        removeReciver(className);
    }





    /**
     * 发送广播
     * @param
     */
    public  void doSendBroadcast( ) {

        /**如果之前有过时间围栏的闹钟应该先取消闹钟---防止之前下发过时间围栏策略又重新发过一次*/
        // cancelTimeReceive();
        /*********************/

        // PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        //获取时间日期范围
      //  String mStartTimeRage = policyBean.getStartTimeRage();//preferencesManager.getFenceData( Common.startimeRage );
       // String mEndTimeRage = policyBean.getEndTimeRage();//preferencesManager.getFenceData( Common.mEndTimeRage );


        if (mStartTimeRage != null && mEndTimeRage != null) {
            /*************************/
            SimpleDateFormat formats = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
            try {



                if (mStartTimeRage.contains( "/" )) {
                    mStartTimeRage = mStartTimeRage.replace( "/","-" );
                }else if(mStartTimeRage.contains("T")){
                    mStartTimeRage=  mStartTimeRage.split("T")[0].trim();
                }




                if (mEndTimeRage.contains( "/" )) {
                    mEndTimeRage = mEndTimeRage.replace( "/","-" );
                 }else if (mEndTimeRage.contains("T")){
                    mEndTimeRage= mEndTimeRage.split("T")[0].trim();
                }

                Date parse = formats.parse( mStartTimeRage + " 00:00" );
                Date parse1 = formats.parse( mEndTimeRage + " 23:59" );
                AlarmManager am = (AlarmManager) TheTang.getSingleInstance().getContext().getSystemService( ALARM_SERVICE );
                /**如果当前时间已经超过时间范围则返回*/
                if (System.currentTimeMillis() > parse1.getTime()) {
                    Log.w( TAG, "如果当前时间已经超过时间围栏设置的结束时间范围，不知执行闹钟，同时取消策略(可以把本地的时间围栏数据删除掉)" );
                    // LogUtil.writeToFile( TAG, "当前时间已经超过时间围栏设置的结束时间范围，不知执行闹钟--结束所有的广播，同时取消策略(可以把本地的时间围栏数据删除掉)" );
                    /**结束所有的广播，同时取消策略(可以把本地的时间围栏数据删除掉)*/
                    Intent intent_startTimeRage = new Intent(  );
                    PendingIntent pi = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(),  baseTimeReceiver.getPendingIntentID(), intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT );

                    if (am != null) {

                        am.cancel( pi );
                    }

                    onTimeUtilsListener.overtime_EndTimeRage();

                    return;
                }
                Log.w( TAG, "发送广播" );
                Intent intent_startTimeRage = new Intent(  );
                intent_startTimeRage.setAction( /*"mStartTimeRage"*/baseTimeReceiver.getStartTimeRage() );

                //第二个参数用于识别AlarmManager
                PendingIntent pi = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(),  baseTimeReceiver.getPendingIntentID(), intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT );

                //如果设定的时间比当前时间还小则立即执行---设置开始时间的闹钟
                am.setExact( AlarmManager.RTC_WAKEUP, parse.getTime(), pi ); //执行一次

                /******************************************/
                Intent intent_endTimeRage = new Intent(  );
                intent_endTimeRage.setAction( /*"mEndTimeRage"*/baseTimeReceiver.getEndTimeRage() );
                PendingIntent pi2 = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(),  baseTimeReceiver.getPendingIntentID(), intent_endTimeRage, PendingIntent.FLAG_UPDATE_CURRENT );

                //如果设定的时间比当前时间还小则立即执行---设置结束时间的闹钟
                Log.w( TAG, mStartTimeRage + " 00:00==" + parse.getTime() + "==size==" + mEndTimeRage + " 23:59===" + parse1.getTime() );
                //执行一次
                am.setExact( AlarmManager.RTC_WAKEUP, parse1.getTime(), pi2 );
            } catch (ParseException e) {
                e.printStackTrace();

            }
        }
    }


    private  void  doTimeFence(String type) {

        Calendar c = Calendar.getInstance();
        String dates = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);

        if (/*"mStartTimeRage"*/baseTimeReceiver.getStartTimeRage().equals(type)) {



            if (mListUnitTime == null ) {
                Log.w(TAG, "timeUnits为空===" + type);
                return ;
            }

          /*初始化选择执行时间**/
            selcetTimeExcute( mListUnitTime);
        } else if (/*"mEndTimeRage"*/baseTimeReceiver.getEndTimeRage().equals(type)) {
            /**如果接收到时间范围的结束广播*/
            Log.w(TAG, "结束的的时间闹钟===" + type);
            /**结束所有的广播，同时取消策略(可以把本地的时间围栏数据删除掉)*/
            Intent intent_startTimeRage = new Intent( );
            PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(),  baseTimeReceiver.getPendingIntentID(), intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);

            //   AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            am.cancel(pi);
            /**清除本地的时间围栏临时数据*/
            PreferencesManager.getSingleInstance().clearTimefenceData();
            //清除时间围栏本地数据
            // FenceManager.deleteTimeFenceData();
            /**执行时间范围外的操作*/
            //ExcuteTimeFence.excute_TiemFence(false); 应该没有这个
            onTimeUtilsListener.setBooleanFlag(false);

        } else if (/*"alarm_start"*/baseTimeReceiver.getAlarm_start().equals(type)) {
            onTimeUtilsListener.setBooleanFlag(true);

            if (timeUnitList != null && ! timeUnitList.isEmpty()) {


                Intent intents = new Intent( );
                intents.setAction(/*"alarm_end"*/baseTimeReceiver.getAlarm_end());
                PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(),  baseTimeReceiver.getPendingIntentID(), intents, PendingIntent.FLAG_UPDATE_CURRENT);
                /*定结束闹钟*/
                try {
                    //定闹钟
                    am.setExact(AlarmManager.RTC_WAKEUP, mm.parse(dates + " " + timeUnitList.get(0).getEndTime()).getTime(), pi);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Log.w(TAG,"定结束 闹钟"+baseTimeReceiver.getAlarm_end()+"---"+timeUnitList.get(0).getEndTime());
            }
        } else if (/*"alarm_end"*/baseTimeReceiver.getAlarm_end().equals(type)) {

            /*执行围栏外的策略*/

            Log.w(TAG, "执行" + type);
            onTimeUtilsListener.setBooleanFlag(false);

            if (timeUnitList == null || timeUnitList.size() <=0){
                Log.w(TAG, "alarm_end-timeUnitList 为空" );
                //LogUtil.writeToFile(TAG, "alarm_end-timeUnitList 为空" );

                //1.最后一个结束闹钟，则定明天的闹钟
                //2.添加符合第二天的时间，排序，然后存储起来发明天开始的广播
                String date= c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);

                if (!date.equals(mEndTimeRage)){

                    nextSelectExcuteTime(mListUnitTime);
                    Log.w(TAG, "最后一个结束闹钟，则定明天的闹钟" + type);
                }else {
                    Log.w(TAG, "最后一个结束闹钟，今天时最后一天的时间范围不定明天的闹钟了...." + type);

                }
            }
            Log.w(TAG, "timeUnits分PreferencesManager段好的时间=extr==" + timeUnitList.toString());

            if (timeUnitList.size() >=2 ) {

                Intent intents = new Intent( );
                intents.setAction(/*"alarm_start"*/baseTimeReceiver.getAlarm_start());
                PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(),  baseTimeReceiver.getPendingIntentID(), intents, PendingIntent.FLAG_UPDATE_CURRENT);
                //删除当前的闹钟，一般都是第一个，定下个开始闹钟
                timeUnitList.remove(0);
                try {
                    //定闹钟
                    am.setExact(AlarmManager.RTC_WAKEUP, mm.parse(dates + " " + timeUnitList.get(0).getStartTime()).getTime(), pi);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                Log.w(TAG,"定开始"+ baseTimeReceiver.getAlarm_start() +"闹钟="+timeUnitList.get(0).getStartTime());
                Log.w(TAG, "timeUnits删除一个后剩余数据===" + timeUnitList.toString());
            } else {
                timeUnitList.remove(0);
                //1.最后一个结束闹钟，则定明天的闹钟
                //2.添加符合第二天的时间，排序，然后存储起来发明天开始的广播
                String date= c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);

                if (!date.equals(mEndTimeRage)){

                    nextSelectExcuteTime(mListUnitTime);
                    Log.w(TAG, "最后一个结束闹钟，则定明天的闹钟" + type);
                }else {
                    Log.w(TAG, "最后一个结束闹钟，今天时最后一天的时间范围不定明天的闹钟了...." + type);
                }
            }

        }

    }





    /**
     * 初始化选择执行时间
     * @param lists
     */
    private  void selcetTimeExcute(List<TimeFenceData.PolicyBean.TimeUnitBean> lists) {
        Calendar c = Calendar.getInstance();
        String dates = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
        /**选择今天符合的条件时间段出来*/
        // list= selectTodayTime(list);
        timeUnitList = selectTodayTime(lists);
        if (timeUnitList ==null || timeUnitList.size()<=0){
            Log.w(TAG, "选择今天符合的条件时间段出来为null或size为0这样的情况，那就选择下一天的闹钟时间，因为有可能开始时间小于今天的时间");

            //今天没有符合相应的时间段，所以属于时间段外
            onTimeUtilsListener.setBooleanFlag(false);

            //2.添加符合第二天的时间，排序，然后存储起来发明天开始的广播

            String date= c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
            // String mEndTimeRage= preferencesManager.getFenceData(Common.mEndTimeRage);
            String endTimeRage =mEndTimeRage ;
            Log.w(TAG, date+"--"+endTimeRage+ timeUnitList.size()+"--"+"执行围栏外的策略后,添加符合第二天的时间，排序，然后存储起来发明天开始的广播FenceExcute.excuteGeographicalFence(false)");
            if (!date.equals(endTimeRage)){

                nextSelectExcuteTime( lists );
            }else {
                Log.w(TAG, "最后一个结束闹钟，今天时最后一天的时间范围不定明天的闹钟了...." );
            }
            return;
        }

        try {

            //Calendar c = Calendar.getInstance();
            // String dates= c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
            //   long     times_Date = format.parse(dates).getCallTime();//今天 00:00的毫秒时间

            long   currentTimeMillis =System.currentTimeMillis();
            Date date = new Date(currentTimeMillis);

            TimeFenceData.PolicyBean.TimeUnitBean unitBean = timeUnitList.get(timeUnitList.size() - 1);

            String unitBeanStartTime      = unitBean.getStartTime();
            String unitBeanEndTime      = unitBean.getEndTime();
            long endTime   =  mm.parse(dates + " " +unitBeanEndTime).getTime();
            long starTime   =  mm.parse(dates + " " +unitBeanStartTime).getTime();
            Log.w(TAG, dates+"现在此刻的时间--"+date.toString());
            if (currentTimeMillis>=endTime){
                //1.执行围栏外的策略
                onTimeUtilsListener.setBooleanFlag(false);

                //2.添加符合第二天的时间，排序，然后存储起来发明天开始的广播
                Log.w(TAG, "--"+ timeUnitList.size()+"--"+"执行围栏外的策略后,添加符合第二天的时间，排序，然后存储起来发明天开始的广播");
                Log.w(TAG," FenceExcute.excuteGeographicalFence(false)");
                nextSelectExcuteTime(lists);

                return;

            }else if ( timeUnitList.size() == 1){
                Intent intent = new Intent( );
                if (currentTimeMillis < starTime){
                    //在第一个时间围栏之前-
                    Log.w(TAG, "--"+0+"--"+"在第一个时间围栏之前="+unitBeanStartTime+"--发送"+unitBeanStartTime+"-de闹钟");
                    Log.w(TAG,"-2-"+date.toString());

                    intent.setAction(/*"alarm_start"*/baseTimeReceiver.getAlarm_start());
                    PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(),  baseTimeReceiver.getPendingIntentID(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    am.setExact(AlarmManager.RTC_WAKEUP, starTime, pi);//定闹钟
                    /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/

                    Log.w(TAG, "--"+0+"--"+"在第一个时间围栏之前闹钟==="+timeUnitList.toString());
                }else if (starTime<= currentTimeMillis && currentTimeMillis< endTime) {
                    //在某个时间围栏内
                    Log.w(TAG, "--" + 0 + "--" + "在某个时间围栏内" + unitBeanStartTime + "==" + unitBeanEndTime);
                    Log.w(TAG, "-2-在某个时间围栏内命令-ExcuteTimeFence.excute_TiemFence(true)");
                    //1.执行时间围栏内的策略

                    onTimeUtilsListener.setBooleanFlag(true);

                    intent.setAction(/*"alarm_end"*/baseTimeReceiver.getAlarm_end());
                    PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(),  baseTimeReceiver.getPendingIntentID(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    //2定结束闹钟
                    am.setExact(AlarmManager.RTC_WAKEUP, endTime, pi);//定闹钟
                    /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                    // PreferencesManager.getSingleInstance().setTimefenceData("key", 0 + "_" + json);
                    //  map.put(0, timeUnitList);
                }


            }else {

                for (int i = 0; i < timeUnitList.size() ; i++) {

                    try {
                        Date parse = mm.parse(dates + " " + timeUnitList.get(i).getStartTime());
                        long start =  mm.parse(dates + " " +timeUnitList.get(i).getStartTime()).getTime();
                        long end =  mm.parse(dates + " " +timeUnitList.get(i).getEndTime()) .getTime();
                        Intent intent = new Intent( );
                        Log.w(TAG, "--"+i+"--"+"时间围--parse--"+parse+"=="+parse.getTime());
                        // Log.w(TAG, "--"+i+"--"+"时间围--00:00--"+dates+"=="+times_Date);
                        Log.w(TAG, "--"+i+"--"+sDateFormat.parse(timeUnitList.get(i).getStartTime()).getTime()+"时间围="+timeUnitList.get(i).getStartTime()+"==="+start);
                        Log.w(TAG, "--"+i+"--"+sDateFormat.parse(timeUnitList.get(i).getEndTime()) .getTime()+"时间围="+timeUnitList.get(i).getEndTime()+"=="+end);
                        Log.w(TAG, "--"+i+"--"+"时间围此刻时间="+currentTimeMillis+"==="+date.toString());

                        if (currentTimeMillis < start){
                            //在第一个时间围栏之前
                            Log.w(TAG, "--"+i+"--"+"在第一个时间围栏之前="+timeUnitList.get(i).getStartTime());
                            Log.w(TAG,"-2-"+date.toString());

                            intent.setAction(/*"alarm_start"*/baseTimeReceiver.getAlarm_start());
                            PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(),  baseTimeReceiver.getPendingIntentID(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            am.setExact(AlarmManager.RTC_WAKEUP, start, pi);//定闹钟
                            /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                            //PreferencesManager.getSingleInstance().setTimefenceData("key",i+"_"+json);
                            //map.put(i,timeUnitList);
                            break;
                        }else if (start <= currentTimeMillis&&currentTimeMillis<end){
                            //在某个时间围栏内
                            Log.w(TAG, "--"+i+"--"+"在某个时间围栏内"+timeUnitList.get(i).getStartTime()+"=="+timeUnitList.get(i).getEndTime());
                            Log.w(TAG,"-2-在某个时间围栏内命令-ExcuteTimeFence.excute_TiemFence(true)");
                            //1.执行时间围栏内的策略

                            onTimeUtilsListener.setBooleanFlag(true);
                            intent.setAction(/*"alarm_end"*/baseTimeReceiver.getAlarm_end());
                            PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(),  baseTimeReceiver.getPendingIntentID(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            //2定结束闹钟
                            am.setExact(AlarmManager.RTC_WAKEUP, end, pi);//定闹钟
                            /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/

                            break;
                        }else if (currentTimeMillis >= end){
                            if ((i+1) >= (timeUnitList.size())) {
                                continue;
                            }else if(currentTimeMillis < mm.parse(dates + " " +timeUnitList.get(i+1).getStartTime()).getTime()){

                                //在某个时间围栏外，下一个时间围栏之前
                                Log.w(TAG, "--"+i+"--"+"在某个时间围栏外="+timeUnitList.get(i).getEndTime()+"，下一个时间围栏之前="+timeUnitList.get(i+1).getStartTime());
                                // Log.w(TAG,"-2-在某个时间围栏内命令-ExcuteTimeFence.excute_TiemFence(false)");
                                //1.执行围栏外的策略
                                onTimeUtilsListener.setBooleanFlag(false);
                                timeUnitList.remove(i);

                                intent.setAction(/*"alarm_start"*/baseTimeReceiver.getAlarm_start());
                                PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(),  baseTimeReceiver.getPendingIntentID(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                //2定下个开始闹钟
                                am.setExact(AlarmManager.RTC_WAKEUP, mm.parse(dates + " " +timeUnitList.get(0).getStartTime()).getTime(), pi);

                                /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/

                                break;
                            }

                        }else {
                            Log.w(TAG,i+"--到这里时"+timeUnitList.get(i).getStartTime()+",="+timeUnitList.get(i).getEndTime());
                        }


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    //删除集合里面数据同时删除下角标数据
                    timeUnitList.remove(i);
                    i--;

                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下一天的执行时间
     * @param timeUnits
     */
    private void nextSelectExcuteTime(List<TimeFenceData.PolicyBean.TimeUnitBean> timeUnits){
        Calendar cc = Calendar.getInstance();

        String dates= cc.get(Calendar.YEAR)+"-"+(cc.get(Calendar.MONTH)+1)+"-"+cc.get(Calendar.DAY_OF_MONTH);
        if (dates.equals(mEndTimeRage)){
            Log.w(TAG, mEndTimeRage+"最后一个结束闹钟，今天时最后一天的时间范围不定明天的闹钟了...." +dates);

           onTimeUtilsListener.overtime_EndTimeRage();
            return;

        }

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
                c.add(Calendar.DAY_OF_MONTH, 1);
                String date= c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
                String typeDate = bean.getTypeDate();
                Log.w(TAG,"typeDate-特殊日期-"+typeDate);
                if(typeDate.contains("/")){

                    typeDate= typeDate.replace("/","-");
                }else if(typeDate.contains("T")){
                    typeDate= typeDate.split("T")[0].trim();
                }
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
            c.add(Calendar.DAY_OF_MONTH, 1);
            String date= c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
            try {
                Log.w(TAG,"如果明天没有符合明天的时间，那就订明天00：00的闹钟,每天查询");
                //LogUtil.writeToFile(TAG,"如果明天没有符合明天的时间，那就订明天00：00的闹钟,每天查询");
                Date parse = formats.parse(date+" 00:00");
                Intent intent_startTimeRage = new Intent( );
                intent_startTimeRage.setAction(/*"mStartTimeRage"*/baseTimeReceiver.getStartTimeRage());

                //第二个参数用于识别AlarmManager
                PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(),  baseTimeReceiver.getPendingIntentID(), intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT);

                am.setExact(AlarmManager.RTC_WAKEUP, parse.getTime(), pi); //执行一次
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return;
        }

        /*把下一天分好的时间段转换成时间格式*/
        String json        = new Gson().toJson(arrayList);
        /**选择第一个开始时间闹钟*/
        Intent intent = new Intent( );

        PendingIntent pi = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(),  baseTimeReceiver.getPendingIntentID(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        intent.setAction(/*"alarm_start"*/baseTimeReceiver.getAlarm_start());
        if (arrayList.size()>0){
            TimeFenceData.PolicyBean.TimeUnitBean unitBean = arrayList.get(0);
            try {
                //long time = sDateFormat.parse(unitBean.getStartTime()) .getCallTime();
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_MONTH, 1);
                String date= c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
                long time =  mm.parse(date + " "+unitBean.getStartTime()).getTime();//第一个开始时间第二天的时间
                // long     times_Date = format.parse(date).getCallTime();//第二天 00:00的毫秒时间
                am.setExact(AlarmManager.RTC_WAKEUP, time, pi);//执行
                /**把分好的时间村粗起来，格式是执行i为第几个时间范围*/
                Log.w(TAG,"定明天的闹钟 = "+unitBean.getStartTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }


        }

    }


    /**
     * 选择当天符合的时间
     * @param timeUnits
     * @return
     */
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
                if(typeDate.contains("/")){

                    typeDate= typeDate.replace("/","-");
                }else if(typeDate.contains("T")){
                    typeDate= typeDate.split("T")[0].trim();
                }
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
     * 获取今天是周几
     * @return
     */
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

        Log.w(TAG,"---"+list.toString());
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
                    // private SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");
                    i = sDateFormat.parse(o1.getStartTime()).compareTo(sDateFormat.parse(o2.getStartTime()));
                    //  Log.w("ss",i+"---");

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return i;
            }
        });


        //  Log.w("ss", "--1--"+list.toString());
       /* for (int i = 0; i < list.size(); i++) {

            Log.w(TAG, "--"+i+"-按时间排序好-"+list.get(i).toString());

        }*/

        for (int i = 0; i < list.size()-1; i++) {
            for (int j=i+1;j<list.size();j++){
                if (list.get(i)!=null){

                    try {
                        TimeFenceData.PolicyBean.TimeUnitBean unitBean  = list.get(i);
                        Date date_End       = sDateFormat.parse(unitBean.getEndTime());
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



    /**
     * 时间接收器
     */
    public  class BaseTimeReceiver extends BroadcastReceiver {

        private  String startTimeRage = "mStartTimeRage";
        private String endTimeRage = "mEndTimeRage";
        private String alarm_start = "alarm_start";
        private String alarm_end = "alarm_end";



        private int pendingIntentID = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            if("mStartTimeRage".equals(intent.getAction())) {

                //  i.putExtra("TimeFenceReceiver",intent.getAction().toString());


            }else  if("mEndTimeRage".equals(intent.getAction())) {

                // i.putExtra("TimeFenceReceiver",intent.getAction().toString());

            }else if("alarm_start".equals(intent.getAction())){

                // i.putExtra("TimeFenceReceiver",intent.getAction().toString());

            }else if("alarm_end".equals(intent.getAction())){

                //  i.putExtra("TimeFenceReceiver",intent.getAction().toString());
            }
            //   Log.d(TAG,"-----------"+ intent.getAction());
            baseTimeReceiver = this ;
            doTimeFence( intent.getAction() );

           /* //泛型解析
            Class<? extends BaseTimeReceiver> clazz = this.getClass();
            try {
                Method method = clazz.getMethod(intent.getAction(), null);
                method.invoke(this);
            } catch (Exception e) {
                e.printStackTrace();
            }*/

        }


        public String getStartTimeRage() {
            //获取调取函数所在类名称
            String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
            String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
            Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers +"pendingIntentID= "+pendingIntentID);

            return startTimeRage+this.getClass().getSimpleName();
        }

        public String getEndTimeRage() {
            //获取调取函数所在类名称
            String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
            String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
            Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers +"pendingIntentID= "+pendingIntentID);

            return endTimeRage+this.getClass().getSimpleName();
        }

        public String getAlarm_start() {
            //获取调取函数所在类名称
            String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
            String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
            Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers +"pendingIntentID= "+pendingIntentID);

            return alarm_start+this.getClass().getSimpleName();
        }

        public String getAlarm_end() {
            //获取调取函数所在类名称
            String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
            String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
            Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers +"pendingIntentID= "+pendingIntentID);

            return alarm_end+this.getClass().getSimpleName();
        }


        public int getPendingIntentID() {
            //获取调取函数所在类名称
            String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
            String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
            Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers +"pendingIntentID= "+pendingIntentID);

            return baseTimeReceiver.pendingIntentID;
        }
    }

    class  TimeFenceReceiver extends BaseTimeReceiver {

        private int pendingIntentID = 1121;
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);

        }


        @Override
        public int getPendingIntentID() {
            //获取调取函数所在类名称
            String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
            String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
            Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers +"pendingIntentID= "+pendingIntentID);

            return pendingIntentID;
        }
    }

    public   class  AppFenceReceiver extends BaseTimeReceiver {
        private int pendingIntentID = 1122;
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);

        }

        @Override
        public int getPendingIntentID() {
            //获取调取函数所在类名称
            String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
            String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
            Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers +"pendingIntentID= "+pendingIntentID);

            return pendingIntentID;
        }
    }


    public   class  TrajectoryReceiver extends BaseTimeReceiver {

        private int pendingIntentID = 1123;
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);

        }


        @Override
        public int getPendingIntentID() {
            //获取调取函数所在类名称
            String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
            String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
            Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers +"pendingIntentID= "+pendingIntentID);

            return pendingIntentID;
        }
    }



    public class SmsReceiver extends BaseTimeReceiver {
        private int pendingIntentID = 1124;
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);

        }


        @Override
        public int getPendingIntentID() {
            //获取调取函数所在类名称
            String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
            String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
            Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers +"pendingIntentID= "+pendingIntentID);

            return pendingIntentID;
        }
    }

    public class RecordReceiver extends BaseTimeReceiver {
        private int pendingIntentID = 1125;
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);

        }


        @Override
        public int getPendingIntentID() {
            //获取调取函数所在类名称
            String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
            String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
            Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers +"pendingIntentID= "+pendingIntentID);

            return pendingIntentID;
        }
    }

    public class T1Receiver extends BaseTimeReceiver {
        private int pendingIntentID = 1126;
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);

        }


        @Override
        public int getPendingIntentID() {
            //获取调取函数所在类名称
            String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
            String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
            Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers +"pendingIntentID= "+pendingIntentID);

            return pendingIntentID;
        }
    }
    public class T2Receiver extends BaseTimeReceiver {
        private int pendingIntentID = 1127;
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);

        }


        @Override
        public int getPendingIntentID() {
            //获取调取函数所在类名称
            String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
            String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
            Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers +"pendingIntentID= "+pendingIntentID);

            return pendingIntentID;
        }
    }

    public class T3Receiver extends BaseTimeReceiver {
        private int pendingIntentID = 1128;
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);

        }


        @Override
        public int getPendingIntentID() {
            //获取调取函数所在类名称
            String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
            String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
            int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
            Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers +"pendingIntentID= "+pendingIntentID);

            return pendingIntentID;
        }
    }


    /**
     * 根据类名获取相应的对象，以后下一个子类广播就在这加相应的类名对象
     * @param className
     * @return
     */
    public BaseTimeReceiver getTimeReceiverObject(String className){

        BaseTimeReceiver timeReceiver = null;
        if (TimeFenceReceiver.class.getSimpleName().equals(className)){
            timeReceiver =new TimeFenceReceiver();
        }else if(AppFenceReceiver.class.getSimpleName().equals( className )){
            timeReceiver =new AppFenceReceiver();
        }else if(TrajectoryReceiver.class.getSimpleName().equals(className)){
            timeReceiver =new TrajectoryReceiver();
        }else if(SmsReceiver.class.getSimpleName().equals(className)){
            timeReceiver =new SmsReceiver();
        }else if(RecordReceiver.class.getSimpleName().equals(className)){
            timeReceiver =new RecordReceiver();
        }else if(T1Receiver.class.getSimpleName().equals(className)){
            timeReceiver =new T1Receiver();
        }else if(T2Receiver.class.getSimpleName().equals(className)){
            timeReceiver =new T2Receiver();
        }

        else if(T3Receiver.class.getSimpleName().equals(className)){
            timeReceiver =new T3Receiver();
        }
        return timeReceiver;
    }


    /**
     * 取消所有的广播
     */
    public static void cancelAllReceive(){
        AlarmManager alarmManager = (AlarmManager) TheTang.getSingleInstance().getContext().getSystemService( ALARM_SERVICE );
        //取消所有的广播
        Intent intentCancelReceiver = new Intent( TheTang.getSingleInstance().getContext(), TimeFenceReceiver.class );
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(),1121, intentCancelReceiver, PendingIntent.FLAG_UPDATE_CURRENT );
        alarmManager.cancel( pendingIntent1 );

        PendingIntent pendingIntent2 = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(),1122,  new Intent( TheTang.getSingleInstance().getContext(), AppFenceReceiver.class ), PendingIntent.FLAG_UPDATE_CURRENT );
        alarmManager.cancel( pendingIntent2 );


        PendingIntent pendingIntent3 = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(),1123,  new Intent( TheTang.getSingleInstance().getContext(), TrajectoryReceiver.class ), PendingIntent.FLAG_UPDATE_CURRENT );
        alarmManager.cancel( pendingIntent3 );


        PendingIntent pendingIntent4 = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(),1124,  new Intent( TheTang.getSingleInstance().getContext(), SmsReceiver.class ), PendingIntent.FLAG_UPDATE_CURRENT );
        alarmManager.cancel( pendingIntent4 );

        PendingIntent pendingIntent5 = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(),1125,  new Intent( TheTang.getSingleInstance().getContext(), RecordReceiver.class ), PendingIntent.FLAG_UPDATE_CURRENT );
        alarmManager.cancel( pendingIntent5 );


        PendingIntent pendingIntent6 = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(),1126,  new Intent( TheTang.getSingleInstance().getContext(), T1Receiver.class ), PendingIntent.FLAG_UPDATE_CURRENT );
        alarmManager.cancel( pendingIntent6 );


        PendingIntent pendingIntent7 = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(),1127,  new Intent( TheTang.getSingleInstance().getContext(), T2Receiver.class ), PendingIntent.FLAG_UPDATE_CURRENT );
        alarmManager.cancel( pendingIntent7 );


        PendingIntent pendingIntent8 = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(),1128,  new Intent( TheTang.getSingleInstance().getContext(), T3Receiver.class ), PendingIntent.FLAG_UPDATE_CURRENT );
        alarmManager.cancel( pendingIntent8 );
    }


    /*
    *开始就初始化相应的类名
    * 以后建立一个BaseTimeReceiver子类广播就在这加下相应的类名
    *
    */
    public static void setAlltimeReceivers(){
        alltimeReceivers.add(TimeFenceReceiver.class.getSimpleName());
        alltimeReceivers.add(AppFenceReceiver.class.getSimpleName());
        alltimeReceivers.add(TrajectoryReceiver.class.getSimpleName());
        alltimeReceivers.add(SmsReceiver.class.getSimpleName());
        alltimeReceivers.add(RecordReceiver.class.getSimpleName());
        alltimeReceivers.add(T1Receiver.class.getSimpleName());
        alltimeReceivers.add(T2Receiver.class.getSimpleName());
        alltimeReceivers.add(T3Receiver.class.getSimpleName());

    }

    /**
     * 添加已使用的类名
     * @param className
     */
    public  void addReciver(String className){
        usingtimeReceivers.add(className);

    }

    /**
     * 从队列移除正使用的广播
     * @param className
     */
    public  void removeReciver(String className){

        if (baseTimeReceiver != null){
            Log.w(TAG,"-注销广播--"+baseTimeReceiver.getClass().getSimpleName());
            TheTang.getSingleInstance().getContext().unregisterReceiver(baseTimeReceiver);
        }
        if (!TextUtils.isEmpty(className)){
            Log.w(TAG,"-从队列移除正使用的广播--");

            usingtimeReceivers.remove(className);
        }

    }

    /**
     * 获取未使用过的广播类名
     * @return
     */
    public  String getunUseClassName(){
        if (alltimeReceivers.size() <= 0){
            return null;
        }
        for (String unUsingclazz : alltimeReceivers) {
            //如果正使用的集合都没有存任何类名，则把存所有的表返回第一个
            if (usingtimeReceivers.size() ==0){
                return alltimeReceivers.get(0);
            }

            //对比正使用的广播，如果没有使用过则返回去
            for (String usingClazz: usingtimeReceivers) {
                if (! unUsingclazz.equals(usingClazz)){
                    return unUsingclazz;
                }
            }

        }


        return null;
    }

}
