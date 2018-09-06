package com.xiaomi.emm.features.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.miui.enterprise.sdk.PhoneManager;
import com.xiaomi.emm.features.policy.phoneCall.CallRecorderManager;
import com.xiaomi.emm.features.policy.sms.SmsManager;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d("baii", "on receive");
        if (intent != null) {
            String action = intent.getAction();
            switch (action) {
                case SmsManager.START_SMS_OBSERVER:
                    SmsManager.newInstance().executeSmsPolicy("", false);
                    Log.d("baii", "alarm sms");
                    break;
                case CallRecorderManager.START_CALL_RECORDER:
//                    CallRecorderManager.newInstance().executeCallRecorderPolicy("", false);
                    PhoneManager.getInstance().setPhoneCallAutoRecord(true);
                default:
                    break;
            }
        }
    }
}
