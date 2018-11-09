package com.zoomtech.emm.features.policy.compliance.machinecard;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.zoomtech.emm.R;
import com.zoomtech.emm.features.presenter.TheTang;

/**
 * Created by Administrator on 2017/8/29.
 */

public class MachineCardBindingService extends Service {

    MachineCardBindingReceiver mMachineCardBindingReceiver = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TheTang.getSingleInstance().startForeground(this,getResources().getString(R.string.card_bind_service),"EMM",8);

        mMachineCardBindingReceiver = new MachineCardBindingReceiver();
        IntentFilter intentFilter = new IntentFilter(  );
        intentFilter.addAction( "android.intent.action.SIM_STATE_CHANGED" );
        registerReceiver( mMachineCardBindingReceiver, intentFilter );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver( mMachineCardBindingReceiver );
    }
}
