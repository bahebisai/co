package com.zoomtech.emm.features.policy.compliance.machinecard;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;

import com.zoomtech.emm.R;
import com.zoomtech.emm.features.lockscreen.flowreceive.FlowTotalReceiver;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;

/**
 * Created by Administrator on 2017/8/29.
 */

public class MachineCardIntentService extends IntentService {

    public MachineCardIntentService() {
        super("MachineCardIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TheTang.getSingleInstance().startForeground(this, getResources().getString(R.string.card_bind_excute), "EMM", 11);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Service.TELEPHONY_SERVICE);
        int state = telephonyManager.getSimState();

        switch (state) {
            case TelephonyManager.SIM_STATE_READY:

                MDM.machineCard(preferencesManager);

                break;
            case TelephonyManager.SIM_STATE_ABSENT:

                if (FlowTotalReceiver.isShutDown) {
                    return;
                }

                MDM.machineCard(preferencesManager);

                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                break;
            default:
                break;
        }
    }
}
