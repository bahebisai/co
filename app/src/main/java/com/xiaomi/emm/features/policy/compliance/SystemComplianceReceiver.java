package com.xiaomi.emm.features.policy.compliance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xiaomi.emm.features.presenter.TheTang;

/**
 * Created by Administrator on 2017/8/8.
 */

public class SystemComplianceReceiver extends BroadcastReceiver {
    private final static String TAG = "SystemComplianceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent systemIntent = new Intent(context, SystemIntentService.class);
        systemIntent.setAction(intent.getAction());
        TheTang.getSingleInstance().startService(systemIntent);
    }
}

