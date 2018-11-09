package com.zoomtech.emm.features.policy.sms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Telephony;

import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.definition.OrderConfig;
import com.zoomtech.emm.features.impl.SendMessageManager;
import com.zoomtech.emm.model.MessageSendData;
import com.zoomtech.emm.utils.DataParseUtil;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.utils.TimeDataUtils;
import com.zoomtech.emm.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.ALARM_SERVICE;

public class SmsManager {
    static final String TAG = SmsManager.class.getName();
    SMSObserver mSmsObserver;
    SmsPolicyInfo mSmsPolicyInfo;
    private Context mContext;
    public static final String SMS_SP_NAME = "sms_policy";
    public static final String SMS_POLICY_KEY = "policy";
    public static final String SMS_POLICY_OPEN = "isPolicyOpen";
    public static final String SMS_DISPLAY_TIME_STRING = "displayTimeString";
    public static final int SMS_CHANGED = 1;
    public static final String START_SMS_OBSERVER = "app.action.START_SMS_OBSERVER";

    private static class SingletonHolder {
        public static SmsManager instance = new SmsManager();
    }

    private SmsManager() {
        init();
    }

    public static SmsManager newInstance() {
        return SingletonHolder.instance;
    }

    private void init() {
        mContext = TheTang.getSingleInstance().getContext();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SMS_SP_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(SMS_POLICY_KEY, "");
        mSmsPolicyInfo = DataParseUtil.getSmsPolicyInfo(json);
        mSmsObserver = new SMSObserver(mContext, mHandler);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SMS_CHANGED:
                    Bundle bundle = msg.getData();
                    if (bundle != null) {
                        Uri uri = Uri.parse(bundle.getString("uri"));
                        getSmsBackupInfo(uri);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void getSmsBackupInfo(Uri uri) {
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                long date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));
//                Log.d("baii", "getSmsBackupInfo date " + date);
                if (TimeDataUtils.isInDateRange(date, mSmsPolicyInfo.getTimeData())) {
                    if (TimeDataUtils.isInTimeUnitRange(date, mSmsPolicyInfo.getTimeData())) {
                        String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                        String person = cursor.getString(cursor.getColumnIndex(Telephony.Sms.PERSON));
                        String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                        String type = cursor.getString(cursor.getColumnIndex(Telephony.Sms.TYPE));
//                        Log.d("baii", "getSmsInfo type " + type);
                        SmsBackupInfo SmsBackupInfo = new SmsBackupInfo();
                        SmsBackupInfo.setAddress(address);
                        SmsBackupInfo.setBody(body);
                        //xiaomi sms数据库保存的person字段为null，在contacts数据库去查
                        if (person == null || person.length() < 1) {
//                            Log.d("baii", "get person name from contacts...");
                            LogUtil.writeToFile(TAG, "get person name from contacts...");
                            Cursor cursorContact = mContext.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address)),
                                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME_PRIMARY}, null, null, null);
                            if (cursorContact != null && cursorContact.getCount() > 0) {
                                if (cursorContact.moveToFirst()) {
                                    person = cursorContact.getString(cursorContact.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME_PRIMARY));
                                }
                            }
                            if (cursorContact != null) {
                                cursorContact.close();
                            }
                        }
                        //xiaomi sms数据库保存的person字段为null，在contacts数据库去查
                        SmsBackupInfo.setPerson(person);
                        SmsBackupInfo.setType(type);
                        SmsBackupInfo.setDate(TimeUtils.getDateString(date));
                        uploadSmsBackupInfo(SmsBackupInfo);
                    }
                } else {
                    executeDeleteSmsPolicy("", true);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     *
     * @param json
     * @param isFromOrder onDestroy后ContentObserver会关闭，重启之后要重新打开
     *                    true:下发的命令
     *                    false:重启检查
     */
    public void executeSmsPolicy(String json, boolean isFromOrder) {
//        Log.d("baii", "exe sms");
        LogUtil.writeToFile(TAG, "exe sms");
        if (isFromOrder) {
            mSmsPolicyInfo = DataParseUtil.getSmsPolicyInfo(json);
            savePolicy(json);
            TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.SEND_SMS_BACKUP_POLICY), mSmsPolicyInfo.getName());
            TheTang.getSingleInstance().addStratege(String.valueOf(OrderConfig.SEND_SMS_BACKUP_POLICY), mSmsPolicyInfo.getName(), System.currentTimeMillis() + "");
        }

        if (TimeDataUtils.isInDateRange(System.currentTimeMillis(), mSmsPolicyInfo.getTimeData())) {
//            Log.d("baii", "directly register observer");
            LogUtil.writeToFile(TAG, "directly register observer");
            registerSmsObserver();
        } else if (TimeDataUtils.isExpired(System.currentTimeMillis(), mSmsPolicyInfo.getTimeData())) {
            executeDeleteSmsPolicy("", true);
        } else {
            setAlarm(TimeUtils.getStartDate(mSmsPolicyInfo.getTimeData().getStartDateRange()).getTime());
        }

    }

    /**
     *
     * @param json
     * @param isExpired 是否过期删除
     *                  true:超过时间范围
     *                  false:下发命令删除
     */
    public void executeDeleteSmsPolicy(String json, boolean isExpired) {
        //todo 下发命令处理
        deletePolicy();
        unregisterSmsObserver();
        if (!isExpired) {
            TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.DELETE_SMS_BACKUP_POLICY), mSmsPolicyInfo.getName());
            TheTang.getSingleInstance().deleteStrategeInfo(String.valueOf(OrderConfig.SEND_SMS_BACKUP_POLICY), mSmsPolicyInfo.getName());
        }
    }

    private void savePolicy(String json) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SMS_SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SMS_POLICY_KEY, json);
        editor.putBoolean(SMS_POLICY_OPEN, true);
        editor.putString(SMS_DISPLAY_TIME_STRING, TimeDataUtils.getDisplayTimeString(mSmsPolicyInfo.getTimeData()));
        editor.commit();
    }

    private void deletePolicy() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SMS_SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SMS_POLICY_KEY, "");
        editor.putBoolean(SMS_POLICY_OPEN, false);
        editor.commit();
    }

    private void registerSmsObserver() {
        if (mSmsObserver != null) {
            mContext.getContentResolver().registerContentObserver(
                    Telephony.Sms.CONTENT_URI, true, mSmsObserver);// Uri.parse("content://sms/")
        }
    }

    private void unregisterSmsObserver() {
        if (mSmsObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mSmsObserver);
        }
    }

    public void checkSmsPolicy() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SMS_SP_NAME, Context.MODE_PRIVATE);
        boolean isOpen = sharedPreferences.getBoolean(SMS_POLICY_OPEN, false);
        //todo baii
        if (isOpen) {
            String json = sharedPreferences.getString(SMS_POLICY_KEY, "");
            executeSmsPolicy(json, false);
        }
//        executeSmsPolicy("", false);
    }
    //todo
    private void uploadSmsBackupInfo(SmsBackupInfo info) {
//        Log.d("baii", "upload sms");
/*        SmsBackupImpl smsBackup = new SmsBackupImpl(mContext);
        smsBackup.sendSmsInfo(mSmsPolicyInfo.getId(), info);*/
//todo impl bai 999999999999
        final JSONObject smsObject = new JSONObject();
        try {
            smsObject.put("strategyId", mSmsPolicyInfo.getId());
            smsObject.put("smsTime", info.getDate());
            smsObject.put("content", info.getBody());
            smsObject.put("communicationName", info.getPerson());
            smsObject.put("communicationNumber", info.getAddress());
            smsObject.put("type", info.getType());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MessageSendData data = new MessageSendData(Common.SMS_BACKUP, smsObject.toString(), true);
        SendMessageManager manager = new SendMessageManager(mContext);
        manager.sendMessage(data);
    }

    public void setAlarm(long time) {
//        Log.d("baii", " setAlarm time " + time);
        LogUtil.writeToFile(TAG, " setAlarm time " + time);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(START_SMS_OBSERVER);
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
}
