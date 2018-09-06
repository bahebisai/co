package com.xiaomi.emm.features.policy.phoneCall;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.provider.CallLog;
import android.util.Log;

import com.miui.enterprise.sdk.PhoneManager;
import com.xiaomi.emm.base.BaseApplication;
import com.xiaomi.emm.definition.OrderConfig;
import com.xiaomi.emm.features.impl.CallRecorderUploadImpl;
import com.xiaomi.emm.utils.DataParseUtil;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.MDM;
import com.xiaomi.emm.utils.TheTang;
import com.xiaomi.emm.utils.TimeUtils;

import java.io.File;

import static android.content.Context.ALARM_SERVICE;

public class CallRecorderManager {
    private static final String TAG = CallRecorderManager.class.getName();
    CallRecorderPolicyInfo mCallRecorderPolicyInfo;
    private Context mContext;
    public static final String CALL_RECORDER_SP_NAME = "call_recorder_policy";
    public static final String CALL_RECORDER_POLICY_KEY = "policy";
    static final String CALL_RECORDER_POLICY_OPEN = "isPolicyOpen";
    public static final String CALL_RECORDER_DISPLAY_TIME_STRING = "callRecorderDisplayTimeString";

    public static final String START_CALL_RECORDER = "app.action.START_CALL_RECORDER";
    public static final String RECORDER_PATH = BaseApplication.baseFilesPath + File.separator + "rec_file";
    boolean mIsPolicyOpen;

    private static class SingletonHolder {
        public static CallRecorderManager instance = new CallRecorderManager();
    }
    public static CallRecorderManager newInstance() {
        return SingletonHolder.instance;
    }

    private CallRecorderManager() {
        init();
    }
    private void init() {
        mContext = TheTang.getSingleInstance().getContext();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(CALL_RECORDER_SP_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(CALL_RECORDER_POLICY_KEY, "");
        mCallRecorderPolicyInfo = DataParseUtil.getCallRecorderPolicyInfo(json);
        mIsPolicyOpen = sharedPreferences.getBoolean(CALL_RECORDER_POLICY_OPEN, false);

        mCallReceiver = new CallReceiver();

        File file = new File(RECORDER_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public void executeCallRecorderPolicy(String json, boolean isFromOrder) {
        if (isFromOrder) {
            mCallRecorderPolicyInfo = DataParseUtil.getCallRecorderPolicyInfo(json);
            savePolicy(json);
            TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.SEND_CALL_RECORDER_BACKUP_POLICY), mCallRecorderPolicyInfo.getName());
            TheTang.getSingleInstance().addStratege(String.valueOf(OrderConfig.SEND_CALL_RECORDER_BACKUP_POLICY), mCallRecorderPolicyInfo.getName(), System.currentTimeMillis() + "");
        }

        if (TimeUtils.isInDateRange(System.currentTimeMillis(), mCallRecorderPolicyInfo.getTimeData())) {
            registerCallReceiver();
            MDM.mMDMController.setCallAutoRecordDir(RECORDER_PATH);
        } else if (TimeUtils.isExpired(System.currentTimeMillis(), mCallRecorderPolicyInfo.getTimeData())) {
            executeDeleteCallRecorderPolicy("", true);
        } else {
            setAlarm(TimeUtils.getStartDate(mCallRecorderPolicyInfo.getTimeData().getStartDateRange()).getTime());
        }
    }

    public void executeDeleteCallRecorderPolicy(String json, boolean isExpired) {
        deletePolicy();
        unregisterCallReceiver();
        if (!isExpired) {
            TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.DELETE_CALL_RECORDER_BACKUP_POLICY), mCallRecorderPolicyInfo.getName());
            TheTang.getSingleInstance().deleteStrategeInfo(String.valueOf(OrderConfig.SEND_CALL_RECORDER_BACKUP_POLICY), mCallRecorderPolicyInfo.getName());
        }
    }

    private void savePolicy(String json) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(CALL_RECORDER_SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CALL_RECORDER_POLICY_KEY, json);
        editor.putBoolean(CALL_RECORDER_POLICY_OPEN, true);
        editor.putString(CALL_RECORDER_DISPLAY_TIME_STRING, TimeUtils.getDisplayTimeString(mCallRecorderPolicyInfo.getTimeData()));
//        Log.d("baii", TimeUtils.getDisplayTimeString(mCallRecorderPolicyInfo.getTimeData()));
        editor.commit();
        mIsPolicyOpen = true;
    }

    private void deletePolicy() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(CALL_RECORDER_SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CALL_RECORDER_POLICY_KEY, "");
        editor.putBoolean(CALL_RECORDER_POLICY_OPEN, false);
        editor.commit();
        mIsPolicyOpen = false;
    }

    public void checkCallRecorderPolicy() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(CALL_RECORDER_SP_NAME, Context.MODE_PRIVATE);
        boolean isOpen = sharedPreferences.getBoolean(CALL_RECORDER_POLICY_OPEN, false);
        if (isOpen) {
            String json = sharedPreferences.getString(CALL_RECORDER_POLICY_KEY, "");
            executeCallRecorderPolicy(json, false);
        }
    }

    public void setAlarm(long time) {
//        Log.d("baii", " setAlarm time " + time);
        LogUtil.writeToFile(TAG, " setAlarm time " + time);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(START_CALL_RECORDER);
        intent.setComponent(new ComponentName(mContext.getPackageName(),
                "com.xiaomi.emm.features.receiver.AlarmReceiver"));
//        time = System.currentTimeMillis();//todo baii to delete
//        Log.d("baii", "time " + time);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, time, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC, time, pendingIntent);
        }
    }

    public void uploadCallRecorder(String path, long duration, int type) {
        //todo baii only upload when recorder completed
        if (!mIsPolicyOpen) {
            return;
        }
        if (mCallRecorderPolicyInfo == null) {
            return;
        }

        String fileName = new File(path).getName();
        CallRecorderInfo callRecorderInfo = new CallRecorderInfo();
        callRecorderInfo.setAddress(fileName.substring(fileName.indexOf('(')+1,fileName.indexOf(')')));
        callRecorderInfo.setDate(TimeUtils.getDateString(fileName.substring(fileName.indexOf('_')+1,fileName.indexOf('.'))));
        callRecorderInfo.setPerson(fileName.substring(fileName.indexOf('@')+1, fileName.indexOf('(')));
        callRecorderInfo.setDuration(duration);
        callRecorderInfo.setPath(path);
        callRecorderInfo.setType(type);
//        Log.d("baii", "upload file " + callRecorderInfo.toString());
        LogUtil.writeToFile(TAG, "upload file " + callRecorderInfo.toString());
        CallRecorderUploadImpl callRecorderUpload = new CallRecorderUploadImpl(mContext);
        callRecorderUpload.uploadCallRecorder(mCallRecorderPolicyInfo.getId(), callRecorderInfo);

/*        String selection = CallLog.Calls.DATE + ">? and " + CallLog.Calls.DURATION + ">?";
        Cursor cursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, //系统方式获取通讯录存储地址
                new String[]{
                        CallLog.Calls.CACHED_NAME,  //姓名
                        CallLog.Calls.NUMBER,    //号码
                        CallLog.Calls.TYPE,  //呼入/呼出(2)/未接
                        CallLog.Calls.DATE,  //拨打时间
                        CallLog.Calls.DURATION,   //通话时长
                }, selection, new String[]{String.valueOf(System.currentTimeMillis() - 24*60*60*1000), String.valueOf(0)},
                CallLog.Calls.DEFAULT_SORT_ORDER);
        Log.d("baii", "cursor size " + cursor.getCount());
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
            String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            long duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
            long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
            CallRecorderInfo callRecorderInfo = new CallRecorderInfo();
            callRecorderInfo.setAddress(number);
            callRecorderInfo.setDate(date);
            callRecorderInfo.setPerson(name);
            callRecorderInfo.setDuration(duration);
            callRecorderInfo.setPath(path);
        }*/

/*        long time = System.currentTimeMillis();
        if (TimeUtils.isInDateRange(time, mCallRecorderPolicyInfo.getTimeData())) {
            if (TimeUtils.isInTimeUnitRange(time, mCallRecorderPolicyInfo.getTimeData())) {
                Log.d("baii", "upload call recorder");
                File directoryFile = new File(RECORDER_PATH);//todo baii upload only the current recorder
                if (directoryFile.exists()) {
                    File[] files = directoryFile.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            String fileName = file.getName();
                            CallRecorderInfo callRecorderInfo = new CallRecorderInfo();
                            callRecorderInfo.setAddress(fileName.substring(fileName.indexOf('(')+1,fileName.indexOf(')')));
                            callRecorderInfo.setDate(TimeUtils.getDateString(fileName.substring(fileName.indexOf('_')+1,fileName.indexOf('.'))));
                            callRecorderInfo.setPerson(fileName.substring(fileName.indexOf('@')+1, fileName.indexOf('(')));
                            callRecorderInfo.setDuration(duration);
                            callRecorderInfo.setPath(file.getPath());//todo baii
                            callRecorderInfo.setType(type);
                            Log.d("baii", "upload file " + callRecorderInfo.toString());
                            CallRecorderUploadImpl callRecorderUpload = new CallRecorderUploadImpl(mContext);
                            callRecorderUpload.uploadCallRecorder(mCallRecorderPolicyInfo.getId(), callRecorderInfo);
                        }
                    }
                }
            }
        } else {
            executeDeleteCallRecorderPolicy("", true);
        }*/
    }

    public boolean isInUploadTimeRange() {
        if (!mIsPolicyOpen) {
            return false;
        }
        if (mCallRecorderPolicyInfo == null) {
            return false;
        }
        long time = System.currentTimeMillis();
        if (TimeUtils.isInDateRange(time, mCallRecorderPolicyInfo.getTimeData())) {
            if (TimeUtils.isInTimeUnitRange(time, mCallRecorderPolicyInfo.getTimeData())) {
//                Log.d("baii", "need to record");
                LogUtil.writeToFile(TAG, "need to record");
                return true;
            } else {
                return false;
            }
        } else {
            executeDeleteCallRecorderPolicy("", true);
            return false;
        }
    }

    private CallReceiver mCallReceiver;
    private void registerCallReceiver() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(PhoneManager.ACTION_CALL_CONNECTED);
        intentFilter.addAction(PhoneManager.ACTION_INCOMING_CALL);
        intentFilter.addAction(PhoneManager.ACTION_OUTGOING_CALL);
        intentFilter.addAction(PhoneManager.ACTION_CALL_RECORD);
//        intentFilter.addAction("android.intent.action.PHONE_STATE");
//        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        mContext.registerReceiver(mCallReceiver, intentFilter);
    }

    private void unregisterCallReceiver() {
        if (mCallReceiver != null) {
            mContext.unregisterReceiver(mCallReceiver);
        }
    }
}
