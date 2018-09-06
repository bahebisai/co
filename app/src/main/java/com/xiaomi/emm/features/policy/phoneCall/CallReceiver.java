package com.xiaomi.emm.features.policy.phoneCall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.miui.enterprise.sdk.PhoneManager;
import com.xiaomi.emm.utils.MDM;

public class CallReceiver extends BroadcastReceiver {
    public static final String ACTION_CALL_CONNECTED = PhoneManager.ACTION_CALL_CONNECTED;
    public static final String ACTION_CALL_RECORD = PhoneManager.ACTION_CALL_RECORD;
    public static final String EXTRA_PHONE_NUMBER = PhoneManager.EXTRA_PHONE_NUMBER;
    public static final String ACTION_INCOMING_CALL = PhoneManager.ACTION_INCOMING_CALL;
    public static final String ACTION_OUTGOING_CALL = PhoneManager.ACTION_OUTGOING_CALL;
    public static final String EXTRA_SLOT_ID = PhoneManager.EXTRA_SLOT_ID;
    public static final String EXTRA_CALL_RECORD_FILE = PhoneManager.EXTRA_CALL_RECORD_FILE;
    private static boolean mNeedToRecord;
    private CallRecorderManager mManager;
    private long mStartTime;
    private int mType = -1;
    private static final int TYPE_INCOMING = 1;
    private static final int TYPE_OUTGOING = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        String action = intent.getAction();
        Log.d("baii", "on receive call, action " + action);
        mManager = CallRecorderManager.newInstance();
        switch (action) {
            case ACTION_INCOMING_CALL:
                mType = TYPE_INCOMING;
                startRecord();
                break;
            case ACTION_OUTGOING_CALL:
                mType = TYPE_OUTGOING;
                startRecord();
                break;
            case ACTION_CALL_CONNECTED:
                String num = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                Log.d("baii", "number " + num);
                if (mNeedToRecord) {
                    mStartTime = System.currentTimeMillis();
                }
                break;
            case ACTION_CALL_RECORD:
                if (mNeedToRecord) {
                    String path = intent.getStringExtra(EXTRA_CALL_RECORD_FILE);
                    Log.d("baii", "recorder path " + path);///storage/emulated/0/MIUI/sound_recorder/call_rec/通话录音@176 8831 4515(17688314515)_20180814153840.mp3
                    long duration = System.currentTimeMillis() - mStartTime;
                    CallRecorderManager.newInstance().uploadCallRecorder(path, duration, mType);
                    if (MDM.mMDMController.isCallAutoRecord()) {
                        MDM.mMDMController.setCallAutoRecord(false);
                    }
                }
                break;
            default:
//                setRecorder(intent);
                break;
        }
    }

    /*在ACTION_CALL_CONNECTED调用setPhoneCallAutoRecord会出现时序问题导致不能录音，所以放在ACTION_INCOMING_CALL和ACTION_OUTGOING_CALL里面*/
    private void startRecord() {
        mNeedToRecord = mManager.isInUploadTimeRange();
        if (mNeedToRecord) {
            mStartTime = System.currentTimeMillis();
            if (!MDM.mMDMController.isCallAutoRecord()) {
                MDM.mMDMController.setCallAutoRecord(true);
            }
        }
    }

    //原生广播通话录音处理方案，无法判断接通，接通可查询数据库获取duration判断
    //来电：EXTRA_STATE_RINGING > EXTRA_STATE_IDLE
    //去电：EXTRA_STATE_OFFHOOK > EXTRA_STATE_IDLE
    private void setRecorder(Intent intent) {
        // 来电去电都会走
        // 获取当前电话状态
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        Log.d("baii", "PhoneStateReceiver onReceive state: " + state);

        // 获取电话号码
        String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        Log.d("baii", "PhoneStateReceiver onReceive number: " + number);

        if (state != null) {
            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
                if (mNeedToRecord) {
                    long duration = System.currentTimeMillis() - mStartTime;
                    CallRecorderManager.newInstance().uploadCallRecorder("", duration, mType);
                    MDM.mMDMController.setCallAutoRecord(false);
                }
            } else {

                mNeedToRecord = mManager.isInUploadTimeRange();
                if (mNeedToRecord) {
                    if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        mType = 1;
                    } else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
                        mType = 0;
                    }
                    MDM.mMDMController.setCallAutoRecord(true);
                    mStartTime = System.currentTimeMillis();
                    Log.d("baii", "offhook/ring time " + System.currentTimeMillis());
                }
            }
        }
    }
}
