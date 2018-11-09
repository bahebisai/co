package com.zoomtech.emm.features.policy.compliance;

import android.app.IntentService;
import android.content.Intent;

import com.zoomtech.emm.R;
import com.zoomtech.emm.features.lockscreen.flowreceive.FlowTotalReceiver;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;


/**
 * Created by Administrator on 2017/8/29.
 */

public class SystemIntentService extends IntentService {

    final static String TAG = "SystemIntentService";
    static PreferencesManager mPreferencesManager = PreferencesManager.getSingleInstance();

    public SystemIntentService() {
        super( "SystemIntentService" );
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TheTang.getSingleInstance().startForeground(this, getResources().getString(R.string.systen_compliance_excute), "EMM", 10);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //挂载
        if (intent.getAction().equals( Intent.ACTION_MEDIA_MOUNTED ) ) {
            MDM.mountSDCard();
        //弹出
        } else if (intent.getAction().equals( Intent.ACTION_MEDIA_EJECT)) {

            if (FlowTotalReceiver.isShutDown) {
                return;
            }

            MDM.ejectSDCard();
        }
    }

}
