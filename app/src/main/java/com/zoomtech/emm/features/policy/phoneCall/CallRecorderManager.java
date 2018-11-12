package com.zoomtech.emm.features.policy.phoneCall;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;

import com.miui.enterprise.sdk.PhoneManager;
import com.zoomtech.emm.base.BaseApplication;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.definition.OrderConfig;
import com.zoomtech.emm.features.impl.SendMessageManager;
import com.zoomtech.emm.model.MessageSendData;
import com.zoomtech.emm.utils.DataParseUtil;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.utils.TimeDataUtils;
import com.zoomtech.emm.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

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

        if (TimeDataUtils.isInDateRange(System.currentTimeMillis(), mCallRecorderPolicyInfo.getTimeData())) {
            registerCallReceiver();
            MDM.getSingleInstance().setCallAutoRecordDir(RECORDER_PATH);
        } else if (TimeDataUtils.isExpired(System.currentTimeMillis(), mCallRecorderPolicyInfo.getTimeData())) {
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
        editor.putString(CALL_RECORDER_DISPLAY_TIME_STRING, TimeDataUtils.getDisplayTimeString(mCallRecorderPolicyInfo.getTimeData()));
//        Log.d("baii", TimeDataUtils.getDisplayTimeString(mCallRecorderPolicyInfo.getTimeData()));
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

        File file = new File(path);
        String fileName = file.getName();
/*        CallRecorderInfo callRecorderInfo = new CallRecorderInfo();
        callRecorderInfo.setAddress(fileName.substring(fileName.indexOf('(')+1,fileName.indexOf(')')));
        callRecorderInfo.setDate(TimeUtils.getDateString(fileName.substring(fileName.indexOf('_')+1,fileName.indexOf('.'))));
        callRecorderInfo.setPerson(fileName.substring(fileName.indexOf('@')+1, fileName.indexOf('(')));
        callRecorderInfo.setDuration(duration);
        callRecorderInfo.setPath(path);
        callRecorderInfo.setType(type);
//        Log.d("baii", "upload file " + callRecorderInfo.toString());
        CallRecorderUploadImpl callRecorderUpload = new CallRecorderUploadImpl(mContext);
        callRecorderUpload.uploadCallRecorder(mCallRecorderPolicyInfo.getId(), callRecorderInfo);*/

        //baii impl 11111111111111111111111111111
        String communicationNumber = fileName.substring(fileName.indexOf('(')+1,fileName.indexOf(')'));
        String communicationName = fileName.substring(fileName.indexOf('@')+1, fileName.indexOf('('));
        String date = TimeUtils.getDateString(fileName.substring(fileName.indexOf('_')+1,fileName.indexOf('.')));

        JSONObject json = new JSONObject();
        try {
            json.put("strategyId", mCallRecorderPolicyInfo.getId());
            json.put("communicationName", communicationName);
            json.put("communicationNumber", communicationNumber);
            json.put("timeDuration", duration);
            json.put("type", type);
            json.put("soundTime", date);
            json.put("filePath", path);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LogUtil.writeToFile(TAG, "upload file " + json.toString());
        MessageSendData data = new MessageSendData(Common.CALL_RECORDER_BACKUP, json.toString(), true);
        SendMessageManager manager = new SendMessageManager(mContext);
        manager.setSendListener(new SendMessageManager.SendListener() {
            @Override
            public void onSuccess() {
                if (file != null && file.exists()) {
                    file.delete();
                }
            }

            @Override
            public void onFailure() {

            }

            @Override
            public void onError() {

            }
        });
        manager.sendMessage(data);
    }

    public boolean isInUploadTimeRange() {
        if (!mIsPolicyOpen) {
            return false;
        }
        if (mCallRecorderPolicyInfo == null) {
            return false;
        }
        long time = System.currentTimeMillis();
        if (TimeDataUtils.isInDateRange(time, mCallRecorderPolicyInfo.getTimeData())) {
            if (TimeDataUtils.isInTimeUnitRange(time, mCallRecorderPolicyInfo.getTimeData())) {
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
