package com.zoomtech.emm.features.policy.compliance.machinecard;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;

/**
 * Created by Administrator on 2017/8/29.
 */

public class MachineCardBindingReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
            PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            if ("true".equals(preferencesManager.getComplianceData(Common.system_sim))) {

                /*if (TextUtils.isEmpty(preferencesManager.getComplianceData(Common.iccid_card))) {
                    //判断是否设备是否有插入SIM卡
                    if (telephonyManager.getSubscriberId() != null) {
                        preferencesManager.setComplianceData(Common.iccid_card, telephonyManager.getSimSerialNumber());
                    }
                }*/

                Intent intentCard = new Intent();
                intentCard.setClass(context, MachineCardIntentService.class);
                TheTang.getSingleInstance().startService(intentCard);
            }
        }
    }
}
