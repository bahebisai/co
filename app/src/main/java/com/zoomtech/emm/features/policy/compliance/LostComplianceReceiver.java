package com.zoomtech.emm.features.policy.compliance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.TheTang;

public class LostComplianceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.writeToFile("LostComplianceReceiver","LostComplianceReceiver!");
        TheTang.getSingleInstance().isLostCompliance(false);
    }
}
